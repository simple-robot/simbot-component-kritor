plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
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

// repositories {
//     mavenCentral()
// }
//
// dependencies {
//     testImplementation("org.jetbrains.kotlin:kotlin-test")
// }
//
// tasks.test {
//     useJUnitPlatform()
// }
// kotlin {
//     jvmToolchain(21)
// }
