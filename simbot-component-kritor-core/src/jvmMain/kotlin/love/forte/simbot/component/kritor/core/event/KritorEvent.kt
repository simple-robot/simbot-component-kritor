package love.forte.simbot.component.kritor.core.event

import io.kritor.event.EventStructure
import io.kritor.event.EventType
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.component.kritor.core.KritorComponent
import love.forte.simbot.component.kritor.core.bot.KritorBot
import love.forte.simbot.event.BotEvent


/**
 * 一个实现 [BotEvent] 的 Kritor 组件事件类型。
 *
 * @author ForteScarlet
 */
public interface KritorEvent : BotEvent {
    override val bot: KritorBot
    override val component: KritorComponent
        get() = bot.component

    /**
     * 内部实际接收到的原始事件结构体。
     */
    public val sourceEventStructure: EventStructure

    /**
     * 事件的ID。
     * 如果无法从 [sourceEventStructure] 中获取，则可能是一个随机的ID。
     */
    override val id: ID

    /**
     * 事件发生时间。
     * 如果无法从 [sourceEventStructure] 中获取，则可能是此类型实例被构建的时间。
     */
    override val time: Timestamp

    /**
     * [sourceEventStructure] 中的 [type][EventStructure.getType].
     */
    public val sourceEventType: EventType
        get() = sourceEventStructure.type
}
