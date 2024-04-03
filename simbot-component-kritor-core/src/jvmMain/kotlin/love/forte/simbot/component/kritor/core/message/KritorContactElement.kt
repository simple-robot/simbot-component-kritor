package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Kritor 的 Contact
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.contact")
public class KritorContactElement private constructor(
    /**
     * 场景
     *
     * @see io.kritor.event.ContactElement.getScene
     */
    public val scene: Scene,
    public val peer: String
) : KritorMessageElement, KritorSendElementTransformer {
            public companion object {
                /**
                 * 使用 [io.kritor.event.ContactElementOrBuilder] 构建 [KritorContactElement].
                 */
                @JvmStatic
                @JvmName("valueOf")
                public fun io.kritor.event.ContactElementOrBuilder.toKritorContact(): KritorContactElement {
                   val scene = when(requireNotNull(scene){ "Required `scene` was null." }) {
                       io.kritor.event.Scene.FRIEND -> Scene.FRIEND
                       io.kritor.event.Scene.GROUP -> Scene.GROUP
                       io.kritor.event.Scene.UNRECOGNIZED -> Scene.UNRECOGNIZED
                       io.kritor.event.Scene.NEARBY -> Scene.NEARBY
                       io.kritor.event.Scene.GUILD -> Scene.GUILD
                       io.kritor.event.Scene.STRANGER -> Scene.STRANGER
                       io.kritor.event.Scene.STRANGER_FROM_GROUP -> Scene.STRANGER_FROM_GROUP
                   }
                    return KritorContactElement(scene, peer)
                   }

                /**
                 * 使用[ContactElement] 构建 [KritorContactElement]
                 */
                @JvmStatic
                @JvmName("valueOf")
                public fun ContactElement.toKritorContact() : KritorContactElement {
                    return KritorContactElement(scene, peer)
                }
            }

    override fun toElement(): Element = element{
        type = ElementType.CONTACT
        contact = contactElement {
            this@KritorContactElement.scene
            this@KritorContactElement.peer
        }
    }
}
