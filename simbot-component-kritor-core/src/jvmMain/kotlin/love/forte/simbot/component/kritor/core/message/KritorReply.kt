package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID


/**
 * Kritor 的消息引用/回复
 *
 * @see ReplyElement
 * @author ForteScarlet
 */
@Serializable
@SerialName("kritor.m.reply")
public data class KritorReply(val messageId: ULongID) : KritorMessageElement, KritorSendElementTransformer {
    public companion object {
        /**
         * 使用 [io.kritor.event.ReplyElement] 构造一个 [KritorReply].
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.ReplyElement.toKritorReply(): KritorReply =
            KritorReply(messageId.toULong().ID)
    }

    override fun toElement(): Element = element {
        type = ElementType.REPLY
        reply = replyElement {
            messageId = this@KritorReply.messageId.value.toLong()
        }
    }
}
