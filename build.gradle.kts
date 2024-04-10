import io.gitlab.arturbosch.detekt.Detekt

plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    alias(libs.plugins.detekt)
}

group = "love.forte.simbot.component"
version = "0.0.1"

allprojects {
    group = "love.forte.simbot.component"
    version = "0.0.1"
    repositories {
        mavenCentral()
    }
}


dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
}

// config detekt
detekt {
    source.setFrom(subprojects.map { it.projectDir.absoluteFile })
    config.setFrom(rootDir.resolve("config/detekt/detekt.yml"))
    baseline = rootDir.resolve("config/detekt/baseline.xml")
    // buildUponDefaultConfig = true
    parallel = true
    reportsDir = rootProject.layout.buildDirectory.dir("reports/detekt").get().asFile
    if (!isCi) {
        autoCorrect = true
    }
    basePath = projectDir.absolutePath
}

// https://detekt.dev/blog/2019/03/03/configure-detekt-on-root-project/
tasks.withType<Detekt>().configureEach {
    // internal 处理器不管
    exclude("internal-processors/**")
    exclude("simbot-component-kritor-proto/**")

    include("**/src/*Main/kotlin/**/*.kt")
    include("**/src/*Main/kotlin/**/*.java")
    include("**/src/*Main/java/**/*.kt")
    include("**/src/*Main/java/**/*.java")
    include("**/src/main/kotlin/**/*.kt")
    include("**/src/main/kotlin/**/*.java")
    include("**/src/main/java/**/*.kt")
    include("**/src/main/java/**/*.java")

    exclude("**/src/*/resources/")
    exclude("**/build/")
    exclude("**/*Test/kotlin/")
    exclude("**/*Test/java/")
    exclude("**/test/kotlin/")
    exclude("**/test/java/")
}
