package love.forte.simbot.component.kritor.core.actor.internal

import love.forte.simbot.common.id.StringID
import love.forte.simbot.common.id.StringID.Companion.ID
import love.forte.simbot.component.kritor.core.actor.KritorContactInfo


internal data class KritorContactInfoEventImpl(private val source: io.kritor.event.Contact) : KritorContactInfo {
    override val peer: StringID
        get() = source.peer.ID
    override val subPeer: StringID?
        get() = if (source.hasSubPeer()) source.subPeer.ID else null

    override fun toString(): String = "KritorContactInfo(peer=$peer, subPeer=$subPeer)"
}

internal fun io.kritor.event.Contact.toKritorContactInfo(): KritorContactInfo =
    KritorContactInfoEventImpl(this)


internal data class KritorContactInfoMessageImpl(private val source: io.kritor.message.Contact) : KritorContactInfo {
    override val peer: StringID
        get() = source.peer.ID
    override val subPeer: StringID?
        get() = if (source.hasSubPeer()) source.subPeer.ID else null

    override fun toString(): String = "KritorContactInfo(peer=$peer, subPeer=$subPeer)"
}

internal fun io.kritor.message.Contact.toKritorContactInfo(): KritorContactInfo =
    KritorContactInfoMessageImpl(this)
