plugins {
    `java-library`
}

group = "love.forte.simbot.component"
version = "0.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    sourceSets.getByName("main").resources.srcDir("src/main/proto")
}
