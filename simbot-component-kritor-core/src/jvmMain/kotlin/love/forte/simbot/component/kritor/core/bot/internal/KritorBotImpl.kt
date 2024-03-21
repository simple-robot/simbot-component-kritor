package love.forte.simbot.component.kritor.core.bot.internal

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.kritor.AuthCode
import io.kritor.AuthReq
import io.kritor.AuthRsp
import io.kritor.AuthenticationGrpcKt
import io.kritor.contact.ContactServiceGrpcKt
import io.kritor.core.GetCurrentAccountResponse
import io.kritor.core.KritorServiceGrpcKt
import io.kritor.core.getCurrentAccountRequest
import io.kritor.event.*
import io.kritor.file.GroupFileServiceGrpcKt
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.group.GroupServiceGrpcKt
import io.kritor.guild.GuildServiceGrpcKt
import io.kritor.message.ForwardMessageServiceGrpcKt
import io.kritor.message.MessageServiceGrpcKt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import love.forte.simbot.bot.ContactRelation
import love.forte.simbot.bot.GroupRelation
import love.forte.simbot.bot.GuildRelation
import love.forte.simbot.bot.JobBasedBot
import love.forte.simbot.common.collectable.Collectable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.component.kritor.core.AuthException
import love.forte.simbot.component.kritor.core.KritorComponent
import love.forte.simbot.component.kritor.core.actor.KritorGroup
import love.forte.simbot.component.kritor.core.bot.KritorBot
import love.forte.simbot.component.kritor.core.bot.KritorBotConfiguration
import love.forte.simbot.component.kritor.core.bot.KritorBotServices
import love.forte.simbot.component.kritor.core.bot.KritorGroupRelation
import love.forte.simbot.logger.LoggerFactory
import kotlin.coroutines.CoroutineContext


/**
 *
 * @author ForteScarlet
 */
internal class KritorBotImpl(
    override val component: KritorComponent,
    override val coroutineContext: CoroutineContext,
    override val job: Job,
    private val authReq: AuthReq,
    private val managedChannelBuilder: ManagedChannelBuilder<*>,
    private val configuration: KritorBotConfiguration,
) : KritorBot, JobBasedBot() {
    private val logger = LoggerFactory.getLogger("love.forte.simbot.component.kritor.core.bot.${authReq.account}")
    internal val subCoroutineContext: CoroutineContext = coroutineContext.minusKey(Job)

    override val id: ID = authReq.account.ID

    private var _currentAccount: GetCurrentAccountResponse? = null

    internal val currentAccount: GetCurrentAccountResponse
        get() = _currentAccount ?: throw IllegalStateException("Bot is not start")

    override val name: String
        get() = currentAccount.accountName

    override fun isMe(id: ID): Boolean {
        return id == this.id
    }


    private val startLock = Mutex()
    override lateinit var services: ServicesImpl
        private set

    private var eventLaunchJob: Job? = null

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun start(): Unit = startLock.withLock {
        if (::services.isInitialized) {
            return
        }

        if (configuration.coroutineDispatcherAsChannelExecutor) {
            coroutineContext[CoroutineDispatcher]?.also {
                managedChannelBuilder.executor(it.asExecutor())
            }
        }

        val channel = managedChannelBuilder.build()
        services = initServices(channel)

        // 鉴权
        doAuth()
        _currentAccount = currentAccount()

        val eventFlow = eventFlow()
        eventLaunchJob = launch { processEvents(eventFlow) }.also { job ->
            job.invokeOnCompletion { e ->
                if (e == null) {
                    logger.info("Process event job is on completion")
                } else {
                    logger.info("Process event job is on completion: {}", e.localizedMessage, e)
                }
            }
        }

        isStarted = true
    }

    private fun initServices(channel: ManagedChannel): ServicesImpl = ServicesImpl(channel = channel)

    private suspend fun doAuth(): AuthRsp {
        val resp = services.authentication.auth(authReq)
        if (resp.code != AuthCode.OK) {
            throw AuthException(resp.code)
        }
        return resp
    }

    private suspend fun currentAccount(): GetCurrentAccountResponse {
        return services.kritorService.getCurrentAccount(getCurrentAccountRequest { })
    }

    /**
     * [event](https://github.com/KarinJS/kritor/blob/main/docs/event/event.md)
     */
    private fun eventFlow(type: EventType? = null): Flow<EventStructure> {
        return services.eventService.registerActiveListener(requestPushEvent {
            type?.also { this.type = it }
        })
    }

    override val groupRelation: KritorGroupRelation = KritorGroupRelationImpl()

    private inner class KritorGroupRelationImpl : KritorGroupRelation {
        override suspend fun groupCount(refresh: Boolean): Int {
            TODO("Not yet implemented")
        }

        override fun groups(refresh: Boolean): Collectable<KritorGroup> {
            TODO("Not yet implemented")
        }

        override suspend fun group(id: ID): KritorGroup? {
            TODO("Not yet implemented")
        }
    }

    override val contactRelation: ContactRelation? = null
    override val guildRelation: GuildRelation? = null


    private suspend fun processEvents(eventFlow: Flow<EventStructure>) {
        eventFlow
            .onEach { eventStructure ->
                logger.debug("Receive event, type: {}, structure: {}", eventStructure.type, eventStructure)
            }
            .collect { eventStructure ->
            // runCatching {
            //
            // }
            TODO()

        }
    }

}


@Suppress("unused")
internal class ServicesImpl(
    override val channel: ManagedChannel,
) : KritorBotServices {
    override val authentication = AuthenticationGrpcKt.AuthenticationCoroutineStub(channel)
    override val contactService = ContactServiceGrpcKt.ContactServiceCoroutineStub(channel)
    override val forwardMessageService = ForwardMessageServiceGrpcKt.ForwardMessageServiceCoroutineStub(channel)
    override val kritorService = KritorServiceGrpcKt.KritorServiceCoroutineStub(channel)
    override val eventService = EventServiceGrpcKt.EventServiceCoroutineStub(channel)
    override val friendService = FriendServiceGrpcKt.FriendServiceCoroutineStub(channel)
    override val groupService = GroupServiceGrpcKt.GroupServiceCoroutineStub(channel)
    override val groupFileService = GroupFileServiceGrpcKt.GroupFileServiceCoroutineStub(channel)
    override val guildService = GuildServiceGrpcKt.GuildServiceCoroutineStub(channel)
    override val messageService = MessageServiceGrpcKt.MessageServiceCoroutineStub(channel)
}
