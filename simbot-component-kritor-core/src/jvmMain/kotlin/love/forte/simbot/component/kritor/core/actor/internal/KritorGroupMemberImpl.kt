package love.forte.simbot.component.kritor.core.actor.internal

import io.kritor.group.GroupInfo
import io.kritor.group.GroupMemberInfo
import io.kritor.group.banMemberRequest
import io.kritor.group.modifyMemberCardRequest
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.component.kritor.core.actor.KritorGroupMember
import love.forte.simbot.component.kritor.core.actor.RemainCountAtAll
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import love.forte.simbot.message.MessageReceipt
import java.time.Duration
import kotlin.coroutines.CoroutineContext


/**
 *
 * @author ForteScarlet
 */
internal class KritorGroupMemberImpl(
    private val bot: KritorBotImpl,
    private val sourceGroup: GroupInfo,
    override val source: GroupMemberInfo
) : KritorGroupMember {
    override val coroutineContext: CoroutineContext = bot.subCoroutineContext
    override var nick: String = source.card

    override suspend fun modifyNick(newNick: String) {
        bot.services.groupService.modifyMemberCard(modifyMemberCardRequest {
            this.groupId = sourceGroup.groupId
            this.targetUid = source.uid
            this.card = newNick
        })
        this.nick = newNick
    }

    override suspend fun ban(duration: Duration) {
        if (duration.isNegative && duration.isZero) {
            throw IllegalArgumentException("Ban duration cannot be negative.")
        }

        bot.services.groupService.banMember(banMemberRequest {
            this.groupId = sourceGroup.groupId
            this.targetUid = source.uid
            this.duration = duration.toMillis().toInt()
        })
    }

    override suspend fun unban() {
        TODO("Not yet implemented")
    }

    override suspend fun poke() {
        TODO("Not yet implemented")
    }

    override suspend fun delete(vararg options: DeleteOption) {
        TODO("Not yet implemented")
    }

    override suspend fun setAdmin(isAdmin: Boolean) {
        TODO("Not yet implemented")
    }

    override suspend fun getRemainCountAtAll(): RemainCountAtAll {
        TODO("Not yet implemented")
    }

    override suspend fun send(text: String): MessageReceipt {
        TODO("Not yet implemented")
    }

    override suspend fun send(message: Message): MessageReceipt {
        TODO("Not yet implemented")
    }

    override suspend fun send(messageContent: MessageContent): MessageReceipt {
        TODO("Not yet implemented")
    }
}


internal fun GroupMemberInfo.toMember(bot: KritorBotImpl, group: GroupInfo): KritorGroupMemberImpl =
    KritorGroupMemberImpl(bot, group, this)
