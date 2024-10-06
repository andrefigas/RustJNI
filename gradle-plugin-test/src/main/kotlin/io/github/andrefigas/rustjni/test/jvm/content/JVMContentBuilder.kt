package io.github.andrefigas.rustjni.test.jvm.content

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

internal class JVMContentBuilder(
    private val rustFile: File,
    private val jniHost: File,
    val provider: JVMContentProvider,
    private val project: Project,
    private val task: Task
) {

    private val logger = project.logger

    private fun clean() {
        if(rustFile.exists()){
            rustFile.deleteRecursively()
        }

        jniHost.writeText(
            provider.restoreJVMContent.trimIndent()
        )
    }

    fun apply(data: String) {
        clean()
        logger.lifecycle("🦀 Starting jvm-test-cases")
        jniHost.writeText(data)

        project.tasks.getByName("rust-jni-compile").actions.forEach { action ->
            action.execute(task)
        }

        logger.lifecycle("🦀 jvm-test-cases finished successfully ✅")
        clean()
    }

}