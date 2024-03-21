plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "simbot-component-kritor"

include(":proto")
include(":simbot-component-kritor-proto")
include(":simbot-component-kritor-core")
