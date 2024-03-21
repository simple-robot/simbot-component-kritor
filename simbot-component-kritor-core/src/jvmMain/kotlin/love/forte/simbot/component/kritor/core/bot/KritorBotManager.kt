package love.forte.simbot.component.kritor.core.bot

import io.grpc.ManagedChannelBuilder
import io.kritor.AuthReq
import io.kritor.authReq
import kotlinx.coroutines.Job
import love.forte.simbot.application.Application
import love.forte.simbot.bot.BotManager
import love.forte.simbot.bot.BotManagerFactory
import love.forte.simbot.bot.SerializableBotConfiguration
import love.forte.simbot.bot.UnsupportedBotConfigurationException
import love.forte.simbot.common.coroutines.mergeWith
import love.forte.simbot.common.function.ConfigurerFunction
import love.forte.simbot.common.function.invokeBy
import love.forte.simbot.component.NoSuchComponentException
import love.forte.simbot.component.kritor.core.KritorComponent
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotManagerImpl
import love.forte.simbot.plugin.PluginConfigureContext
import love.forte.simbot.plugin.PluginFactory
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * 针对 [KritorBot] 的 [BotManager] 实现。
 *
 * @author ForteScarlet
 */
public interface KritorBotManager : BotManager {
    /**
     * 校验 [configuration] 是否为 [SerializableKritorBotConfiguration].
     */
    override fun configurable(configuration: SerializableBotConfiguration): Boolean =
        configuration is SerializableKritorBotConfiguration

    /**
     * 使用 [SerializableKritorBotConfiguration] 注册一个 [KritorBot].
     *
     * @throws UnsupportedBotConfigurationException 如果 [configuration] 不是 [SerializableKritorBotConfiguration]
     */
    override fun register(configuration: SerializableBotConfiguration): KritorBot {
        if (configuration !is SerializableKritorBotConfiguration) {
            throw UnsupportedBotConfigurationException("Required 'SerializableKritorBotConfiguration', but ${configuration::class}")
        }

        val authReq = configuration.toAuthReq()
        val builder = configuration.toManagedChannelBuilder()
        val botConfig = configuration.toConfiguration()

        return register(authReq, builder, botConfig)
    }

    /**
     * 直接提供最基础、最原始的所需信息，并构建一个 [KritorBot].
     */
    public fun register(authReq: AuthReq, channelBuilder: ManagedChannelBuilder<*>, configuration: KritorBotConfiguration): KritorBot

    /**
     * 直接提供最基础、最原始的所需信息，并构建一个 [KritorBot].
     */
    public fun register(authReq: AuthReq, channelBuilder: ManagedChannelBuilder<*>): KritorBot =
        register(authReq, channelBuilder, KritorBotConfiguration())

    /**
     * 直接提供最基础、最原始的所需信息，并构建一个 [KritorBot].
     */
    public fun register(authReq: AuthReq, channelBuilder: ManagedChannelBuilder<*>, configurer: ConfigurerFunction<KritorBotConfiguration>): KritorBot =
        register(authReq, channelBuilder, KritorBotConfiguration().invokeBy(configurer))

    /**
     * 提供鉴权信息和连接信息，构建一个 [KritorBot].
     */
    public fun register(account: String, ticket: String, name: String, port: Int, configuration: KritorBotConfiguration): KritorBot {
        return register(authReq {
            this.account = account
            this.ticket = ticket
        }, ManagedChannelBuilder.forAddress(name, port).usePlaintext().enableRetry(), configuration)
    }

    /**
     * 提供鉴权信息和连接信息，构建一个 [KritorBot].
     */
    public fun register(account: String, ticket: String, name: String, port: Int, configurer: ConfigurerFunction<KritorBotConfiguration>): KritorBot =
        register(account, ticket, name, port, KritorBotConfiguration().invokeBy(configurer))

    /**
     * 提供鉴权信息和连接信息，构建一个 [KritorBot].
     */
    public fun register(account: String, ticket: String, name: String, port: Int): KritorBot =
        register(account, ticket, name, port, KritorBotConfiguration())


    public companion object Factory : BotManagerFactory<KritorBotManager, KritorBotManagerConfiguration> {
        override val key: PluginFactory.Key = object : PluginFactory.Key {}

        override fun create(
            context: PluginConfigureContext,
            configurer: ConfigurerFunction<KritorBotManagerConfiguration>
        ): KritorBotManager {
            val component = context.components.find { it is KritorComponent } as? KritorComponent
                ?: throw NoSuchComponentException("KritorComponent(id=${KritorComponent.ID_VALUE}) was not installed")

            val configuration = KritorBotManagerConfiguration().invokeBy(configurer)
            val appConfiguration = context.applicationConfiguration

            val mergedContext = configuration.coroutineContext.mergeWith(appConfiguration.coroutineContext)

            return KritorBotManagerImpl(
                component,
                configuration,
                mergedContext,
                mergedContext[Job]!!
            )
        }
    }
}

/**
 * [KritorBotManager] 的配置类。
 *
 */
public class KritorBotManagerConfiguration {
    /**
     * 提供给 [KritorBot] 内使用的协程上下文。
     * 最终使用的上下文是由 [Application] 提供的上下文和当前配置的上下文的合并结果，
     * 以当前配置的上下文**为主**。
     */
    public var coroutineContext: CoroutineContext = EmptyCoroutineContext
}

