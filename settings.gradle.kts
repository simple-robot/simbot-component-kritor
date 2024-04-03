plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "simbot-component-kritor"

include("internal-processors:include-component-message-elements-processor")
// include(":proto")
include(":simbot-component-kritor-proto")
include(":simbot-component-kritor-core")
