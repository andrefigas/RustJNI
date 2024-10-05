package io.github.andrefigas.rustjni.test.jvm.content

import io.github.andrefigas.rustjni.test.jvm.JVMTestData
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

    val logger = project.logger

    private fun clean() {
        if(rustFile.exists()){
            rustFile.deleteRecursively()
        }

        jniHost.writeText(
            provider.restoreJVMContent.trimIndent()
        )
    }

    fun apply(vararg data: JVMTestData) {
        clean()
        logger.lifecycle("🦀 Starting to run ${data.size} jvm-test-cases")
        data.forEachIndexed { index, dataTest ->
            logger.lifecycle("🦀 Starting jvm-test-cases ${index + 1}/${data.size} - '${dataTest.testCase}'")
            jniHost.writeText(dataTest.content)
            project.tasks.getByName("rust-jni-compile").actions.forEach { action ->
                action.execute(task)
            }

            jniHost.writeText(
                provider.restoreJVMContent.trimIndent()
            )

            logger.lifecycle("🦀 jvm-test-cases ${index + 1} - '${dataTest.testCase}' finished successfully ✅")
        }

    }

}