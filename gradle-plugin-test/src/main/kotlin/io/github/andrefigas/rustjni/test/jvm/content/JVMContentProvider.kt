package io.github.andrefigas.rustjni.test.jvm.content

interface JVMContentProvider {

    fun generate(argType: List<String>, returnType: String): String {
        val method = generateMethod(argType, returnType)
        return generateJVMContent(method).trimIndent()
    }

    fun generateMethod(argType : List<String>, returnType : String) : String

    fun generateJVMContent(generated: String) : String

    val restoreJVMContent : String

    val primitiveInt: String

    val primitiveLong: String

    val primitiveBoolean: String

    val primitiveByte: String

    val primitiveChar: String

    val primitiveDouble: String

    val primitiveFloat: String

    val primitiveShort: String

    val primitiveString: String

    val primitiveObject: String

    val primitiveVoid: String

}