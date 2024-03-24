package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.xmlElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * XML消息
 * @author ForteScarlet
 */
@Suppress("MemberVisibilityCanBePrivate")
@Serializable
@SerialName("kritor.m.xml")
public data class KritorXml(public val xml: String) : KritorMessageElement, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.XML
        xml = xmlElement { xml = this@KritorXml.xml }
    }
}
