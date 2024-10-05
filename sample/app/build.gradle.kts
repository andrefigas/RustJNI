plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("io.github.andrefigas.rustjni") version "0.0.16"
}

rustJni{
    rustPath = "./app/src/main/rust"
    jniHost = "com.devfigas.rustjni.sample.MainActivity"
    ndkVersion = "27.1.12297006"
    architectures {
        armv7_linux_androideabi("armv7a-linux-androideabi21-clang")
        aarch64_linux_android("aarch64-linux-android21-clang")
        i686_linux_android("i686-linux-android21-clang")
        x86_64_linux_android("x86_64-linux-android21-clang")
    }
}

android {
    namespace = "com.devfigas.rustjni.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.devfigas.rustjni.sample"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}