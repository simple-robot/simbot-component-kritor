package love.forte.simbot.component.kritor.core.actor.internal

import io.grpc.Status
import io.grpc.Status.Code
import io.kritor.group.*
import io.kritor.message.*
import love.forte.simbot.ability.DeleteFailureException
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.StandardDeleteOption
import love.forte.simbot.common.collectable.Collectable
import love.forte.simbot.common.collectable.flowCollectable
import love.forte.simbot.common.id.ID
import love.forte.simbot.common.id.LongID.Companion.ID
import love.forte.simbot.common.id.NumericalID
import love.forte.simbot.common.id.literal
import love.forte.simbot.component.kritor.core.actor.KritorGroup
import love.forte.simbot.component.kritor.core.actor.KritorGroupMember
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.message.KritorMessageContent
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.component.kritor.core.message.internal.toReceipt
import love.forte.simbot.component.kritor.core.message.resolveToSendMessageRequest
import love.forte.simbot.component.kritor.core.message.toMessageElement
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import kotlin.coroutines.CoroutineContext


/**
 *
 * @author ForteScarlet
 */
internal class KritorGroupImpl(
    private val bot: KritorBotImpl,
    override val source: GroupInfo,
) : KritorGroup {
    override var name: String = source.groupName
    override var remark: String = source.groupRemark

    override val coroutineContext: CoroutineContext = bot.subCoroutineContext

    override suspend fun modifyName(name: String) {
        bot.services.groupService.modifyGroupName(modifyGroupNameRequest {
            this.groupId = source.groupId
            this.groupName = name
        })
        this.name = name
    }

    override suspend fun modifyRemark(remark: String) {
        bot.services.groupService.modifyGroupRemark(modifyGroupRemarkRequest {
            this.groupId = source.groupId
            this.remark = remark
        })
        this.remark = remark
    }

    override fun members(refresh: Boolean): Collectable<KritorGroupMember> =
        flowCollectable {
            val resp = bot.services.groupService.getGroupMemberList(getGroupMemberListRequest {
                this.groupId = source.groupId
                this.refresh = refresh
            })

            if (resp.groupMemberInfoCount == 0) {
                return@flowCollectable
            }

            for (memberInfo in resp.groupMemberInfoList) {
                emit(memberInfo.toMember(bot, source))
            }
        }

    override suspend fun member(id: ID, refresh: Boolean): KritorGroupMember? {
        val memberInfo = runCatching {
            bot.services.groupService.getGroupMemberInfo(getGroupMemberInfoRequest {
                this.groupId = source.groupId
                this.refresh = refresh
                if (id is NumericalID) {
                    this.uin = id.toLong()
                } else {
                    this.uid = id.literal
                }
            })
        }.getOrElse { e ->
            val status = Status.fromThrowable(e)
            if (status.code == Code.NOT_FOUND) null else throw e
        }

        return memberInfo?.groupMemberInfo?.toMember(bot, source)
    }

    override suspend fun botAsMember(): KritorGroupMember = with(bot.currentAccount.accountUin.ID) {
        member(this, false)
            ?: member(this, true) // refresh and try again.?
            ?: throw IllegalStateException("Bot info not found in group(id=${source.groupId}, name=${source.groupName})")
    }

    override suspend fun delete(vararg options: DeleteOption) {
        runCatching {
            bot.services.groupService.leaveGroup(leaveGroupRequest {
                this.groupId = source.groupId
            })
        }.onFailure { e ->
            if (StandardDeleteOption.IGNORE_ON_FAILURE !in options) {
                throw DeleteFailureException(e)
            }
        }
    }

    private val contact: Contact
        get() = contact {
            this.scene = Scene.GROUP
            this.peer = source.groupId.toString()
        }

    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt {
        val contact = contact
        if (messageContent is KritorMessageContent) {
            val response = bot.services.messageService.sendMessage(sendMessageRequest {
                this.contact = contact
                messageContent.sourceElements.asSequence()
                    .map { it.toMessageElement() }
                    .filter {
                        val case = it.dataCase
                        case != null && case != Element.DataCase.DATA_NOT_SET
                    }
                    .forEach { element ->
                        elements.add(element)
                    }
            })

            return response.toReceipt(bot, contact)
        }

        return send(messageContent.messages)
    }

    override suspend fun send(message: Message): KritorMessageReceipt {
        val contact = contact
        val request = message.resolveToSendMessageRequest(pre = {
            this.contact = contact
        })

        return bot.services.messageService.sendMessage(request).toReceipt(bot, contact)
    }

    override suspend fun send(text: String): KritorMessageReceipt {
        val contact = contact

        return bot.services.messageService.sendMessage(
            sendMessageRequest {
                this.contact = contact
                this.elements.add(element {
                    this.type = ElementType.TEXT
                    this.text = textElement { this.text = text }
                })
            }
        ).toReceipt(bot, contact)
    }
}


internal fun GroupInfo.toGroup(bot: KritorBotImpl): KritorGroupImpl =
    KritorGroupImpl(bot, this)
