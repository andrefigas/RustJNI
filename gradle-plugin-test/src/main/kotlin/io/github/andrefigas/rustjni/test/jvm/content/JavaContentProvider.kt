package io.github.andrefigas.rustjni.test.jvm.content

object JavaContentProvider : JVMContentProvider {

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

    override fun generateMethod(argType: List<String>, returnType: String) : String {
        val params = argType.mapIndexed { index, arg -> "$arg param$index" }
        return """
            private static native $returnType someMethod(${params.joinToString(separator = ", ")});
            """
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