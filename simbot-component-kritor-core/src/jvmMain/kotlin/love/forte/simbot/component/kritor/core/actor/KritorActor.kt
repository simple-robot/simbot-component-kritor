package love.forte.simbot.component.kritor.core.actor

import kotlinx.coroutines.Job
import love.forte.simbot.component.kritor.core.bot.KritorBot
import love.forte.simbot.definition.Actor
import kotlin.coroutines.CoroutineContext


/**
 * 一个 Kritor 的 actor 类型，
 * 相比 [Actor] 额外提供了可以获取原始数据对象的 [source] 属性。
 *
 * @author ForteScarlet
 */
public interface KritorActor<T> : Actor {
    /**
     * 作为作用域的上下文。来自 [KritorBot]，但不含 [Job]。
     */
    override val coroutineContext: CoroutineContext

    /**
     * 此 [KritorActor] 的数据源头。
     */
    public val source: T
}

