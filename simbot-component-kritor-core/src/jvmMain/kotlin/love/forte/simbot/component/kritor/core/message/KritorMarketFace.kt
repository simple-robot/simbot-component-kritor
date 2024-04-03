package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Kritor 的 MarketFace
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.makertface")
public data class KritorMarketFace(val id: String, val markdown: String) :
        KritorMessageElement, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.MARKET_FACE
        marketFace = marketFaceElement {
            this@KritorMarketFace.id
        }
        markdown = markdownElement {
            this@KritorMarketFace.markdown
        }
    }
}
