package io.github.andrefigas.rustjni

import io.github.andrefigas.rustjni.reflection.ReflectionJVM
import io.github.andrefigas.rustjni.reflection.ReflectionNative
import io.github.andrefigas.rustjni.utils.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.tomlj.Toml
import java.io.File
import java.util.Properties

class RustJNI : Plugin<Project> {

    private companion object {
        const val RUST_JNI_COMPILE = "rust-jni-compile"
    }

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

    private class Helper(
        private val project: Project,
        private val extension: RustJniExtension,
    ) {

        /** The directory where the rust project lives. See [RustJniExtension.rustPath]. */
        val rustDir: File by lazy { FileUtils.getRustDir(project, extension) }


        fun configureAndroidSettings() {
            AndroidSettings.configureAndroidSourceSets(project, extension)
            AndroidSettings.configureNdkAbiFilters(project, extension)
        }

        private fun initializeRustProject() {
            if (rustDir.exists()) {
                println("The directory '$rustDir' already exists. Skipping initialization.")
                return
            }

            createNewRustProject()
            configureRustEnvironment()
        }

        private fun createNewRustProject() {
            project.exec {
                workingDir = rustDir.parentFile
                commandLine = listOf("cargo", "new", "--lib", "rust")
            }
        }

        private fun configureRustEnvironment() {
            configCargo()
            configRustLibFile()
            generateConfigToml()
            ReflectionJVM.update(project, extension)
        }

        fun registerCompileTask() {
            project.tasks.register(RUST_JNI_COMPILE) {
                group = "build"
                description = "Compiles Rust code for specified architectures"

                doFirst {
                    if (!rustDir.exists()) {
                        println("Rust directory  $rustDir not found. Initializing Rust project...")
                        initializeRustProject()
                    } else {
                        println("Rust directory found at: $rustDir")
                    }
                }

                doLast {
                    generateConfigToml()
                    ReflectionNative.update(project, extension)
                    compileRustCode()
                    copyCompiledLibraries()
                    ReflectionJVM.update(project, extension)
                }
            }
        }

        private fun compileRustCode() {
            validateArchitectures()
            addRustTargets()
            cleanBuildDirectory()
            buildRustForArchitectures()
        }

        private fun validateArchitectures() {
            if (extension.architecturesList.isEmpty()) {
                throw org.gradle.api.GradleException("No architectures specified in rustJni extension")
            }
        }

        private fun addRustTargets() {
            extension.architecturesList.forEach { archConfig ->
                project.exec {
                    workingDir = rustDir
                    commandLine = listOf("rustup", "--verbose", "target", "add", archConfig.target)
                }
            }
        }

        private fun cleanBuildDirectory() {
            val buildDir = File(project.buildDir, "rust")
            if (buildDir.exists()) {
                buildDir.deleteRecursively()
            }
        }

        private fun buildRustForArchitectures() {
            extension.architecturesList.forEach { archConfig ->
                project.exec {
                    workingDir = rustDir
                    commandLine = listOf("cargo", "build", "--target", archConfig.target, "--release", "--verbose")
                }
            }
        }

        private fun copyCompiledLibraries() {
            val libName = FileUtils.getLibName(project, extension)
            val architectures = extension.architecturesList

            architectures.forEach { archConfig ->
                val outputDir = createOutputDirForArchitecture(archConfig)
                val fileExtension = getFileExtensionForTarget(archConfig.target)
                val sourceLib = File(
                    rustDir,
                    "target${File.separator}${archConfig.target}${File.separator}release${File.separator}lib${libName}$fileExtension"
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

            val outputDir = File(project.buildDir, "rust${File.separator}$outputDirName")
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

        /** Configures the `Cargo.toml` file created by [createNewRustProject]. */
        private fun configCargo() {
            val configToml = File(rustDir, "Cargo.toml")
            val libName = extension.libName.ifEmpty {
                RustJniExtension.DEFAULT_LIB_NAME
            }
            configToml.writeText(
                """
                [package]
                name = "$libName"
                version = "${extension.libVersion}"
                edition = "2021"
    
                [lib]
                crate-type = ["cdylib"]
                
                [dependencies]
                jni = "0.21"
            """.trimIndent())
            println("Cargo.toml updated")
        }

        /** Configures the `lib.rs` (entry point) file created by [createNewRustProject]. */
        private fun configRustLibFile() {
            val libFile = FileUtils.getRustSrcFile(rustDir)
            println("creating $libFile")

            if (ReflectionJVM.isRustJniBlockPresent(project, extension)) {
                libFile.writeText("""
                    use jni::JNIEnv;
                    use jni::objects::JClass;
                    //<RustJNI>
                    // primitive imports
                    use jni::sys::{};
                    //</RustJNI>
                """.trimIndent())
                ReflectionNative.update(project, extension)
            } else {
                libFile.writeText(buildRustJNIContent())
            }

            println("Updated '${libFile.name}' with the new content.")
        }

        private fun buildRustJNIContent(): String {
            val javaClassPath = extension.jniHost.replace('.', '_')
            return """
                use jni::JNIEnv;
                use jni::objects::JClass;
                //<RustJNI>
                // primitive imports
                use jni::sys::{jstring};
                //</RustJNI>
    
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
                Do your rust implementation there: /rust/src/lib.rs
                ---------------------------------------------------------"#;
    
                    env.new_string(output)
                        .expect("Couldn't create Java string!")
                        .into_raw()
                }
            """.trimIndent()
        }

        /** Creates and configures the `config.toml` for the Rust project created by [createNewRustProject].
         *
         * This file tells cargo where to find the *compiler* and *linker* for different architectures when compiling for Android. */
        private fun generateConfigToml() {
            val configToml = File(rustDir, ".cargo${File.separator}config.toml")
            if (configToml.exists()) {
                configToml.delete()
            }
            val prebuiltPath = getPrebuiltPath()
            configToml.parentFile.mkdirs()
            configToml.writeText(buildConfigTomlContent(prebuiltPath))
        }

        private fun buildConfigTomlContent(prebuiltPath: String): String {
            val architectures = extension.architecturesList
            if (architectures.isEmpty()) {
                throw org.gradle.api.GradleException("No architectures specified in rustJni extension")
            }

            val prebuiltPath = OSHelper.doubleSeparatorIfNeeded(prebuiltPath)

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

        private fun getPrebuiltPath(): String {
            val props = Properties()
            project.file("${project.rootProject.projectDir}${File.separator}local.properties")
                .inputStream().use { props.load(it) }

            var ndkDir = props.getProperty("ndk.dir")

            // If ndk.dir is not present, try using sdk.dir and adding "/ndk"
            if (ndkDir == null) {
                val sdkDir = props.getProperty("sdk.dir")
                    ?: throw org.gradle.api.GradleException("Neither ndk.dir not defined in local.properties")

                ndkDir = "$sdkDir${File.separator}ndk"

                // Check if the NDK directory exists inside the SDK
                if (!File(ndkDir).exists()) {
                    throw org.gradle.api.GradleException("ndk.dir not defined and no NDK directory found at: $ndkDir")
                }
            }

            if (!ndkDir.endsWith(File.separator)) {
                ndkDir += File.separator
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
            }.let { prebuilt -> "$ndkDir${File.separator}toolchains${File.separator}llvm${File.separator}prebuilt${File.separator}$prebuilt${File.separator}bin${File.separator}" }
        }

    }

}

