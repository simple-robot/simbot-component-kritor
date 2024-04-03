package love.forte.simbot.component.kritor.core.message

import io.kritor.event.ImageElement
import kotlinx.serialization.KSerializer
import love.forte.simbot.annotations.ExperimentalSimbotAPI
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.component.kritor.core.message.internal.KritorRemoteEventElementImageImpl
import love.forte.simbot.message.Image
import love.forte.simbot.message.OfflineImage
import love.forte.simbot.message.RemoteImage

/**
 * 一个 kritor 中的图片。
 *
 * ## 发送图片
 * 你可以直接使用接收到的 [KritorImage] 进行转发，
 * 或者simbot API中定义的标准 [OfflineImage] 实现来发送图片。
 *
 * ```Kotlin
 * // 使用二进制数据
 * val byteArray = ...
 * val image = byteArray.toOfflineImage()
 *
 * // 使用Resource，例如在JVM中，可以使用 File 或 Path 等。
 * val resource = Path("./image.png").toResource()
 * val image = resource.toOfflineImage()
 * ```
 *
 * 如果希望更加灵活地控制图片消息元素的内容，
 * 你也可以选择使用 [KritorSourceMessageElement]
 * 来直接使用一个原始的 [io.kritor.message.ImageElement]。
 *
 * ```Kotlin
 * val message = createKritorSourceMessageElement {
 *     type = ElementType.IMAGE
 *     image = imageElement {
 *         ...
 *     }
 * }
 * ```
 *
 * ## 接收图片
 * [KritorRemoteImage] 表示一个从 Kritor 事件中提取出来的图片元素，
 * 例如基于 [io.kritor.event.ImageElement] 的 [KritorRemoteEventElementImage].
 *
 * ```Kotlin
 * if (image is KritorRemoteEventElementImage) {
 *     val file = image.file
 *     val url = image.url // !Note: its nullable
 * }
 * ```
 *
 *
 *
 *
 *
 *
 * @author ForteScarlet
 */
public sealed interface KritorImage : KritorMessageElement, Image


/**
 * 一个表示远端图片的 [KritorImage]。
 * 一个具体从 [ImageElement] 得到的消息类型可参考 [KritorRemoteEventElementImage]。
 *
 * @see KritorImage
 * @see RemoteImage
 * @see KritorRemoteEventElementImage
 */
public sealed interface KritorRemoteImage : KritorImage, RemoteImage

/**
 * 一个基于 [io.kritor.event.ImageElement] 的图片消息类型。
 * @see io.kritor.event.ImageElement
 * @see KritorRemoteImage
 */
public interface KritorRemoteEventElementImage : KritorRemoteImage {
    /**
     * 内部原始的消息元素内容。
     */
    public val sourceElement: ImageElement

    /**
     * 获取 [sourceElement.file][ImageElement.getFile]. 此值会被视为 [id].
     */
    public val file: String
        get() = sourceElement.file

    /**
     * 值同 [sourceElement.file][ImageElement.getFile].
     * @see file
     */
    override val id: ID
        get() = file.ID

    /**
     * 获取 [sourceElement.url][ImageElement.getUrl]
     */
    public val url: String?
        get() = if (sourceElement.hasUrl()) sourceElement.url else null

    public companion object {
        @ExperimentalSimbotAPI
        public val serializer: KSerializer<out KritorRemoteEventElementImage>
            get() = KritorRemoteEventElementImageImpl.serializer()
    }
}
