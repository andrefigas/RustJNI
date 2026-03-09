plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.andrefigas.rustjni")
}

android {
    namespace = "com.example.flappybirdstandalone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.flappybirdstandalone"
        minSdk = 28
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
    applyAsCompileDependency = false
}

rustWasm {
    corePath = "./rust"
    wasmHost = "com.example.flappybirdstandalone.MainActivity"
    mode = io.github.andrefigas.rustjni.wasm.WasmMode.STANDALONE
    wasmBridgePath = "./standalone/wasm"
    chicoryVersion = "1.1.0"
    wasmFileName = "flappy.wasm"
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.dylibso.chicory:runtime:1.1.0")
}
