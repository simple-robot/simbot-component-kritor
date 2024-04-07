package love.forte.simbot.component.kritor.core.message.internal

import io.kritor.message.SendMessageResponse
import io.kritor.message.recallMessageRequest
import love.forte.simbot.ability.DeleteFailureException
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.StandardDeleteOption
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.component.kritor.core.message.KritorSourceMessageContact
import love.forte.simbot.component.kritor.core.time.secondsTimestamp


/**
 *
 * @author ForteScarlet
 */
internal class KritorMessageReceiptImpl(
    private val botImpl: KritorBotImpl,
    private val source: SendMessageResponse,
    private val sourceContact: KritorSourceMessageContact
) : KritorMessageReceipt {
    override val id: ULongID
        get() = source.messageId.toULong().ID

    override val time: Timestamp
        get() = secondsTimestamp(source.messageTime)

    override suspend fun delete(vararg options: DeleteOption) {
        // 只用到了一个状态，用到了再判断即可
        // val stdOpts = options.standardAnalysis()

        runCatching {
            botImpl.services.messageService.recallMessage(recallMessageRequest {
                contact = sourceContact
                messageId = source.messageId
            })
        }.onFailure {
            if (StandardDeleteOption.IGNORE_ON_FAILURE !in options) {
                throw DeleteFailureException(it)
            }
        }
    }

    override fun toString(): String =
        "KritorMessageReceipt(id=${source.messageId}, time=${source.messageTime})"
}


internal fun SendMessageResponse.toReceipt(
    bot: KritorBotImpl,
    sourceContact: KritorSourceMessageContact
): KritorMessageReceiptImpl =
    KritorMessageReceiptImpl(bot, this, sourceContact)
