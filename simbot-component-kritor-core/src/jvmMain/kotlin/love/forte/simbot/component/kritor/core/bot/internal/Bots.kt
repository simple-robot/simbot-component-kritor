package love.forte.simbot.component.kritor.core.bot.internal

import io.kritor.group.GroupInfo
import io.kritor.group.getGroupInfoRequest
import io.kritor.message.*
import love.forte.simbot.component.kritor.core.message.KritorMessageContent
import love.forte.simbot.component.kritor.core.message.resolveToSendMessageRequest
import love.forte.simbot.component.kritor.core.message.toMessageElement
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent

internal suspend inline fun KritorBotImpl.getGroupInfo(block: io.kritor.group.GetGroupInfoRequestKt.Dsl.() -> Unit): GroupInfo =
    services.groupService.getGroupInfo(getGroupInfoRequest(block)).groupInfo


internal suspend fun KritorBotImpl.sendMessage(request: SendMessageRequest): SendMessageResponse {
    return services.messageService.sendMessage(request)
}

internal typealias RequestDsl = SendMessageRequestKt.Dsl.() -> Unit

internal suspend inline fun KritorBotImpl.sendMessage(block: RequestDsl): SendMessageResponse {
    return services.messageService.sendMessage(sendMessageRequest(block))
}

internal suspend inline fun KritorBotImpl.sendMessage(
    contact: Contact,
    text: String,
    pre: RequestDsl = {},
    post: RequestDsl = {}
): SendMessageResponse = sendMessage {
    pre()
    this.contact = contact
    elements.add(element {
        this.type = ElementType.TEXT
        this.text = textElement { this.text = text }
    })
    post()
}

internal suspend inline fun KritorBotImpl.sendMessage(
    contact: Contact,
    message: Message,
    pre: RequestDsl = {},
    each: (Message.Element, Element) -> Element? = { _, element -> element },
    post: RequestDsl = {}
): SendMessageResponse = sendMessage(
    message.resolveToSendMessageRequest(
        pre = {
            this.contact = contact
            pre()
        },
        each = each,
        post = post,
    )
)

internal suspend inline fun KritorBotImpl.sendMessage(
    contact: Contact,
    messageContent: MessageContent,
    directPre: RequestDsl = {},
    directEach: (io.kritor.event.Element, Element) -> Element? = { _, element -> element },
    directPost: RequestDsl = {},

    pre: RequestDsl = {},
    each: (Message.Element, Element) -> Element? = { _, element -> element },
    post: RequestDsl = {}
): SendMessageResponse {
    if (messageContent is KritorMessageContent) {
        return sendMessage {
            this.contact = contact
            directPre()
            for (element in messageContent.sourceElements) {
                val messageElement = directEach(element, element.toMessageElement())
                    ?: continue

                val dataCause = messageElement.dataCase
                if (dataCause != null && dataCause != Element.DataCase.DATA_NOT_SET) {
                    elements.add(messageElement)
                }
            }
            directPost()
        }
    } else {
        return sendMessage(
            contact = contact,
            message = messageContent.messages,
            pre = pre,
            each = each,
            post = post,
        )
    }
}
