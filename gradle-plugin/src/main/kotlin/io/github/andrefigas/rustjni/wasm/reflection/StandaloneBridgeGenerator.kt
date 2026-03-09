package io.github.andrefigas.rustjni.wasm.reflection

import io.github.andrefigas.rustjni.wasm.TypeMapping
import io.github.andrefigas.rustjni.wasm.model.ResolvedFunction
import io.github.andrefigas.rustjni.wasm.model.ResolvedParam

/**
 * Generates Rust WASM bridge code (raw, no wasm-bindgen) and Kotlin WasmLib (Chicory)
 * for the standalone mode.
 */
object StandaloneBridgeGenerator {

    // =========================================================================
    // Rust Cargo.toml
    // =========================================================================

    fun generateCargoToml(bridgeCrateName: String, coreCrateName: String, coreRelativePath: String): String {
        return buildString {
            appendLine("[package]")
            appendLine("name = \"$bridgeCrateName\"")
            appendLine("version = \"0.1.0\"")
            appendLine("edition = \"2021\"")
            appendLine()
            appendLine("[lib]")
            appendLine("crate-type = [\"cdylib\"]")
            appendLine()
            appendLine("[dependencies]")
            appendLine("$coreCrateName = { path = \"$coreRelativePath\" }")
            appendLine()
            appendLine("[profile.release]")
            appendLine("opt-level = \"s\"")
            appendLine("lto = true")
        }
    }

    // =========================================================================
    // Rust src/lib.rs
    // =========================================================================

    fun generateRustLib(coreCrateName: String, functions: List<ResolvedFunction>): String {
        val coreIdent = coreCrateName.replace("-", "_")
        val needsAlloc = functions.any { fn -> needsAllocDealloc(fn) }

        return buildString {
            if (needsAlloc) {
                appendLine("use std::mem::ManuallyDrop;")
                appendLine()
            }

            // Collect all host imports across all functions
            val imports = collectHostImports(functions)
            if (imports.isNotEmpty()) {
                appendLine("#[link(wasm_import_module = \"env\")]")
                appendLine("extern \"C\" {")
                for (import in imports) {
                    appendLine("    $import")
                }
                appendLine("}")
                appendLine()
            }

            // alloc/dealloc
            if (needsAlloc) {
                appendLine("#[no_mangle]")
                appendLine("pub extern \"C\" fn alloc(size: i32) -> i32 {")
                appendLine("    let mut buf = ManuallyDrop::new(vec![0u8; size as usize]);")
                appendLine("    buf.as_mut_ptr() as i32")
                appendLine("}")
                appendLine()
                appendLine("#[no_mangle]")
                appendLine("pub extern \"C\" fn dealloc(ptr: i32, size: i32) {")
                appendLine("    unsafe {")
                appendLine("        let _ = Vec::from_raw_parts(ptr as *mut u8, size as usize, size as usize);")
                appendLine("    }")
                appendLine("}")
                appendLine()
            }

            // Generate each exported function
            for (fn in functions) {
                appendLine(generateRustExportFunction(coreIdent, fn))
            }
        }
    }

    private fun needsAllocDealloc(fn: ResolvedFunction): Boolean {
        val hasStringParam = fn.params.any { TypeMapping.isStringType(it.rustType) && !it.isHostProvided }
        val hasStringReturn = fn.returnType?.let { rt ->
            TypeMapping.parseReturnTypes(rt).any { it == "String" || it == "&str" }
        } ?: false
        return hasStringParam || hasStringReturn
    }

    private fun collectHostImports(functions: List<ResolvedFunction>): List<String> {
        val imports = mutableListOf<String>()

        for (fn in functions) {
            // Host-provided params → getter imports
            for (param in fn.params.filter { it.isHostProvided }) {
                val wasmRetType = TypeMapping.rustToWasmC(param.rustType)
                imports.add("fn get_${param.name}() -> $wasmRetType;")
            }

            // Callback params → callback imports
            for (param in fn.params.filter { it.isCallback }) {
                val callbackTypes = param.callbackParamTypes ?: continue
                val wasmParamsIndexed = callbackTypes.mapIndexed { i, type ->
                    "arg$i: ${TypeMapping.rustToWasmC(type)}"
                }.joinToString(", ")
                imports.add("fn ${param.name}($wasmParamsIndexed);")
            }

            // Return via host callback → callback import
            fn.returnConfig?.let { rc ->
                val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) } ?: emptyList()
                val stringTypes = returnTypes.filter { it == "String" || it == "&str" }
                if (stringTypes.isNotEmpty()) {
                    // String return → ptr + len
                    imports.add("fn ${rc.hostCallbackName}(ptr: i32, len: i32);")
                }
            }
        }

        return imports.distinct()
    }

    private fun generateRustExportFunction(coreIdent: String, fn: ResolvedFunction): String {
        return buildString {
            // Build export params (exclude host-provided and callbacks)
            val exportParams = mutableListOf<String>()
            for (param in fn.params) {
                if (param.isHostProvided || param.isCallback) continue
                if (TypeMapping.isStringType(param.rustType)) {
                    exportParams.add("${param.name}_ptr: i32")
                    exportParams.add("${param.name}_len: i32")
                } else {
                    exportParams.add("${param.name}: ${TypeMapping.rustToWasmC(param.rustType)}")
                }
            }

            // Determine if we can return a simple numeric type directly
            val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) }
            val hasSimpleReturn = fn.returnConfig == null
                && returnTypes != null && returnTypes.size == 1
                && TypeMapping.isSimpleNumericType(returnTypes[0])
            val wasmReturnType = if (hasSimpleReturn) " -> ${TypeMapping.rustToWasmC(returnTypes!![0])}" else ""

            appendLine("#[no_mangle]")
            appendLine("pub extern \"C\" fn ${fn.name}(${exportParams.joinToString(", ")})$wasmReturnType {")

            // Unmarshal string params
            for (param in fn.params.filter { TypeMapping.isStringType(it.rustType) && !it.isHostProvided }) {
                appendLine("    let ${param.name} = unsafe {")
                appendLine("        let slice = std::slice::from_raw_parts(${param.name}_ptr as *const u8, ${param.name}_len as usize);")
                appendLine("        std::str::from_utf8_unchecked(slice)")
                appendLine("    };")
            }

            // Get host-provided params
            for (param in fn.params.filter { it.isHostProvided }) {
                appendLine("    let ${param.name} = unsafe { get_${param.name}() };")
            }

            // Build core function call (with type casts where WASM C type differs from Rust core type)
            val coreArgs = fn.params.joinToString(", ") { param ->
                when {
                    param.isCallback -> {
                        val callbackTypes = param.callbackParamTypes ?: emptyList()
                        val callbackArgs = callbackTypes.mapIndexed { i, _ -> "arg$i" }.joinToString(", ")
                        val importCall = callbackTypes.mapIndexed { i, _ -> "arg$i" }.joinToString(", ")
                        "&mut |$callbackArgs| {\n        unsafe { ${param.name}($importCall); }\n    }"
                    }
                    else -> {
                        val wasmType = TypeMapping.rustToWasmC(param.rustType)
                        val rustType = param.rustType.trim()
                        // Cast if the WASM C type differs from the original Rust type
                        if (wasmType != rustType) "${param.name} as $rustType" else param.name
                    }
                }
            }

            // Handle return
            if (hasSimpleReturn) {
                // Simple numeric return — return directly
                appendLine("    ${coreIdent}::${fn.name}($coreArgs) as ${TypeMapping.rustToWasmC(returnTypes!![0])}")
            } else if (returnTypes != null && returnTypes.size > 1) {
                val resultVars = returnTypes.mapIndexed { i, _ -> "_result_$i" }.joinToString(", ")
                appendLine("    let ($resultVars) = ${coreIdent}::${fn.name}($coreArgs);")

                // Deliver return via host callback
                fn.returnConfig?.let { rc ->
                    for ((i, type) in returnTypes.withIndex()) {
                        if (type == "String" || type == "&str") {
                            appendLine("    let bytes = _result_$i.as_bytes();")
                            appendLine("    let msg_ptr = alloc(bytes.len() as i32);")
                            appendLine("    unsafe {")
                            appendLine("        std::ptr::copy_nonoverlapping(bytes.as_ptr(), msg_ptr as *mut u8, bytes.len());")
                            appendLine("        ${rc.hostCallbackName}(msg_ptr, bytes.len() as i32);")
                            appendLine("    }")
                            appendLine("    dealloc(msg_ptr, bytes.len() as i32);")
                        }
                    }
                }
            } else if (returnTypes != null && returnTypes.size == 1) {
                appendLine("    let _result_0 = ${coreIdent}::${fn.name}($coreArgs);")

                fn.returnConfig?.let { rc ->
                    val type = returnTypes[0]
                    if (type == "String" || type == "&str") {
                        appendLine("    let bytes = _result_0.as_bytes();")
                        appendLine("    let msg_ptr = alloc(bytes.len() as i32);")
                        appendLine("    unsafe {")
                        appendLine("        std::ptr::copy_nonoverlapping(bytes.as_ptr(), msg_ptr as *mut u8, bytes.len());")
                        appendLine("        ${rc.hostCallbackName}(msg_ptr, bytes.len() as i32);")
                        appendLine("    }")
                        appendLine("    dealloc(msg_ptr, bytes.len() as i32);")
                    }
                }
            } else {
                appendLine("    ${coreIdent}::${fn.name}($coreArgs);")
            }

            appendLine("}")
        }
    }

    // =========================================================================
    // Kotlin — required imports for Chicory standalone
    // =========================================================================

    private val REQUIRED_IMPORTS = listOf(
        "android.content.Context",
        "android.os.Handler",
        "android.os.Looper",
        "android.widget.Toast",
        "com.dylibso.chicory.runtime.HostFunction",
        "com.dylibso.chicory.runtime.Instance",
        "com.dylibso.chicory.runtime.Store",
        "com.dylibso.chicory.wasm.Parser",
        "com.dylibso.chicory.wasm.types.ValueType"
    )

    /**
     * Generates the imports block with markers.
     * If fileContent is provided, imports that already exist outside the markers
     * are commented out to avoid duplicates.
     */
    fun generateImportsBlock(fileContent: String = ""): String {
        // Strip existing markers to only check user imports
        val contentWithoutMarkers = removeRustWasmImportsBlock(fileContent)
        return buildString {
            appendLine("//<RustWasm-imports>")
            for (imp in REQUIRED_IMPORTS) {
                if (contentWithoutMarkers.contains("import $imp")) {
                    appendLine("// import $imp // already imported")
                } else {
                    appendLine("import $imp")
                }
            }
            append("//</RustWasm-imports>")
        }
    }

    // =========================================================================
    // Kotlin — Full file generation (when file does NOT exist)
    // =========================================================================

    fun generateKotlinWasmLib(
        packageName: String,
        className: String,
        wasmFileName: String,
        moduleName: String,
        functions: List<ResolvedFunction>
    ): String {
        val injectionBlock = generateKotlinInjectionBlock(wasmFileName, moduleName, functions)

        return buildString {
            appendLine("package $packageName")
            appendLine()
            appendLine(generateImportsBlock())
            appendLine()
            appendLine("object $className {")
            appendLine()
            append(indentBlock(injectionBlock, "    "))
            appendLine()
            appendLine("}")
        }
    }

    // =========================================================================
    // Kotlin — Injection block (inner content with markers)
    // =========================================================================

    fun generateKotlinInjectionBlock(
        wasmFileName: String,
        moduleName: String,
        functions: List<ResolvedFunction>
    ): String {
        return buildString {
            appendLine("//<RustWasm>")
            appendLine("// auto-generated code")
            appendLine()
            appendLine("private lateinit var appContext: Context")
            appendLine("private lateinit var instance: Instance")
            appendLine("private val handler = Handler(Looper.getMainLooper())")
            appendLine()

            // Listener fields for each function's callbacks
            for (fn in functions) {
                for (param in fn.params.filter { it.isCallback }) {
                    val listenerName = TypeMapping.snakeToPascal(param.name) + "Listener"
                    appendLine("private var current$listenerName: $listenerName? = null")
                }
            }
            appendLine()

            // setup method
            appendLine("fun setupWasm(context: Context) {")
            appendLine("    appContext = context.applicationContext")
            appendLine("    val wasmBytes = context.assets.open(\"$wasmFileName\").use { it.readBytes() }")
            appendLine("    val module = Parser.parse(wasmBytes)")
            appendLine("    val store = Store()")
            appendLine()

            // Register host functions
            for (fn in functions) {
                // Host-provided params
                for (param in fn.params.filter { it.isHostProvided }) {
                    val valueType = TypeMapping.rustToChicoryValueType(param.rustType)
                    val provider = param.hostProvider?.takeIf { it.isNotEmpty() }
                        ?: "System.currentTimeMillis()"
                    appendLine("    store.addFunction(HostFunction(\"env\", \"get_${param.name}\", listOf(), listOf($valueType)) { _, _ ->")
                    appendLine("        longArrayOf($provider)")
                    appendLine("    })")
                    appendLine()
                }

                // Callback params
                for (param in fn.params.filter { it.isCallback }) {
                    val callbackTypes = param.callbackParamTypes ?: continue
                    val wasmParamTypes = callbackTypes.joinToString(", ") { TypeMapping.rustToChicoryValueType(it) }
                    val listenerName = TypeMapping.snakeToPascal(param.name) + "Listener"
                    val methodName = TypeMapping.snakeToCamel(param.name)

                    appendLine("    store.addFunction(HostFunction(\"env\", \"${param.name}\", listOf($wasmParamTypes), listOf()) { _, args ->")
                    if (callbackTypes.size == 1) {
                        appendLine("        current$listenerName?.${methodName}(args[0])")
                    } else {
                        val argsList = callbackTypes.mapIndexed { i, _ -> "args[$i]" }.joinToString(", ")
                        appendLine("        current$listenerName?.${methodName}($argsList)")
                    }
                    appendLine("        null")
                    appendLine("    })")
                    appendLine()
                }

                // Return via host callback
                fn.returnConfig?.let { rc ->
                    val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) } ?: emptyList()
                    val hasString = returnTypes.any { it == "String" || it == "&str" }
                    if (hasString) {
                        appendLine("    store.addFunction(HostFunction(\"env\", \"${rc.hostCallbackName}\", listOf(ValueType.I32, ValueType.I32), listOf()) { inst, args ->")
                        appendLine("        val ptr = args[0].toInt()")
                        appendLine("        val len = args[1].toInt()")
                        appendLine("        val bytes = inst.memory().readBytes(ptr, len)")
                        appendLine("        val message = String(bytes, Charsets.UTF_8)")
                        appendLine("        handler.post {")
                        appendLine("            Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()")
                        appendLine("        }")
                        appendLine("        null")
                        appendLine("    })")
                        appendLine()
                    }
                }
            }

            appendLine("    instance = store.instantiate(\"$moduleName\", module)")
            appendLine("}")
            appendLine()

            // Public methods
            for (fn in functions) {
                append(generateKotlinMethod(fn))
            }

            // Listener interfaces
            for (fn in functions) {
                for (param in fn.params.filter { it.isCallback }) {
                    val callbackTypes = param.callbackParamTypes ?: continue
                    val listenerName = TypeMapping.snakeToPascal(param.name) + "Listener"
                    val methodName = TypeMapping.snakeToCamel(param.name)
                    val kotlinParams = callbackTypes.mapIndexed { i, type ->
                        "arg$i: ${TypeMapping.rustToKotlin(type)}"
                    }.joinToString(", ")

                    appendLine("interface $listenerName {")
                    appendLine("    fun $methodName($kotlinParams)")
                    appendLine("}")
                    appendLine()
                }
            }

            appendLine("//</RustWasm>")
        }
    }

    // =========================================================================
    // Kotlin — Injection into existing file
    // =========================================================================

    /**
     * Injects the generated block into an existing Kotlin file.
     * - If //<RustWasm> ... //</RustWasm> markers exist: replaces the block.
     * - If no markers exist: inserts after the class/object opening brace.
     */
    fun injectIntoKotlinFile(fileContent: String, className: String, injectionBlock: String): String {
        // First, remove any existing RustWasm block
        val cleanedContent = removeRustWasmBlock(fileContent)

        // Find the class/object declaration
        val classPattern = Regex("(class|object|public\\s+class|final\\s+class|open\\s+class)\\s+$className\\b[^\\{]*\\{")
        val matchResult = classPattern.find(cleanedContent)
            ?: throw org.gradle.api.GradleException("Could not find class/object definition for '$className' in target file")

        val insertionPoint = matchResult.range.last + 1

        // Detect indentation from the class declaration
        val linesBeforeInsertion = cleanedContent.substring(0, insertionPoint).lines()
        val classLine = linesBeforeInsertion.last()
        val classIndent = classLine.takeWhile { it == ' ' || it == '\t' }
        val memberIndent = classIndent + "    "

        // Indent the injection block to match class member indentation
        val indentedBlock = indentBlock(injectionBlock, memberIndent)

        val beforeInsertion = cleanedContent.substring(0, insertionPoint)
        val afterInsertion = cleanedContent.substring(insertionPoint)

        return buildString {
            append(beforeInsertion)
            if (!beforeInsertion.endsWith("\n")) append("\n")
            append("\n")
            append(indentedBlock)
            if (!afterInsertion.startsWith("\n")) append("\n")
            append(afterInsertion)
        }
    }

    /**
     * Removes the //<RustWasm-imports> ... //</RustWasm-imports> block from file content.
     */
    fun removeRustWasmImportsBlock(fileContent: String): String {
        val pattern = Regex(
            pattern = "(?s)(?:\\r?\\n)?[ \\t]*//<RustWasm-imports>.*?//</RustWasm-imports>[ \\t]*(?:\\r?\\n)?",
            options = setOf(RegexOption.MULTILINE)
        )
        return pattern.replace(fileContent, "\n")
    }

    /**
     * Injects the imports block after the last existing import line,
     * or after the package declaration if no imports exist.
     * Also removes any loose (non-marked) imports that match REQUIRED_IMPORTS.
     */
    fun injectImportsBlock(fileContent: String, importsBlock: String): String {
        // Remove any loose imports that match our required imports (legacy cleanup)
        var cleaned = fileContent
        for (imp in REQUIRED_IMPORTS) {
            cleaned = cleaned.replace(Regex("^import\\s+${Regex.escape(imp)}\\s*\\r?\\n", RegexOption.MULTILINE), "")
        }

        // Find the last import line to insert after it
        val importPattern = Regex("^import\\s+.+$", RegexOption.MULTILINE)
        val lastImportMatch = importPattern.findAll(cleaned).lastOrNull()

        return if (lastImportMatch != null) {
            val insertPos = lastImportMatch.range.last + 1
            cleaned.substring(0, insertPos) + "\n" + importsBlock + cleaned.substring(insertPos)
        } else {
            // No imports found — insert after package declaration
            val packagePattern = Regex("^package\\s+.+$", RegexOption.MULTILINE)
            val packageMatch = packagePattern.find(cleaned)
            if (packageMatch != null) {
                val insertPos = packageMatch.range.last + 1
                cleaned.substring(0, insertPos) + "\n\n" + importsBlock + cleaned.substring(insertPos)
            } else {
                importsBlock + "\n\n" + cleaned
            }
        }
    }

    /**
     * Removes the //<RustWasm> ... //</RustWasm> block from file content.
     */
    fun removeRustWasmBlock(fileContent: String): String {
        val rustWasmBlockPattern = Regex(
            pattern = "(?s)(?:\\r?\\n)?[ \\t]*//<RustWasm>.*?//</RustWasm>[ \\t]*(?:\\r?\\n)?",
            options = setOf(RegexOption.MULTILINE)
        )
        return rustWasmBlockPattern.replace(fileContent, "\n")
    }

    /**
     * Indents every non-empty line in `block` with the given `indent` string.
     */
    private fun indentBlock(block: String, indent: String): String {
        return block.lines().joinToString("\n") { line ->
            if (line.isNotBlank()) indent + line else line
        }
    }

    private fun generateKotlinMethod(fn: ResolvedFunction): String {
        return buildString {
            // Method params: exclude host-provided, convert callbacks to listeners
            val methodParams = mutableListOf<String>()
            for (param in fn.params) {
                if (param.isHostProvided) continue
                if (param.isCallback) {
                    val listenerName = TypeMapping.snakeToPascal(param.name) + "Listener"
                    methodParams.add("callback: $listenerName")
                } else {
                    val kotlinType = TypeMapping.rustToKotlin(param.rustType)
                    methodParams.add("${TypeMapping.snakeToCamel(param.name)}: $kotlinType")
                }
            }

            val methodName = TypeMapping.snakeToCamel(fn.name)
            val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) }
            val hasSimpleReturn = fn.returnConfig == null
                && returnTypes != null && returnTypes.size == 1
                && TypeMapping.isSimpleNumericType(returnTypes[0])
            val kotlinReturnType = if (hasSimpleReturn) ": ${TypeMapping.rustToKotlin(returnTypes!![0])}" else ""
            appendLine("fun $methodName(${methodParams.joinToString(", ")})$kotlinReturnType {")

            // Set listener fields
            for (param in fn.params.filter { it.isCallback }) {
                val listenerName = TypeMapping.snakeToPascal(param.name) + "Listener"
                appendLine("    current$listenerName = callback")
            }

            // Prepare string params (alloc + write)
            val stringParams = fn.params.filter { TypeMapping.isStringType(it.rustType) && !it.isHostProvided }
            for (param in stringParams) {
                val camelName = TypeMapping.snakeToCamel(param.name)
                appendLine("    val ${camelName}Bytes = $camelName.toByteArray(Charsets.UTF_8)")
                appendLine("    val ${camelName}Ptr = instance.export(\"alloc\").apply(${camelName}Bytes.size.toLong())[0]")
                appendLine("    instance.memory().write(${camelName}Ptr.toInt(), ${camelName}Bytes)")
            }

            // Build WASM export call args
            val callArgs = mutableListOf<String>()
            for (param in fn.params) {
                if (param.isHostProvided || param.isCallback) continue
                if (TypeMapping.isStringType(param.rustType)) {
                    val camelName = TypeMapping.snakeToCamel(param.name)
                    callArgs.add("${camelName}Ptr")
                    callArgs.add("${camelName}Bytes.size.toLong()")
                } else {
                    val camelName = TypeMapping.snakeToCamel(param.name)
                    callArgs.add(TypeMapping.kotlinParamToLong(camelName, param.rustType))
                }
            }

            if (hasSimpleReturn) {
                appendLine("    val _rawResult = instance.export(\"${fn.name}\").apply(${callArgs.joinToString(", ")})[0]")
            } else {
                appendLine("    instance.export(\"${fn.name}\").apply(${callArgs.joinToString(", ")})")
            }

            // Dealloc string params
            for (param in stringParams) {
                val camelName = TypeMapping.snakeToCamel(param.name)
                appendLine("    instance.export(\"dealloc\").apply(${camelName}Ptr, ${camelName}Bytes.size.toLong())")
            }

            // Clear listener fields
            for (param in fn.params.filter { it.isCallback }) {
                val listenerName = TypeMapping.snakeToPascal(param.name) + "Listener"
                appendLine("    current$listenerName = null")
            }

            if (hasSimpleReturn) {
                val returnExpr = when (returnTypes!![0].trim()) {
                    "f32" -> "java.lang.Float.intBitsToFloat(_rawResult.toInt())"
                    "f64" -> "java.lang.Double.longBitsToDouble(_rawResult)"
                    "i64", "u64" -> "_rawResult"
                    "bool" -> "_rawResult.toInt() != 0"
                    else -> "_rawResult.toInt()" // i32, u32, u8, u16
                }
                appendLine("    return $returnExpr")
            }

            appendLine("}")
            appendLine()
        }
    }
}
