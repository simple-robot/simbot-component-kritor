package love.forte.simbot.component.kritor.core.event.internal

import io.kritor.event.EventStructure
import io.kritor.event.MessageEvent
import io.kritor.message.*
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.UUID
import love.forte.simbot.component.kritor.core.actor.KritorGroup
import love.forte.simbot.component.kritor.core.actor.KritorGroupMember
import love.forte.simbot.component.kritor.core.actor.internal.KritorBasicGroupEventInfoImpl
import love.forte.simbot.component.kritor.core.actor.internal.toGroup
import love.forte.simbot.component.kritor.core.actor.internal.toGroupInfo
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.bot.internal.getGroupInfo
import love.forte.simbot.component.kritor.core.bot.internal.sendMessage
import love.forte.simbot.component.kritor.core.event.KritorGroupMessageEvent
import love.forte.simbot.component.kritor.core.message.KritorMessageContent
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.component.kritor.core.message.internal.resolveMessageContent
import love.forte.simbot.component.kritor.core.message.internal.toReceipt
import love.forte.simbot.component.kritor.core.message.resolve
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent


/**
 *
 * @author ForteScarlet
 */
internal class KritorGroupMessageEventImpl(
    override val bot: KritorBotImpl,
    override val sourceEventStructure: EventStructure,
    override val sourceEvent: MessageEvent,
) : KritorGroupMessageEvent {
    override val id: ID = UUID.random()
    override val groupInfo: KritorBasicGroupEventInfoImpl = sourceEvent.contact.toGroupInfo(bot)

    override val authorId: ID
        get() = sourceEvent.sender.uid.ID

    override val messageContent: KritorMessageContent by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceEvent.resolveMessageContent(bot)
    }

    override suspend fun author(): KritorGroupMember {
        TODO("Not yet implemented")
    }

    override suspend fun content(): KritorGroup {
        val groupInfo = bot.getGroupInfo {
            this.groupId = sourceEvent.contact.peer.toLong()
        }

        return groupInfo.toGroup(bot, this.groupInfo)
    }

    private fun replyElement(): Element = element {
        type = ElementType.REPLY
        reply = replyElement {
            messageId = sourceEvent.messageId
        }
    }

    private val contact: Contact
        get() = sourceEvent.contact.resolve()

    override suspend fun reply(text: String): KritorMessageReceipt {
        val contact = this.contact
        return bot.sendMessage(
            contact = contact,
            text = text,
            pre = {
                elements.add(replyElement())
            }).toReceipt(bot, contact)
    }

    override suspend fun reply(message: Message): KritorMessageReceipt {
        val contact = contact
        var hasReply = false
        return bot.sendMessage(
            contact = contact,
            message = message,
            each = { _, e ->
                if (e.type == ElementType.REPLY) {
                    hasReply = true
                }
                e
            },
            post = {
                if (!hasReply) {
                    // 需要放到第一个元素吗？
                    elements.add(replyElement())
                }
            }).toReceipt(bot, contact)
    }

    override suspend fun reply(messageContent: MessageContent): KritorMessageReceipt {
        val contact = contact
        var hasReply = false

        fun each(e: Element): Element {
            if (e.type == ElementType.REPLY) {
                hasReply = true
            }
            return e
        }

        fun SendMessageRequestKt.Dsl.post() {
            if (!hasReply) {
                elements.add(replyElement())
            }
        }

        return bot.sendMessage(
            contact = contact,
            messageContent = messageContent,
            directEach = { _, e -> each(e) },
            directPost = { post() },
            each = { _, e -> each(e) },
            post = { post() })
            .toReceipt(bot, contact)
    }
}
