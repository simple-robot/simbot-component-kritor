@file:Suppress("unused")

import love.forte.gradle.common.core.project.ProjectDetail
import love.forte.gradle.common.core.project.Version
import love.forte.gradle.common.core.project.minus
import love.forte.gradle.common.core.project.version
import love.forte.gradle.common.core.property.systemProp

inline fun isSnapshot(b: () -> Unit = {}): Boolean {
    b()
    val snapProp = System.getProperty("isSnapshot")?.toBoolean() ?: false
    val snapEnv = System.getenv(Env.IS_SNAPSHOT)?.toBoolean() ?: false

    logger.info("IsSnapshot from system.property: {}", snapProp)
    logger.info("IsSnapshot from system.env:      {}", snapEnv)

    return snapProp || snapEnv
}


/**
 * Project versions.
 */
@Suppress("MemberVisibilityCanBePrivate")
object P : ProjectDetail() {
    const val GROUP = "love.forte.simbot.component"

    override val group: String
        get() = GROUP

    const val DESCRIPTION = "一个对Kritor协议作为客户端进行实现的 Simple Robot 组件库，Java 友好、异步高效~"
    const val HOMEPAGE = "https://github.com/simple-robot/simbot-component-kritor"

    override val homepage: String get() = HOMEPAGE

    override val version: Version
    val versionWithoutSnapshot: Version

    init {
        val mainVersion = version(0, 0, 1) - version("dev1")

        fun initVersionWithoutSnapshot(status: Version?): Version = if (status == null) {
            mainVersion
        } else {
            mainVersion - status.copy()
        }

        versionWithoutSnapshot = initVersionWithoutSnapshot(null)

        version = if (isSnapshot()) {
            versionWithoutSnapshot - Version.SNAPSHOT
        } else {
            versionWithoutSnapshot
        }

        logger.info(
            "version={}, versionWithoutSnapshot={}",
            version,
            versionWithoutSnapshot
        )
    }


    override val description: String get() = DESCRIPTION
    override val developers: List<Developer> = developers {
        developer {
            id = "forte"
            name = "ForteScarlet"
            email = "ForteScarlet@163.com"
            url = "https://github.com/ForteScarlet"
        }
        developer {
            id = "forliy"
            name = "ForliyScarlet"
            email = "ForliyScarlet@163.com"
            url = "https://github.com/ForliyScarlet"
        }
    }
    override val licenses: List<License> = licenses {
        license {
            name = "GNU GENERAL PUBLIC LICENSE, Version 3"
            url = "https://www.gnu.org/licenses/gpl-3.0-standalone.html"
        }
        license {
            name = "GNU LESSER GENERAL PUBLIC LICENSE, Version 3"
            url = "https://www.gnu.org/licenses/lgpl-3.0-standalone.html"
        }
    }
    override val scm: Scm = scm {
        url = HOMEPAGE
        connection = "scm:git:$HOMEPAGE.git"
        developerConnection = "scm:git:ssh://git@github.com/simple-robot/simbot-component-kritor.git"
    }


}

fun isSimbotLocal(): Boolean = systemProp("SIMBOT_LOCAL").toBoolean()
