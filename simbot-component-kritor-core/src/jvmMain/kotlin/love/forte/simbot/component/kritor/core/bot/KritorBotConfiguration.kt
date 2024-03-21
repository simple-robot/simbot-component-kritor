package love.forte.simbot.component.kritor.core.bot

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineDispatcher
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 *
 * @author ForteScarlet
 */
public class KritorBotConfiguration {
    /**
     * 提供给 [KritorBot] 内使用的协程上下文。
     * 最终使用的上下文是由 [KritorBotManager] 提供的上下文和当前配置的上下文的合并结果，
     * 以当前配置的上下文**为主**。
     */
    public var coroutineContext: CoroutineContext = EmptyCoroutineContext

    /**
     * 是否将 [coroutineContext]
     * 中的 [CoroutineDispatcher]
     * 作为 [ManagedChannelBuilder.executor]。
     * 如果为 `true`, 则会尝试获取 [coroutineContext] 中提供的 [CoroutineDispatcher]
     * 并转化为 [Executor] 后提供。如果没找到则不提供。
     *
     * 默认为 `false`。
     */
    public var coroutineDispatcherAsChannelExecutor: Boolean = false
}

