package love.forte.simbot.component.kritor.core.actor

import love.forte.simbot.common.id.ID


/**
 * 一个联系人基本信息，与 [io.kritor.message.Contact]
 * 和 [io.kritor.event.Contact] 比较相似。
 *
 * @author ForteScarlet
 */
public interface KritorContactInfo {
    /**
     * `peer`. 群聊则为群号 私聊则为QQ号
     */
    public val peer: ID

    /**
     * `sub_peer`.
     * 群临时聊天则为群号 频道消息则为子频道号 其它情况可不提供
     */
    public val subPeer: ID?
}

