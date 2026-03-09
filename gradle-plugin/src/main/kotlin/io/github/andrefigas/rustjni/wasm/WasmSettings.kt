package io.github.andrefigas.rustjni.wasm

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import java.io.File

object WasmSettings {

    /**
     * Configures Android assets source set to include the WASM output directory.
     */
    fun configureAndroidAssets(project: Project, extension: RustWasmExtension) {
        val android = project.extensions.findByType(BaseExtension::class.java) ?: return
        val assetsDir = getAssetsDir(project, extension)
        assetsDir.mkdirs()

        android.sourceSets.getByName("main").assets.srcDirs(assetsDir)
    }

    /**
     * Adds Chicory dependencies for standalone mode.
     */
    fun addChicoryDependencies(project: Project, extension: RustWasmExtension) {
        if (extension.mode != WasmMode.STANDALONE) return

        val chicoryVersion = extension.chicoryVersion
        project.dependencies.add("implementation", "com.dylibso.chicory:runtime:$chicoryVersion")
        project.dependencies.add("implementation", "com.dylibso.chicory:wasm:$chicoryVersion")
    }

    /**
     * Adds WebView dependencies for browser mode.
     */
    fun addWebViewDependencies(project: Project, extension: RustWasmExtension) {
        if (extension.mode != WasmMode.BROWSER) return

        project.dependencies.add("implementation", "androidx.webkit:webkit:1.8.0")
    }

    fun getAssetsDir(project: Project, extension: RustWasmExtension): File {
        return File(project.projectDir, "src${File.separator}main${File.separator}assets")
    }
}
