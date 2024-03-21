package love.forte.simbot.component.kritor.core.bot

import io.grpc.ManagedChannelBuilder
import io.kritor.AuthReq
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import love.forte.simbot.bot.SerializableBotConfiguration
import love.forte.simbot.component.kritor.core.KritorComponent


/**
 * 用于基于可序列化配置文件动态注册 [KritorBot] 的可序列化配置类。
 *
 * 注意：仅用于序列化的类型，不保证直接使用的稳定性。
 *
 * @see KritorBot
 * @see KritorBotConfiguration
 *
 * @author ForteScarlet
 */
@Serializable
@SerialName(KritorComponent.ID_VALUE)
public class SerializableKritorBotConfiguration(
    public val auth: Auth,
    public val config: Config? = null
) : SerializableBotConfiguration() {

    /**
     * 额外的可配置项。
     */
    @Serializable
    public data class Config(
        /**
         * @see KritorBotConfiguration.coroutineDispatcherAsChannelExecutor
         */
        var coroutineDispatcherAsChannelExecutor: Boolean? = null,
    )

    /**
     * 将 [auth] 转为 [AuthReq]。
     */
    public fun toAuthReq(): AuthReq = io.kritor.authReq {
        account = auth.account
        ticket = auth.ticket
    }

    /**
     * 将 [auth] 转为 [ManagedChannelBuilder]。
     */
    public fun toManagedChannelBuilder(): ManagedChannelBuilder<*> =
        auth.channel.toManagedChannelBuilder()

    /**
     * 将 [config] 转为 [KritorBotConfiguration]
     */
    public fun toConfiguration(): KritorBotConfiguration {
        return KritorBotConfiguration().apply {
            config?.also { c ->
                c.coroutineDispatcherAsChannelExecutor?.also { coroutineDispatcherAsChannelExecutor = it }
            }
        }
    }
}

/**
 * Auth信息,
 * 与 [AuthReq] 对应。
 *
 * ```json
 * {
 *   "account": "...",
 *   "ticket": "...",
 *   "channel": {
 *       "type": "address",
 *       "name": "localhost",
 *       "port": 8080
 *   }
 * }
 * ```
 *
 * @see AuthReq
 *
 * @property channel 用于在 [ManagedChannelBuilder] 中创建连接信息的配置。
 */
@Serializable
public data class Auth(
    val account: String,
    val ticket: String,
    val channel: Channel = DefaultChannel
) {
    @Serializable
    public sealed class Channel {
        /**
         * 默认方式，使用 [ManagedChannelBuilder.forAddress]
         * @see ManagedChannelBuilder.forAddress
         */
        @Serializable
        @SerialName("address")
        public data class Address(val name: String, val port: Int) : Channel() {
            override fun toManagedChannelBuilder(): ManagedChannelBuilder<*> =
                ManagedChannelBuilder.forAddress(name, port)
        }

        /**
         * 使用 [ManagedChannelBuilder.forTarget]
         * @see ManagedChannelBuilder.forTarget
         */
        @Serializable
        @SerialName("target")
        public data class Target(val target: String) : Channel() {
            override fun toManagedChannelBuilder(): ManagedChannelBuilder<*> =
                ManagedChannelBuilder.forTarget(target)
        }

        /**
         * 转为 [ManagedChannelBuilder]
         */
        public abstract fun toManagedChannelBuilder(): ManagedChannelBuilder<*>
    }


    public companion object {
        private val DefaultChannel = Channel.Address("localhost", 8080)
    }
}
