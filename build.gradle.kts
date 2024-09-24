plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
    mavenCentral()
    google()
}

version = "0.0.10"
group = "com.rustjni.plugin"

gradlePlugin {
    plugins {
        create("rustJniPlugin") {
            id = "com.rustjni.plugin"
            implementationClass = "com.rustjni.plugin.RustJNI"
            displayName = "Rust JNI Gradle Plugin"
            description = "A Gradle plugin that simplifies the creation and compilation of Rust code integrated with Android applications via JNI."
        }
    }
}

pluginBundle {
    website = "https://github.com/andrefigas/RustJNI"
    vcsUrl = "https://github.com/andrefigas/RustJNI"
    description = "A Gradle plugin that simplifies the creation and compilation of Rust code integrated with Android applications via JNI."
    tags = listOf("rust", "jni", "android", "gradle-plugin", "native-code", "android-development", "rust-jni")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:7.0.4")
}
