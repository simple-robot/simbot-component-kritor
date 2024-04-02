package love.forte.simbot.component.kritor.core.message.internal

import io.kritor.event.Contact
import io.kritor.event.Element
import io.kritor.event.ElementType
import io.kritor.event.MessageEvent
import io.kritor.message.recallMessageRequest
import love.forte.simbot.ability.DeleteFailureException
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.StandardDeleteOption
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.message.KritorMessageContent
import love.forte.simbot.component.kritor.core.message.resolve
import love.forte.simbot.component.kritor.core.message.toMessages
import love.forte.simbot.message.Messages


/**
 *
 * @author ForteScarlet
 */
internal class KritorMessageContentImpl(
    private val bot: KritorBotImpl,
    private val messageId: ULong,
    override val seq: ULong,
    override val sourceElements: List<Element>,
    private val contact: Contact
) : KritorMessageContent {
    override val id: ULongID
        get() = messageId.ID

    override val messages: Messages by lazy(LazyThreadSafetyMode.PUBLICATION) {
        sourceElements.toMessages()
    }

    override val plainText: String? by lazy(LazyThreadSafetyMode.PUBLICATION) {
        var sb: StringBuilder? = null
        fun sb(): StringBuilder =
            sb ?: StringBuilder().also { sb = it }

        for (element in sourceElements) {
            if (element.type == ElementType.TEXT) {
                sb().append(element.text.text)
            }
        }

        sb?.toString()
    }

    override suspend fun delete(vararg options: DeleteOption) {
        runCatching {
            bot.services.messageService.recallMessage(recallMessageRequest {
                messageId = this@KritorMessageContentImpl.messageId.toLong()
                contact = this@KritorMessageContentImpl.contact.resolve()
            })
        }.onFailure { e ->
            if (StandardDeleteOption.IGNORE_ON_FAILURE in options) {
                throw DeleteFailureException(e.localizedMessage, e)
            }
        }
    }
}

internal fun MessageEvent.resolveMessageContent(
    bot: KritorBotImpl,
): KritorMessageContentImpl {
    return KritorMessageContentImpl(
        bot = bot,
        messageId = messageId.toULong(),
        seq = messageSeq.toULong(),
        sourceElements = elementsList,
        contact = contact
    )
}
