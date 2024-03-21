package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.component.kritor.core.utils.byteArraySerialDescriptor


/**
 * 直接提供一个原始的 [Element] 用于发送。
 *
 * @author ForteScarlet
 */
@SendOnlyKritorMessageElement
@Serializable
public class KritorSourceMessageElement(
    @Serializable(KritorElementSerializer::class)
    override val source: Element
) : KritorMessageElement


public object KritorElementSerializer : KSerializer<Element> {
    override fun deserialize(decoder: Decoder): Element {
        val bytes = ByteArraySerializer().deserialize(decoder)
        return Element.parseFrom(bytes)
    }

    override val descriptor: SerialDescriptor = byteArraySerialDescriptor("Element")

    override fun serialize(encoder: Encoder, value: Element) {
        return ByteArraySerializer().serialize(encoder, value.toByteArray())
    }
}
