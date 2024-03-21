package love.forte.simbot.component.kritor.core.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * XML消息
 * @author ForteScarlet
 */
@Suppress("MemberVisibilityCanBePrivate")
@Serializable
@SerialName("kritor.m.xml")
public class KritorXml(public val xml: String) : KritorMessageElement {
    override val source: String
        get() = xml
}
