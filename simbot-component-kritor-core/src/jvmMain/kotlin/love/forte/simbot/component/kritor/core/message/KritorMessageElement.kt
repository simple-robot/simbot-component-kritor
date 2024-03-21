package love.forte.simbot.component.kritor.core.message

import io.kritor.message.Element
import io.kritor.message.TextElement
import kotlinx.serialization.encoding.Decoder
import love.forte.simbot.message.Message
import love.forte.simbot.message.PlainText

/**
 * 一个标记注解，标记一个消息元素为仅用于发送、不会在事件中接收、获取到的消息元素类型。
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class SendOnlyKritorMessageElement


/**
 * 一个 Kritor 组件下的 [Message.Element].
 *
 * [KritorMessageElement] 不代表所有 Kritor 中消息元素的父类型，
 * 部分类型可以直接与 simbot 标准消息类型相对应，
 * 例如 [TextElement] 可以直接对应为 [PlainText].
 *
 * 大部分可序列化的 [KritorMessageElement] 实现均建议使用二进制序列化器进行序列化存储，
 * 因为它们大部分都是基于 proto 提供的序列化支持。
 */
public interface KritorMessageElement : Message.Element {
    /**
     * 如果此消息元素包含一个源。
     */
    public val source: Any?
}

/**
 * 支持直接转化为 [Element] 的 [KritorMessageElement].
 */
public interface KritorSendMessageElement : KritorMessageElement {
    public fun toElement(): Element
}
