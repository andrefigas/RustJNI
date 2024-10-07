package io.github.andrefigas.rustjni.reflection

import io.github.andrefigas.rustjni.RustJniExtension
import io.github.andrefigas.rustjni.reflection.primitive.PrimitiveJVM
import io.github.andrefigas.rustjni.reflection.primitive.PrimitiveRust
import io.github.andrefigas.rustjni.utils.FileUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

internal object ReflectionNative {

    private val mockReturn = mapOf(
        PrimitiveJVM.KT_STRING to PrimitiveRust.mock(PrimitiveRust.RS_JSTRING),
        PrimitiveJVM.JV_STRING to PrimitiveRust.mock(PrimitiveRust.RS_JSTRING),
        PrimitiveJVM.KT_INT to PrimitiveRust.mock(PrimitiveRust.RS_JINT),
        PrimitiveJVM.JV_INT to PrimitiveRust.mock(PrimitiveRust.RS_JINT),
        PrimitiveJVM.KT_BOOLEAN to PrimitiveRust.mock(PrimitiveRust.RS_JBOOLEAN),
        PrimitiveJVM.JV_BOOLEAN to PrimitiveRust.mock(PrimitiveRust.RS_JBOOLEAN),
        PrimitiveJVM.KT_LONG to PrimitiveRust.mock(PrimitiveRust.RS_JLONG),
        PrimitiveJVM.JV_LONG to PrimitiveRust.mock(PrimitiveRust.RS_JLONG),
        PrimitiveJVM.KT_BYTE to PrimitiveRust.mock(PrimitiveRust.RS_JBYTE),
        PrimitiveJVM.JV_BYTE to PrimitiveRust.mock(PrimitiveRust.RS_JBYTE),
        PrimitiveJVM.KT_CHAR to PrimitiveRust.mock(PrimitiveRust.RS_JCHAR),
        PrimitiveJVM.JV_CHAR to PrimitiveRust.mock(PrimitiveRust.RS_JCHAR),
        PrimitiveJVM.KT_DOUBLE to PrimitiveRust.mock(PrimitiveRust.RS_JDOUBLE),
        PrimitiveJVM.JV_DOUBLE to PrimitiveRust.mock(PrimitiveRust.RS_JDOUBLE),
        PrimitiveJVM.KT_FLOAT to PrimitiveRust.mock(PrimitiveRust.RS_JFLOAT),
        PrimitiveJVM.JV_FLOAT to PrimitiveRust.mock(PrimitiveRust.RS_JFLOAT),
        PrimitiveJVM.KT_SHORT to PrimitiveRust.mock(PrimitiveRust.RS_JSHORT),
        PrimitiveJVM.JV_SHORT to PrimitiveRust.mock(PrimitiveRust.RS_JSHORT),
        PrimitiveJVM.JV_VOID to PrimitiveRust.mock(PrimitiveRust.RS_VOID),
        PrimitiveJVM.KT_UNIT to PrimitiveRust.mock(PrimitiveRust.RS_VOID),
        PrimitiveRust.RS_JCLASS to PrimitiveRust.mock(PrimitiveRust.RS_JCLASS),
        PrimitiveRust.RS_JOBJECT to PrimitiveRust.mock(PrimitiveRust.RS_JOBJECT)
    )

    fun update(project: Project, extension: RustJniExtension) {
        val jniHost = extension.jniHost.trim()

        if (RustJniExtension.shouldSkipAddingMethods(jniHost, extension)) return

        val className = FileUtils.extractClassName(jniHost)
        val packagePath =  FileUtils.extractPackagePath(jniHost)

        val classFile = FileUtils.findClassFile(project, packagePath, className)
        val isKotlinFile = FileUtils.isKotlinFile(classFile)

        val fileContent = FileUtils.readFileContent(classFile)

        // Extract method signatures
        val kotlinMethodSignatures = ReflectionJVM.extractMethodSignaturesFromClass(fileContent, isKotlinFile)

        val rustMethodSignatures = parseRustJniFunctions(extension, jniHost, isKotlinFile)

        compareMethodSignatures(kotlinMethodSignatures, rustMethodSignatures, isKotlinFile).forEach { methodSignature ->
            updateRustFileIfMethodNotExists(
                extension,
                methodSignature.methodName,
                methodSignature.parameters,
                methodSignature.returnType,
                isKotlinFile
            )
        }

        val rustFilePath = FileUtils.getRustSrcFile(FileUtils.getRustDir(project, extension))
        removePrimitiveImports(rustFilePath)
        addPrimitiveImports(rustFilePath)
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
        val returnType = mapRustTypeToJVM(returnTypeString, isKotlinFile)

        return MethodSignature(jniFunctionName, returnType, parameters)
    }

    private fun mapRustTypeToJVM(rustType: String, isKotlinFile: Boolean): String {
        return when (rustType) {
            PrimitiveRust.RS_JSTRING -> if (isKotlinFile) PrimitiveJVM.KT_STRING else PrimitiveJVM.JV_STRING
            PrimitiveRust.RS_JINT -> if (isKotlinFile) PrimitiveJVM.KT_INT else PrimitiveJVM.JV_INT
            PrimitiveRust.RS_JLONG -> if (isKotlinFile) PrimitiveJVM.KT_LONG else PrimitiveJVM.JV_LONG
            PrimitiveRust.RS_JBOOLEAN -> if (isKotlinFile) PrimitiveJVM.KT_BOOLEAN else PrimitiveJVM.JV_BOOLEAN
            PrimitiveRust.RS_VOID -> if (isKotlinFile) PrimitiveJVM.KT_UNIT else PrimitiveJVM.JV_VOID
            else -> if (isKotlinFile) PrimitiveJVM.KT_ANY else PrimitiveJVM.JV_OBJECT
        }
    }

    private fun mapJVMtoRustType(type: String, isKotlin: Boolean): String {
        val delimiter = if (isKotlin) ":" else " "
        val normalizedType = if (isKotlin) {
            type.substringAfter(delimiter).trim()
        } else {
            type.substringBefore(delimiter).trim()
        }

        val result = when (normalizedType) {
            PrimitiveJVM.KT_STRING, PrimitiveJVM.JV_STRING -> PrimitiveRust.RS_JSTRING
            PrimitiveJVM.KT_INT, PrimitiveJVM.JV_INT -> PrimitiveRust.RS_JINT
            PrimitiveJVM.KT_LONG, PrimitiveJVM.JV_LONG -> PrimitiveRust.RS_JLONG
            PrimitiveJVM.KT_BOOLEAN, PrimitiveJVM.JV_BOOLEAN -> PrimitiveRust.RS_JBOOLEAN
            PrimitiveJVM.KT_UNIT, PrimitiveJVM.JV_VOID -> PrimitiveRust.RS_VOID
            else -> PrimitiveRust.RS_JOBJECT
        }

        return result
    }

    private fun extractParameters(paramsString: String, isKotlinFile: Boolean): List<String> {
        return paramsString.split(',')
            .drop(2) // Ignore the first two parameters (env, _class)
            .mapNotNull { param ->
                val parts = param.trim().split(':')
                if (parts.size == 2) {
                    val paramName = parts[0].trim()
                    val paramTypeRust = parts[1].trim()
                    "$paramName: ${mapRustTypeToJVM(paramTypeRust, isKotlinFile)}"
                } else null
            }
    }

    private fun extractClassNameFromRust(jniFunctionName: String): String {
        val javaFunctionName = jniFunctionName.removePrefix("Java_")
        val nameParts = javaFunctionName.split('_')
        val classNameParts = nameParts.dropLast(1)
        return classNameParts.joinToString(".").replace('_', '.')
    }

    private fun readRustJniFile(extension: RustJniExtension): String {
        val rustLibFile = FileUtils.getRustSrcFile(File(extension.rustPath))
        if (!rustLibFile.exists()) {
            throw org.gradle.api.GradleException("Could not find '${rustLibFile.name}' file at ${rustLibFile.absolutePath}")
        }
        return rustLibFile.readText()
    }

    // Compares method signatures between Kotlin/Java and Rust
    private fun compareMethodSignatures(kotlin: List<MethodSignature>, rust: List<MethodSignature>, isKotlin: Boolean): List<MethodSignature> {
        val rustSignatures = rust.map {
            MethodSignature(it.methodName.substringAfterLast('_'), it.returnType, it.parameters)
        }

        val delimiter = if (isKotlin) ":" else " "
        return kotlin.filterNot { kotlinMethod ->
            rustSignatures.any { rustMethod ->
                val jvmParametersType = kotlinMethod.parameters.map {
                    if (isKotlin) it.substringAfter(delimiter).trim().toLowerCase() else it.substringBefore(delimiter).trim().toLowerCase()
                }

                val rustParametersType = rustMethod.parameters.map { it.substringAfter(":").trim().toLowerCase()}
                val match = kotlinMethod.methodName == rustMethod.methodName && jvmParametersType == rustParametersType
                if(match) {
                    val kotlinReturnType = mapJVMtoRustType(kotlinMethod.returnType, isKotlin)
                    val rustReturnType = mapJVMtoRustType(rustMethod.returnType, isKotlin)
                    if(kotlinReturnType != rustReturnType){
                        throw GradleException("Method \"${kotlinMethod.methodName}\" is already defined in Rust " +
                                "with the same signature but a different return type.\n" +
                                "Check the Rust file: src/lib.rs")
                    }
                }

                match
            }
        }
    }

    // Updates the Rust file if the corresponding method does not exist
    private fun updateRustFileIfMethodNotExists(
        extension: RustJniExtension,
        methodName: String,
        parameters: List<String>,
        returnType: String,
        isKotlinFile: Boolean
    ) {
        val jniHost = extension.jniHost.trim()

        if (RustJniExtension.shouldSkipAddingMethods(jniHost, extension)) return

        val rustFilePath = FileUtils.getRustSrcFile(File(extension.rustPath))
        addMethodToRust(rustFilePath, methodName, parameters, returnType, jniHost, isKotlinFile)
    }

    // Removes primitive imports from the Rust file
    private fun removePrimitiveImports(rustFile: File) {
        val lines = rustFile.readLines()
        val filteredLines = mutableListOf<String>()
        val removedImports = mutableListOf<String>()
        var insideRustJniBlock = false
        var insidePrimitiveImportBlock = false

        for (line in lines) {
            if (line.contains("//<RustJNI>")) {
                insideRustJniBlock = true
                filteredLines.add(line)
                continue
            }
            if (line.contains("//</RustJNI>")) {
                insideRustJniBlock = false
                filteredLines.add(line)
                insidePrimitiveImportBlock = false
                continue
            }
            if (insideRustJniBlock && line.contains("// primitive imports")) {
                insidePrimitiveImportBlock = true
                filteredLines.add(line)
                continue
            }
            if (insidePrimitiveImportBlock) {
                if (line.isNotBlank()) {
                    removedImports.add(line)
                    continue
                }
            }
            filteredLines.add(line)
        }

        if (removedImports.isNotEmpty()) {
            println("Removed Imports:")
            removedImports.forEach { println(it) }
        } else {
            println("No imports were removed.")
        }

        rustFile.writeText(filteredLines.joinToString("\n"))
    }

    // Adds primitive imports to the Rust file if needed
    private fun addPrimitiveImports(rustFile: File) {
        println("Adding imports to $rustFile")
        val lines = rustFile.readLines()
        val primitiveTypes = mutableSetOf<String>()

        val typeRegex = Regex("jstring|jint|jclass|jboolean|jbyte|jchar|jdouble|jfloat|jlong|jshort|jobject")
        for (line in lines) {
            val match = typeRegex.find(line)
            if (match != null) {
                primitiveTypes.add(match.value)
            }
        }

        if (primitiveTypes.isNotEmpty()) {
            val importsLine = "use jni::sys::{${primitiveTypes.joinToString(", ")}};"
            val updatedLines = lines.toMutableList()
            var inserted = false

            for (i in updatedLines.indices) {
                if (updatedLines[i].contains("//<RustJNI>")) {
                    updatedLines.add(i + 2, importsLine)
                    inserted = true
                    break
                }
            }

            if (!inserted) {
                updatedLines.add("//<RustJNI>")
                updatedLines.add("// primitive imports")
                updatedLines.add(importsLine)
                updatedLines.add("//</RustJNI>")
            }

            println("Added Imports: $importsLine")
            rustFile.writeText(updatedLines.joinToString("\n"))
        }
    }

    // Adds a method to the Rust file
    private fun addMethodToRust(
        rustFile: File,
        methodName: String,
        parameters: List<String>,
        returnType: String,
        jniHost: String,
        isKotlinFile: Boolean
    ) {
        val rustContent = buildRustJNIContent(methodName, parameters, returnType, jniHost, isKotlinFile)
        rustFile.appendText(rustContent)
    }

    // Builds the Rust JNI content for a specific method
    private fun buildRustJNIContent(
        methodName: String,
        parameters: List<String>,
        returnType: String,
        jniHost: String,
        isKotlin: Boolean,
    ): String {
        val javaClassPath = jniHost.replace('.', '_')
        val delimiter = if (isKotlin) ":" else " "

        val paramList = parameters.joinToString(", ") {
            val argument = if (isKotlin) it.substringBefore(delimiter).trim() else it.substringAfter(delimiter).trim()
            val type = mapJVMtoRustType(it, isKotlin)
            "$argument: $type"
        }

        val (rustType, exampleReturnValue) = mockReturn[returnType] ?: return generateUnsupportedTypeMethod(javaClassPath, methodName, paramList, returnType)
        return generateRustMethod(rustType, javaClassPath, methodName, paramList, exampleReturnValue)
    }

    private fun extractParameterNames(paramList: String): String {
        return paramList.split(", ")
            .joinToString(", ") { it.substringBefore(":").trim() }
    }

    // Generates a Rust method based on the return type and parameters
    private fun generateRustMethod(
        returnType: String,
        javaClassPath: String,
        methodName: String,
        paramList: String,
        exampleReturnValue: String
    ): String {
        val paramNames = extractParameterNames(paramList)
        val returnStatement = when (returnType) {
            PrimitiveRust.RS_JSTRING -> """
            let output = r#"Rust Method: $methodName"#;
            env.new_string(output)
                .expect("Couldn't create Java string!")
                .into_inner()
        """.trimIndent()
            PrimitiveRust.RS_JINT -> exampleReturnValue
            PrimitiveRust.RS_JBOOLEAN -> """
            if $exampleReturnValue { jni::sys::JNI_TRUE } else { jni::sys::JNI_FALSE }
        """.trimIndent()
            PrimitiveRust.RS_JLONG, PrimitiveRust.RS_JBYTE, PrimitiveRust.RS_JCHAR,
            PrimitiveRust.RS_JDOUBLE, PrimitiveRust.RS_JFLOAT, PrimitiveRust.RS_JSHORT -> exampleReturnValue
            PrimitiveRust.RS_JCLASS -> "_class" // Returning the class object itself
            PrimitiveRust.RS_JOBJECT -> "let obj = JObject::null(); obj.into_inner()" // Returning a null object
            PrimitiveRust.RS_VOID -> "" // No return statement for void
            else -> throw IllegalArgumentException("Unsupported return type: $returnType")
        }

        // Conditionally include the return type declaration only if it's not void
        val returnTypeDeclaration = if (returnType == PrimitiveRust.RS_VOID) "" else " -> $returnType"

        return """
        
#[no_mangle]
pub extern "C" fn Java_${javaClassPath}_$methodName(
    env: JNIEnv,
    _class: JClass,
    $paramList
)${returnTypeDeclaration} {
    println!("Parameters: {:?}", ($paramNames));

    $returnStatement
}
    """.trimMargin()
    }

    private fun generateUnsupportedTypeMethod(
        javaClassPath: String,
        methodName: String,
        paramList: String,
        returnType: String
    ): String {
        val paramNames = extractParameterNames(paramList)
        return """
        
        
#[no_mangle]
pub extern "C" fn Java_${javaClassPath}_$methodName(
    env: JNIEnv,
    _class: JClass,
    $paramList
) {
    // Print parameters
    println!("Parameters: {:?}", ($paramNames));

    panic!("Unsupported return type: $returnType");
}
    """.trimMargin()
    }

}
