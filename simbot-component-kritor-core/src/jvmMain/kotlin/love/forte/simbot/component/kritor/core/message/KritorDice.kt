package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.message.EmoticonMessage

/**
 * Kritor 的 Dice.
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.dice")
public data class KritorDice(val id: UIntID):
    EmoticonMessage, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.DICE
        dice = diceElement {
            id = this@KritorDice.id.value.toInt()
        }
    }
}