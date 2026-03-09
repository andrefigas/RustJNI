package io.github.andrefigas.rustjni.wasm.reflection

import io.github.andrefigas.rustjni.wasm.TypeMapping
import io.github.andrefigas.rustjni.wasm.model.ResolvedFunction

/**
 * Generates Rust wasm-bindgen bridge code, bridge.js, and Kotlin WebView injection
 * for the browser mode.
 */
object BrowserBridgeGenerator {

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
            appendLine("wasm-bindgen = \"0.2\"")
            appendLine("js-sys = \"0.3\"")
            appendLine()
            appendLine("[profile.release]")
            appendLine("opt-level = \"s\"")
            appendLine("lto = true")
        }
    }

    // =========================================================================
    // Rust src/lib.rs (wasm-bindgen)
    // =========================================================================

    fun generateRustLib(coreCrateName: String, functions: List<ResolvedFunction>): String {
        val coreIdent = coreCrateName.replace("-", "_")

        return buildString {
            appendLine("use wasm_bindgen::prelude::*;")
            appendLine()

            // Collect JS imports from bridge.js
            val jsImports = collectJsImports(functions)
            if (jsImports.isNotEmpty()) {
                appendLine("#[wasm_bindgen(raw_module = \"../bridge.js\")]")
                appendLine("extern \"C\" {")
                for (import in jsImports) {
                    appendLine("    $import")
                }
                appendLine("}")
                appendLine()
            }

            // Generate each exported function
            for (fn in functions) {
                appendLine(generateRustExportFunction(coreIdent, fn))
            }
        }
    }

    private fun collectJsImports(functions: List<ResolvedFunction>): List<String> {
        val imports = mutableListOf<String>()

        for (fn in functions) {
            // Callback params → JS imports
            for (param in fn.params.filter { it.isCallback }) {
                val callbackTypes = param.callbackParamTypes ?: continue
                val params = callbackTypes.mapIndexed { i, type ->
                    "arg$i: ${rustTypeToWasmBindgen(type)}"
                }.joinToString(", ")
                imports.add("fn ${param.name}($params);")
            }

            // Return via host callback → JS import
            fn.returnConfig?.let { rc ->
                val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) } ?: emptyList()
                val hasString = returnTypes.any { it == "String" || it == "&str" }
                if (hasString) {
                    imports.add("fn ${rc.hostCallbackName}(message: &str);")
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
                    exportParams.add("${param.name}: &str")
                } else {
                    exportParams.add("${param.name}: ${param.rustType}")
                }
            }

            // Determine if we can return a simple numeric type directly
            val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) }
            val hasSimpleReturn = fn.returnConfig == null
                && returnTypes != null && returnTypes.size == 1
                && TypeMapping.isSimpleNumericType(returnTypes[0])
            val wasmReturnType = if (hasSimpleReturn) " -> ${rustTypeToWasmBindgen(returnTypes!![0])}" else ""

            appendLine("#[wasm_bindgen]")
            appendLine("pub fn ${fn.name}(${exportParams.joinToString(", ")})$wasmReturnType {")

            // Get host-provided params (use js_sys for timestamp)
            for (param in fn.params.filter { it.isHostProvided }) {
                if (param.rustType == "i64") {
                    appendLine("    let ${param.name} = js_sys::Date::now() as i64;")
                } else {
                    appendLine("    let ${param.name} = 0 as ${param.rustType}; // TODO: provide value")
                }
            }

            // Build core function call
            val coreArgs = fn.params.joinToString(", ") { param ->
                when {
                    param.isCallback -> {
                        val callbackTypes = param.callbackParamTypes ?: emptyList()
                        val callbackArgs = callbackTypes.mapIndexed { i, _ -> "arg$i" }.joinToString(", ")
                        val castArgs = callbackTypes.mapIndexed { i, type ->
                            val wasmType = rustTypeToWasmBindgen(type)
                            if (wasmType != type) "arg$i as $wasmType" else "arg$i"
                        }.joinToString(", ")
                        "&mut |$callbackArgs| {\n        ${param.name}($castArgs);\n    }"
                    }
                    else -> param.name
                }
            }

            // Handle return
            if (hasSimpleReturn) {
                // Simple numeric return — return directly
                val cast = if (returnTypes!![0].trim() == "i64") " as f64" else ""
                appendLine("    ${coreIdent}::${fn.name}($coreArgs)$cast")
            } else if (returnTypes != null && returnTypes.size > 1) {
                val resultVars = returnTypes.mapIndexed { i, _ -> "_result_$i" }.joinToString(", ")
                appendLine("    let ($resultVars) = ${coreIdent}::${fn.name}($coreArgs);")

                fn.returnConfig?.let { rc ->
                    for ((i, type) in returnTypes.withIndex()) {
                        if (type == "String" || type == "&str") {
                            appendLine("    ${rc.hostCallbackName}(&_result_$i);")
                        }
                    }
                }
            } else if (returnTypes != null && returnTypes.size == 1) {
                appendLine("    let _result_0 = ${coreIdent}::${fn.name}($coreArgs);")

                fn.returnConfig?.let { rc ->
                    val type = returnTypes[0]
                    if (type == "String" || type == "&str") {
                        appendLine("    ${rc.hostCallbackName}(&_result_0);")
                    }
                }
            } else {
                appendLine("    ${coreIdent}::${fn.name}($coreArgs);")
            }

            appendLine("}")
        }
    }

    private fun rustTypeToWasmBindgen(rustType: String): String = when (rustType.trim()) {
        "i32" -> "i32"
        "i64" -> "f64" // wasm-bindgen uses f64 for i64 by default
        "f32" -> "f32"
        "f64" -> "f64"
        "bool" -> "bool"
        "&str", "String" -> "&str"
        else -> rustType
    }

    // =========================================================================
    // bridge.js
    // =========================================================================

    fun generateBridgeJs(functions: List<ResolvedFunction>): String {
        return buildString {
            appendLine("// Auto-generated by RustWasm plugin")
            appendLine("// Bridge between wasm-bindgen exports and Android WebView")
            appendLine()

            for (fn in functions) {
                // Callback params → JS functions that call Android interface
                for (param in fn.params.filter { it.isCallback }) {
                    val callbackTypes = param.callbackParamTypes ?: continue
                    val jsParams = callbackTypes.mapIndexed { i, _ -> "arg$i" }.joinToString(", ")
                    val androidMethod = TypeMapping.snakeToCamel(param.name)
                    val androidArgs = callbackTypes.mapIndexed { i, type ->
                        if (type == "i64") "Number(arg$i)" else "arg$i"
                    }.joinToString(", ")

                    appendLine("export function ${param.name}($jsParams) {")
                    appendLine("    window.Android.${androidMethod}($androidArgs);")
                    appendLine("}")
                    appendLine()
                }

                // Return via host callback → JS function
                fn.returnConfig?.let { rc ->
                    val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) } ?: emptyList()
                    val hasString = returnTypes.any { it == "String" || it == "&str" }
                    if (hasString) {
                        val androidMethod = TypeMapping.snakeToCamel(rc.hostCallbackName)
                        appendLine("export function ${rc.hostCallbackName}(message) {")
                        appendLine("    window.Android.${androidMethod}(message);")
                        appendLine("}")
                        appendLine()
                    }
                }
            }
        }
    }

    // =========================================================================
    // index.js (public API + documentation)
    // =========================================================================

    fun generateIndexJs(bridgeCrateName: String, functions: List<ResolvedFunction>): String {
        val moduleFile = bridgeCrateName.replace("-", "_")

        return buildString {
            appendLine("// Auto-generated by RustWasm plugin — DO NOT EDIT")
            appendLine("// Public API for the WASM bridge module.")
            appendLine("//")
            appendLine("// Usage:")
            appendLine("//   import { init, ${functions.joinToString(", ") { TypeMapping.snakeToCamel(it.name) }} } from './wasm-bridge/index.js';")
            appendLine("//   await init();")
            for (fn in functions) {
                val camelName = TypeMapping.snakeToCamel(fn.name)
                val exampleArgs = fn.params
                    .filter { !it.isHostProvided && !it.isCallback }
                    .joinToString(", ") { param ->
                        when {
                            TypeMapping.isStringType(param.rustType) -> "\"...\""
                            else -> "0"
                        }
                    }
                appendLine("//   $camelName($exampleArgs);")
            }
            appendLine()

            appendLine("import wasmInit, {")
            appendLine("    ${functions.joinToString(",\n    ") { it.name }}")
            appendLine("} from './pkg/$moduleFile.js';")
            appendLine()

            // init()
            appendLine("/**")
            appendLine(" * Initialize the WASM module. Must be called once before any other function.")
            appendLine(" */")
            appendLine("export async function init() {")
            appendLine("    await wasmInit();")
            appendLine("}")
            appendLine()

            // Wrapper functions with camelCase + JSDoc
            for (fn in functions) {
                val camelName = TypeMapping.snakeToCamel(fn.name)
                val exportParams = fn.params.filter { !it.isHostProvided && !it.isCallback }

                appendLine("/**")
                for (param in exportParams) {
                    val jsType = rustTypeToJsDoc(param.rustType)
                    val camelParam = TypeMapping.snakeToCamel(param.name)
                    appendLine(" * @param {$jsType} $camelParam")
                }
                appendLine(" */")

                val jsParams = exportParams.joinToString(", ") { TypeMapping.snakeToCamel(it.name) }
                val snakeArgs = exportParams.joinToString(", ") { TypeMapping.snakeToCamel(it.name) }

                val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) }
                val hasReturn = fn.returnConfig == null
                    && returnTypes != null && returnTypes.size == 1
                    && TypeMapping.isSimpleNumericType(returnTypes[0])
                val returnKeyword = if (hasReturn) "return " else ""

                appendLine("export function $camelName($jsParams) {")
                appendLine("    ${returnKeyword}${fn.name}($snakeArgs);")
                appendLine("}")
                appendLine()
            }
        }
    }

    private fun rustTypeToJsDoc(rustType: String): String = when (rustType.trim()) {
        "i32", "i64", "f32", "f64", "u32", "u64" -> "number"
        "bool" -> "boolean"
        "&str", "String" -> "string"
        else -> "any"
    }

    // =========================================================================
    // Kotlin — required imports for WebView browser mode
    // =========================================================================

    private val REQUIRED_IMPORTS = listOf(
        "android.os.Handler",
        "android.os.Looper",
        "android.webkit.JavascriptInterface",
        "android.webkit.WebResourceRequest",
        "android.webkit.WebView",
        "android.webkit.WebViewClient",
        "android.widget.Toast",
        "androidx.webkit.WebViewAssetLoader"
    )

    /**
     * Generates the imports block with markers.
     * If fileContent is provided, imports that already exist outside the markers
     * are commented out to avoid duplicates.
     */
    fun generateImportsBlock(fileContent: String = ""): String {
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

    fun removeRustWasmImportsBlock(fileContent: String): String {
        val pattern = Regex(
            pattern = "(?s)(?:\\r?\\n)?[ \\t]*//<RustWasm-imports>.*?//</RustWasm-imports>[ \\t]*(?:\\r?\\n)?",
            options = setOf(RegexOption.MULTILINE)
        )
        return pattern.replace(fileContent, "\n")
    }

    fun injectImportsBlock(fileContent: String, importsBlock: String): String {
        // Remove any loose imports that match our required imports (legacy cleanup)
        var cleaned = fileContent
        for (imp in REQUIRED_IMPORTS) {
            cleaned = cleaned.replace(Regex("^import\\s+${Regex.escape(imp)}\\s*\\r?\\n", RegexOption.MULTILINE), "")
        }

        val importPattern = Regex("^import\\s+.+$", RegexOption.MULTILINE)
        val lastImportMatch = importPattern.findAll(cleaned).lastOrNull()

        return if (lastImportMatch != null) {
            val insertPos = lastImportMatch.range.last + 1
            cleaned.substring(0, insertPos) + "\n" + importsBlock + cleaned.substring(insertPos)
        } else {
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

    // =========================================================================
    // Kotlin WebView method injection
    // =========================================================================

    fun generateWebViewMethod(
        hostClassName: String,
        assetsPath: String,
        htmlFileName: String,
        functions: List<ResolvedFunction>
    ): String {
        return buildString {
            appendLine("//<RustWasm>")
            appendLine("fun createWasmWebView(): WebView {")
            appendLine("    val assetLoader = WebViewAssetLoader.Builder()")
            appendLine("        .addPathHandler(\"/assets/\", WebViewAssetLoader.AssetsPathHandler(this))")
            appendLine("        .build()")
            appendLine()
            appendLine("    return WebView(this).apply {")
            appendLine("        settings.javaScriptEnabled = true")
            appendLine("        settings.domStorageEnabled = true")
            appendLine("        addJavascriptInterface(object {")

            // Generate @JavascriptInterface methods
            for (fn in functions) {
                // Callback params → @JavascriptInterface methods
                for (param in fn.params.filter { it.isCallback }) {
                    val callbackTypes = param.callbackParamTypes ?: continue
                    val methodName = TypeMapping.snakeToCamel(param.name)
                    val kotlinParams = callbackTypes.mapIndexed { i, type ->
                        "arg$i: ${TypeMapping.rustToKotlin(type)}"
                    }.joinToString(", ")

                    appendLine("            @JavascriptInterface")
                    appendLine("            fun $methodName($kotlinParams) {")
                    appendLine("                // Callback: ${param.name}")
                    appendLine("            }")
                    appendLine()
                }

                // Return via host callback → @JavascriptInterface method
                fn.returnConfig?.let { rc ->
                    val returnTypes = fn.returnType?.let { TypeMapping.parseReturnTypes(it) } ?: emptyList()
                    val hasString = returnTypes.any { it == "String" || it == "&str" }
                    if (hasString) {
                        val methodName = TypeMapping.snakeToCamel(rc.hostCallbackName)
                        appendLine("            @JavascriptInterface")
                        appendLine("            fun $methodName(message: String) {")
                        appendLine("                Handler(Looper.getMainLooper()).post {")
                        appendLine("                    Toast.makeText(this@$hostClassName, message, Toast.LENGTH_LONG).show()")
                        appendLine("                }")
                        appendLine("            }")
                        appendLine()
                    }
                }
            }

            appendLine("        }, \"Android\")")
            appendLine("        webViewClient = object : WebViewClient() {")
            appendLine("            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest) =")
            appendLine("                assetLoader.shouldInterceptRequest(request.url)")
            appendLine("        }")
            appendLine("        loadUrl(\"https://appassets.androidplatform.net/assets/$assetsPath/$htmlFileName\")")
            appendLine("    }")
            appendLine("}")
            appendLine("//</RustWasm>")
        }
    }

    /**
     * Injects the createWasmWebView() method into an existing Kotlin class file.
     * Replaces content between //<RustWasm> and //</RustWasm> markers if they exist,
     * or inserts before the last closing brace.
     */
    fun injectIntoKotlinFile(fileContent: String, generatedMethod: String): String {
        val startMarker = "//<RustWasm>"
        val endMarker = "//</RustWasm>"

        val startIdx = fileContent.indexOf(startMarker)
        val endIdx = fileContent.indexOf(endMarker)

        return if (startIdx >= 0 && endIdx >= 0) {
            // Replace existing block
            fileContent.substring(0, startIdx) +
                generatedMethod +
                fileContent.substring(endIdx + endMarker.length)
        } else {
            // Insert before last closing brace
            val lastBrace = fileContent.lastIndexOf('}')
            if (lastBrace >= 0) {
                fileContent.substring(0, lastBrace) +
                    "\n    " + generatedMethod.lines().joinToString("\n    ") + "\n" +
                    fileContent.substring(lastBrace)
            } else {
                fileContent + "\n" + generatedMethod
            }
        }
    }
}
