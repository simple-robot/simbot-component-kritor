package love.forte.simbot.component.kritor.core.actor

import love.forte.simbot.common.id.StringID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.definition.Contact


public typealias KritorSourceFriend = io.kritor.friend.FriendData

/**
 * 好友信息
 *
 * @author ForteScarlet
 */
public interface KritorFriend : Contact, KritorActor {
    public val source: KritorSourceFriend

    /**
     * 好友信息中的 `uid`
     *
     * @see io.kritor.friend.FriendData.getUid
     */
    override val id: StringID
        get() = source.uid.ID

    /**
     * 好友信息中的 `uin`
     * @see io.kritor.friend.FriendData.getUin
     */
    public val uin: ULongID
        get() = source.uin.toULong().ID

    /**
     * 好友昵称
     */
    override val name: String
        get() = source.nick

    /**
     * 好友头像
     */
    override val avatar: String?
        get() = "https://q1.qlogo.cn/g?b=qq&nk=${source.uin}&s=100"


}
