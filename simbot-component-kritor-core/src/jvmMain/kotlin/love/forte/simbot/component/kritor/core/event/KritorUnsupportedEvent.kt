package love.forte.simbot.component.kritor.core.event

import io.kritor.event.EventStructure
import io.kritor.event.EventType
import love.forte.simbot.annotations.ExperimentalSimbotAPI
import love.forte.simbot.annotations.FragileSimbotAPI
import love.forte.simbot.annotations.InternalSimbotAPI
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.UUID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.component.kritor.core.bot.KritorBot
import love.forte.simbot.component.kritor.core.time.secondsTimestamp


/**
 * 一个kritor组件中尚未被支持的事件。
 * 所有尚未被针对性实现的事件都会直接作为 [KritorUnsupportedEvent.sourceEventStructure] 使用。
 *
 * ## FragileSimbotAPI
 *
 * 需要注意的是，[KritorUnsupportedEvent] 是一种用于“捡漏”的类型，当其可以代表的范围被缩减时，
 * 可能不会有任何提示。
 *
 * 举个例子，在 `v1.0` 版本中，有一个事件 `CatEat` 不被支持，那么此时它就会被作为 [KritorUnsupportedEvent]
 * 被推送；而到了 `v2.0` 后，这个事件被包装为了 `KritorCatEatEvent`，那么当再遇见这个事件时，
 * 就不会再被包装成 [KritorUnsupportedEvent] 了。
 * 也因此，假设有如下代码：
 *
 * ```kotlin
 * process<KritorUnsupportedEvent> { event ->
 *     if (event.sourceEventStructure.isCatEat) {
 *         println("捡漏到了 CatEat 事件！") // ①
 *     }
 * }
 * ```
 *
 * **①** 处的代码在 `v1.0` 版本会被执行，而 `v2.0` 则可能再也不会执行到了。
 *
 *
 * @author ForteScarlet
 */
@FragileSimbotAPI
public class KritorUnsupportedEvent @InternalSimbotAPI constructor(
    override val bot: KritorBot,
    override val sourceEventStructure: EventStructure
) : KritorEvent {
    /**
     * 一个随机ID。
     */
    override val id: ID = UUID.random()

    @OptIn(ExperimentalSimbotAPI::class)
    override val time: Timestamp = when (sourceEventStructure.type) {
        EventType.EVENT_TYPE_MESSAGE -> secondsTimestamp(sourceEventStructure.message.time)
        EventType.EVENT_TYPE_NOTICE -> secondsTimestamp(sourceEventStructure.notice.time)
        EventType.EVENT_TYPE_REQUEST -> secondsTimestamp(sourceEventStructure.request.time)
        else -> Timestamp.now()
    }

    override fun toString(): String =
        "KritorUnsupportedEvent(" +
            "type=${sourceEventStructure.type}, " +
            "sourceEventStructure=$sourceEventStructure)"
}
