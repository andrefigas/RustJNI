package io.github.andrefigas.rustjni.test.cases

import io.github.andrefigas.rustjni.test.JVMTestRunner
import io.github.andrefigas.rustjni.test.toml.TomlContentProvider
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.org.junit.Test
import java.io.File

class TestCases(
    private val project: Project,
    private val task: Task,
    private val jniHost: File,
    private val rustFile: File,
    private val cargoConfigFile : File,
    ndkDir : String,
) {

    private val tomlContentProvider = TomlContentProvider(ndkDir)
    private val rustContent by lazy { rustFile.readText() }
    private val jvmContent by lazy { jniHost.readText() }
    private val cargoConfigContent
        get() = cargoConfigFile.readText()

    private val isKotlin = jniHost.toString().endsWith(JVMTestRunner.KT)
    private val logger = project.logger
    private var successCounter = 0
    private var errorCounter = 0

    @Test
    fun assertCompilation(){
        project.tasks.getByName("rust-jni-compile").actions.forEach { action ->
            action.execute(task)
        }

        assert(true, "given a generated code, it should compile successfully")
    }

    @Test
    fun assertCargoConfigIsSuccessfullyGenerated(){
        assertCargoConfigContains(
            tomlContentProvider.cargoConfig().trim(),
            "given a generated code, it should generate the correct .cargo/config.toml"
        )
    }

    @Test
    fun assertCargoConfigEditsIsPreserved(){

        val content = buildString {
            appendLine("[section1]")
            appendLine("definition1 = \"definition1\"")
            appendLine(tomlContentProvider.cargoConfig().trim())
            appendLine("[section2]")
            appendLine("definition2 = \"definition2\"")
        }

        cargoConfigFile.writeText(
            content
        )

        project.tasks.getByName("rust-jni-compile").actions.forEach { action ->
            action.execute(task)
        }

        assertCargoConfigContains(
            "[section1]\ndefinition1 = \"definition1\"",
            "given a generated .cargo/config.toml edited manually, it should not remove the existing content before the generated one"
        )

        assertCargoConfigContains(
            "[section2]\ndefinition2 = \"definition2\"",
            "given a generated .cargo/config.toml edited manually, it should not remove the existing content after the generated one"
        )
    }

    private fun assert(condition : Boolean, useCase : String, errorMessage : String = ""){
        if(condition){
            logger.lifecycle("RustJNI Test: 🦀 $useCase: ✅")
            successCounter++
        } else {
            logger.error("RustJNI Test: 🦀 $useCase: ❌ $errorMessage")
            errorCounter++
        }

    }

    private fun assertRustFileContains(substring: String, useCase: String) {
        assertContains(rustContent, rustFile.toString(), substring, useCase)
    }

    private fun assertJVMFileContains(substring: String, useCase: String) {
        assertContains(jvmContent, jniHost.toString(), substring, useCase)
    }

    private fun assertCargoConfigContains(substring: String, useCase: String) {
        assertContains(cargoConfigContent, cargoConfigFile.toString(), substring, useCase)
    }

    private fun assertContains(text: String, path : String , substring: String, useCase: String) {

        val normalizedText = text.replace("\\s".toRegex(), "")
        val normalizedSubstring = substring.replace("\\s".toRegex(), "")

        if (!normalizedText.contains(normalizedSubstring)) {
            val missingPart = substring.lines().firstOrNull { !text.contains(it.trim()) }

            val errorMessage = "assertContains fails:\nfile:\n$path\n\ncontent:\n$text\n\nDoes not contain:\n$substring\n\nMissing part: $missingPart"
            assert(false, useCase, errorMessage)
        } else {
            assert(true, useCase)
        }
    }

    fun finish(){
        val message = "RustJNI Test: 🦀 Test finished with $successCounter success and $errorCounter errors"
        if(errorCounter > 0){
            throw GradleException(message)
        } else {
            logger.lifecycle(message)
        }
    }

    ////////////////////// TEST CASES //////////////////////

    fun all() {
        assertCompilation()
        assertCargoConfigIsSuccessfullyGenerated()
        assertCargoConfigEditsIsPreserved()

        assertIntParam()
        assertLongParam()
        assertBooleanParam()
        assertByteParam()
        assertCharParam()
        assertDoubleParam()
        assertFloatParam()
        assertShortParam()
        assertStringParam()
    }

    fun assertIntParam() {
        assertIntParamAndIntReturnNative()
        assertIntParamAndIntReturnJVM()
    }

    fun assertLongParam() {
        assertLongParamAndDoubleReturnNative()
        assertLongParamAndDoubleReturnJVM()
    }

    fun assertBooleanParam() {
        assertBooleanParamAndStringReturnNative()
        assertBooleanParamAndStringReturnJVM()
    }

    fun assertByteParam() {
        assertByteParamAndFloatReturnNative()
        assertByteParamAndFloatReturnJVM()
    }

    fun assertCharParam() {
        assertCharParamAndBooleanReturnNative()
        assertCharParamAndBooleanReturnJVM()
    }

    fun assertDoubleParam() {
        assertDoubleParamAndCharReturnNative()
        assertDoubleParamAndCharReturnJVM()
    }

    fun assertFloatParam() {
        assertFloatParamAndByteReturnNative()
        assertFloatParamAndByteReturnJVM()
    }

    fun assertShortParam() {
        assertShortParamAndVoidReturnNative()
        assertShortParamAndVoidReturnJVM()
    }

    fun assertStringParam() {
        assertStringParamAndShortReturnNative()
        assertStringParamAndShortReturnJVM()
    }

    //////////// Native Tests ////////////

    @Test
    fun assertIntParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod0(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod1(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod2(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod3(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod4(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod5(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod6(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod7(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod8(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and short return, it should generate the correct rust signature"
        )
    }

// JVM Tests

    @Test
    fun assertIntParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod0(param0: Int): Int"
        else
            "private static native int someMethod0(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod1(param0: Long): Double"
        else
            "private static native double someMethod1(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod2(param0: Boolean): String"
        else
            "private static native String someMethod2(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod3(param0: Byte): Float"
        else
            "private static native float someMethod3(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod4(param0: Char): Boolean"
        else
            "private static native boolean someMethod4(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod5(param0: Double): Char"
        else
            "private static native char someMethod5(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod6(param0: Float): Byte"
        else
            "private static native byte someMethod6(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod7(param0: Short)"
        else
            "private static native void someMethod7(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod8(param0: String): Short"
        else
            "private static native short someMethod8(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and short return, it should generate the correct JVM signature"
        )
    }

}