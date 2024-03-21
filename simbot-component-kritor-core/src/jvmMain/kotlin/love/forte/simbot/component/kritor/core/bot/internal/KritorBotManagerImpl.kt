package love.forte.simbot.component.kritor.core.bot.internal

import io.grpc.ManagedChannelBuilder
import io.kritor.AuthReq
import kotlinx.coroutines.Job
import love.forte.simbot.bot.Bot
import love.forte.simbot.bot.JobBasedBotManager
import love.forte.simbot.bot.NoSuchBotException
import love.forte.simbot.common.coroutines.mergeWith
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.kritor.core.KritorComponent
import love.forte.simbot.component.kritor.core.bot.KritorBot
import love.forte.simbot.component.kritor.core.bot.KritorBotConfiguration
import love.forte.simbot.component.kritor.core.bot.KritorBotManager
import love.forte.simbot.component.kritor.core.bot.KritorBotManagerConfiguration
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext


/**
 *
 * @author ForteScarlet
 */
internal class KritorBotManagerImpl(
    private val component: KritorComponent,
    private val configuration: KritorBotManagerConfiguration,
    private val coroutineContext: CoroutineContext,
    override val job: Job,
) : KritorBotManager, JobBasedBotManager() {
    private val bots = ConcurrentHashMap<String, Bot>()

    override fun all(): Sequence<Bot> = bots.values.asSequence()

    override fun get(id: ID): Bot = find(id) ?: throw NoSuchBotException("KritorBot(id=$id)")

    override fun find(id: ID): Bot? = bots[id.literal]

    override fun register(
        authReq: AuthReq,
        channelBuilder: ManagedChannelBuilder<*>,
        configuration: KritorBotConfiguration
    ): KritorBot {
        val mergedContext = configuration.coroutineContext.mergeWith(coroutineContext)

        return KritorBotImpl(
            component,
            mergedContext,
            mergedContext[Job]!!,
            authReq,
            channelBuilder,
            configuration
        )
    }
}
