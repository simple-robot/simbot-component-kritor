package love.forte.simbot.component.kritor.core.actor

import love.forte.simbot.common.id.ID


/**
 * 一个包含了 `uid`、`uin` 和 `nick` 的基本送信人信息，
 * 与 [io.kritor.event.Sender] 和 [io.kritor.message.Sender]
 * 基本相似。
 *
 * @author ForteScarlet
 */
public interface KritorSenderInfo {
    /**
     * 用户昵称。
     */
    public val name: String

    /**
     * `uid`.
     */
    public val id: ID

    /**
     * `uin`. Optional.
     * 一些情况下可以被确认为非空，例如在 [KritorGroupMember] 中。
     */
    public val uin: ID?
}


