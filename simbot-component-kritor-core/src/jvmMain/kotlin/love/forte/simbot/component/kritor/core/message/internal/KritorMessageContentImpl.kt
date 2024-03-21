package love.forte.simbot.component.kritor.core.message.internal

import io.kritor.event.Element
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.message.KritorMessageContent
import love.forte.simbot.message.Messages


/**
 *
 * @author ForteScarlet
 */
internal class KritorMessageContentImpl(
    private val bot: KritorBotImpl,
    private val messageId: String,
    override val sourceElements: List<Element>
) : KritorMessageContent {
    override val id: ID
        get() = messageId.ID

    override val messages: Messages
        get() = TODO("Not yet implemented")


    override val plainText: String?
        get() = TODO("Not yet implemented")

    override suspend fun delete(vararg options: DeleteOption) {
        TODO("Not yet implemented")
    }
}
