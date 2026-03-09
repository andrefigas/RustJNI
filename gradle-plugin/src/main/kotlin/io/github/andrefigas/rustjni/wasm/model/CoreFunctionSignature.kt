package io.github.andrefigas.rustjni.wasm.model

data class CoreParam(
    val name: String,
    val rustType: String
)

/**
 * Annotation parsed from `/// @wasm:` doc comments above a `pub fn`.
 */
data class WasmAnnotation(
    /** Parameter names marked with `/// @wasm:host_provided` or `/// @wasm:host_provided(provider)` */
    val hostProvidedParams: Map<String, String> = emptyMap(), // paramName -> provider (empty string if no provider)
    /** Callback name from `/// @wasm:returns(callback_name)` */
    val returnsVia: String? = null
)

data class CoreFunctionSignature(
    val name: String,
    val params: List<CoreParam>,
    val returnType: String?,
    val annotations: WasmAnnotation = WasmAnnotation()
)
