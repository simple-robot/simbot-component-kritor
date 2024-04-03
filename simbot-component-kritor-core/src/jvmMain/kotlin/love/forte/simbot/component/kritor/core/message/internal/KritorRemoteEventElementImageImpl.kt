package love.forte.simbot.component.kritor.core.message.internal

import io.kritor.event.ImageElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.component.kritor.core.message.KritorRemoteEventElementImage
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
) : KritorRemoteEventElementImage


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
