package love.forte.simbot.component.kritor.core.message

import io.kritor.event.Element
import io.kritor.event.MessageEvent
import love.forte.simbot.message.MessageContent


/**
 * 从消息事件中接收到的消息内容。
 *
 * @author ForteScarlet
 */
public interface KritorMessageContent : MessageContent {
    /**
     * 得到消息事件中原始的元素列表
     */
    public val sourceElements: List<Element>

}
