package love.forte.simbot.component.kritor.core.message

import love.forte.simbot.ability.DeleteFailureException
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.StandardDeleteOption
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.ULongID
import love.forte.simbot.common.time.Timestamp
import love.forte.simbot.message.MessageReceipt


/**
 * Kritor 消息发送后的回执结果。
 *
 * Kritor 发送消息允许提供多个不同的消息元素，
 * 因此在 **Kritor组件** 中，每次发送消息都会一次性作为一个整体发送。
 *
 * @author ForteScarlet
 */
public interface KritorMessageReceipt : MessageReceipt {
    /**
     * 发送成功后的消息ID
     */
    public val id: ULongID

    /**
     * 发送时间
     */
    public val time: Timestamp

    /**
     * 撤回这条消息。
     *
     * @throws DeleteFailureException 如果请求撤回产生异常且未指定 [StandardDeleteOption.IGNORE_ON_FAILURE]
     */
    @JvmSynthetic
    override suspend fun delete(vararg options: DeleteOption)
}
