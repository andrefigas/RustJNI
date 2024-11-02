plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
    mavenCentral()
    google()
}

version = "0.0.19"
group = "io.github.andrefigas.rustjni"

gradlePlugin {
    website.set("https://github.com/andrefigas/RustJNI")
    vcsUrl.set("https://github.com/andrefigas/RustJNI")
    plugins {
        create("rustJniPlugin") {
            id = "io.github.andrefigas.rustjni"
            implementationClass = "io.github.andrefigas.rustjni.RustJNI"
            displayName = "Rust JNI Gradle Plugin"
            description = "A Gradle plugin that simplifies the creation and compilation of Rust code integrated with Android applications via JNI."
            tags.set(listOf("rust", "jni", "android", "gradle-plugin", "native-code", "android-development", "rust-jni"))
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(localGroovy())
    compileOnly("com.android.tools.build:gradle:8.1.1")
    implementation("org.tomlj:tomlj:1.1.1")
}
