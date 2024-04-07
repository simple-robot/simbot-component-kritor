package love.forte.simbot.component.kritor.core.message.internal

import io.kritor.event.VoiceElement
import io.kritor.message.Element
import io.kritor.message.ElementType
import io.kritor.message.element
import io.kritor.message.voiceElement
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.component.kritor.core.message.KritorRemoteEventElementVoice
import love.forte.simbot.component.kritor.core.message.KritorSendElementTransformer
import love.forte.simbot.component.kritor.core.utils.byteArraySerialDescriptor


/**
 *
 * @author ForteScarlet
 */
@Serializable
@SerialName("kritor.m.event_voice")
internal class KritorRemoteEventElementVoiceImpl(
    @Serializable(VoiceElementSerializer::class)
    override val sourceElement: VoiceElement
) : KritorRemoteEventElementVoice, KritorSendElementTransformer {
    override fun toElement(): Element = element {
        type = ElementType.VOICE
        voice = voiceElement {
            if (sourceElement.hasFile()) {
                file = sourceElement.file
            }
            url = sourceElement.url
            if (sourceElement.hasMagic()) {
                magic = sourceElement.magic
            }
        }
    }

    override fun toString(): String =
        with(sourceElement) {
            "KritorRemoteEventElementVoice(" +
                    "file=${if (hasFile()) file else UNKNOWN_VALUE}, " +
                    "url=$url, " +
                    "magic=$magic" +
                    ")"
        }
}

private const val UNKNOWN_VALUE = "<UNKNOWN>"

internal fun VoiceElement.toRemoteVoice(): KritorRemoteEventElementVoiceImpl = KritorRemoteEventElementVoiceImpl(this)

internal object VoiceElementSerializer : KSerializer<VoiceElement> {
    override fun deserialize(decoder: Decoder): VoiceElement {
        val data = ByteArraySerializer().deserialize(decoder)
        return VoiceElement.parseFrom(data)
    }

    override val descriptor: SerialDescriptor = byteArraySerialDescriptor("VoiceElement")

    override fun serialize(encoder: Encoder, value: VoiceElement) {
        ByteArraySerializer().serialize(encoder, value.toByteArray())
    }
}
