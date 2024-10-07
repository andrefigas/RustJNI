package io.github.andrefigas.rustjni.test

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class RustJNITest : Plugin<Project> {

    companion object{
        const val RUST_JNI_COMPILE_TEST = "rust-jni-compile-test"
        val JNI_HOST = "app${File.separator}" +
                "src${File.separator}" +
                "main${File.separator}" +
                "java${File.separator}" +
                "com${File.separator}" +
                "devfigas${File.separator}" +
                "rustjni${File.separator}" +
                "sample${File.separator}" +
                "MainActivity"
    }

    override fun apply(project: Project) {

        project.tasks.register(RUST_JNI_COMPILE_TEST) {
            group = "test"
            description = "Compiles Rust code for specified test cases"

            doFirst {
                JVMTestRunner.test(project, this)
            }

        }

    }



}

