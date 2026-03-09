package io.github.andrefigas.rustjni.wasm

import io.github.andrefigas.rustjni.wasm.model.ResolvedFunction
import io.github.andrefigas.rustjni.wasm.reflection.BrowserBridgeGenerator
import io.github.andrefigas.rustjni.wasm.reflection.RustCoreParser
import io.github.andrefigas.rustjni.wasm.reflection.StandaloneBridgeGenerator
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.util.Properties

/**
 * Orchestrator for WASM bridge generation and compilation.
 * Handles the full pipeline: parse core → merge DSL → generate code → build WASM → copy assets.
 */
class RustWasm(
    private val project: Project,
    private val extension: RustWasmExtension
) {

    companion object {
        const val TASK_NAME = "rust-wasm-compile"
    }

    private val coreDir: File by lazy {
        val path = extension.corePath
        val dir = if (File(path).isAbsolute) File(path)
        else File(project.rootProject.projectDir, path)
        if (!dir.exists()) throw GradleException("Core Rust directory not found: $dir")
        dir
    }

    private val bridgeDir: File by lazy {
        val path = extension.wasmBridgePath
        val dir = if (File(path).isAbsolute) File(path)
        else File(project.rootProject.projectDir, path)
        dir
    }

    private val coreCrateName: String by lazy {
        readCrateName(coreDir)
    }

    private val bridgeCrateName: String by lazy {
        "$coreCrateName-wasm-bridge"
    }

    /** The actual filename produced by cargo build (derived from crate name). */
    private val compiledWasmFileName: String by lazy {
        "${bridgeCrateName.replace("-", "_")}.wasm"
    }

    /** The filename used in assets (user-configured or derived). */
    private val wasmFileName: String by lazy {
        val configured = extension.wasmFileName
        if (configured.isNotEmpty()) configured
        else compiledWasmFileName
    }

    // =========================================================================
    // Main pipeline
    // =========================================================================

    fun execute() {
        println("RustWasm: Starting WASM bridge generation (mode=${extension.mode})")

        // 1. Parse core
        val coreLibRs = File(coreDir, "src${File.separator}lib.rs")
        if (!coreLibRs.exists()) throw GradleException("Core lib.rs not found: $coreLibRs")
        val coreSource = coreLibRs.readText()
        val coreFunctions = RustCoreParser.parse(coreSource)
        println("RustWasm: Parsed ${coreFunctions.size} functions from core")

        // 2. Resolve (merge with DSL config)
        val resolvedFunctions = RustCoreParser.resolve(coreFunctions, extension.functionConfigs)
        println("RustWasm: Resolved ${resolvedFunctions.size} functions")

        // 3. Generate code based on mode
        when (extension.mode) {
            WasmMode.STANDALONE -> generateStandalone(resolvedFunctions)
            WasmMode.BROWSER -> generateBrowser(resolvedFunctions)
        }

        // 4. Build WASM
        when (extension.mode) {
            WasmMode.STANDALONE -> buildStandaloneWasm()
            WasmMode.BROWSER -> buildBrowserWasm()
        }

        // 5. Copy to assets
        copyWasmToAssets()

        println("RustWasm: Done!")
    }

    // =========================================================================
    // Standalone generation
    // =========================================================================

    private fun generateStandalone(functions: List<ResolvedFunction>) {
        val coreRelativePath = coreDir.relativeTo(bridgeDir).path.replace("\\", "/")

        // Generate Rust bridge crate
        bridgeDir.mkdirs()
        File(bridgeDir, "src").mkdirs()

        val cargoToml = StandaloneBridgeGenerator.generateCargoToml(
            bridgeCrateName, coreCrateName, coreRelativePath
        )
        File(bridgeDir, "Cargo.toml").writeText(cargoToml)
        println("RustWasm: Generated ${bridgeDir}/Cargo.toml")

        val rustLib = StandaloneBridgeGenerator.generateRustLib(coreCrateName, functions)
        File(bridgeDir, "src${File.separator}lib.rs").writeText(rustLib)
        println("RustWasm: Generated ${bridgeDir}/src/lib.rs")

        // Generate Kotlin bridge
        val (packageName, className) = parseFullyQualifiedName(extension.wasmHost)
        val moduleName = bridgeCrateName

        val kotlinFile = findKotlinFile(extension.wasmHost)

        if (kotlinFile != null && kotlinFile.exists()) {
            // File exists → check if it has meaningful content outside markers
            val content = kotlinFile.readText()
            val contentWithoutBlock = StandaloneBridgeGenerator.removeRustWasmBlock(content)

            if (contentWithoutBlock.isBlank() || !containsClassDeclaration(contentWithoutBlock, className)) {
                // Entire file was generated (or class only exists inside markers) → recreate
                val kotlinSource = StandaloneBridgeGenerator.generateKotlinWasmLib(
                    packageName, className, wasmFileName, moduleName, functions
                )
                kotlinFile.writeText(kotlinSource)
                println("RustWasm: Regenerated $kotlinFile")
            } else {
                // File has user code → inject between markers
                val injectionBlock = StandaloneBridgeGenerator.generateKotlinInjectionBlock(
                    wasmFileName, moduleName, functions
                )
                var updated = contentWithoutBlock
                // Remove old import markers and inject fresh ones
                updated = StandaloneBridgeGenerator.removeRustWasmImportsBlock(updated)
                val importsBlock = StandaloneBridgeGenerator.generateImportsBlock(updated)
                updated = StandaloneBridgeGenerator.injectImportsBlock(updated, importsBlock)
                updated = StandaloneBridgeGenerator.injectIntoKotlinFile(updated, className, injectionBlock)
                kotlinFile.writeText(updated)
                println("RustWasm: Injected WASM bridge into $kotlinFile")
            }
        } else {
            // File does not exist → create full file
            val kotlinSource = StandaloneBridgeGenerator.generateKotlinWasmLib(
                packageName, className, wasmFileName, moduleName, functions
            )
            val newFile = findOrCreateKotlinFile(packageName, className)
            newFile.writeText(kotlinSource)
            println("RustWasm: Created $newFile")
        }
    }

    // =========================================================================
    // Browser generation
    // =========================================================================

    private fun generateBrowser(functions: List<ResolvedFunction>) {
        val coreRelativePath = coreDir.relativeTo(bridgeDir).path.replace("\\", "/")

        // Generate Rust bridge crate
        bridgeDir.mkdirs()
        File(bridgeDir, "src").mkdirs()

        val cargoToml = BrowserBridgeGenerator.generateCargoToml(
            bridgeCrateName, coreCrateName, coreRelativePath
        )
        File(bridgeDir, "Cargo.toml").writeText(cargoToml)
        println("RustWasm: Generated ${bridgeDir}/Cargo.toml")

        val rustLib = BrowserBridgeGenerator.generateRustLib(coreCrateName, functions)
        File(bridgeDir, "src${File.separator}lib.rs").writeText(rustLib)
        println("RustWasm: Generated ${bridgeDir}/src/lib.rs")

        // Generate bridge.js and index.js into wasm-bridge/ subfolder
        val assetsDir = WasmSettings.getAssetsDir(project, extension)
        val wasmBridgeDir = File(assetsDir, "${extension.assetsPath}${File.separator}wasm-bridge")
        wasmBridgeDir.mkdirs()

        val bridgeJs = BrowserBridgeGenerator.generateBridgeJs(functions)
        File(wasmBridgeDir, "bridge.js").writeText(bridgeJs)
        println("RustWasm: Generated ${wasmBridgeDir}/bridge.js")

        val indexJs = BrowserBridgeGenerator.generateIndexJs(bridgeCrateName, functions)
        File(wasmBridgeDir, "index.js").writeText(indexJs)
        println("RustWasm: Generated ${wasmBridgeDir}/index.js")

        // Inject createWasmWebView() into target class
        if (extension.webViewHost.isNotEmpty()) {
            val (_, hostClassName) = parseFullyQualifiedName(extension.webViewHost)
            val webViewMethod = BrowserBridgeGenerator.generateWebViewMethod(
                hostClassName, extension.assetsPath, extension.htmlFileName, functions
            )

            val hostFile = findKotlinFile(extension.webViewHost)
            if (hostFile != null && hostFile.exists()) {
                var updated = hostFile.readText()
                // Remove old import markers and inject fresh ones
                updated = BrowserBridgeGenerator.removeRustWasmImportsBlock(updated)
                val importsBlock = BrowserBridgeGenerator.generateImportsBlock(updated)
                updated = BrowserBridgeGenerator.injectImportsBlock(updated, importsBlock)
                updated = BrowserBridgeGenerator.injectIntoKotlinFile(updated, webViewMethod)
                hostFile.writeText(updated)
                println("RustWasm: Injected createWasmWebView() into $hostFile")
            } else {
                println("RustWasm: WARNING - Could not find WebView host file for ${extension.webViewHost}")
            }
        }
    }

    // =========================================================================
    // WASM build
    // =========================================================================

    private fun buildStandaloneWasm() {
        println("RustWasm: Building standalone WASM...")
        ensureWasmTarget()
        runCargoCommand(
            listOf("build", "--target", "wasm32-unknown-unknown", "--release"),
            bridgeDir
        )
    }

    private fun ensureWasmTarget() {
        val cargoDir = getCargoDir()
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val execExt = if (isWindows) ".exe" else ""
        val rustupPath = "${cargoDir}rustup${execExt}"

        try {
            println("RustWasm: Ensuring wasm32-unknown-unknown target is installed...")
            val result = project.exec {
                commandLine = listOf(rustupPath, "target", "add", "wasm32-unknown-unknown")
                isIgnoreExitValue = true
            }
            if (result.exitValue != 0) {
                println("RustWasm: WARNING - rustup target add failed (exit ${result.exitValue}), trying cargo build anyway...")
            }
        } catch (e: Exception) {
            println("RustWasm: WARNING - Could not run rustup: ${e.message}, trying cargo build anyway...")
        }
    }

    private fun buildBrowserWasm() {
        println("RustWasm: Building browser WASM with wasm-pack...")
        runCommand(
            "wasm-pack",
            listOf("build", "--target", "web", "--release"),
            bridgeDir
        )
    }

    private fun copyWasmToAssets() {
        val assetsDir = WasmSettings.getAssetsDir(project, extension)
        assetsDir.mkdirs()

        when (extension.mode) {
            WasmMode.STANDALONE -> {
                val wasmFile = File(
                    bridgeDir,
                    "target${File.separator}wasm32-unknown-unknown${File.separator}release${File.separator}${compiledWasmFileName}"
                )
                if (!wasmFile.exists()) {
                    throw GradleException("Compiled WASM not found: $wasmFile")
                }
                val destFile = File(assetsDir, wasmFileName)
                wasmFile.copyTo(destFile, overwrite = true)
                println("RustWasm: Copied $wasmFile → $destFile")
            }

            WasmMode.BROWSER -> {
                val pkgDir = File(bridgeDir, "pkg")
                if (!pkgDir.exists()) {
                    throw GradleException("wasm-pack output not found: $pkgDir")
                }
                val destPkgDir = File(assetsDir, "${extension.assetsPath}${File.separator}wasm-bridge${File.separator}pkg")
                destPkgDir.mkdirs()
                pkgDir.listFiles()?.forEach { file ->
                    file.copyTo(File(destPkgDir, file.name), overwrite = true)
                }
                println("RustWasm: Copied pkg/ → $destPkgDir")
            }
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private fun containsClassDeclaration(content: String, className: String): Boolean {
        val classPattern = Regex("(class|object|public\\s+class|final\\s+class|open\\s+class)\\s+$className\\b")
        return classPattern.containsMatchIn(content)
    }

    private fun readCrateName(crateDir: File): String {
        val cargoToml = File(crateDir, "Cargo.toml")
        if (!cargoToml.exists()) throw GradleException("Cargo.toml not found in $crateDir")

        val nameRegex = Regex("""name\s*=\s*"([^"]+)"""")
        for (line in cargoToml.readLines()) {
            val match = nameRegex.find(line)
            if (match != null) return match.groupValues[1]
        }

        throw GradleException("Could not find package name in $cargoToml")
    }

    private fun parseFullyQualifiedName(fqn: String): Pair<String, String> {
        val lastDot = fqn.lastIndexOf('.')
        return if (lastDot >= 0) {
            Pair(fqn.substring(0, lastDot), fqn.substring(lastDot + 1))
        } else {
            Pair("", fqn)
        }
    }

    private fun findOrCreateKotlinFile(packageName: String, className: String): File {
        val packagePath = packageName.replace('.', File.separatorChar)
        val android = project.extensions.findByType(com.android.build.gradle.BaseExtension::class.java)
        val srcDir = android?.sourceSets?.getByName("main")?.java?.srcDirs?.firstOrNull()
            ?: File(project.projectDir, "src${File.separator}main${File.separator}java")
        val dir = File(srcDir, packagePath)
        dir.mkdirs()
        return File(dir, "$className.kt")
    }

    private fun findKotlinFile(fqn: String): File? {
        val (packageName, className) = parseFullyQualifiedName(fqn)
        val packagePath = packageName.replace('.', File.separatorChar)
        val android = project.extensions.findByType(com.android.build.gradle.BaseExtension::class.java)
        val srcDirs = android?.sourceSets?.getByName("main")?.java?.srcDirs ?: return null

        for (srcDir in srcDirs) {
            val file = File(srcDir, "$packagePath${File.separator}$className.kt")
            if (file.exists()) return file
            val javaFile = File(srcDir, "$packagePath${File.separator}$className.java")
            if (javaFile.exists()) return javaFile
        }
        return null
    }

    private fun getCargoDir(): String {
        val props = Properties()
        val localPropsFile = project.file("${project.rootProject.projectDir}${File.separator}local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { props.load(it) }
        }

        var cargoDir = props.getProperty("cargo.dir")
        if (cargoDir.isNullOrEmpty()) {
            val userHome = System.getProperty("user.home")
            cargoDir = "$userHome${File.separator}.cargo${File.separator}bin"
        }

        if (!cargoDir.endsWith(File.separator)) {
            cargoDir += File.separator
        }

        return cargoDir
    }

    private fun runCargoCommand(arguments: List<String>, dir: File) {
        runCommand("cargo", arguments, dir)
    }

    private fun runCommand(executable: String, arguments: List<String>, dir: File) {
        val isWindows = System.getProperty("os.name").lowercase().contains("win")
        val execExt = if (isWindows) ".exe" else ""
        val cargoDir = getCargoDir()

        val executablePath = "$cargoDir$executable$execExt"
        val executableFile = File(executablePath)

        // Fallback to PATH if not in cargo dir
        val fullCommand = if (executableFile.exists()) {
            listOf(executableFile.absolutePath) + arguments
        } else {
            listOf(executable) + arguments
        }

        try {
            println("RustWasm: Running ${fullCommand.joinToString(" ")} in $dir")

            val result = project.exec {
                workingDir = dir
                commandLine = fullCommand
                isIgnoreExitValue = true
                errorOutput = System.err
            }

            if (result.exitValue != 0) {
                throw GradleException("$executable command failed with exit code ${result.exitValue}")
            }
        } catch (e: IOException) {
            throw GradleException("IOException while executing $executable in $dir", e)
        }
    }

    // =========================================================================
    // Task registration
    // =========================================================================

    fun registerTask() {
        project.tasks.register(TASK_NAME) {
            group = "build"
            description = "Generates WASM bridge code and compiles WASM module"

            doLast {
                execute()
            }
        }

        // Make compile tasks depend on WASM compilation
        project.tasks.matching { it.name.startsWith("compile") }.configureEach {
            this.dependsOn(TASK_NAME)
        }
    }
}
