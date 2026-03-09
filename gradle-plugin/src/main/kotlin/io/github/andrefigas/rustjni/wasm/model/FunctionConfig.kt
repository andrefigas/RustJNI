package io.github.andrefigas.rustjni.wasm.model

data class HostProvidedConfig(
    val paramName: String,
    val provider: String // Kotlin expression, e.g. "System.currentTimeMillis()"
)

data class ReturnConfig(
    val hostCallbackName: String
)

data class FunctionConfig(
    val name: String,
    val hostProvidedParams: List<HostProvidedConfig>,
    val returnConfig: ReturnConfig?
)
