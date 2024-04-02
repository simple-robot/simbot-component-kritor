package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.component.kritor.core.utils.byteArraySerialDescriptor

/**
 * Kritor 的 Music.
 *
 * @author Roy
 */
@Serializable
@SerialName("kritor.m.music")
public data class KritorMusic(
    val platform: MusicPlatform,
    val id: String,
    @Serializable(CustomMusicDataSerializer::class) val custom: CustomMusicData? = null
) :
    KritorMessageElement, KritorSendElementTransformer {

        public companion object {
            @JvmStatic
            @JvmName("valueOf")
            public fun io.kritor.event.MusicElement.toKritorMusic(): KritorMusic {
                return KritorMusic(
                    MusicPlatform.valueOf(platform.name),
                    id,
                    if (hasCustom()) {
                        customMusicData {
                            this@toKritorMusic.custom.url
                            this@toKritorMusic.custom.audio
                            this@toKritorMusic.custom.title
                            this@toKritorMusic.custom.author
                            this@toKritorMusic.custom.pic
                        }
                    } else {
                      null
                    }
                )
            }
    }

    public constructor(source: MusicElement) : this (
        MusicPlatform.valueOf(source.platform.name),
        source.id,
        if (source.hasCustom()) source.custom else null
    )
    override fun toElement(): Element = element {
        type = ElementType.MUSIC
        music = musicElement {
            platform = this@KritorMusic.platform
            platformValue = this@KritorMusic.platform.number
            id = this@KritorMusic.id
            this@KritorMusic.custom?.also { custom = it }
            }
        }
    }

public object CustomMusicDataSerializer : KSerializer<CustomMusicData> {
    override fun deserialize(decoder: Decoder): CustomMusicData {
        return CustomMusicData.parseFrom(ByteArraySerializer().deserialize(decoder))
    }

    override val descriptor: SerialDescriptor = byteArraySerialDescriptor("CustomMusicData")
    override fun serialize(encoder: Encoder, value: CustomMusicData) {
        return ByteArraySerializer().serialize(encoder, value.toByteArray())
    }
}