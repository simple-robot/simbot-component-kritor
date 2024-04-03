plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    alias(libs.plugins.ksp)
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

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.mockk)
            implementation(libs.log4j.api)
            implementation(libs.log4j.core)
            implementation(libs.log4j.slf4j2)
        }
    }
}

// KSP
dependencies {
    add("kspJvm", project(":internal-processors:include-component-message-elements-processor"))
}

// see https://github.com/google/ksp/issues/567#issuecomment-1510477456
// tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
//     if(name != "kspCommonMainKotlinMetadata") {
//         dependsOn("kspCommonMainKotlinMetadata")
//     }
// }
//
// kotlin.sourceSets.commonMain {
//     kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
// }

// tasks.withType<DokkaTaskPartial>().configureEach {
//     dokkaSourceSets.configureEach {
//         suppressGeneratedFiles.set(false)
//     }
// }

