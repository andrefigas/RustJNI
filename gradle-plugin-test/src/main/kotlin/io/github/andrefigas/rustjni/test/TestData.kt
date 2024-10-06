package io.github.andrefigas.rustjni.test

import io.github.andrefigas.rustjni.test.jvm.content.JVMContentProvider

object TestData {

    fun all(provider: JVMContentProvider) =
        provider.generate(
            mapOf(
                arrayOf(provider.primitiveInt) to provider.primitiveInt,
                arrayOf(provider.primitiveInt) to provider.primitiveLong,
                arrayOf(provider.primitiveInt) to provider.primitiveBoolean,
                arrayOf(provider.primitiveInt) to provider.primitiveByte,
                arrayOf(provider.primitiveInt) to provider.primitiveChar,
                arrayOf(provider.primitiveInt) to provider.primitiveDouble,
                arrayOf(provider.primitiveInt) to provider.primitiveFloat,
                arrayOf(provider.primitiveInt) to provider.primitiveShort,
                arrayOf(provider.primitiveInt) to provider.primitiveString,
                arrayOf(provider.primitiveInt) to provider.primitiveVoid,

                arrayOf(provider.primitiveLong) to provider.primitiveInt,
                arrayOf(provider.primitiveLong) to provider.primitiveLong,
                arrayOf(provider.primitiveLong) to provider.primitiveBoolean,
                arrayOf(provider.primitiveLong) to provider.primitiveByte,
                arrayOf(provider.primitiveLong) to provider.primitiveChar,
                arrayOf(provider.primitiveLong) to provider.primitiveDouble,
                arrayOf(provider.primitiveLong) to provider.primitiveFloat,
                arrayOf(provider.primitiveLong) to provider.primitiveShort,
                arrayOf(provider.primitiveLong) to provider.primitiveString,
                arrayOf(provider.primitiveLong) to provider.primitiveVoid,

                arrayOf(provider.primitiveBoolean) to provider.primitiveInt,
                arrayOf(provider.primitiveBoolean) to provider.primitiveLong,
                arrayOf(provider.primitiveBoolean) to provider.primitiveBoolean,
                arrayOf(provider.primitiveBoolean) to provider.primitiveByte,
                arrayOf(provider.primitiveBoolean) to provider.primitiveChar,
                arrayOf(provider.primitiveBoolean) to provider.primitiveDouble,
                arrayOf(provider.primitiveBoolean) to provider.primitiveFloat,
                arrayOf(provider.primitiveBoolean) to provider.primitiveShort,
                arrayOf(provider.primitiveBoolean) to provider.primitiveString,
                arrayOf(provider.primitiveBoolean) to provider.primitiveVoid,

                arrayOf(provider.primitiveByte) to provider.primitiveInt,
                arrayOf(provider.primitiveByte) to provider.primitiveLong,
                arrayOf(provider.primitiveByte) to provider.primitiveBoolean,
                arrayOf(provider.primitiveByte) to provider.primitiveByte,
                arrayOf(provider.primitiveByte) to provider.primitiveChar,
                arrayOf(provider.primitiveByte) to provider.primitiveDouble,
                arrayOf(provider.primitiveByte) to provider.primitiveFloat,
                arrayOf(provider.primitiveByte) to provider.primitiveShort,
                arrayOf(provider.primitiveByte) to provider.primitiveString,
                arrayOf(provider.primitiveByte) to provider.primitiveVoid,

                arrayOf(provider.primitiveChar) to provider.primitiveInt,
                arrayOf(provider.primitiveChar) to provider.primitiveLong,
                arrayOf(provider.primitiveChar) to provider.primitiveBoolean,
                arrayOf(provider.primitiveChar) to provider.primitiveByte,
                arrayOf(provider.primitiveChar) to provider.primitiveChar,
                arrayOf(provider.primitiveChar) to provider.primitiveDouble,
                arrayOf(provider.primitiveChar) to provider.primitiveFloat,
                arrayOf(provider.primitiveChar) to provider.primitiveShort,
                arrayOf(provider.primitiveChar) to provider.primitiveString,
                arrayOf(provider.primitiveChar) to provider.primitiveVoid,

                arrayOf(provider.primitiveDouble) to provider.primitiveInt,
                arrayOf(provider.primitiveDouble) to provider.primitiveLong,
                arrayOf(provider.primitiveDouble) to provider.primitiveBoolean,
                arrayOf(provider.primitiveDouble) to provider.primitiveByte,
                arrayOf(provider.primitiveDouble) to provider.primitiveChar,
                arrayOf(provider.primitiveDouble) to provider.primitiveDouble,
                arrayOf(provider.primitiveDouble) to provider.primitiveFloat,
                arrayOf(provider.primitiveDouble) to provider.primitiveShort,
                arrayOf(provider.primitiveDouble) to provider.primitiveString,
                arrayOf(provider.primitiveDouble) to provider.primitiveVoid,

                arrayOf(provider.primitiveFloat) to provider.primitiveInt,
                arrayOf(provider.primitiveFloat) to provider.primitiveLong,
                arrayOf(provider.primitiveFloat) to provider.primitiveBoolean,
                arrayOf(provider.primitiveFloat) to provider.primitiveByte,
                arrayOf(provider.primitiveFloat) to provider.primitiveChar,
                arrayOf(provider.primitiveFloat) to provider.primitiveDouble,
                arrayOf(provider.primitiveFloat) to provider.primitiveFloat,
                arrayOf(provider.primitiveFloat) to provider.primitiveShort,
                arrayOf(provider.primitiveFloat) to provider.primitiveString,
                arrayOf(provider.primitiveFloat) to provider.primitiveVoid,

                arrayOf(provider.primitiveShort) to provider.primitiveInt,
                arrayOf(provider.primitiveShort) to provider.primitiveLong,
                arrayOf(provider.primitiveShort) to provider.primitiveBoolean,
                arrayOf(provider.primitiveShort) to provider.primitiveByte,
                arrayOf(provider.primitiveShort) to provider.primitiveChar,
                arrayOf(provider.primitiveShort) to provider.primitiveDouble,
                arrayOf(provider.primitiveShort) to provider.primitiveFloat,
                arrayOf(provider.primitiveShort) to provider.primitiveShort,
                arrayOf(provider.primitiveShort) to provider.primitiveString,
                arrayOf(provider.primitiveShort) to provider.primitiveVoid,

                arrayOf(provider.primitiveString) to provider.primitiveInt,
                arrayOf(provider.primitiveString) to provider.primitiveLong,
                arrayOf(provider.primitiveString) to provider.primitiveBoolean,
                arrayOf(provider.primitiveString) to provider.primitiveByte,
                arrayOf(provider.primitiveString) to provider.primitiveChar,
                arrayOf(provider.primitiveString) to provider.primitiveDouble,
                arrayOf(provider.primitiveString) to provider.primitiveFloat,
                arrayOf(provider.primitiveString) to provider.primitiveShort,
                arrayOf(provider.primitiveString) to provider.primitiveString,
                arrayOf(provider.primitiveString) to provider.primitiveVoid,

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
