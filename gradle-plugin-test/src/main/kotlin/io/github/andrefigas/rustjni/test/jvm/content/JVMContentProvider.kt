package io.github.andrefigas.rustjni.test.jvm.content

import java.lang.StringBuilder

interface JVMContentProvider {

    fun generate(methods : Map<Array<String>, String>): String {
        val separator = "\n\n"
        val sb = StringBuilder()
        var ident = ""
        var i = 0;
        methods.forEach { (argType, returnType) ->
            sb.append(ident)
            sb.append(generateMethod(i, argType, returnType).trimIndent())
            sb.append(separator)
            ident =  "            "
            i++
        }

        val methods = sb.deleteRange(sb.length - separator.length, sb.length).toString()
        return generateJVMContent(methods).trimIndent()
    }

    fun generateMethod(index : Int, argType : Array<String>, returnType : String) : String

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