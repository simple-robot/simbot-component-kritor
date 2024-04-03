package love.forte.simbot.component.kritor.core.message

import kotlin.test.Test
import kotlin.test.assertEquals
import io.kritor.event.MusicPlatform as EMusicPlatform
import io.kritor.event.Scene as EScene
import io.kritor.message.MusicPlatform as MMusicPlatform
import io.kritor.message.Scene as MScene


/**
 *
 * @author ForteScarlet
 */
class MessageTests {

    @Test
    fun sceneResolveTest() {
        assertEquals(MScene.GROUP, EScene.GROUP.resolve())
        assertEquals(MScene.FRIEND, EScene.FRIEND.resolve())
        assertEquals(MScene.GUILD, EScene.GUILD.resolve())
        assertEquals(MScene.STRANGER_FROM_GROUP, EScene.STRANGER_FROM_GROUP.resolve())
        assertEquals(MScene.NEARBY, EScene.NEARBY.resolve())
        assertEquals(MScene.STRANGER, EScene.STRANGER.resolve())
        assertEquals(MScene.UNRECOGNIZED, EScene.UNRECOGNIZED.resolve())
    }

    @Test
    fun musicPlatformResolveTest() {
        assertEquals(MMusicPlatform.QQ, EMusicPlatform.QQ.resolve())
        assertEquals(MMusicPlatform.NetEase, EMusicPlatform.NetEase.resolve())
        assertEquals(MMusicPlatform.Custom, EMusicPlatform.Custom.resolve())
        assertEquals(MMusicPlatform.UNRECOGNIZED, EMusicPlatform.UNRECOGNIZED.resolve())
    }

}
