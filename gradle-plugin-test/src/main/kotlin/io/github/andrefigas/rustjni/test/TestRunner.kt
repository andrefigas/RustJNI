package io.github.andrefigas.rustjni.test

import io.github.andrefigas.rustjni.test.cases.TestCases
import io.github.andrefigas.rustjni.test.jvm.content.JVMContentProvider
import io.github.andrefigas.rustjni.test.jvm.content.JavaContentProvider
import io.github.andrefigas.rustjni.test.jvm.content.KotlinContentProvider
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.util.Properties

object JVMTestRunner {

    private const val JAVA = ".java"
    internal const val KT = ".kt"

    fun test(project: Project, task: Task) {
        val jniHost = provideJNIHost(project)
        var contentProvider = contentProvider(jniHost)

        val rustFile = File(
            project.rootDir.toString(),
            "app${File.separator}" +
                    "src${File.separator}" +
                    "main${File.separator}" +
                    "rust"
        )

        val props = Properties()
        project.file("${project.rootProject.projectDir}${File.separator}local.properties")
            .inputStream().use { props.load(it) }

        val ndkDir = "${props.getProperty("sdk.dir")}${File.separator}ndk"

        apply(
            project,
            task,
            ndkDir,
            rustFile,
            jniHost,
            contentProvider,
            TestData.all(contentProvider)
        )
    }

    private fun contentProvider(jniHost: File): JVMContentProvider {
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

    private fun clean(rustFile : File, jniHost : File, provider : JVMContentProvider){
        if(rustFile.exists()){
            rustFile.deleteRecursively()
        }

        jniHost.writeText(
            provider.restoreJVMContent.trimIndent()
        )
    }

    private fun apply(project: Project,
                      task: Task,
                      ndkDir: String,
                      rustFile : File,
                      jniHost : File,
                      provider : JVMContentProvider,
                      data: String
    ) {
        clean(rustFile, jniHost, provider)
        jniHost.writeText(data)

        TestCases(project,
            task,
            jniHost,
            ndkDir = ndkDir,
            rustFile = File(rustFile, "src${File.separator}lib.rs"),
            cargoConfigFile = File(rustFile,".cargo/config.toml"),
        ).apply {
            all()
            finish()
        }

        clean(rustFile, jniHost, provider)
    }

}


