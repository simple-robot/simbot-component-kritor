package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.kritor.core.utils.byteArraySerialDescriptor

/**
 * Kritor 的 Music.
 *
 * 如果 [custom] 不为 `null`，
 * 则在序列化时会被作为 protobuf 的二进制数据，
 * 参考 [CustomMusicDataSerializer]。
 *
 * @author Roy, ForteScarlet
 */
@Serializable
@SerialName("kritor.m.music")
public class KritorMusic private constructor(
    /**
     * 音乐平台。
     *
     * @see io.kritor.event.MusicElement.getPlatform
     */
    public val platform: MusicPlatform,
    /**
     * 音乐id。
     * 与 [custom] 只会有一个不为 `null`。
     */
    public val id: StringID?,

    /**
     * 音乐数据。
     * 与 [id] 只会有一个不为 `null`。
     *
     * @see io.kritor.event.MusicElement.getCustom
     * @see CustomMusicData
     */
    @Serializable(CustomMusicDataSerializer::class)
    public val custom: CustomMusicData?
) : KritorMessageElement, KritorSendElementTransformer {

    public companion object {

        /**
         * 使用 [io.kritor.event.MusicElementOrBuilder] 构建 [KritorMusic].
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun io.kritor.event.MusicElementOrBuilder.toKritorMusic(): KritorMusic {
            val platform = when(requireNotNull(platform){ "Required `platform` was null." }) {
                io.kritor.event.MusicPlatform.QQ -> MusicPlatform.QQ
                io.kritor.event.MusicPlatform.NetEase -> MusicPlatform.NetEase
                io.kritor.event.MusicPlatform.Custom -> MusicPlatform.Custom
                io.kritor.event.MusicPlatform.UNRECOGNIZED -> MusicPlatform.UNRECOGNIZED
            }
            val id = if (hasId()) id.ID else null
            val custom = if (hasCustom()) customMusicData {
                this@toKritorMusic.custom.url
                this@toKritorMusic.custom.audio
                this@toKritorMusic.custom.title
                this@toKritorMusic.custom.author
                this@toKritorMusic.custom.pic
            } else null

            return KritorMusic(platform, id, custom)
        }

        /**
         * 使用 [MusicElementOrBuilder] 构建 [KritorMusic].
         */
        @JvmStatic
        @JvmName("valueOf")
        public fun MusicElementOrBuilder.toKritorMusic(): KritorMusic {
            val platform = platform
            val id = if (hasId()) id.ID else null
            val custom = customOrNull

            return KritorMusic(platform, id, custom)
        }

        /**
         * 以ID为主构建 [KritorMusic].
         */
        @JvmStatic
        public fun createById(platform: MusicPlatform, id: ID): KritorMusic =
            KritorMusic(platform, id = id as? StringID ?: id.literal.ID, custom = null)

        /**
         * 以 [CustomMusicData] 为主构建 [KritorMusic].
         *
         * @see CustomMusicData
         */
        @JvmStatic
        public fun createByCustom(platform: MusicPlatform, custom: CustomMusicData): KritorMusic =
            KritorMusic(platform, id = null, custom = custom)
    }

    override fun toElement(): Element = element {
        type = ElementType.MUSIC
        music = musicElement {
            platform = this@KritorMusic.platform
            this@KritorMusic.id?.literal?.also { id = it }
            this@KritorMusic.custom?.also { custom = it }
        }
    }
}

/**
 * 将 [CustomMusicData] 以 protobuf 二进制数据的形式进行序列化。
 *
 * @author Roy
 */
public object CustomMusicDataSerializer : KSerializer<CustomMusicData> {
    override fun deserialize(decoder: Decoder): CustomMusicData {
        return CustomMusicData.parseFrom(ByteArraySerializer().deserialize(decoder))
    }

    override val descriptor: SerialDescriptor = byteArraySerialDescriptor("CustomMusicData")
    override fun serialize(encoder: Encoder, value: CustomMusicData) {
        return ByteArraySerializer().serialize(encoder, value.toByteArray())
    }
}
