package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.UIntID
import love.forte.simbot.common.id.UIntID.Companion.ID
import love.forte.simbot.message.EmoticonMessage

/**
 * 一个表情。
 * 当发送的时候，使用 `Face(id)` 等同于 `KritorFace(id, isBig = false)`。
 * 当接收的时候，Kritor会始终将 [io.kritor.event.FaceElement] 转化为 [KritorFace]。
 *
 * @author ForteScarlet
 */
@Serializable
@SerialName("kritor.m.face")
public data class KritorFace(val id: UIntID, val isBig: Boolean = false, val result: Int? = null) :
    EmoticonMessage,
    KritorSendElementTransformer {
    public companion object {
        /**
         * 使用 [io.kritor.event.FaceElement] 构建一个 [KritorFace]。
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.FaceElement.toKritorFace(): KritorFace {
            return KritorFace(id.toUInt().ID, isBig, if (hasResult()) result else null)
        }

        /**
         * 使用 [FaceElement] 构建一个 [KritorFace]。
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun FaceElement.toKritorFace(): KritorFace {
            return KritorFace(
                id.toUInt().ID,
                isBig,
                if (hasResult()) result else null
            )
        }
    }

    override fun toElement(): Element {
        return element {
            type = ElementType.FACE
            face = faceElement {
                this.id = this@KritorFace.id.value.toInt()
                this.isBig = isBig
                this@KritorFace.result?.also { result = it }
            }
        }
    }
}
