package love.forte.simbot.component.kritor.core.time

import love.forte.simbot.common.time.TimeUnit
import love.forte.simbot.common.time.Timestamp


/**
 * 基于秒的时间戳 [Timestamp] 实现。
 *
 * @author ForteScarlet
 */
internal class SecondsTimestamp(private val time: Int) : Timestamp {
    override val milliseconds: Long
        get() = TimeUnit.SECONDS.toMillis(time.toLong())

    override fun timeAs(unit: TimeUnit): Long =
        unit.convert(time.toLong(), TimeUnit.SECONDS)

    override fun toString(): String = "SecondsTimestamp(time=$time)"
}

internal fun secondsTimestamp(seconds: Int): Timestamp = SecondsTimestamp(seconds)
