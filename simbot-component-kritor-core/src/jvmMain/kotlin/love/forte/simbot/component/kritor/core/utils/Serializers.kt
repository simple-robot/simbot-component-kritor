package love.forte.simbot.component.kritor.core.utils

import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder

/**
 * **粗略地** 判断 [Decoder] 是否为一个 JsonDecoder.
 */
internal val Decoder.isJson: Boolean
    get() = this::class.simpleName?.contains("Json") == true


internal fun byteArraySerialDescriptor(name: String): SerialDescriptor =
    buildClassSerialDescriptor(name, ByteArraySerializer().descriptor)
