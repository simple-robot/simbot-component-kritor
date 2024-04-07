package love.forte.simbot.component.kritor.core.message

import java.util.*
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals


/**
 *
 * @author ForteScarlet
 */
class B64Tests {

    @Test
    fun encodeTest() {
        val str = "Hello, World. 你好, 世界"
        val strBytes = str.toByteArray(Charsets.UTF_8)

        // encode b64 via ISO-8859-1
        val encode1 = String(Base64.getEncoder().encode(strBytes), Charsets.ISO_8859_1)
        val encode2 = String(Base64.getEncoder().encode(strBytes), Charsets.UTF_8)

        assertEquals(encode1, encode2)

        val decode1 = Base64.getDecoder().decode(encode1.toByteArray(Charsets.ISO_8859_1))
        val decode2 = Base64.getDecoder().decode(encode2.toByteArray(Charsets.UTF_8))

        assertContentEquals(decode1, decode2)
    }

}
