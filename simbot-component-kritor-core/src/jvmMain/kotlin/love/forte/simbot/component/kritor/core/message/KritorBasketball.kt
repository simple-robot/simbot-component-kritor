package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.basketballElement
import io.kritor.message.element
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.message.EmoticonMessage

/**
 * Kritor 的 Basketball.
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.basketball")
public data class KritorBasketball(val id: UIntID) :
    EmoticonMessage, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.BASKETBALL
        basketball = basketballElement {
            id = this@KritorBasketball.id.value.toInt()
        }
    }
}
