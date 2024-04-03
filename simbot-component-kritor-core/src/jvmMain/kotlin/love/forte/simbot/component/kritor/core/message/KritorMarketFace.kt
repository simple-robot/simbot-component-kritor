package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.ID


/**
 * Kritor 的 MarketFace
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.makertface")
public data class KritorMarketFace(val id: ID, val markdown: String) :
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
