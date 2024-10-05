package io.github.andrefigas.rustjni.test.jvm

import io.github.andrefigas.rustjni.test.RustJNITest
import io.github.andrefigas.rustjni.test.cases.JVMTestCases
import io.github.andrefigas.rustjni.test.jvm.content.JVMContentBuilder
import io.github.andrefigas.rustjni.test.jvm.content.JVMContentProvider
import io.github.andrefigas.rustjni.test.jvm.content.JavaContentProvider
import io.github.andrefigas.rustjni.test.jvm.content.KotlinContentProvider
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.org.junit.Test
import java.io.File

object JVMTestRunner {

    private const val JAVA = ".java"
    private const val KT = ".kt"

    fun test(project: Project, task: Task) {
        val builder = provideContentBuilder(project, task)
        val contentProvider = builder.provider

        builder.apply (
            JVMTestCases.compileMethodSignature_Arg_Int_Return_String(contentProvider),
        )
    }

    private fun provideContentBuilder(project: Project, task: Task): JVMContentBuilder {
        val jniHost = provideJNIHost(project)
        var printer = providePrinter(jniHost)

        val rustProject = File(
            project.rootDir.toString(),
            "app${File.separator}" +
                    "src${File.separator}" +
                    "main${File.separator}" +
                    "rust"
        )

        return JVMContentBuilder(rustProject, jniHost, printer, project, task)
    }

    private fun providePrinter(jniHost: File): JVMContentProvider {
        val path = jniHost.toString()
        return when {
            path.endsWith(KT) -> KotlinContentProvider
            path.endsWith(JAVA) -> JavaContentProvider
            else -> throw IllegalStateException("No printer found")
        }
    }

    private fun provideJNIHost(project: Project): File {
        var jvmFile = File(
            project.rootDir.toString(),
            RustJNITest.JNI_HOST + KT
        )

        if (jvmFile.exists()) {
            return jvmFile
        }

        jvmFile = File(
            project.rootDir.toString(),
            RustJNITest.JNI_HOST + JAVA
        )

        if (jvmFile.exists()) {
            return jvmFile
        }

        throw IllegalStateException("No JVM file found")
    }

}


