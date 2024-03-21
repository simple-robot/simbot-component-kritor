package love.forte.simbot.component.kritor.core.actor

import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.definition.Contact


public typealias KritorSourceFriend = io.kritor.friend.FriendData

/**
 * 好友信息
 *
 * @author ForteScarlet
 */
public interface KritorFriend : Contact, KritorActor<KritorSourceFriend> {
    /**
     * 好友信息中的 `uid`
     *
     * @see io.kritor.friend.FriendData.getUid
     */
    override val id: ID
        get() = source.uid.ID

    /**
     * 好友信息中的 `uin`
     * @see io.kritor.friend.FriendData.getUin
     */
    public val uin: Long
        get() = source.uin

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
