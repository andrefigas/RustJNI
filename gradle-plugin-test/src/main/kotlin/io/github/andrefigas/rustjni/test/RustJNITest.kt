package io.github.andrefigas.rustjni.test

import org.gradle.api.Plugin
import org.gradle.api.Project

class RustJNITest : Plugin<Project> {

    private companion object{
        const val RUST_JNI_COMPILE_TEST = "rust-jni-compile-test"
    }

    override fun apply(project: Project) {

        project.tasks.register(RUST_JNI_COMPILE_TEST) {
            group = "test"
            description = "Compiles Rust code for specified test cases"

            doFirst {
                JVMTest.updateJVMContent(project, JVMTest.intArgReturnString, this)
            }

            doLast {

            }
        }

    }



}

