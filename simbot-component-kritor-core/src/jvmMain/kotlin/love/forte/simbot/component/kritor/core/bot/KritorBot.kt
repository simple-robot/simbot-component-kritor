package love.forte.simbot.component.kritor.core.bot

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Status
import io.kritor.AuthReq
import io.kritor.AuthenticationGrpcKt
import io.kritor.contact.ContactServiceGrpcKt
import io.kritor.core.KritorServiceGrpcKt
import io.kritor.event.EventServiceGrpcKt
import io.kritor.file.GroupFileServiceGrpcKt
import io.kritor.friend.FriendServiceGrpcKt
import io.kritor.group.GroupServiceGrpcKt
import io.kritor.guild.GuildServiceGrpcKt
import io.kritor.message.ForwardMessageServiceGrpcKt
import io.kritor.message.MessageServiceGrpcKt
import love.forte.simbot.bot.Bot
import love.forte.simbot.bot.GroupRelation
import love.forte.simbot.common.collectable.Collectable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.component.kritor.core.AuthException
import love.forte.simbot.component.kritor.core.actor.KritorGroup
import love.forte.simbot.definition.ChatGroup
import love.forte.simbot.suspendrunner.ST
import love.forte.simbot.suspendrunner.STP
import kotlin.coroutines.CoroutineContext


/**
 * 一个 `Kritor` 组件的 Bot。
 *
 * @author ForteScarlet
 */
public interface KritorBot : Bot {
    override val coroutineContext: CoroutineContext

    /**
     * 注册Bot时使用的 [`account`][AuthReq.getAccount]。
     */
    override val id: ID

    /**
     * Bot的账户信息。在 [start] 后被初始化。
     */
    public val accountInfo: BotAccountInfo

    /**
     * Bot的用户名。在 [start] 后才会被初始化。
     *
     * @see accountInfo
     * @see BotAccountInfo.name
     */
    override val name: String
        get() = accountInfo.name

    /**
     * [KritorBot] 内部使用的原始的gRPC服务实例信息。
     * 只有在 [启动][start] 后才能获取。
     */
    public val services: KritorBotServices

    /**
     * 对 [KritorGroup] 的相关操作。
     */
    override val groupRelation: KritorGroupRelation

    /**
     * 启动 [KritorBot].
     *
     * 会使用 [ManagedChannelBuilder] 建立 gRPC 连接并开始处理事件。
     *
     * @throws AuthException 如果鉴权失败
     * @throws Exception 任何gRPC可能产生的错误
     */
    @JvmSynthetic
    override suspend fun start()
}

/**
 * [KritorBot] 中对 [KritorGroup] 的相关操作。
 */
public interface KritorGroupRelation : GroupRelation {
    /**
     * 获取群数量。会请求获取群列表后统计数量。
     * 默认不刷新缓存。
     */
    @STP
    override suspend fun groupCount(): Int = groupCount(refresh = false)

    /**
     * 获取群数量。会请求获取群列表后统计数量。
     *
     * @param refresh 是否刷新缓存
     * @throws Exception 所有gRPC可能产生的异常。
     */
    @STP
    public suspend fun groupCount(refresh: Boolean): Int

    /**
     * 获取群列表。
     * 默认不刷新缓存。
     */
    override val groups: Collectable<KritorGroup>
        get() = groups(refresh = false)

    /**
     * 获取群列表。
     * @param refresh 是否刷新缓存
     */
    public fun groups(refresh: Boolean): Collectable<KritorGroup>

    /**
     * 根据群ID寻找指定的群信息。如果响应状态为 [Status.NOT_FOUND] 则视为未找到而返回 `null`。
     *
     * @throws Exception 所有gRPC可能产生的异常。
     */
    @ST(blockingBaseName = "getGroup", blockingSuffix = "", asyncBaseName = "getGroup", reserveBaseName = "getGroup")
    override suspend fun group(id: ID): KritorGroup?
}

/**
 * Bot 启动后可获取到的账户信息。
 */
public interface BotAccountInfo {
    /**
     * 当前账户
     */
    public val uid: StringID
    public val uin: ULongID

    /**
     * 当前账户名称
     */
    public val name: String
}

/**
 * [KritorBot] 内部使用的原始的gRPC服务实例信息。
 *
 * @see KritorBot.services
 */
public interface KritorBotServices {
        public val channel: ManagedChannel
        public val authentication:  AuthenticationGrpcKt.AuthenticationCoroutineStub
        public val contactService:  ContactServiceGrpcKt.ContactServiceCoroutineStub
        public val forwardMessageService:  ForwardMessageServiceGrpcKt.ForwardMessageServiceCoroutineStub
        public val kritorService:  KritorServiceGrpcKt.KritorServiceCoroutineStub
        public val eventService:  EventServiceGrpcKt.EventServiceCoroutineStub
        public val friendService:  FriendServiceGrpcKt.FriendServiceCoroutineStub
        public val groupService:  GroupServiceGrpcKt.GroupServiceCoroutineStub
        public val groupFileService:  GroupFileServiceGrpcKt.GroupFileServiceCoroutineStub
        public val guildService:  GuildServiceGrpcKt.GuildServiceCoroutineStub
        public val messageService:  MessageServiceGrpcKt.MessageServiceCoroutineStub
}
