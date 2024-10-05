package io.github.andrefigas.rustjni.test

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

object JVMTest {

    val intArgReturnString = """
        private external fun someMethod(param1 : Int) : String
    """.trimIndent()

    fun updateJVMContent(project: Project, content: String, task: Task) {
        // Path to the MainActivity.kt file
        val jvmFile = File(
            project.rootDir.toString(),
            "app${File.separator}" +
                    "src${File.separator}" +
                    "main${File.separator}" +
                    "java${File.separator}" +
                    "com${File.separator}" +
                    "devfigas${File.separator}" +
                    "rustjni${File.separator}" +
                    "sample${File.separator}" +
                    "MainActivity.kt"
        )

        val rustProject = File(
            project.rootDir.toString(),
            "app${File.separator}" +
                    "src${File.separator}" +
                    "main${File.separator}" +
                    "rust"
        )

        if(rustProject.exists()){
            rustProject.deleteRecursively()
        }

        // Generate the new content based on input
        val generatedContent = generateJVMContent(content)

        // Check if the file exists
        if (jvmFile.exists()) {
            // Write the new content to the file
            jvmFile.writeText(generatedContent)
            println("File content updated successfully.")
            project.tasks.getByName("rust-jni-compile").actions.forEach {
                it.execute(task)
            }

            jvmFile.writeText(generateOriginal())
        } else {
            // If the file doesn't exist, display an error message
            println("File not found: ${jvmFile.absolutePath}")
        }
    }

    private fun generateJVMContent(generated: String): String {
        return """
        package com.devfigas.rustjni.sample

        import android.os.Bundle
        import androidx.activity.enableEdgeToEdge
        import androidx.appcompat.app.AppCompatActivity


        class MainActivity : AppCompatActivity() {
        
            //<RustJNI>
            // auto-generated code
        
            $generated
        
            init { System.loadLibrary("my_rust_lib") }

            //</RustJNI>
           
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContentView(R.layout.activity_main)
            }
        }
        """.trimIndent()
    }

    private fun generateOriginal(): String {
        return """
        package com.devfigas.rustjni.sample

        import android.os.Bundle
        import androidx.activity.enableEdgeToEdge
        import androidx.appcompat.app.AppCompatActivity


        class MainActivity : AppCompatActivity() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                enableEdgeToEdge()
                setContentView(R.layout.activity_main)
            }
        }
        """.trimIndent()
    }



}