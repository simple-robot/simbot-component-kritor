package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.giftElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.common.id.toLong

/**
 *
 * Kritor 的 Gift
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.gift")
public data class KritorGift(val qq: ID, var id: UIntID) :
        KritorMessageElement,
        KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.GIFT
        gift = giftElement {
            qq = this@KritorGift.qq.toLong()
            id = this@KritorGift.id.toInt()
        }
    }
}
