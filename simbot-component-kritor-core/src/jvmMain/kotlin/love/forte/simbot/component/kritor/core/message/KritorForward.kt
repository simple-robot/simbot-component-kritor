package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID


/**
 * 转发消息
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.forward")
public data class KritorForward(
    val id: ID,
    val uniseq: String,
    val summary: String,
    val description: String
) : KritorMessageElement, KritorSendElementTransformer {

    public companion object {
        /**
         * 使用 [io.kritor.event.ForwardElement] 构建一个 [KritorForward]。
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.ForwardElement.toKritorForward(): KritorForward {
            return KritorForward(id.ID, uniseq, summary, description)
        }

        /**
         * 使用 [ForwardElement] 构建一个 [KritorForward]。
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun ForwardElement.toKritorForward(): KritorForward {
            return KritorForward(id.ID, uniseq, summary, description)
        }
    }

    /**
     * 转换为 [Element]
     */
    override fun toElement(): Element = element {
        type = ElementType.FORWARD
        forward = forwardElement {
            this@KritorForward.id
            this@KritorForward.uniseq
            this@KritorForward.summary
            this@KritorForward.description
        }
    }
}
