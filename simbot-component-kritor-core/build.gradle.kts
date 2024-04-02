plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

useK2()
configJavaCompileWithModule("simbot.component.kritor.core")

kotlin {
    explicitApi()

    configKotlinJvm {
        withJava()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.simbot.api)
        }

        commonTest.dependencies {
            api(libs.simbot.core)
        }

        jvmMain {
            dependencies {
                // compileOnly.
                compileOnly(libs.simbot.api)
                compileOnly(libs.simbot.common.annotations)
                api(project(":simbot-component-kritor-proto"))
            }
        }
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
