package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.rpsElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.message.EmoticonMessage

/**
 * Kritor 的 Rps.
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.rps")
public data class KritorRps(val id: UIntID) :
    EmoticonMessage, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.RPS
        rps = rpsElement {
            id = this@KritorRps.id.value.toInt()
        }
    }
}
