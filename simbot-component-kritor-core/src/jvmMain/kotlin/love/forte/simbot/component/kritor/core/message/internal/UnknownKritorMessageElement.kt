package love.forte.simbot.component.kritor.core.message.internal

import love.forte.simbot.component.kritor.core.message.KritorMessageElement

/**
 * 一个尚且不可知的 [KritorMessageElement]
 */
private class UnknownKritorMessageElement(public val source: Any) : KritorMessageElement {
    override fun toString(): String = "UnknownKritorMessageElement(source=$source)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UnknownKritorMessageElement) return false

        if (source != other.source) return false

        return true
    }

    override fun hashCode(): Int {
        return source.hashCode()
    }
}


internal fun unknownKritorMessageElement(source: Any): KritorMessageElement =
    UnknownKritorMessageElement(source)
