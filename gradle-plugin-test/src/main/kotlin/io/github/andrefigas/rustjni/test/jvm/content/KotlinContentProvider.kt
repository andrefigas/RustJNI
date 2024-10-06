package io.github.andrefigas.rustjni.test.jvm.content

object KotlinContentProvider : JVMContentProvider {

    override val primitiveInt = "Int"
    override val primitiveLong = "Long"
    override val primitiveBoolean = "Boolean"
    override val primitiveByte = "Byte"
    override val primitiveChar = "Char"
    override val primitiveDouble = "Double"
    override val primitiveFloat = "Float"
    override val primitiveShort = "Short"
    override val primitiveString = "String"
    override val primitiveObject = "Any"
    override val primitiveVoid = "Unit"

    override fun generateMethod(index : Int, argType: Array<String>, returnType: String): String {
        val params = argType.mapIndexed { index, arg -> "param$index : $arg" }
        return """
            private external fun someMethod$index(${params.joinToString(separator = ", ")}) : $returnType
            """.trimIndent()
    }

    override fun generateJVMContent(generated: String) ="""
        package com.devfigas.rustjni.sample

        import android.os.Bundle
        import androidx.appcompat.app.AppCompatActivity


        class MainActivity : AppCompatActivity() {
        
            //<RustJNI>
            // auto-generated code
        
            $generated
        
            init { System.loadLibrary("my_rust_lib") }

            //</RustJNI>
           
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)
            }
        }
        """

    override val restoreJVMContent =  """
        package com.devfigas.rustjni.sample

        import android.os.Bundle
        import androidx.appcompat.app.AppCompatActivity


        class MainActivity : AppCompatActivity() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_main)
            }
        }
        """

}