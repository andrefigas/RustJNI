package io.github.andrefigas.rustjni.test.jvm.content

object JavaContentProvider : JVMContentProvider {

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

    override fun generateMethod(index : Int, argType: Array<String>, returnType: String) : String {
        val params = argType.mapIndexed { index, arg -> "$arg param$index" }
        return """
            private static native $returnType someMethod$index(${params.joinToString(separator = ", ")});
            """.trimIndent()
    }

    override fun generateJVMContent(generated: String) = """
        package com.devfigas.rustjni.sample;
        
        import android.os.Bundle;
        import androidx.appcompat.app.AppCompatActivity;
        
        public class MainActivity extends AppCompatActivity {
        
            //<RustJNI>
            // auto-generated code
        
            $generated
        
            static { System.loadLibrary("my_rust_lib"); }
        
            //</RustJNI>
        
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
            }
        
        }
        """

    override val restoreJVMContent = """
        package com.devfigas.rustjni.sample;
        
        import android.os.Bundle;
        
        import androidx.appcompat.app.AppCompatActivity;
        
        public class MainActivity extends AppCompatActivity {
        
            @Override
            public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
            }
        
        }
        """

}