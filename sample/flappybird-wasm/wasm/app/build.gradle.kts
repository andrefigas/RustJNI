import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// ─── wasm-pack build task ────────────────────────────────────────────────────
val wasmPackVersion = "0.13.1"
val wasmBindgenCliVersion = "0.2.113"
val wasmGameDir = file("${rootProject.projectDir}/wasm/game")
val wasmPkgDest = file("${projectDir}/src/main/assets/www/pkg")

tasks.register("wasm-pack-build") {
    group = "build"
    description = "Compiles Rust WASM game with wasm-pack"
    inputs.dir(file("${wasmGameDir}/src"))
    inputs.file(file("${wasmGameDir}/Cargo.toml"))
    outputs.dir(wasmPkgDest)

    doLast {
        // Find wasm-pack executable
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val userHome = System.getProperty("user.home")
        val props = Properties()
        val localPropsFile = file("${rootProject.projectDir}/local.properties")
        if (localPropsFile.exists()) localPropsFile.inputStream().use { props.load(it) }
        var cargoDir = props.getProperty("cargo.dir") ?: ""
        if (cargoDir.isEmpty()) cargoDir = "$userHome${File.separator}.cargo${File.separator}bin"
        if (!cargoDir.endsWith(File.separator)) cargoDir = cargoDir + File.separator

        // Ensure wasm32-unknown-unknown target is installed
        val execExt = if (isWindows) ".exe" else ""
        val rustupPath = "${cargoDir}rustup${execExt}"
        try {
            exec {
                commandLine = listOf(rustupPath, "target", "add", "wasm32-unknown-unknown")
                isIgnoreExitValue = true
            }
        } catch (_: Exception) {}

        // Find or install wasm-pack
        val cargoPath = "${cargoDir}cargo${execExt}"
        val wasmPackPath = "${cargoDir}wasm-pack${execExt}"
        val wasmPackFile = File(wasmPackPath)
        if (!wasmPackFile.exists()) {
            println("wasm-pack not found at $wasmPackPath, installing via cargo...")
            exec {
                commandLine = listOf(cargoPath, "install", "wasm-pack", "--version", wasmPackVersion, "--locked")
                isIgnoreExitValue = false
            }
        }
        val cmd = if (wasmPackFile.exists()) listOf(wasmPackFile.absolutePath) else listOf("wasm-pack")

        // Pre-install wasm-bindgen-cli with --locked to avoid build failures on older Rust
        println("Ensuring wasm-bindgen-cli $wasmBindgenCliVersion is installed...")
        exec {
            commandLine = listOf(cargoPath, "install", "wasm-bindgen-cli", "--version", wasmBindgenCliVersion, "--locked")
            isIgnoreExitValue = true
        }

        println("Running wasm-pack build in $wasmGameDir ...")
        exec {
            workingDir = wasmGameDir
            commandLine = cmd + listOf("build", "--target", "web", "--release")
        }

        // Copy pkg/ to assets
        val srcPkg = file("${wasmGameDir}/pkg")
        wasmPkgDest.mkdirs()
        srcPkg.listFiles()?.forEach { f ->
            f.copyTo(File(wasmPkgDest, f.name), overwrite = true)
        }
        println("Copied wasm-pack output to $wasmPkgDest")
    }
}

tasks.matching { it.name.startsWith("compile") || it.name.startsWith("merge") }.configureEach {
    dependsOn("wasm-pack-build")
}
// ─────────────────────────────────────────────────────────────────────────────

android {
    namespace = "com.example.flappybird"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.flappybird"
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

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
