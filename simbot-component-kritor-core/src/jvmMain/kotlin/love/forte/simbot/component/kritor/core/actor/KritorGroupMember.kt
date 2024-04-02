package love.forte.simbot.component.kritor.core.actor

import io.kritor.group.GroupMemberInfo
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.DeleteSupport
import love.forte.simbot.ability.SendSupport
import love.forte.simbot.common.id.StringID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.definition.Member
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.suspendrunner.ST
import java.time.Duration

/**
 * 一个在 [KritorSenderInfo] 所能够提供的信息的基础上提供最低限度功能的群成员信息类型。
 */
public interface KritorBasicGroupMemberInfo : KritorSenderInfo, DeleteSupport, SendSupport {

    /**
     * 向此成员发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt

    /**
     * 向此成员发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(message: Message): KritorMessageReceipt

    /**
     * 向此成员发送消息
     *
     * @throws Exception 任何gRPC可能产生的异常
     */
    @ST
    override suspend fun send(text: String): KritorMessageReceipt

    /**
     * 踢出此成员。
     * [options] 中可以额外使用 [KritorGroupMemberDeleteOption] 提供的可选项。
     *
     * @see KritorGroupMemberDeleteOption
     */
    @JvmSynthetic
    override suspend fun delete(vararg options: DeleteOption)
}


/**
 * 群成员信息
 *
 * @author ForteScarlet
 */
public interface KritorGroupMember : Member, KritorActor, DeleteSupport, KritorSenderInfo {
    public val source: GroupMemberInfo

    /**
     * 群成员信息中的 `uid`
     */
    override val id: StringID
        get() = source.uid.ID

    /**
     * 群成员信息中的 `uin`
     * @see GroupMemberInfo.getUin
     */
    override val uin: ULongID
        get() = source.uin.toULong().ID

    /**
     * 群成员昵称
     */
    override val name: String
        get() = source.nick

    /**
     * 群成员群昵称。
     * [modifyNick] 修改成功后会随之发生变化。
     */
    override val nick: String

    /**
     * 修改此成员的成员名片。
     * 修改完成后的结果会反应到 [nick] 上。
     *
     * @throws Exception 任何gRPC可能产生的异常，例如你没有权限
     */
    public suspend fun modifyNick(newNick: String)

    /**
     * 群成员头像
     */
    override val avatar: String?
        get() = "https://q1.qlogo.cn/g?b=qq&nk=${source.uin}&s=100"

    /**
     * 群成员年龄
     * @see GroupMemberInfo.getAge
     */
    public val age: Int
        get() = source.age

    /**
     * 群成员级别
     * @see GroupMemberInfo.getLevel
     */
    public val level: Int
        get() = source.level

    /**
     * 禁言此成员。
     * 时间如果小于1秒，则抛出异常 [IllegalArgumentException].
     *
     * @throws IllegalArgumentException 如果时间小于1秒
     * @throws Exception 所有gRPC可能产生的异常
     */
    @ST
    public suspend fun ban(duration: Duration)

    /**
     * 取消此成员的禁言。
     */
    @ST
    public suspend fun unban()

    /**
     * 戳一戳此成员的头像。
     */
    @ST
    public suspend fun poke()

    /**
     * 踢出此成员。
     * [options] 中可以额外使用 [KritorGroupMemberDeleteOption] 提供的可选项。
     *
     * @see KritorGroupMemberDeleteOption
     */
    @JvmSynthetic
    override suspend fun delete(vararg options: DeleteOption)

    /**
     * 设置为管理员/取消管理员
     *
     * @throws Exception 任何gRPC可能产生的错误
     */
    @ST
    public suspend fun setAdmin(isAdmin: Boolean)

    /**
     * 获取 `@全体成员` 剩余次数
     */
    @ST
    public suspend fun getRemainCountAtAll(): RemainCountAtAll
}


/**
 * 可用于 [踢出群成员][KritorGroupMember.delete] 的额外可选项。
 *
 */
public sealed class KritorGroupMemberDeleteOption : DeleteOption {
    /**
     * 拒绝再次申请
     */
    public data object RejectAddRequest : KritorGroupMemberDeleteOption()

    /**
     * 提供拒绝原因
     */
    public data class KickReason(val reason: String) : KritorGroupMemberDeleteOption()

    public companion object {
        /**
         * 得到 [RejectAddRequest]
         */
        @JvmStatic
        public fun rejectAddRequest(): KritorGroupMemberDeleteOption = RejectAddRequest

        /**
         * 得到 [KickReason]
         */
        @JvmStatic
        public fun kickReason(reason: String): KritorGroupMemberDeleteOption = KickReason(reason)
    }
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
