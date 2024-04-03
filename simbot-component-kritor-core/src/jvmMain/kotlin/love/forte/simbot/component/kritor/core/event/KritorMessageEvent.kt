package love.forte.simbot.component.kritor.core.event

import io.kritor.event.EventStructure
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.component.kritor.core.actor.*
import love.forte.simbot.component.kritor.core.message.KritorMessageContent
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.component.kritor.core.time.secondsTimestamp
import love.forte.simbot.event.ChatGroupMessageEvent
import love.forte.simbot.event.ContactMessageEvent
import love.forte.simbot.event.MemberMessageEvent
import love.forte.simbot.event.MessageEvent
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.suspendrunner.ST
import love.forte.simbot.suspendrunner.STP
import io.kritor.event.MessageEvent as KritorProtoMessageEvent


/**
 * [KritorEvent] 的子类型，
 * 当 [sourceEventStructure.event][EventStructure.getEventCase] 的 `oneof` 值为 [KritorProtoMessageEvent] 时的类型。
 *
 * @author ForteScarlet
 */
public interface KritorMessageEvent : KritorEvent, MessageEvent {
    /**
     * 事件内容。
     *
     * @see EventStructure.getMessage
     */
    public val sourceEvent: KritorProtoMessageEvent
        get() = sourceEventStructure.message

    /**
     * 消息时间。
     */
    override val time: Timestamp
        get() = secondsTimestamp(sourceEvent.time)

    /**
     * 接收到的消息内容。
     */
    override val messageContent: KritorMessageContent
}

/**
 * [KritorMessageEvent] 的子类型，表示一个群消息。
 *
 * @author ForteScarlet
 */
public interface KritorGroupMessageEvent : KritorMessageEvent, ChatGroupMessageEvent {

    /**
     * 查询并获得发送消息的群成员信息。
     */
    @STP
    override suspend fun author(): KritorGroupMember

    /**
     * 发送消息的群成员信息。
     * 与 [author] 相比，[authorInfo] 会直接基于事件中的信息，
     * 而不会发生一次查询行为。
     */
    public val authorInfo: KritorBasicGroupMemberInfo

    /**
     * 查询并获得消息事件发生的群。
     */
    @STP
    override suspend fun content(): KritorGroup

    /**
     * 消息事件发送的群。
     * 与 [content] 相比，[groupInfo] 会直接基于事件中的信息，
     * 而不会发生一次查询行为。
     */
    public val groupInfo: KritorBasicGroupInfo

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(text: String): KritorMessageReceipt

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(message: Message): KritorMessageReceipt

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(messageContent: MessageContent): KritorMessageReceipt
}

/**
 * [KritorMessageEvent] 的子类型，表示一个来自群的临时会话私聊消息。
 * @author ForteScarlet
 */
public interface KritorMemberMessageEvent : KritorMessageEvent, MemberMessageEvent {

    /**
     * 查询并获得发送消息的群成员信息。
     */
    @STP
    override suspend fun content(): KritorGroupMember

    /**
     * 发送消息的群成员信息。
     * 与 [content] 相比，[authorInfo] 会直接基于事件中的信息，
     * 而不会发生一次查询行为。
     */
    public val authorInfo: KritorBasicGroupMemberInfo

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(text: String): KritorMessageReceipt

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(message: Message): KritorMessageReceipt

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(messageContent: MessageContent): KritorMessageReceipt
}


/**
 * [KritorMessageEvent] 的子类型，表示一个来自好友的私聊消息。
 * @author ForteScarlet
 */
public interface KritorFriendMessageEvent : KritorMessageEvent, ContactMessageEvent {
    // TODO

    override val authorId: ID
        get() = sourceEvent.sender.uid.ID

    @STP
    override suspend fun content(): KritorFriend

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(text: String): KritorMessageReceipt

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(message: Message): KritorMessageReceipt

    /**
     * 回复本事件内的这条消息
     */
    @ST
    override suspend fun reply(messageContent: MessageContent): KritorMessageReceipt
}

// 临时会话？
// 陌生人？
// enum Scene {
//   GROUP = 0; // 群聊
//   FRIEND = 1; // 私聊
//   GUILD = 2; // 频道
//   STRANGER_FROM_GROUP = 10; // 群临时会话
//
//   // 以下类型为可选实现
//   NEARBY = 5; // 附近的人
//   STRANGER = 9; // 陌生人
// }
