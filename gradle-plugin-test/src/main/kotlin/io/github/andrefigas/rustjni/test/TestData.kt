package io.github.andrefigas.rustjni.test

import io.github.andrefigas.rustjni.test.jvm.content.JVMContentProvider

object TestData {

    fun all(provider: JVMContentProvider) =
        provider.generate(
            mapOf(
                arrayOf(provider.primitiveInt) to provider.primitiveInt,       // Int as argument and return
                arrayOf(provider.primitiveLong) to provider.primitiveDouble,   // Long as argument, Double as return
                arrayOf(provider.primitiveBoolean) to provider.primitiveString,// Boolean as argument, String as return
                arrayOf(provider.primitiveByte) to provider.primitiveFloat,    // Byte as argument, Float as return
                arrayOf(provider.primitiveChar) to provider.primitiveBoolean,  // Char as argument, Boolean as return
                arrayOf(provider.primitiveDouble) to provider.primitiveChar,   // Double as argument, Char as return
                arrayOf(provider.primitiveFloat) to provider.primitiveByte,    // Float as argument, Byte as return
                arrayOf(provider.primitiveShort) to provider.primitiveVoid,    // Short as argument, Void as return
                arrayOf(provider.primitiveString) to provider.primitiveShort,   // String as argument, Short as return
                *generateRandomArgCombinations(provider, 2),
                *generateRandomArgCombinations(provider, 3),
                *generateRandomArgCombinations(provider, 4),
                *generateRandomArgCombinations(provider, 5),
                *generateRandomArgCombinations(provider, 6),
                *generateRandomArgCombinations(provider, 7),
                *generateRandomArgCombinations(provider, 8),
                *generateRandomArgCombinations(provider, 9),
                *generateRandomArgCombinations(provider, 10)

            )
        )

    private fun generateRandomArgCombinations(provider: JVMContentProvider, numArgs: Int): Array<Pair<Array<String>, String>> {
        val argumentTypes = listOf(
            provider.primitiveInt,
            provider.primitiveLong,
            provider.primitiveBoolean,
            provider.primitiveByte,
            provider.primitiveChar,
            provider.primitiveDouble,
            provider.primitiveFloat,
            provider.primitiveShort,
            provider.primitiveString
        )

        val returnTypes = argumentTypes + provider.primitiveVoid

        return Array(3) {
            val args = Array(numArgs) { argumentTypes.random() }
            args to returnTypes.random()
        }
    }


}
