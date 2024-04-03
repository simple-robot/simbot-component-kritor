package love.forte.simbot.component.kritor.core.actor.internal

import io.kritor.event.MessageEvent
import io.kritor.event.Sender
import io.kritor.group.GroupMemberInfo
import io.kritor.group.banMemberRequest
import io.kritor.group.kickMemberRequest
import io.kritor.group.modifyMemberCardRequest
import io.kritor.message.Contact
import io.kritor.message.Scene
import io.kritor.message.contact
import love.forte.simbot.ability.DeleteFailureException
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.StandardDeleteOption.Companion.standardAnalysis
import love.forte.simbot.ability.isIgnoreOnFailure
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.kritor.core.actor.KritorBasicGroupMemberInfo
import love.forte.simbot.component.kritor.core.actor.KritorGroupMember
import love.forte.simbot.component.kritor.core.actor.KritorGroupMemberDeleteOption
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.bot.internal.sendMessage
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.component.kritor.core.message.internal.toReceipt
import love.forte.simbot.component.kritor.core.message.resolve
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import java.time.Duration
import kotlin.coroutines.CoroutineContext

/**
 * 内部使用，基本的 [KritorBasicGroupMemberInfo] 功能抽象实现。
 */
internal abstract class AbstractKritorBasicGroupMemberInfoImpl : KritorBasicGroupMemberInfo {
    internal abstract val bot: KritorBotImpl
    internal abstract val contact: Contact
    internal abstract val groupId: Long
    // message Contact {
    //     Scene scene = 1;
    //     string peer = 2; // 群聊则为群号 私聊则为QQ号
    //     optional string sub_peer = 3; // 群临时聊天则为群号 频道消息则为子频道号 其它情况可不提供
    // }

    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt {
        val contact = contact
        return bot.sendMessage(
            contact = contact,
            messageContent = messageContent,
        ).toReceipt(bot, contact)
    }

    override suspend fun send(message: Message): KritorMessageReceipt {
        val contact = contact
        return bot.sendMessage(contact, message).toReceipt(bot, contact)
    }

    override suspend fun send(text: String): KritorMessageReceipt {
        val contact = contact
        return bot.sendMessage(contact, text).toReceipt(bot, contact)
    }

    override suspend fun delete(vararg options: DeleteOption) {
        // see KritorGroupMemberDeleteOption
        val kickAnalyzer = KritorGroupMemberDeleteOption.Analyzer()

        val analysis = options.standardAnalysis(onEach = {
            // 同时伴随着对 KritorGroupMemberDeleteOption 的分析
            kickAnalyzer.analysis(it)
        })

        runCatching {
            bot.services.groupService.kickMember(kickMemberRequest {
                groupId = this@AbstractKritorBasicGroupMemberInfoImpl.groupId
                targetUid = id.literal
                if (kickAnalyzer.isRejectAddRequest) {
                    rejectAddRequest = true
                }
                kickAnalyzer.kickReason?.also {
                    kickReason = it
                }
            })
        }.onFailure { e ->
            if (!analysis.isIgnoreOnFailure) {
                throw DeleteFailureException(e.localizedMessage, e)
            }
        }
    }
}

/**
 * 基于群临时会话消息事件的 [AbstractKritorBasicGroupMemberInfoImpl].
 * 构建前应当确定事件是一个临时会话。
 */
internal class KritorBasicEventGroupMemberImpl(
    override val bot: KritorBotImpl,
    val sourceContact: io.kritor.event.Contact, // Sender
    val sender: Sender
) : AbstractKritorBasicGroupMemberInfoImpl() {
    override val contact: Contact
        get() = sourceContact.resolve()

    override val name: String
        get() = sender.nick

    override val id: ID
        get() = sender.uid.ID

    override val uin: ID?
        // 私聊的临时会话，peer 应当是qq号
        get() = sourceContact.peer.toULongOrNull()?.ID

    override val groupId: Long
        // 私聊的临时会话，subPeer 应当是群号
        get() = sourceContact.subPeer.toLong()
}

/**
 * 直接基于 [GroupMemberInfo] 对象本身实现的 [AbstractKritorBasicGroupMemberInfoImpl].
 */
internal class KritorBasicGroupMemberInfoImpl(
    override val bot: KritorBotImpl,
    val source: GroupMemberInfo,
    override val groupId: Long
) : AbstractKritorBasicGroupMemberInfoImpl() {
    override val name: String
        get() = source.nick

    override val id: StringID
        get() = source.uid.ID

    override val uin: ULongID
        get() = source.uin.toULong().ID

    override val contact: Contact
        get() = contact {
            // 作为群成员，这是群临时会话
            this.scene = Scene.STRANGER_FROM_GROUP
            this.peer = source.uin.toString()
            this.subPeer = groupId.toString()
        }
}

/**
 * 使用 [GroupMemberInfo] 构建 [KritorBasicGroupMemberInfoImpl].
 */
internal fun GroupMemberInfo.toMemberInfoImpl(
    bot: KritorBotImpl, groupId: Long
): KritorBasicGroupMemberInfoImpl = KritorBasicGroupMemberInfoImpl(
    bot, this, groupId
)

/**
 * 使用临时会话事件构建 [KritorBasicEventGroupMemberImpl].
 * 需要自行确保此事件属于来自群的临时会话消息事件，
 * 即 scene = [io.kritor.event.Scene.STRANGER_FROM_GROUP]
 */
internal fun MessageEvent.toMemberInfoImpl(bot: KritorBotImpl): KritorBasicEventGroupMemberImpl =
    KritorBasicEventGroupMemberImpl(bot, contact, sender)

/**
 *
 * @author ForteScarlet
 */
internal class KritorGroupMemberImpl(
    private val bot: KritorBotImpl,
    private val groupId: Long,
    override val source: GroupMemberInfo,
    private val sourceInfo: KritorBasicGroupMemberInfoImpl,
) : KritorGroupMember {
    override val coroutineContext: CoroutineContext = bot.subCoroutineContext
    override var nick: String = source.card

    override suspend fun modifyNick(newNick: String) {
        bot.services.groupService.modifyMemberCard(modifyMemberCardRequest {
            this.groupId = this@KritorGroupMemberImpl.groupId
            this.targetUid = source.uid
            this.card = newNick
        })
        this.nick = newNick
    }

    override suspend fun ban(duration: Duration) {
        if (duration.isNegative && duration.isZero) {
            throw IllegalArgumentException("Ban duration cannot be negative.")
        }

        bot.services.groupService.banMember(banMemberRequest {
            this.groupId = this@KritorGroupMemberImpl.groupId
            this.targetUid = source.uid
            this.duration = duration.toMillis().toInt()
        })
    }

    override suspend fun unban() {
        bot.services.groupService.banMember(banMemberRequest {
            this.groupId = this@KritorGroupMemberImpl.groupId
            this.targetUid = source.uid
            this.duration = 0
        })
    }

    override suspend fun poke() {
        TODO("Not yet implemented")
    }


    override suspend fun setAdmin(isAdmin: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(vararg options: DeleteOption) {
        sourceInfo.delete(options = options)
    }

    override suspend fun send(text: String): KritorMessageReceipt {
        return sourceInfo.send(text)
    }

    override suspend fun send(message: Message): KritorMessageReceipt {
        return sourceInfo.send(message)
    }

    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt {
        return sourceInfo.send(messageContent)
    }
}


internal fun GroupMemberInfo.toMember(
    bot: KritorBotImpl,
    groupId: Long,
    sourceInfo: KritorBasicGroupMemberInfoImpl = toMemberInfoImpl(bot, groupId)
): KritorGroupMemberImpl =
    KritorGroupMemberImpl(bot, groupId, this, sourceInfo)
