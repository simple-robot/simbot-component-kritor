package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.shareElement
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 * Kritor 的 Share
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.share")
public data class KritorShare(
    val url: String,
    val title: String,
    val content: String,
    val image: String,
) : KritorMessageElement, KritorSendElementTransformer {

    public companion object {
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.ShareElement.toKritorShare(): KritorShare {
            return KritorShare(url, title, content, image)
        }
    }
    override fun toElement(): Element = element {
        type = ElementType.SHARE
        share = shareElement {
            this@KritorShare.url
            this@KritorShare.title
            this@KritorShare.content
            this@KritorShare.image
        }
    }
}
