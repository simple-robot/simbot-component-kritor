package love.forte.simbot.component.kritor.core.bot

import io.grpc.ManagedChannelBuilder
import love.forte.simbot.bot.Bot
import love.forte.simbot.suspendrunner.ST


/**
 * 一个 `Kritor` 组件的 Bot。
 *
 * @author ForteScarlet
 */
public interface KritorBot : Bot {


    /**
     * 启动 [KritorBot].
     *
     * 会使用 [ManagedChannelBuilder] 建立 gRPC 连接并开始处理事件。
     *
     */
    @JvmSynthetic
    override suspend fun start()
}

