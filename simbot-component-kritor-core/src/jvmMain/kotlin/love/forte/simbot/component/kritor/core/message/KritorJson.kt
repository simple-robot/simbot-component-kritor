package love.forte.simbot.component.kritor.core.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * JSON消息
 * @author ForteScarlet
 */
@Suppress("MemberVisibilityCanBePrivate")
@Serializable
@SerialName("kritor.m.json")
public class KritorJson(public val json: String) : KritorMessageElement {
    override val source: String
        get() = json
}
