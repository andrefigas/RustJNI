package io.github.andrefigas.rustjni.utils

import io.github.andrefigas.rustjni.RustJniExtension
import org.gradle.api.Project
import org.tomlj.Toml
import java.io.File

internal object FileUtils {

    /** This method retrieves the source directories of the project */
    fun getSourceDirs(project: Project): Set<File> {
        return when (val androidExtension = project.extensions.findByName("android")) {
            is com.android.build.gradle.AppExtension -> androidExtension.sourceSets.getByName("main").java.srcDirs
            is com.android.build.gradle.LibraryExtension -> androidExtension.sourceSets.getByName("main").java.srcDirs
            else -> throw org.gradle.api.GradleException("Android extension not found in project")
        }
    }

    /** This method finds a class file within the source directories */
    fun findClassFile(project: Project, packagePath: String, className: String): File {
        val sourceDirs = getSourceDirs(project)
        val possibleExtensions = listOf("kt", "java")

        return sourceDirs
            .flatMap { srcDir ->
                possibleExtensions.map { ext ->
                    File(srcDir, "$packagePath${File.separator}$className.$ext")
                }
            }
            .firstOrNull { it.exists() }
            ?: throw org.gradle.api.GradleException("Class file not found for jniHost: $packagePath.$className")
    }

    /** Reads the content of a given file */
    fun readFileContent(file: File): String {
        return file.readText()
    }

    /** Checks if a given file has a specific extension */
    fun isKotlinFile(file: File): Boolean {
        return file.extension.equals("kt", ignoreCase = true)
    }

    /** Extract class name from jniHost */
    fun extractClassName(jniHost: String): String {
        return jniHost.substringAfterLast('.')
    }

    /** Extract package path from jniHost */
    fun extractPackagePath(jniHost: String): String {
        return jniHost.substringBeforeLast('.').replace('.', File.separatorChar)
    }

    /** Gets the directory where the rust project lives. See [RustJniExtension.rustPath]. */
    fun getRustDir(project: Project, extension: RustJniExtension): File =
        project.file("${project.rootProject.projectDir}${File.separator}${extension.rustPath}")

    /** Gets the file that is used as the Rust library's main file,
     * i.e. where all the Java-to-Rust functions are defined. */
    fun getRustSrcFile(rustDir: File): File {
        // Get file's path from Cargo.toml
        val toml = try {
            Toml.parse(File(rustDir, "Cargo.toml").toPath())
        } catch (e: Exception) {
            null
        }
        val path = toml?.getString("lib.path")
            // Use the default if not specified
            ?: "./src/lib.rs"
        return File(rustDir, path)
    }
}
