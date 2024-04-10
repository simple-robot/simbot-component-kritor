
import love.forte.gradle.common.core.property.systemProp


object CI {
    const val IS_CI = "IS_CI"
}

val isCi = systemProp(CI.IS_CI).toBoolean()

val isLinux: Boolean = systemProp("os.name")?.contains("linux", true) ?: false
