package io.github.andrefigas.rustjni.reflection

internal data class MethodSignature(
    val methodName: String,
    val returnType: String,
    val parameters: List<String>
)