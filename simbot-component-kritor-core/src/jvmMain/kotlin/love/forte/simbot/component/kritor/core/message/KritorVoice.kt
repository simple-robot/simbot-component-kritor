package love.forte.simbot.component.kritor.core.message

import io.kritor.message.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import love.forte.simbot.annotations.ExperimentalSimbotAPI
import love.forte.simbot.component.kritor.core.message.KritorOfflineVoice.Companion.toKritorOfflineVoice
import love.forte.simbot.component.kritor.core.message.internal.KritorRemoteEventElementVoiceImpl
import love.forte.simbot.resource.*
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.toPath


/**
 *
 * 一个 kritor 的语音消息元素。
 *
 * ## 发送
 * 使用 [KritorOfflineVoice] 来使用一个离线资源发送一个语音。
 * 会根据Resource的类型，产生如下可能的结果：
 * 1. 得知是一个本地图片(例如是一个file或path)，
 * 会使用 `base64` 的形式发送。
 * 2. 是一个连接，例如 `https://xxx`，
 * 会直接提供这个连接地址给kritor服务端。
 * 3. 其他无法检测到的情况，会尝试读取它的
 * `data` (也就是字节数据) 后编码为 `base64` 发送。
 *
 * 如果希望更加灵活地控制图片消息元素的内容，
 * 你也可以选择使用 [KritorSourceMessageElement]
 * 来直接使用一个原始的 [io.kritor.message.VoiceElement]。
 *
 * ```Kotlin
 * val message = createKritorSourceMessageElement {
 *     type = ElementType.VOICE
 *     voice = voiceElement {
 *         ...
 *     }
 * }
 * ```
 * ## 接收
 *
 * 从事件中接收到的语音消息类型为 [KritorRemoteVoice],
 * 例如 [KritorRemoteEventElementVoice]。
 *
 * @see VoiceElement
 *
 * @author ForteScarlet
 */
public sealed interface KritorVoice : KritorMessageElement

/**
 * 一个本地用于发送的语音消息元素。
 * 例如使用基于 [Resource] 的 [KritorOfflineResourceVoice]。
 *
 * @see toKritorOfflineVoice
 *
 * @author ForteScarlet
 */
@SendOnlyKritorMessageElement
public abstract class KritorOfflineVoice : KritorVoice, KritorSendElementTransformer {
    final override fun toElement(): Element = element {
        type = ElementType.VOICE
        voice = toVoiceElement()
    }

    /**
     * to [VoiceElement] for [toElement].
     */
    protected abstract fun toVoiceElement(): VoiceElement

    public companion object {
        /**
         * 使用 [Resource] 构建 [KritorOfflineVoice].
         *
         * @see KritorOfflineResourceVoice
         */
        @JvmStatic
        @JvmName("of")
        public fun Resource.toKritorOfflineVoice(magic: Boolean): KritorOfflineVoice =
            KritorOfflineResourceVoice(this, magic)
    }
}

/**
 * 一个基于 [Resource] 的 [KritorOfflineVoice] 实现。
 *
 * ## 序列化
 *
 * [KritorOfflineResourceVoice] 支持序列化，但是**不建议**对其使用序列化：
 * 因为 [Resource] 的序列化是将其中的字节数据以 `base64` 的形式存储，很可能会占用大量内存空间或影响性能。
 * 更多说明参考 [Resource] 和 [ResourceBase64Serializer].
 *
 * @author ForteScarlet
 */
@OptIn(ExperimentalEncodingApi::class)
@Serializable
@SendOnlyKritorMessageElement
public data class KritorOfflineResourceVoice(
    @Serializable(ResourceBase64Serializer::class)
    val resource: Resource,
    val magic: Boolean,
) : KritorOfflineVoice() {
    private fun b64Element(b64: String): VoiceElement = voiceElement {
        fileBase64 = b64
        magic = this@KritorOfflineResourceVoice.magic
    }

    private fun urlElement(url: String): VoiceElement = voiceElement {
        this.url = url
        magic = this@KritorOfflineResourceVoice.magic
    }

    override fun toVoiceElement(): VoiceElement {
        return when (resource) {
            is FileResource, is PathResource -> {
                val b64 = when (resource) {
                    is FileResource -> resource.file.b64()
                    is PathResource -> resource.path.b64()
                    else -> error("Unknown resource type")
                }

                b64Element(b64)
            }

            is URIResource -> {
                val uri = resource.uri
                if (uri.isFileScheme) {
                    val b64 = uri.toPath().b64()
                    b64Element(b64)
                } else {
                    urlElement(uri.toASCIIString())
                }
            }

            else -> b64Element(resource.data().b64())
        }
    }
}

/**
 *
 * 一个从远端接收到的 kritor 的语音消息元素。一个具体从 [io.kritor.event.VoiceElement]
 *  * 中得到的消息类型可参考 [KritorRemoteEventElementVoice]。
 *
 * @see VoiceElement
 * @see KritorRemoteEventElementVoice
 *
 * @author ForteScarlet
 */
public sealed interface KritorRemoteVoice : KritorVoice

/**
 *
 * 一个从消息事件中的 [io.kritor.event.VoiceElement] 接收到的 kritor 的语音消息元素。
 *
 * Note: 此类型由组件内部实现，第三方实现不稳定。
 *
 * @see KritorRemoteVoice
 *
 * @author ForteScarlet
 */
public interface KritorRemoteEventElementVoice : KritorRemoteVoice {
    public val sourceElement: io.kritor.event.VoiceElement

    /**
     * @see io.kritor.event.VoiceElement.getFile
     */
    public val file: String?
        get() = if (sourceElement.hasFile()) sourceElement.file else null

    /**
     * @see io.kritor.event.VoiceElement.getUrl
     */
    public val url: String
        get() = sourceElement.url

    /**
     * @see io.kritor.event.VoiceElement.getMagic
     */
    public val magic: Boolean?
        get() = if (sourceElement.hasMagic()) sourceElement.magic else null

    public companion object {
        @ExperimentalSimbotAPI
        public val serializer: KSerializer<out KritorRemoteEventElementVoice>
            get() = KritorRemoteEventElementVoiceImpl.serializer()
    }
}
