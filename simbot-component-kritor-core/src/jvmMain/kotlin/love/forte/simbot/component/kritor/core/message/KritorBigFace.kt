package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.IntID.Companion.ID
import love.forte.simbot.common.id.NumericalID
import love.forte.simbot.common.id.literal
import love.forte.simbot.message.EmoticonMessage

/**
 *
 * @author ForteScarlet
 */
@Serializable
@SerialName("kritor.m.face")
public data class KritorFace(val id: ID, val isBig: Boolean, val result: Int? = null) : EmoticonMessage,
    KritorSendElementTransformer {
    public companion object {
        /**
         * 使用 [FaceElement] 构建一个 [KritorFace]。
         */
        @JvmStatic
        @JvmName("of")
        public fun io.kritor.event.FaceElement.toKritorFace(): KritorFace {
            return KritorFace(id.ID, isBig, if (hasResult()) result else null)
        }
    }

    public constructor(source: FaceElement) : this(
        source.id.ID,
        source.isBig,
        if (source.hasResult()) source.result else null
    )

    override fun toElement(): Element {
        return element {
            type = ElementType.FACE
            face = faceElement {
                this.id = (this@KritorFace.id as? NumericalID)?.toInt() ?: this@KritorFace.id.literal.toInt()
                this.isBig = isBig
                this@KritorFace.result?.also { result = it }
            }
        }
    }
}
