package love.forte.simbot.component.kritor.core.message

import io.kritor.event.Element
import love.forte.simbot.annotations.Api4J
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.message.MessageContent


/**
 * 从消息事件中接收到的消息内容。
 *
 * @author ForteScarlet
 */
public interface KritorMessageContent : MessageContent {
    /**
     * 消息ID
     */
    override val id: ULongID

    /**
     * 消息序列。
     */
    @get:JvmSynthetic
    public val seq: ULong

    /**
     * 消息序列（的 [Long] 值）。
     */
    @Api4J
    public val seqValue: Long
        get() = seq.toLong()

    /**
     * 得到消息事件中原始的元素列表
     */
    public val sourceElements: List<Element>

}
