package love.forte.simbot.component.kritor.core

import io.mockk.mockk
import kotlinx.serialization.ExperimentalSerializationApi
import love.forte.simbot.bot.SerializableBotConfiguration
import love.forte.simbot.component.ComponentConfigureContext
import love.forte.simbot.component.kritor.core.bot.SerializableKritorBotConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue


/**
 *
 * @author ForteScarlet
 */
class KritorComponentTests {

    @Test
    fun componentIDTest() {
        assertEquals(KritorComponent.ID_VALUE, KritorComponent().id)
        assertSame(KritorComponent.key, KritorComponent.key)
    }

    /**
     * 验证create内的DSL是会被执行的。
     */
    @Test
    fun componentFactoryTest() {
        var i = false
        val context = mockk<ComponentConfigureContext>()

        KritorComponent.create(context) {
            i = true
        }

        assertTrue(i)
    }

    /**
     * 验证可序列化bot配置类已经被注册
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun componentSerializableKritorBotConfigurationSerializersModuleTest() {
        val serializer = KritorComponent().serializersModule
            .getPolymorphic(SerializableBotConfiguration::class, SerializableKritorBotConfiguration.serializer().descriptor.serialName)

        assertEquals(SerializableKritorBotConfiguration.serializer(), serializer)
    }

}
