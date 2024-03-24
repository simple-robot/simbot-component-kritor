package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.bubbleFaceElement
import io.kritor.message.element
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.common.id.UIntID.Companion.ID
import love.forte.simbot.message.EmoticonMessage


/**
 * Kritor 的 bubbleFace.
 *
 * @author ForteScarlet
 */
@Serializable
@SerialName("kritor.m.bubbleFace")
public data class KritorBubbleFace(val id: UIntID, val count: Int) : EmoticonMessage,
    KritorSendElementTransformer {

    public companion object {
        /**
         * 将 [io.kritor.event.BubbleFaceElement] 转为 [KritorBubbleFace]
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.BubbleFaceElement.toKritorBubbleFace(): KritorBubbleFace =
            KritorBubbleFace(id.toUInt().ID, count)
    }

    override fun toElement(): Element = element {
        type = ElementType.BUBBLE_FACE
        bubbleFace = bubbleFaceElement {
            id = this@KritorBubbleFace.id.value.toInt()
        }
    }
}
