package love.forte.simbot.component.kritor.core.bot.internal

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
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
import io.kritor.group.getGroupListRequest
import io.kritor.guild.GuildServiceGrpcKt
import io.kritor.message.ForwardMessageServiceGrpcKt
import io.kritor.message.MessageServiceGrpcKt
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import love.forte.simbot.annotations.FragileSimbotAPI
import love.forte.simbot.annotations.InternalSimbotAPI
import love.forte.simbot.bot.ContactRelation
import love.forte.simbot.bot.GuildRelation
import love.forte.simbot.bot.JobBasedBot
import love.forte.simbot.common.collectable.Collectable
import love.forte.simbot.common.collectable.flowCollectable
import love.forte.simbot.common.id.*
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.component.kritor.core.AuthException
import love.forte.simbot.component.kritor.core.KritorComponent
import love.forte.simbot.component.kritor.core.actor.KritorGroup
import love.forte.simbot.component.kritor.core.actor.internal.toGroup
import love.forte.simbot.component.kritor.core.bot.*
import love.forte.simbot.component.kritor.core.event.KritorUnsupportedEvent
import love.forte.simbot.component.kritor.core.event.internal.KritorGroupMessageEventImpl
import love.forte.simbot.event.Event
import love.forte.simbot.event.EventDispatcher
import love.forte.simbot.event.onEachError
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
    private val eventDispatcher: EventDispatcher
) : KritorBot, JobBasedBot() {
    private val logger = LoggerFactory.getLogger("love.forte.simbot.component.kritor.core.bot.${authReq.account}")
    private val eventLogger = LoggerFactory.getLogger("love.forte.simbot.component.kritor.core.bot.event.${authReq.account}")
    internal val subCoroutineContext: CoroutineContext = coroutineContext.minusKey(Job)

    override val id: ID = authReq.account.ID

    private var _currentAccount: GetCurrentAccountResponse? = null

    internal val currentAccount: GetCurrentAccountResponse
        get() = _currentAccount ?: throw IllegalStateException("Bot is not start")

    override val accountInfo: BotAccountInfo
        get() = BotAccountInfoImpl(currentAccount)

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
        job.invokeOnCompletion {
            logger.info("Shutdown channel {}", channel)
            channel.shutdown()
        }

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
            return getGroupListResponse(refresh).groupInfoCount
        }

        override fun groups(refresh: Boolean): Collectable<KritorGroup> = flowCollectable {
            val list = getGroupListResponse(refresh).groupInfoList
            list.forEach { g -> emit(g.toGroup(this@KritorBotImpl)) }
        }

        private suspend fun getGroupListResponse(refresh: Boolean) =
            services.groupService.getGroupList(getGroupListRequest {
                this.refresh = refresh
            })

        override suspend fun group(id: ID): KritorGroup? {
            val groupIdValue = (id as? NumericalID)?.toLong() ?: id.literal.toLong()

            return kotlin.runCatching {
                this@KritorBotImpl.getGroupInfo {
                    this.groupId = groupIdValue
                }
            }.getOrElse { e ->
                val status = Status.fromThrowable(e)
                if (status.code == Status.Code.NOT_FOUND) null else throw e
            }?.toGroup(this@KritorBotImpl)
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
                runCatching {
                    when (eventStructure.type) {
                        EventType.EVENT_TYPE_MESSAGE -> acceptMessageEvent(eventStructure, eventStructure.message)

                        else -> pushUnsupported(eventStructure)// TODO("processEvents $eventStructure")
                    }
                }
            }
    }

    @OptIn(InternalSimbotAPI::class, FragileSimbotAPI::class)
    private fun pushUnsupported(sourceStructure: EventStructure) {
        pushLaunch(KritorUnsupportedEvent(this, sourceStructure))
    }

    private fun pushLaunch(event: Event) {
        eventLogger.debug("On event: {}", event)
        eventDispatcher.push(event)
            .onEachError { r ->
                eventLogger.error("On event error result: {}", r, r.content)
            }
            .launchIn(this)
    }

    private fun acceptMessageEvent(eventStructure: EventStructure, event: MessageEvent) {
        val contact = event.contact
        val sender = event.sender
        when (contact.scene) {
            Scene.GROUP -> {
                pushLaunch(
                    KritorGroupMessageEventImpl(
                        bot = this,
                        sourceEventStructure = eventStructure,
                        sourceEvent = event
                    )
                )
            }

            else -> pushUnsupported(eventStructure) // TODO("acceptMessageEvent $event")
        }
    }

}

private class BotAccountInfoImpl(private val response: GetCurrentAccountResponse) : BotAccountInfo {
    override val uid: StringID
        get() = response.accountUid.ID

    override val uin: ULongID
        get() = response.accountUin.toULong().ID

    override val name: String
        get() = response.accountName
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





