package love.forte.simbot.component.kritor.core.actor.internal

import io.grpc.Status
import io.grpc.Status.Code
import io.kritor.group.*
import io.kritor.message.Contact
import io.kritor.message.Scene
import io.kritor.message.contact
import love.forte.simbot.ability.DeleteFailureException
import love.forte.simbot.ability.DeleteOption
import love.forte.simbot.ability.StandardDeleteOption
import love.forte.simbot.common.collectable.Collectable
import love.forte.simbot.common.collectable.flowCollectable
import love.forte.simbot.common.id.*
import love.forte.simbot.common.id.LongID.Companion.ID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.common.id.ULongID.Companion.ID
import love.forte.simbot.component.kritor.core.actor.KritorBasicGroupInfo
import love.forte.simbot.component.kritor.core.actor.KritorGroup
import love.forte.simbot.component.kritor.core.actor.KritorGroupMember
import love.forte.simbot.component.kritor.core.actor.RemainCountAtAll
import love.forte.simbot.component.kritor.core.bot.internal.KritorBotImpl
import love.forte.simbot.component.kritor.core.bot.internal.sendMessage
import love.forte.simbot.component.kritor.core.message.KritorMessageReceipt
import love.forte.simbot.component.kritor.core.message.internal.toReceipt
import love.forte.simbot.component.kritor.core.message.resolve
import love.forte.simbot.message.Message
import love.forte.simbot.message.MessageContent
import kotlin.coroutines.CoroutineContext


internal abstract class AbstractKritorBasicGroupInfoImpl : KritorBasicGroupInfo {
    internal abstract val bot: KritorBotImpl
    internal abstract val contact: Contact
    internal abstract val groupId: Long

    override val subPeer: ID?
        get() = null

    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt {
        val contact = contact
        return bot.sendMessage(
            contact = contact,
            messageContent = messageContent,
        ).toReceipt(bot, contact)
    }

    override suspend fun send(message: Message): KritorMessageReceipt {
        val contact = contact
        return bot.sendMessage(contact, message).toReceipt(bot, contact)
    }

    override suspend fun send(text: String): KritorMessageReceipt {
        val contact = contact
        return bot.sendMessage(contact, text).toReceipt(bot, contact)
    }

    override suspend fun delete(vararg options: DeleteOption) {
        runCatching {
            bot.services.groupService.leaveGroup(
                leaveGroupRequest {
                    this.groupId = this@AbstractKritorBasicGroupInfoImpl.groupId
                }
            )
        }.onFailure { e ->
            if (StandardDeleteOption.IGNORE_ON_FAILURE !in options) {
                throw DeleteFailureException(e)
            }
        }
    }
}


internal class KritorBasicGroupInfoImpl(
    override val bot: KritorBotImpl,
    val source: GroupInfo
) : AbstractKritorBasicGroupInfoImpl() {
    override val groupId: Long
        get() = source.groupId

    override val contact: Contact
        get() = contact {
            this.scene = Scene.GROUP
            this.peer = groupId.toString()
        }

    override val peer: ULongID
        get() = source.groupId.toULong().ID
}

internal class KritorBasicGroupEventInfoImpl(
    override val bot: KritorBotImpl,
    val source: io.kritor.event.Contact
) : AbstractKritorBasicGroupInfoImpl() {
    override val peer: StringID
        get() = source.peer.ID

    override val groupId: Long
        get() = source.peer.toULong().toLong()

    override val contact: Contact
        get() = source.resolve()
}

internal fun GroupInfo.toGroupInfo(bot: KritorBotImpl): KritorBasicGroupInfoImpl =
    KritorBasicGroupInfoImpl(bot, this)

internal fun io.kritor.event.Contact.toGroupInfo(bot: KritorBotImpl): KritorBasicGroupEventInfoImpl =
    KritorBasicGroupEventInfoImpl(bot, this)

/**
 *
 * @author ForteScarlet
 */
internal class KritorGroupInfoImpl(
    private val bot: KritorBotImpl,
    private val source: GroupInfo,
    private val sourceGroupInfo: AbstractKritorBasicGroupInfoImpl,
) : KritorGroup {
    override val id: ULongID
        get() = source.groupId.toULong().ID

    override val ownerId: ULongID
        get() = source.owner.toULong().ID

    override var name: String = source.groupName
    override var remark: String = source.groupRemark

    override val coroutineContext: CoroutineContext = bot.subCoroutineContext

    override suspend fun modifyName(name: String) {
        bot.services.groupService.modifyGroupName(
            modifyGroupNameRequest {
                this.groupId = source.groupId
                this.groupName = name
            }
        )
        this.name = name
    }

    override suspend fun modifyRemark(remark: String) {
        bot.services.groupService.modifyGroupRemark(
            modifyGroupRemarkRequest {
                this.groupId = source.groupId
                this.remark = remark
            }
        )
        this.remark = remark
    }

    override fun members(refresh: Boolean): Collectable<KritorGroupMember> =
        flowCollectable {
            val resp = bot.services.groupService.getGroupMemberList(
                getGroupMemberListRequest {
                    this.groupId = source.groupId
                    this.refresh = refresh
                }
            )

            if (resp.groupMemberInfoCount == 0) {
                return@flowCollectable
            }

            for (memberInfo in resp.groupMemberInfoList) {
                emit(memberInfo.toMember(bot, source.groupId))
            }
        }

    override suspend fun member(id: ID, refresh: Boolean): KritorGroupMember? {
        val memberInfo = runCatching {
            bot.services.groupService.getGroupMemberInfo(
                getGroupMemberInfoRequest {
                    this.groupId = source.groupId
                    this.refresh = refresh
                    if (id is NumericalID) {
                        this.uin = id.toLong()
                    } else {
                        this.uid = id.literal
                    }
                }
            )
        }.getOrElse { e ->
            val status = Status.fromThrowable(e)
            if (status.code == Code.NOT_FOUND) null else throw e
        }

        return memberInfo?.groupMemberInfo?.toMember(bot, source.groupId)
    }

    override suspend fun botAsMember(): KritorGroupMember = with(bot.currentAccount.accountUin.ID) {
        member(this, false)
            ?: member(this, true) // refresh and try again.?
            ?: error("Bot info not found in group(id=${source.groupId}, name=${source.groupName})")
    }

    override suspend fun delete(vararg options: DeleteOption) {
        sourceGroupInfo.delete(options = options)
    }

    override suspend fun send(messageContent: MessageContent): KritorMessageReceipt =
        sourceGroupInfo.send(messageContent)

    override suspend fun send(message: Message): KritorMessageReceipt =
        sourceGroupInfo.send(message)

    override suspend fun send(text: String): KritorMessageReceipt =
        sourceGroupInfo.send(text)

    override suspend fun getRemainCountAtAll(): RemainCountAtAll {
        val result = bot.services.groupService.getRemainCountAtAll(
            getRemainCountAtAllRequest {
                this.groupId = source.groupId
            }
        )

        return RemainCountAtAllImpl(
            accessAtAll = result.accessAtAll,
            forGroup = result.remainCountForGroup,
            forSelf = result.remainCountForSelf,
        )
    }
}

private data class RemainCountAtAllImpl(
    override val accessAtAll: Boolean,
    override val forGroup: Int,
    override val forSelf: Int
) : RemainCountAtAll {
    override fun toString(): String {
        return "RemainCountAtAll(accessAtAll=$accessAtAll, forGroup=$forGroup, forSelf=$forSelf)"
    }
}


internal fun GroupInfo.toGroup(
    bot: KritorBotImpl,
    groupInfoImpl: AbstractKritorBasicGroupInfoImpl = toGroupInfo(bot)
): KritorGroupInfoImpl =
    KritorGroupInfoImpl(bot, this, groupInfoImpl)
