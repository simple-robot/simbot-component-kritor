package love.forte.simbot.component.kritor.core.actor

import io.kritor.group.MemberRole
import love.forte.simbot.common.id.IntID
import love.forte.simbot.common.id.IntID.Companion.ID
import love.forte.simbot.definition.Role

/**
 * 群成员角色
 * @see MemberRole
 */
public enum class KritorMemberRole(public val source: MemberRole) : Role {
    ADMIN(MemberRole.ADMIN),
    MEMBER(MemberRole.MEMBER),
    OWNER(MemberRole.OWNER),
    STRANGER(MemberRole.STRANGER);

    /**
     * 同 [ordinal]
     */
    override val id: IntID
        get() = ordinal.ID

    /**
     * 是否为 [OWNER] 或 [ADMIN]
     */
    override val isAdmin: Boolean
        get() = this == OWNER || this == ADMIN
}
