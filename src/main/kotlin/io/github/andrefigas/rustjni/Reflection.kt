package io.github.andrefigas.rustjni

import org.gradle.api.Project
import java.io.File

// Data class to represent method signature
internal data class MethodSignature(
    val methodName: String,
    val returnType: String,
    val parameters: List<String>
)

internal object Reflection {

    internal fun addNativeMethodDeclaration(project: Project, extension: RustJniExtension) {
        if (!extension.exportFunctions) return

        val jniHost = extension.jniHost.trim()

        val className = extractClassName(jniHost)
        val packagePath = extractPackagePath(jniHost)

        val classFile = findClassFile(project, packagePath, className)
        val isKotlinFile = classFile.extension.equals("kt", ignoreCase = true)

        var fileContent = classFile.readText()
        fileContent = removeExistingRustJniBlockContent(fileContent)

        val codeToInsertWithoutIndent = generateMethodDeclarations(project, extension, isKotlinFile)

        val newFileContent = insertGeneratedCode(fileContent, codeToInsertWithoutIndent, className)

        classFile.writeText(newFileContent)

        println("Added native method declarations and library loading to ${classFile.absolutePath}")
    }

    private fun extractClassName(jniHost: String): String {
        return jniHost.substringAfterLast('.')
    }

    private fun extractPackagePath(jniHost: String): String {
        return jniHost.substringBeforeLast('.').replace('.', File.separatorChar)
    }

    private fun findClassFile(project: Project, packagePath: String, className: String): File {
        val sourceDirs = getSourceDirs(project)
        val possibleExtensions = listOf("kt", "java")

        return sourceDirs
            .flatMap { srcDir ->
                possibleExtensions.map { ext ->
                    File(srcDir, "$packagePath${File.separator}$className.$ext")
                }
            }
            .firstOrNull { it.exists() }
            ?: throw org.gradle.api.GradleException("Class file not found for jniHost: $packagePath.$className")
    }

    private fun getSourceDirs(project: Project): Set<File> {
        val androidExtension = project.extensions.findByName("android")
        return when (androidExtension) {
            is com.android.build.gradle.AppExtension -> androidExtension.sourceSets.getByName("main").java.srcDirs
            is com.android.build.gradle.LibraryExtension -> androidExtension.sourceSets.getByName("main").java.srcDirs
            else -> throw org.gradle.api.GradleException("Android extension not found in project")
        }
    }

    private fun insertGeneratedCode(
        fileContent: String,
        codeToInsert: String,
        className: String
    ): String {
        val classPattern =
            Regex("(class|object|public\\s+class|final\\s+class|open\\s+class)\\s+$className\\b[^\\{]*\\{")
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

    private fun String.splitAt(index: Int): Pair<String, String> {
        return substring(0, index) to substring(index)
    }

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

    private fun generateMethodDeclarations(
        project: Project,
        extension: RustJniExtension,
        isKotlinFile: Boolean
    ): String {
        val jniHost = extension.jniHost.trim()
        val methodsToGenerate = parseRustJniFunctions(project, jniHost, isKotlinFile)

        if (methodsToGenerate.isEmpty()) {
            throw org.gradle.api.GradleException("No JNI methods found for class $jniHost in rust_jni.rs")
        }

        return buildMethodDeclarations(methodsToGenerate, extension.libName, isKotlinFile)
    }

    private fun parseRustJniFunctions(
        project: Project,
        jniHost: String,
        isKotlinFile: Boolean
    ): List<MethodSignature> {
        val rustLibContent = readRustJniFile(project)

        val jniFunctionPattern = Regex(
            """(?s)#\s*\[\s*no_mangle\s*\]\s*pub\s+extern\s+"C"\s+fn\s+(Java_\w+)\s*\((.*?)\)\s*(->\s*[\w:]+)?\s*\{""",
            RegexOption.MULTILINE
        )

        return jniFunctionPattern.findAll(rustLibContent).mapNotNull { functionMatch ->
            createMethodSignature(functionMatch, jniHost, isKotlinFile)
        }.toList()
    }

    private fun readRustJniFile(project: Project): String {
        val rustLibFile =
            File(project.rootDir, "rust${File.separator}src${File.separator}rust_jni.rs")
        if (!rustLibFile.exists()) {
            throw org.gradle.api.GradleException("Could not find 'rust_jni.rs' file at ${rustLibFile.absolutePath}")
        }
        return rustLibFile.readText()
    }

    private fun createMethodSignature(
        functionMatch: MatchResult,
        jniHost: String,
        isKotlinFile: Boolean
    ): MethodSignature? {
        val jniFunctionName = functionMatch.groupValues[1]
        val paramsString = functionMatch.groupValues[2]
        val returnTypeString =
            functionMatch.groupValues.getOrNull(3)?.removePrefix("->")?.trim() ?: "void"

        val classNameFromRust = extractClassNameFromRust(jniFunctionName)

        if (classNameFromRust != jniHost.replace('$', '.')) return null

        val parameters = extractParameters(paramsString, isKotlinFile)
        val returnType = mapRustTypeToJavaType(returnTypeString, isKotlinFile)

        return MethodSignature(jniFunctionName, returnType, parameters)
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
            "jstring" -> if (isKotlinFile) "String" else "String"
            "jint" -> if (isKotlinFile) "Int" else "int"
            "jlong" -> if (isKotlinFile) "Long" else "long"
            "jboolean" -> if (isKotlinFile) "Boolean" else "boolean"
            "void" -> if (isKotlinFile) "Unit" else "void"
            else -> if (isKotlinFile) "Any" else "Object"
        }
    }

    private fun buildMethodDeclarations(
        methodsToGenerate: List<MethodSignature>,
        libName: String,
        isKotlinFile: Boolean
    ): String {
        val methodDeclarations = methodsToGenerate.joinToString("\n\n") { method ->
            if (isKotlinFile) {
                "private external fun ${method.methodName.substringAfterLast('_')}(${
                    method.parameters.joinToString(
                        ", "
                    )
                }): ${method.returnType}"
            } else {
                val paramsJava = method.parameters.joinToString(", ") { param ->
                    val parts = param.split(":")
                    "${parts[1].trim()} ${parts[0].trim()}"
                }
                "private static native ${method.returnType} ${
                    method.methodName.substringAfterLast(
                        '_'
                    )
                }($paramsJava);"
            }
        }

        return if (isKotlinFile) {
            """
        //<RustJNI>
        // auto-generated code
        // Checkout the source: rust${File.separator}src${File.separator}rust_jni.rs
        
        $methodDeclarations
        
        init { System.loadLibrary("$libName") }
        
        //</RustJNI>
        """.trim()
        } else {
            """
        //<RustJNI>
        // auto-generated code
        // Checkout the source: rust${File.separator}src${File.separator}rust_jni.rs
        
        $methodDeclarations
        
        static { System.loadLibrary("$libName"); }
        
        //</RustJNI>
        """.trim()
        }
    }

    internal fun removeNativeMethodDeclaration(project: Project, extension: RustJniExtension) {
        if (!extension.exportFunctions) return

        val jniHost = extension.jniHost.trim()

        val className = extractClassName(jniHost)
        val packagePath = extractPackagePath(jniHost)

        val classFile = findClassFile(project, packagePath, className)

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
}

