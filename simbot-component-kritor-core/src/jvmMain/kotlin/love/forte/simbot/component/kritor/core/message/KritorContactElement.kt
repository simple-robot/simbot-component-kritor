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
            val scene = getScene().resolve()
            return KritorContactElement(scene, peer)
        }

        /**
         * 使用[ContactElement] 构建 [KritorContactElement]
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun ContactElement.toKritorContact(): KritorContactElement {
            return KritorContactElement(scene, peer)
        }
    }

    override fun toElement(): Element = element {
        type = ElementType.CONTACT
        contact = contactElement {
            this@KritorContactElement.scene
            this@KritorContactElement.peer
        }
    }

    override fun toString(): String {
        return "KritorContactElement(scene=$scene, peer='$peer')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KritorContactElement) return false

        if (scene != other.scene) return false
        if (peer != other.peer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = scene.hashCode()
        result = 31 * result + peer.hashCode()
        return result
    }


}
