package love.forte.simbot.component.kritor.core.actor

import love.forte.simbot.ability.*
import love.forte.simbot.common.collectable.Collectable
import love.forte.simbot.common.collectable.IterableCollectable
import love.forte.simbot.common.collectable.asCollectable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.definition.ChatGroup
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.suspendrunner.ST
import love.forte.simbot.suspendrunner.STP

/**
 * 一个在 [KritorContactInfo] 所能够提供的信息的基础上提供最低限度功能的群信息类型。
 */
public interface KritorBasicGroupInfo : KritorContactInfo, SendSupport, DeleteSupport {
    // TODO members API ?
    // TODO resolve(): KritorGroup ?

    /**
     * 得到 [KritorMemberRole] 的所有元素值。
     */
    public val roles: IterableCollectable<KritorMemberRole>
        get() = KritorMemberRole.entries.asCollectable()

    /**
     * 向此群发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt

    /**
     * 向此群发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(message: Message): KritorMessageReceipt

    /**
     * 向此群发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(text: String): KritorMessageReceipt

    /**
     * 离开这个群。
     *
     * @throws DeleteFailureException 如果删除失败且 [options] 未指定 [StandardDeleteOption.IGNORE_ON_FAILURE]
     */
    @JvmSynthetic
    override suspend fun delete(vararg options: DeleteOption)
}


/**
 * 群信息
 *
 * @author ForteScarlet
 */
public interface KritorGroup : ChatGroup, KritorActor, DeleteSupport {
    /**
     * 群ID
     */
    override val id: ULongID

    /**
     * 群名称
     */
    override val name: String

    /**
     * 修改群名称。
     * 修改完成后的结果会反应到 [name][KritorGroup.name] 上。
     * 注意：这可能会导致并行获取 [name][KritorGroup.name] 的值不同。
     *
     * [modifyName] 内无锁，不能保证并发安全。
     * 并发使用可能会导致 [name][KritorGroup.name] 的变更结果与预期不符。
     *
     * @throws Exception 任何gRPC可能产生的异常，例如你没有权限
     */
    @ST
    public suspend fun modifyName(name: String)

    /**
     * 群备注
     */
    public val remark: String

    /**
     * 修改群备注。
     * 修改完成后的结果会反应到 [remark][KritorGroup.remark] 上。
     * 注意：这可能会导致并行获取 [remark][KritorGroup.remark] 的值不同。
     *
     * [modifyRemark] 内无锁，不能保证并发安全。
     * 并发使用可能会导致 [remark][KritorGroup.remark] 的变更结果与预期不符。
     *
     * @throws Exception 任何gRPC可能产生的异常，例如你没有权限
     */
    @ST
    public suspend fun modifyRemark(remark: String)

    /**
     * 群主的ID
     */
    override val ownerId: ULongID

    /**
     * 得到 [KritorMemberRole] 的所有元素值。
     */
    override val roles: IterableCollectable<KritorMemberRole>
        get() = KritorMemberRole.entries.asCollectable()

    /**
     * 得到群成员列表。默认不刷新缓存
     */
    override val members: Collectable<KritorGroupMember>
        get() = members(refresh = false)

    /**
     * 得到群成员列表。
     * @param refresh 是否刷新缓存
     */
    public fun members(refresh: Boolean): Collectable<KritorGroupMember>

    /**
     * 寻找指定ID的成员
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST(blockingBaseName = "getMember", blockingSuffix = "", asyncBaseName = "getMember", reserveBaseName = "getMember")
    override suspend fun member(id: ID): KritorGroupMember? =
        member(id, false)

    /**
     * 寻找指定ID的成员
     *
     * @throws Exception 任何gRPC可能产生的异常
     * @param refresh 是否刷新缓存
     */
    @ST(blockingBaseName = "getMember", blockingSuffix = "", asyncBaseName = "getMember", reserveBaseName = "getMember")
    public suspend fun member(id: ID, refresh: Boolean): KritorGroupMember?

    /**
     * 将当前bot作为此群的成员获取。
     */
    @STP
    override suspend fun botAsMember(): KritorGroupMember

    /**
     * 离开这个群。
     *
     * @throws DeleteFailureException 如果删除失败且 [options] 未指定 [StandardDeleteOption.IGNORE_ON_FAILURE]
     */
    @JvmSynthetic
    override suspend fun delete(vararg options: DeleteOption)

    /**
     * 向此群发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt

    /**
     * 向此群发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(message: Message): KritorMessageReceipt

    /**
     * 向此群发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(text: String): KritorMessageReceipt

    /**
     * 获取 `@全体成员` 剩余次数
     */
    @ST
    public suspend fun getRemainCountAtAll(): RemainCountAtAll
}


/**
 * `@全体成员` 剩余次数信息
 */
public interface RemainCountAtAll {
    public val accessAtAll: Boolean

    /**
     * 剩余次数对于全群
     */
    public val forGroup: Int

    /**
     * 剩余次数对于自己
     */
    public val forSelf: Int
}
