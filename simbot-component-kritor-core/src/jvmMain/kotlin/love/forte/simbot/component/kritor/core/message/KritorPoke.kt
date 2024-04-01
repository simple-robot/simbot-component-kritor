package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.common.id.UIntID.Companion.ID

/**
 * Kritor 的 Poke.
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.poke")
public data class KritorPoke(val id: UIntID, val type: Int, val strength: Int):
    KritorMessageElement, KritorSendElementTransformer {
    public companion object {
        @JvmStatic
        @JvmName("valueof")
        public fun io.kritor.event.PokeElement.toKritorPoke(): KritorPoke {
            return KritorPoke(id.toUInt().ID, type, strength)
        }
    }
    override fun toElement(): Element = element {
        type = ElementType.POKE
        poke = pokeElement {
            id = this@KritorPoke.id.value.toInt()
            type = this@KritorPoke.type
            strength = this@KritorPoke.strength
        }

   }
}