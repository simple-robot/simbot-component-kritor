plugins {
    `java-library`
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

configJavaCompileWithModule(null, jvmVersion = "8")

dependencies {
    protobuf(files("proto/"))

    api(libs.kotlinx.coroutines.core)
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
    api(libs.protobuf.java.util)
    api(libs.protobuf.kotlin)
    api(libs.grpc.kotlin.stub)
}

kotlin {
    explicitApi()
    configKotlinJvm(jdkVersion = 8)
}


protobuf {
    protoc {
        artifact = libs.protoc.asProvider().get().toString()
    }
    plugins {
        create("grpc") {
            artifact = libs.protoc.gen.grpc.java.get().toString()
        }
        create("grpckt") {
            artifact = libs.protoc.gen.grpc.kotlin.get().toString() + ":jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                create("grpc")
                create("grpckt")
            }
            it.builtins {
                create("kotlin")
            }
        }
    }
}
