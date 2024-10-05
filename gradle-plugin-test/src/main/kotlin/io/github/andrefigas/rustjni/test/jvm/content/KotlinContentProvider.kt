package io.github.andrefigas.rustjni.test.jvm.content

object KotlinContentProvider : JVMContentProvider {

    override val primitiveInt = "int"
    override val primitiveLong = "long"
    override val primitiveBoolean = "boolean"
    override val primitiveByte = "byte"
    override val primitiveChar = "char"
    override val primitiveDouble = "double"
    override val primitiveFloat = "float"
    override val primitiveShort = "short"
    override val primitiveString = "String"
    override val primitiveObject = "Object"
    override val primitiveVoid = "void"

    override fun generateMethod(argType: List<String>, returnType: String): String {
        val params = argType.mapIndexed { index, arg -> "param$index : $arg" }
        return """
            private external fun someMethod(${params.joinToString(separator = ", ")}) : $returnType
            """
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