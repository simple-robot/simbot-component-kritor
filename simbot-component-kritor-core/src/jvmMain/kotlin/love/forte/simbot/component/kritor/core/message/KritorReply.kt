package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.replyElement
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID


/**
 *
 * @author ForteScarlet
 */
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
