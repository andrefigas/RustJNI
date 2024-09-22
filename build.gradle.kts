plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "com.rustjni.plugin"
version = "1.0.0"

gradlePlugin {
    plugins {
        create("com.rustjni.plugin") {
            id = "com.rustjni.plugin"
            implementationClass = "com.rustjni.plugin.RustJNI"
        }
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:7.0.4")
}
