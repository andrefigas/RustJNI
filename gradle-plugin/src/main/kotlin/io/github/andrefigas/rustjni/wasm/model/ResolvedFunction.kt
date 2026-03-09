package io.github.andrefigas.rustjni.wasm.model

data class ResolvedParam(
    val name: String,
    val rustType: String,
    val isHostProvided: Boolean,
    val hostProvider: String?,
    val isCallback: Boolean,
    val callbackParamTypes: List<String>?
)

data class ResolvedFunction(
    val name: String,
    val params: List<ResolvedParam>,
    val returnType: String?,
    val returnConfig: ReturnConfig?
)
