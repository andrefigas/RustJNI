package io.github.andrefigas.rustjni.wasm.reflection

import io.github.andrefigas.rustjni.wasm.TypeMapping
import io.github.andrefigas.rustjni.wasm.model.CoreFunctionSignature
import io.github.andrefigas.rustjni.wasm.model.CoreParam
import io.github.andrefigas.rustjni.wasm.model.FunctionConfig
import io.github.andrefigas.rustjni.wasm.model.ResolvedFunction
import io.github.andrefigas.rustjni.wasm.model.ResolvedParam
import io.github.andrefigas.rustjni.wasm.model.WasmAnnotation

object RustCoreParser {

    /**
     * Parses all `pub fn` declarations from a Rust core lib.rs source,
     * including `/// @wasm:` annotations from doc comments.
     *
     * Supported annotations (placed in `///` comments above `pub fn`):
     * - `/// @wasm:host_provided(param_name)` — marks a parameter as host-provided
     * - `/// @wasm:host_provided(param_name, "provider expression")` — with explicit provider
     * - `/// @wasm:returns(callback_name)` — return value delivered via host callback
     */
    fun parse(source: String): List<CoreFunctionSignature> {
        val functions = mutableListOf<CoreFunctionSignature>()
        val fnRegex = Regex("""pub\s+fn\s+(\w+)\s*\(""")

        for (match in fnRegex.findAll(source)) {
            val fnName = match.groupValues[1]
            val openParenIdx = match.range.last // index of '('

            // Skip functions inside impl/struct/enum blocks (brace depth > 0)
            if (braceDepthAt(source, match.range.first) > 0) continue

            // Collect doc comments above this pub fn
            val annotations = parseAnnotations(source, match.range.first)

            val paramsEnd = findMatchingParen(source, openParenIdx)
            if (paramsEnd < 0) continue

            val paramsStr = source.substring(openParenIdx + 1, paramsEnd)
            val params = parseParams(paramsStr)

            // Find return type between ')' and '{'
            val afterParams = source.substring(paramsEnd + 1)
            val braceIdx = afterParams.indexOf('{')
            val returnType = if (braceIdx >= 0) {
                val segment = afterParams.substring(0, braceIdx).trim()
                if (segment.startsWith("->")) {
                    segment.substring(2).trim().ifEmpty { null }
                } else null
            } else null

            functions.add(CoreFunctionSignature(fnName, params, returnType, annotations))
        }

        return functions
    }

    /**
     * Merges parsed core functions with DSL function configs to produce resolved functions.
     * Priority: DSL config overrides annotations; annotations serve as defaults.
     */
    fun resolve(
        coreFunctions: List<CoreFunctionSignature>,
        configs: Map<String, FunctionConfig>
    ): List<ResolvedFunction> {
        return coreFunctions.map { fn ->
            val config = configs[fn.name]
            val annotations = fn.annotations

            // DSL host-provided params override annotations
            val hostProvidedNames: Set<String>
            val hostProviders: Map<String, String>
            if (config != null && config.hostProvidedParams.isNotEmpty()) {
                // DSL takes priority
                hostProvidedNames = config.hostProvidedParams.map { it.paramName }.toSet()
                hostProviders = config.hostProvidedParams.associate { it.paramName to it.provider }
            } else {
                // Fall back to annotations
                hostProvidedNames = annotations.hostProvidedParams.keys
                hostProviders = annotations.hostProvidedParams
            }

            // DSL returnConfig overrides annotation returnsVia
            val returnConfig = config?.returnConfig
                ?: annotations.returnsVia?.let { io.github.andrefigas.rustjni.wasm.model.ReturnConfig(it) }

            val resolvedParams = fn.params.map { param ->
                val callbackTypes = TypeMapping.extractCallbackTypes(param.rustType)
                ResolvedParam(
                    name = param.name,
                    rustType = param.rustType,
                    isHostProvided = param.name in hostProvidedNames,
                    hostProvider = hostProviders[param.name],
                    isCallback = callbackTypes != null,
                    callbackParamTypes = callbackTypes
                )
            }

            ResolvedFunction(
                name = fn.name,
                params = resolvedParams,
                returnType = fn.returnType,
                returnConfig = returnConfig
            )
        }
    }

    /**
     * Walks backwards from the `pub fn` start position to collect `///` doc-comment lines,
     * then extracts `@wasm:` annotations from them.
     */
    private fun parseAnnotations(source: String, pubFnStart: Int): WasmAnnotation {
        // Collect doc-comment lines above `pub fn`
        val docLines = mutableListOf<String>()
        val lines = source.substring(0, pubFnStart).trimEnd().lines()

        // Walk backwards from the last line, collecting consecutive `///` lines
        for (i in lines.indices.reversed()) {
            val trimmed = lines[i].trim()
            if (trimmed.startsWith("///")) {
                // Extract content after `///`
                docLines.add(0, trimmed.removePrefix("///").trim())
            } else if (trimmed.isEmpty()) {
                // Skip blank lines between comments and pub fn
                continue
            } else {
                // Hit non-comment, non-blank line — stop
                break
            }
        }

        val hostProvided = mutableMapOf<String, String>()
        var returnsVia: String? = null

        val hostProvidedRegex = Regex("""@wasm:host_provided\(\s*(\w+)\s*(?:,\s*"([^"]*)")?\s*\)""")
        val returnsRegex = Regex("""@wasm:returns\(\s*(\w+)\s*\)""")

        for (line in docLines) {
            hostProvidedRegex.find(line)?.let { m ->
                val paramName = m.groupValues[1]
                val provider = m.groupValues[2] // empty string if not specified
                hostProvided[paramName] = provider
            }
            returnsRegex.find(line)?.let { m ->
                returnsVia = m.groupValues[1]
            }
        }

        return WasmAnnotation(hostProvided, returnsVia)
    }

    /**
     * Computes the brace `{}` nesting depth at a given position in the source.
     * Returns 0 for top-level (module scope), 1+ for inside impl/struct/etc blocks.
     */
    private fun braceDepthAt(source: String, position: Int): Int {
        var depth = 0
        for (i in 0 until position) {
            when (source[i]) {
                '{' -> depth++
                '}' -> depth--
            }
        }
        return depth
    }

    private fun findMatchingParen(text: String, openIdx: Int): Int {
        var depth = 1
        var i = openIdx + 1
        while (i < text.length && depth > 0) {
            when (text[i]) {
                '(' -> depth++
                ')' -> depth--
            }
            i++
        }
        return if (depth == 0) i - 1 else -1
    }

    private fun parseParams(paramsStr: String): List<CoreParam> {
        if (paramsStr.isBlank()) return emptyList()

        val params = mutableListOf<CoreParam>()
        var depth = 0
        val current = StringBuilder()

        for (c in paramsStr) {
            when {
                c in "(<" -> { depth++; current.append(c) }
                c in ")>" -> { depth--; current.append(c) }
                c == ',' && depth == 0 -> {
                    parseOneParam(current.toString())?.let { params.add(it) }
                    current.clear()
                }
                else -> current.append(c)
            }
        }
        parseOneParam(current.toString())?.let { params.add(it) }

        return params
    }

    private fun parseOneParam(raw: String): CoreParam? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val colonIdx = trimmed.indexOf(':')
        if (colonIdx < 0) return null
        val name = trimmed.substring(0, colonIdx).trim()
        val type = trimmed.substring(colonIdx + 1).trim()
        return CoreParam(name, type)
    }
}
