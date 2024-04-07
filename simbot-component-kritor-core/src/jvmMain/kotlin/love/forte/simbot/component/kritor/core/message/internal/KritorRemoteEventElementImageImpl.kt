package love.forte.simbot.component.kritor.core.message.internal

import io.kritor.event.ImageElement
import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.imageElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.component.kritor.core.message.KritorRemoteEventElementImage
import love.forte.simbot.component.kritor.core.message.KritorSendElementTransformer
import love.forte.simbot.component.kritor.core.message.resolve
import love.forte.simbot.component.kritor.core.utils.byteArraySerialDescriptor


/**
 *
 * @author ForteScarlet
 */
@Serializable
@SerialName("kritor.m.event_image")
internal class KritorRemoteEventElementImageImpl(
    @Serializable(ImageElementSerializer::class)
    override val sourceElement: ImageElement
) : KritorRemoteEventElementImage, KritorSendElementTransformer {
    override fun toElement(): Element {
        return element {
            type = ElementType.IMAGE
            image = imageElement {
                val s = sourceElement
                file = s.file
                if (s.hasUrl()) {
                    url = s.url
                }
                if (s.hasType()) {
                    type = s.type.resolve()
                }
                if (s.hasSubType()) {
                    subType = s.subType
                }
            }
        }
    }

    override fun toString(): String =
        with(sourceElement) {
            "KritorRemoteEventElementImage(" +
                    "file=${file}, " +
                    "url=${if (hasUrl()) url else UNKNOWN_VALUE}, " +
                    "type=${if (hasType()) type else UNKNOWN_VALUE}, " +
                    "subType=${if (hasSubType()) subType else UNKNOWN_VALUE}" +
                    ")"
        }

}

private const val UNKNOWN_VALUE = "<UNKNOWN>"

internal fun ImageElement.toRemoteImage(): KritorRemoteEventElementImageImpl = KritorRemoteEventElementImageImpl(this)

internal object ImageElementSerializer : KSerializer<ImageElement> {
    override fun deserialize(decoder: Decoder): ImageElement {
        val data = ByteArraySerializer().deserialize(decoder)
        return ImageElement.parseFrom(data)
    }

    override val descriptor: SerialDescriptor = byteArraySerialDescriptor("ImageElement")

    override fun serialize(encoder: Encoder, value: ImageElement) {
        ByteArraySerializer().serialize(encoder, value.toByteArray())
    }
}
