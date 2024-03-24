package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.jsonElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * JSON消息
 * @author ForteScarlet
 */
@Suppress("MemberVisibilityCanBePrivate")
@Serializable
@SerialName("kritor.m.json")
public data class KritorJson(public val json: String) : KritorMessageElement, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.JSON
        json = jsonElement { json = this@KritorJson.json }
    }
}
