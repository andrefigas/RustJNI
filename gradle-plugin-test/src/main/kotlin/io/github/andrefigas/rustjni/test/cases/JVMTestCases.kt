package io.github.andrefigas.rustjni.test.cases

import io.github.andrefigas.rustjni.test.jvm.JVMTestData
import io.github.andrefigas.rustjni.test.jvm.content.JVMContentProvider
import org.gradle.internal.impldep.org.junit.Test

object JVMTestCases{

    @Test
    fun compileMethodSignature_Arg_Int_Return_String(provider: JVMContentProvider) =
        JVMTestData(
            provider.generate(listOf(provider.primitiveInt), provider.primitiveString),
            "compile method signature with Int argument and String return"
        )

}