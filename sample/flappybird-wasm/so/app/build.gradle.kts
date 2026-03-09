plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.andrefigas.rustjni")
}

android {
    namespace = "com.example.flappybirdnative"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.flappybirdnative"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

rustJni {
    jniHost = "com.example.flappybirdnative.MainActivity"
    jniMethodsVisibility = io.github.andrefigas.rustjni.reflection.Visibility.PUBLIC
    rustPath = "./so/app/src/main/rust"
    ndkVersion = "27.1.12297006"
    exportFunctions = true
    architectures {
        armv7_linux_androideabi("armv7a-linux-androideabi21-clang")
        aarch64_linux_android("aarch64-linux-android21-clang")
        i686_linux_android("i686-linux-android21-clang")
        x86_64_linux_android("x86_64-linux-android21-clang")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
