package love.forte.simbot.component.kritor.core.bot

import love.forte.simbot.application.Application
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 *
 * @author ForteScarlet
 */
public class KritorBotConfiguration {
    /**
     * 提供给 [KritorBot] 内使用的协程上下文。
     * 最终使用的上下文是由 [Application] 提供的上下文和当前配置的上下文的合并结果，
     * 以当前配置的上下文**为主**。
     */
    public var coroutineContext: CoroutineContext = EmptyCoroutineContext

}
