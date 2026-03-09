package io.github.andrefigas.rustjni.wasm

object TypeMapping {

    // Rust core type → Kotlin type
    fun rustToKotlin(rustType: String): String = when (rustType.trim()) {
        "&str", "String", "&String" -> "String"
        "i32" -> "Int"
        "i64" -> "Long"
        "f32" -> "Float"
        "f64" -> "Double"
        "bool" -> "Boolean"
        "u8" -> "Int"
        "u16" -> "Int"
        "u32" -> "Int"
        "u64" -> "Long"
        else -> "Any"
    }

    // Rust core type → Chicory ValueType constant name
    fun rustToChicoryValueType(rustType: String): String = when (rustType.trim()) {
        "i32", "u8", "u16", "u32", "bool" -> "ValueType.I32"
        "i64", "u64" -> "ValueType.I64"
        "f32" -> "ValueType.F32"
        "f64" -> "ValueType.F64"
        else -> throw IllegalArgumentException("No Chicory ValueType mapping for: $rustType")
    }

    // Rust core type → WASM extern "C" type
    fun rustToWasmC(rustType: String): String = when (rustType.trim()) {
        "i32", "u8", "u16", "u32", "bool" -> "i32"
        "i64", "u64" -> "i64"
        "f32" -> "f32"
        "f64" -> "f64"
        else -> throw IllegalArgumentException("No WASM C type mapping for: $rustType")
    }

    // Check if a return type is a simple numeric type that can be returned directly from WASM
    fun isSimpleNumericType(rustType: String): Boolean =
        rustType.trim() in listOf("i32", "u8", "u16", "u32", "i64", "u64", "f32", "f64", "bool")

    // Check if a float type (needs bit conversion for Chicory calls)
    fun isFloatType(rustType: String): Boolean =
        rustType.trim() in listOf("f32", "f64")

    // Kotlin expression to convert a param value to long for Chicory apply()
    fun kotlinParamToLong(paramExpr: String, rustType: String): String = when (rustType.trim()) {
        "f64" -> "java.lang.Double.doubleToRawLongBits($paramExpr)"
        "f32" -> "java.lang.Float.floatToRawIntBits($paramExpr).toLong()"
        else -> "$paramExpr.toLong()"
    }

    // Check if rust type is a string type (needs ptr+len in WASM)
    fun isStringType(rustType: String): Boolean =
        rustType.trim() in listOf("&str", "String", "&String")

    // Check if rust type is a callback (FnMut, Fn, FnOnce)
    fun isCallbackType(rustType: String): Boolean =
        rustType.contains("FnMut") || rustType.contains("dyn Fn(") || rustType.contains("FnOnce")

    // Extract inner types from FnMut(T1, T2, ...)
    fun extractCallbackTypes(rustType: String): List<String>? {
        val regex = Regex("""(?:&mut\s+)?dyn\s+FnMut\(([^)]*)\)""")
        val match = regex.find(rustType) ?: return null
        val inner = match.groupValues[1].trim()
        return if (inner.isEmpty()) emptyList() else inner.split(",").map { it.trim() }
    }

    // Parse return type tuple: "(i32, String)" → ["i32", "String"]
    fun parseReturnTypes(returnType: String): List<String> {
        val trimmed = returnType.trim()
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            val inner = trimmed.substring(1, trimmed.length - 1)
            return inner.split(",").map { it.trim() }
        }
        return listOf(trimmed)
    }

    // snake_case to camelCase: "calculate_age" → "calculateAge"
    fun snakeToCamel(name: String): String =
        name.split("_").mapIndexed { i, part ->
            if (i == 0) part else part.replaceFirstChar { it.uppercase() }
        }.joinToString("")

    // snake_case to PascalCase: "on_start_to_think" → "OnStartToThink"
    fun snakeToPascal(name: String): String =
        name.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
}
