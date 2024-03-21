package love.forte.simbot.component.kritor.core.time

import love.forte.simbot.common.time.TimeUnit
import love.forte.simbot.common.time.Timestamp


/**
 * 基于秒的时间戳 [Timestamp] 实现。
 *
 * @author ForteScarlet
 */
internal class SecondsTimestamp(private val seconds: Int) : Timestamp {
    override val milliseconds: Long
        get() = TimeUnit.SECONDS.toMillis(seconds.toLong())

    override fun timeAs(unit: TimeUnit): Long =
        unit.convert(seconds.toLong(), TimeUnit.SECONDS)
}

internal fun secondsTimestamp(seconds: Int): Timestamp = SecondsTimestamp(seconds)
