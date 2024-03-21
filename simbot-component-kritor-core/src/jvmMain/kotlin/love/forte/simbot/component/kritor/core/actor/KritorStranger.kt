package love.forte.simbot.component.kritor.core.actor

import io.kritor.contact.StrangerInfo
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.LongID.Companion.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.definition.Contact
import love.forte.simbot.suspendrunner.ST

public typealias KritorSourceStrangerInfo = StrangerInfo

/**
 * 一个陌生人的信息。
 *
 * @author ForteScarlet
 */
public interface KritorStranger : KritorActor<KritorSourceStrangerInfo>, Contact {
    /**
     * 陌生人的 `uid`
     */
    override val id: ID
        get() = source.uid.ID

    /**
     * 陌生人的 `uin`
     *
     * @see StrangerInfo.getUin
     */
    public val uin: ID
        get() = source.uin.ID

    /**
     * 请求并判断此陌生人是否为黑名单用户。
     */
    @ST
    public suspend fun isBlackList(): Boolean

    /**
     * 点赞这个陌生人。
     */
    @ST
    public suspend fun vote()
}
