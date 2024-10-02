package io.github.andrefigas.rustjni

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.Properties

/** Path component separator. `/` on unix and `\` on windows. */
val SEP: String = File.separator

private const val RUST_JNI_COMPILE = "rust-jni-compile"

@Suppress("unused")
class RustJNI : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("rustJni", RustJniExtension::class.java)

        if (extension.applyAsCompileDependency) {
            project.tasks.matching { it.name.startsWith("compile") }.configureEach {
                this.dependsOn(RUST_JNI_COMPILE)
            }
        }

        val helper = Helper(project, extension)

        helper.registerCompileTask()
        helper.registerInitTask()
        helper.configureAndroidSettings()
    }
}

private class Helper(
    private val project: Project,
    private val extension: RustJniExtension,
) {
    fun registerCompileTask() {
        project.tasks.register(RUST_JNI_COMPILE) {
            group = "build"
            description = "Compiles Rust code for specified architectures"

            doFirst {
                if (!rustDir.exists()) {
                    println("Rust directory not found. Initializing Rust project...")
                    initializeRustProject()
                }
            }

            doLast {
                generateConfigToml()
                compileRustCode()
                copyCompiledLibraries()
                reconfigureNativeMethods()
            }
        }
    }

    fun registerInitTask() {
        project.tasks.register("rust-jni-init") {
            group = "setup"
            description = "Initializes the Rust project"

            doFirst {
                println("architectureList: ${extension.architecturesList}")
            }

            doLast {
                initializeRustProject()
            }
        }
    }

    fun configureAndroidSettings() {
        AndroidSettings.configureAndroidSourceSets(project, extension)
        AndroidSettings.configureNdkAbiFilters(project, extension)
    }

    /** The directory where the rust project lives. See [RustJniExtension.rustPath]. */
    private val rustDir =
        project.file("${project.rootProject.projectDir}${SEP}${extension.rustPath}")

    private fun initializeRustProject() {
        if (rustDir.exists()) {
            println("The directory 'rust' already exists. Skipping initialization.")
            return
        }
        createNewRustProject()
        configureRustEnvironment()
    }

    private fun createNewRustProject() {
        project.exec {
            workingDir = rustDir.parentFile
            commandLine = listOf("cargo", "new", "--lib", "rust", "--vcs", "none")
        }
    }

    private fun configureRustEnvironment() {
        configCargo()
        configRustLib()
        generateConfigToml()
        reconfigureNativeMethods()
    }

    private fun reconfigureNativeMethods() {
        Reflection.removeNativeMethodDeclaration(project, extension)
        Reflection.addNativeMethodDeclaration(project, extension)
    }

    private fun compileRustCode() {
        validateArchitectures()
        addRustTargets(extension.architecturesList)
        cleanBuildDirectory()
        buildRustForArchitectures(project, extension.architecturesList)
    }

    private fun validateArchitectures() {
        if (extension.architecturesList.isEmpty()) {
            throw org.gradle.api.GradleException("No architectures specified in rustJni extension")
        }
    }

    private fun addRustTargets(architectures: List<ArchitectureConfig>) {
        architectures.forEach { archConfig ->
            project.exec {
                workingDir = rustDir
                commandLine = listOf("rustup", "--verbose", "target", "add", archConfig.target)
            }
        }
    }

    private fun cleanBuildDirectory() {
        val buildDir = File(rustDir, "build")
        if (buildDir.exists()) {
            buildDir.deleteRecursively()
        }
    }

    private fun buildRustForArchitectures(
        project: Project,
        architectures: List<ArchitectureConfig>
    ) {
        architectures.forEach { archConfig ->
            project.exec {
                workingDir = rustDir
                commandLine = listOf(
                    "cargo",
                    "build",
                    "--target",
                    archConfig.target,
                    "--release",
                    "--verbose"
                )
            }
        }
    }

    private fun copyCompiledLibraries() {
        val libName = extension.libName
        val architectures = extension.architecturesList

        architectures.forEach { archConfig ->
            val outputDir = createOutputDirForArchitecture(archConfig)
            val fileExtension = getFileExtensionForTarget(archConfig.target)
            val sourceLib = File(
                rustDir,
                "target${SEP}${archConfig.target}${SEP}release${SEP}lib${libName}$fileExtension"
            )
            val destLib = File(outputDir, "lib${libName}$fileExtension")
            copyLibraryFile(sourceLib, destLib)
        }
    }

    private fun createOutputDirForArchitecture(archConfig: ArchitectureConfig): File {
        val outputDirName = when (archConfig.target) {
            AndroidTarget.ARMV7_LINUX_ANDROIDEABI -> "armeabi-v7a"
            AndroidTarget.AARCH64_LINUX_ANDROID -> "arm64-v8a"
            AndroidTarget.I686_LINUX_ANDROID -> "x86"
            AndroidTarget.X86_64_LINUX_ANDROID -> "x86_64"
            "aarch64-apple-ios" -> "ios-arm64"
            else -> archConfig.target.replace('-', '_')
        }
        val outputDir = File(rustDir, "build${SEP}$outputDirName")
        outputDir.mkdirs()
        return outputDir
    }

    private fun getFileExtensionForTarget(target: String) =
        if (target.contains("apple-ios")) ".dylib" else ".so"

    private fun copyLibraryFile(sourceLib: File, destLib: File) {
        if (sourceLib.exists()) {
            sourceLib.copyTo(destLib, overwrite = true)
        } else {
            throw org.gradle.api.GradleException("Compiled library not found: ${sourceLib.absolutePath}")
        }
    }

    private fun configCargo() {
        val configToml = File(rustDir, "Cargo.toml")
        configToml.writeText(buildCargoConfig())
        println("Cargo.toml updated")
    }

    private fun buildCargoConfig(): String {
        return """
            [package]
            name = "${extension.libName}"
            version = "${extension.libVersion}"
            edition = "2021"

            [lib]
            crate-type = ["cdylib"]
            path = "src/rust_jni.rs"

            [dependencies]
            jni = "0.19"
        """.trimIndent()
    }

    private fun configRustLib() {
        val rustSrcDir = File(rustDir, "src")
        deleteMainRsFile(rustSrcDir)
        createRustJNIFile(rustSrcDir, extension.jniHost)
    }

    private fun deleteMainRsFile(rustSrcDir: File) {
        val mainFile = File(rustSrcDir, "main.rs")
        if (mainFile.exists() && !mainFile.delete()) {
            throw org.gradle.api.GradleException("Failed to delete main.rs")
        } else {
            println("Deleted 'main.rs'")
        }
    }

    private fun createRustJNIFile(rustSrcDir: File, jniHost: String) {
        val libFile = File(rustSrcDir, "rust_jni.rs")
        libFile.writeText(buildRustJNIContent(jniHost))
        println("Updated 'rust_jni.rs' with the new content.")
    }

    private fun buildRustJNIContent(jniHost: String): String {
        val javaClassPath = jniHost.replace('.', '_')
        return """
            use jni::JNIEnv;
            use jni::objects::JClass;
            use jni::sys::{jstring};

            #[no_mangle]
            pub extern "C" fn Java_${javaClassPath}_sayHello(
                env: JNIEnv,
                _class: JClass,
            ) -> jstring {
                let output = r#"
            __________________________
            < Hello RustJNI >
            --------------------------
                    \\
                     \\
                        _~^~^~_
                    \\) /  o o  \\ (/
                      '_   -   _'
                      / '-----' \\
            _________________________________________________________
            Do your rust implementation there: /rust/src/rust_jni.rs
            ---------------------------------------------------------"#;

                env.new_string(output)
                    .expect("Couldn't create Java string!")
                    .into_inner()
            }
        """.trimIndent()
    }

    private fun generateConfigToml() {
        val configToml = File(rustDir, ".cargo${SEP}config.toml")
        if (configToml.exists()) {
            configToml.delete()
        }
        val prebuiltPath = getPrebuiltPath()
        configToml.parentFile.mkdirs()
        configToml.writeText(buildConfigTomlContent(prebuiltPath))
        println("config.toml generated at: ${configToml.absolutePath}")
    }

    private fun getPrebuiltPath(): String {
        val props = Properties()
        project.file("${project.rootProject.projectDir}${SEP}local.properties")
            .inputStream()
            .use { props.load(it) }

        var ndkDir = props.getProperty("ndk.dir")

        // If ndk.dir is not present, try using sdk.dir and adding "/ndk"
        if (ndkDir == null) {
            val sdkDir = props.getProperty("sdk.dir")
                ?: throw org.gradle.api.GradleException("Neither ndk.dir not defined in local.properties")

            ndkDir = "$sdkDir${SEP}ndk"

            // Check if the NDK directory exists inside the SDK
            if (!File(ndkDir).exists()) {
                throw org.gradle.api.GradleException("ndk.dir not defined and no NDK directory found at: $ndkDir")
            }
        }

        if (!ndkDir.endsWith(SEP)) {
            ndkDir += SEP
        }

        if (extension.ndkVersion.isEmpty()) {
            throw org.gradle.api.GradleException("define ndkVersion in rustJni extension")
        }

        ndkDir += extension.ndkVersion

        if (!File(ndkDir).exists()) {
            throw org.gradle.api.GradleException("NDK ${extension.ndkVersion} is not available in the path: $ndkDir")
        }

        val osName = System.getProperty("os.name").toLowerCase()
        val defaultPrebuilt = when {
            osName.contains("win") -> "windows-x86_64"
            osName.contains("mac") -> "darwin-x86_64"
            osName.contains("linux") -> "linux-x86_64"
            else -> throw org.gradle.api.GradleException("Unsupported operating system: $osName")
        }

        val localPrebuilt = props.getProperty("prebuilt")
        val extensionPrebuilt = extension.preBuilt

        return when {
            !localPrebuilt.isNullOrEmpty() -> {
                if (extensionPrebuilt.isNotEmpty()) {
                    println("Warning: 'prebuilt' specified in local.properties overrides the value in rustJni extension.")
                }
                localPrebuilt
            }

            extensionPrebuilt.isNotEmpty() -> extensionPrebuilt
            else -> {
                println("No 'prebuilt' specified. Using default for OS: $defaultPrebuilt")
                defaultPrebuilt
            }
        }.let { prebuilt -> "$ndkDir${SEP}toolchains${SEP}llvm${SEP}prebuilt${SEP}$prebuilt${SEP}bin${SEP}" }
    }

    private fun buildConfigTomlContent(prebuiltPath: String): String {
        @Suppress("NAME_SHADOWING")
        val prebuiltPath = OSHelper.doubleSeparatorIfNeeded(prebuiltPath)
        val architectures = extension.architecturesList
        if (architectures.isEmpty()) {
            throw org.gradle.api.GradleException("No architectures specified in rustJni extension")
        }

        return buildString {
            appendLine("#<RustJNI>")
            appendLine("#auto-generated code")
            appendLine()
            architectures.forEach { archConfig ->
                val linker = OSHelper.addLinkerExtensionIfNeeded(archConfig.linker)
                appendLine("[target.${archConfig.target}]")
                appendLine("""ar = "${prebuiltPath}${archConfig.ar}"""")
                appendLine("""linker = "${prebuiltPath}${linker}"""")
                appendLine()
            }
            appendLine("#</RustJNI>")
        }
    }
}

