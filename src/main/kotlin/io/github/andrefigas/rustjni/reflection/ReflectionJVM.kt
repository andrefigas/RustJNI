package io.github.andrefigas.rustjni.reflection

import io.github.andrefigas.rustjni.RustJniExtension
import io.github.andrefigas.rustjni.reflection.primitive.PrimitiveJVM
import io.github.andrefigas.rustjni.reflection.primitive.PrimitiveRust
import io.github.andrefigas.rustjni.utils.FileUtils
import org.gradle.api.Project
import java.io.File

internal object ReflectionJVM {

    fun update(project: Project, extension: RustJniExtension){
        removeNativeMethodDeclaration(project, extension)
        addNativeMethodDeclaration(project, extension)
    }

    private fun addNativeMethodDeclaration(project: Project, extension: RustJniExtension) {
        val jniHost = extension.jniHost.trim()

        if (RustJniExtension.shouldSkipAddingMethods(jniHost, extension)) return

        val className = FileUtils.extractClassName(jniHost)
        val packagePath = FileUtils.extractPackagePath(jniHost)

        val classFile = FileUtils.findClassFile(project, packagePath, className)
        val isKotlinFile = classFile.extension.equals("kt", ignoreCase = true)

        var fileContent = classFile.readText()

        fileContent = removeExistingRustJniBlockContent(fileContent)

        val codeToInsertWithoutIndent = generateMethodDeclarations(extension, isKotlinFile)

        val newFileContent = insertGeneratedCode(fileContent, codeToInsertWithoutIndent, className)

        classFile.writeText(newFileContent)

        println("Added native method declarations and library loading to ${classFile.absolutePath}")
    }

    private fun removeNativeMethodDeclaration(project: Project, extension: RustJniExtension) {
        val jniHost = extension.jniHost.trim()

        if (RustJniExtension.shouldSkipAddingMethods(jniHost, extension)) return

        val className = FileUtils.extractClassName(jniHost)
        val packagePath = FileUtils.extractPackagePath(jniHost)

        val classFile = FileUtils.findClassFile(project, packagePath, className)

        val fileContent = classFile.readText()
        val modifiedContent = removeExistingRustJniBlockContent(fileContent)

        if (modifiedContent != fileContent) {
            classFile.writeText(modifiedContent)
            println("Removed existing RustJNI block from ${classFile.absolutePath}")
        } else {
            println("No RustJNI block found in ${classFile.absolutePath}")
        }
    }

    private fun removeExistingRustJniBlockContent(fileContent: String): String {
        val rustJniBlockPattern = Regex(
            pattern = "(?s)(?:\\r?\\n)?[ \\t]*//<RustJNI>.*?//</RustJNI>[ \\t]*(?:\\r?\\n)?",
            options = setOf(RegexOption.MULTILINE)
        )
        return rustJniBlockPattern.replace(fileContent, "")
    }

    private fun generateMethodDeclarations(
        extension: RustJniExtension,
        isKotlinFile: Boolean
    ): String {
        val jniHost = extension.jniHost.trim()
        val methodsToGenerate = parseRustJniFunctions(extension, jniHost, isKotlinFile)

        if (methodsToGenerate.isEmpty()) {
            throw org.gradle.api.GradleException("No JNI methods found for class $jniHost in rust_jni.rs")
        }

        return buildMethodDeclarations(methodsToGenerate, extension.libName, isKotlinFile)
    }

    private fun parseRustJniFunctions(
        extension: RustJniExtension,
        jniHost: String,
        isKotlinFile: Boolean
    ): List<MethodSignature> {
        val rustLibContent = readRustJniFile(extension)

        val jniFunctionPattern = Regex(
            """(?s)#\s*\[\s*no_mangle\s*\]\s*pub\s+extern\s+"C"\s+fn\s+(Java_\w+)\s*\((.*?)\)\s*(->\s*[\w:]+)?\s*\{""",
            RegexOption.MULTILINE
        )

        return jniFunctionPattern.findAll(rustLibContent).mapNotNull { functionMatch ->
            createMethodSignature(functionMatch, jniHost, isKotlinFile)
        }.toList()
    }

    private fun extractClassNameFromRust(jniFunctionName: String): String {
        val javaFunctionName = jniFunctionName.removePrefix("Java_")
        val nameParts = javaFunctionName.split('_')
        val classNameParts = nameParts.dropLast(1)
        return classNameParts.joinToString(".").replace('_', '.')
    }

    private fun extractParameters(paramsString: String, isKotlinFile: Boolean): List<String> {
        return paramsString.split(',')
            .drop(2) // Ignore the first two parameters (env, _class)
            .mapNotNull { param ->
                val parts = param.trim().split(':')
                if (parts.size == 2) {
                    val paramName = parts[0].trim()
                    val paramTypeRust = parts[1].trim()
                    "$paramName: ${mapRustTypeToJavaType(paramTypeRust, isKotlinFile)}"
                } else null
            }
    }

    private fun mapRustTypeToJavaType(rustType: String, isKotlinFile: Boolean): String {
        return when (rustType) {
            PrimitiveRust.RS_JSTRING -> if (isKotlinFile) PrimitiveJVM.KT_STRING else PrimitiveJVM.JV_STRING
            PrimitiveRust.RS_JINT -> if (isKotlinFile) PrimitiveJVM.KT_INT else PrimitiveJVM.JV_INT
            PrimitiveRust.RS_JLONG -> if (isKotlinFile) PrimitiveJVM.KT_LONG else PrimitiveJVM.JV_LONG
            PrimitiveRust.RS_JBOOLEAN -> if (isKotlinFile) PrimitiveJVM.KT_BOOLEAN else PrimitiveJVM.JV_BOOLEAN
            PrimitiveRust.RS_VOID -> if (isKotlinFile) PrimitiveJVM.KT_UNIT else PrimitiveJVM.JV_VOID
            else -> if (isKotlinFile) PrimitiveJVM.KT_ANY else PrimitiveJVM.JV_OBJECT
        }
    }

    private fun createMethodSignature(
        functionMatch: MatchResult,
        jniHost: String,
        isKotlinFile: Boolean
    ): MethodSignature? {
        val jniFunctionName = functionMatch.groupValues[1]
        val paramsString = functionMatch.groupValues[2]
        val returnTypeString = functionMatch.groupValues[3]?.removePrefix("->")?.trim() ?: "void"

        val classNameFromRust = extractClassNameFromRust(jniFunctionName)

        if (classNameFromRust != jniHost.replace('$', '.')) return null

        val parameters = extractParameters(paramsString, isKotlinFile)
        val returnType = mapRustTypeToJavaType(returnTypeString, isKotlinFile)

        return MethodSignature(jniFunctionName, returnType, parameters)
    }

    private fun readRustJniFile(extension : RustJniExtension): String {
        val rustLibFile = File(extension.rustPath , "src${File.separator}rust_jni.rs")
        if (!rustLibFile.exists()) {
            throw org.gradle.api.GradleException("Could not find 'rust_jni.rs' file at ${rustLibFile.absolutePath}")
        }
        return rustLibFile.readText()
    }

    // Generates method declarations for Kotlin or Java files
    private fun buildMethodDeclarations(
        methodsToGenerate: List<MethodSignature>,
        libName: String,
        isKotlinFile: Boolean
    ): String {
        val methodDeclarations = methodsToGenerate.joinToString("\n\n") { method ->
            if (isKotlinFile) {
                "private external fun ${method.methodName.substringAfterLast('_')}(${method.parameters.joinToString(", ")}): ${method.returnType}"
            } else {
                val paramsJava = method.parameters.joinToString(", ") { param ->
                    val parts = param.split(":")
                    "${parts[1].trim()} ${parts[0].trim()}"
                }
                "private static native ${method.returnType} ${method.methodName.substringAfterLast('_')}($paramsJava);"
            }
        }

        return if (isKotlinFile) {
            """
            //<RustJNI>
            // auto-generated code
            
            $methodDeclarations
            
            init { System.loadLibrary("$libName") }
            
            //</RustJNI>
            """.trimIndent()
        } else {
            """
            //<RustJNI>
            // auto-generated code
            
            $methodDeclarations
            
            static { System.loadLibrary("$libName"); }
            
            //</RustJNI>
            """.trimIndent()
        }
    }

    // Inserts the generated code into the target file content
    fun insertGeneratedCode(fileContent: String, codeToInsert: String, className: String): String {
        val classPattern = Regex("(class|object|public\\s+class|final\\s+class|open\\s+class)\\s+$className\\b[^\\{]*\\{")
        val matchResult = classPattern.find(fileContent)
            ?: throw org.gradle.api.GradleException("Could not find class definition for $className")

        val insertionPoint = matchResult.range.last + 1
        val (beforeInsertion, afterInsertion) = fileContent.splitAt(insertionPoint)

        val indentedCode = indentCode(fileContent, insertionPoint, codeToInsert)

        return buildString {
            append(beforeInsertion)
            if (!beforeInsertion.endsWith("\n")) append("\n")
            append("\n")
            append(indentedCode)
            if (!afterInsertion.startsWith("\n")) append("\n")
            append(afterInsertion)
        }
    }

    // Adds indentation to the generated code to match the surrounding code
    private fun indentCode(fileContent: String, insertionPoint: Int, codeToInsert: String): String {
        val lines = fileContent.substring(0, insertionPoint).lines()
        val classLineIndex = lines.size - 1
        val classLine = lines[classLineIndex]
        val classIndentation = classLine.takeWhile { it == ' ' || it == '\t' }

        // Maintain consistent indentation for the entire block
        val memberIndentation = classIndentation + "    "

        return codeToInsert.lines().joinToString("\n") { line ->
            val content = line.trimStart()
            if (content.isNotEmpty()) {
                // Apply proper indentation to non-empty lines
                memberIndentation + content
            } else {
                line // Maintain empty lines as-is
            }
        }
    }

    // Extracts method signatures from the class file content (Kotlin/Java)
    fun extractMethodSignaturesFromClass(fileContent: String, isKotlinFile: Boolean): List<MethodSignature> {
        println("extractMethodSignaturesFromClass content: $fileContent isKotlin: $isKotlinFile")

        val methodPattern = if (isKotlinFile) {
            // Kotlin method pattern that captures methods with or without parameters
            Regex("""fun\s+(\w+)\s*\(([^)]*)\)\s*:\s*(\w+)""")
        } else {
            // Java native method pattern that captures methods with or without parameters
            Regex("""\b(?:private|protected|public)?\s*(?:static)?\s*native\s+(\w+)\s+(\w+)\s*\(([^)]*)\)\s*;""")
        }

        return methodPattern.findAll(fileContent).map { result ->
            val (methodName, returnType, parameters) = if (isKotlinFile) {
                // Kotlin uses groupValues[1] for method name, groupValues[3] for return type, groupValues[2] for parameters
                Triple(result.groupValues[1], result.groupValues[3], result.groupValues[2].trim())
            } else {
                // Java uses groupValues[2] for method name, groupValues[1] for return type, groupValues[3] for parameters
                Triple(result.groupValues[2], result.groupValues[1], result.groupValues[3].trim())
            }

            val parameterList = if (parameters.isNotEmpty()) {
                parameters
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            } else {
                emptyList() // No parameters
            }

            MethodSignature(methodName, returnType, parameterList)
        }.toList()
    }


}

// Extension function to split a string at a specified index
private fun String.splitAt(index: Int): Pair<String, String> {
    return substring(0, index) to substring(index)
}
