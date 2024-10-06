package io.github.andrefigas.rustjni.test.cases

import io.github.andrefigas.rustjni.test.JVMTestRunner
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.impldep.org.junit.Test
import java.io.File

class TestCases(val project: Project,
                val task: Task,
                val jniHost: File,
                val rustFile: File
) {

    private val rustContent by lazy { rustFile.readText() }
    private val jvmContent by lazy { jniHost.readText() }

    val isKotlin = jniHost.toString().endsWith(JVMTestRunner.KT)
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

    private fun assertContains(text: String, path : String , substring: String, useCase: String) {

        val normalizedText = text.replace("\\s".toRegex(), "")
        val normalizedSubstring = substring.replace("\\s".toRegex(), "")

        if (!normalizedText.contains(normalizedSubstring)) {
            val missingPart = substring.lines().firstOrNull { !text.contains(it.trim()) }

            val errorMessage = "assertContains fails:\n$path\nDoes not contain:\n$substring\nMissing part: $missingPart"
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
        assertIntParamAndLongReturnNative()
        assertIntParamAndBooleanReturnNative()
        assertIntParamAndByteReturnNative()
        assertIntParamAndCharReturnNative()
        assertIntParamAndDoubleReturnNative()
        assertIntParamAndFloatReturnNative()
        assertIntParamAndShortReturnNative()
        assertIntParamAndStringReturnNative()
        assertIntParamAndVoidReturnNative()

        assertIntParamAndIntReturnJVM()
        assertIntParamAndLongReturnJVM()
        assertIntParamAndBooleanReturnJVM()
        assertIntParamAndByteReturnJVM()
        assertIntParamAndCharReturnJVM()
        assertIntParamAndDoubleReturnJVM()
        assertIntParamAndFloatReturnJVM()
        assertIntParamAndShortReturnJVM()
        assertIntParamAndStringReturnJVM()
        assertIntParamAndVoidReturnJVM()
    }

    fun assertLongParam() {
        assertLongParamAndIntReturnNative()
        assertLongParamAndLongReturnNative()
        assertLongParamAndBooleanReturnNative()
        assertLongParamAndByteReturnNative()
        assertLongParamAndCharReturnNative()
        assertLongParamAndDoubleReturnNative()
        assertLongParamAndFloatReturnNative()
        assertLongParamAndShortReturnNative()
        assertLongParamAndStringReturnNative()
        assertLongParamAndVoidReturnNative()

        assertLongParamAndIntReturnJVM()
        assertLongParamAndLongReturnJVM()
        assertLongParamAndBooleanReturnJVM()
        assertLongParamAndByteReturnJVM()
        assertLongParamAndCharReturnJVM()
        assertLongParamAndDoubleReturnJVM()
        assertLongParamAndFloatReturnJVM()
        assertLongParamAndShortReturnJVM()
        assertLongParamAndStringReturnJVM()
        assertLongParamAndVoidReturnJVM()
    }


    fun assertBooleanParam() {
        assertBooleanParamAndIntReturnNative()
        assertBooleanParamAndLongReturnNative()
        assertBooleanParamAndBooleanReturnNative()
        assertBooleanParamAndByteReturnNative()
        assertBooleanParamAndCharReturnNative()
        assertBooleanParamAndDoubleReturnNative()
        assertBooleanParamAndFloatReturnNative()
        assertBooleanParamAndShortReturnNative()
        assertBooleanParamAndStringReturnNative()
        assertBooleanParamAndVoidReturnNative()

        assertBooleanParamAndIntReturnJVM()
        assertBooleanParamAndLongReturnJVM()
        assertBooleanParamAndBooleanReturnJVM()
        assertBooleanParamAndByteReturnJVM()
        assertBooleanParamAndCharReturnJVM()
        assertBooleanParamAndDoubleReturnJVM()
        assertBooleanParamAndFloatReturnJVM()
        assertBooleanParamAndShortReturnJVM()
        assertBooleanParamAndStringReturnJVM()
        assertBooleanParamAndVoidReturnJVM()
    }

    fun assertByteParam() {
        assertByteParamAndIntReturnNative()
        assertByteParamAndLongReturnNative()
        assertByteParamAndBooleanReturnNative()
        assertByteParamAndByteReturnNative()
        assertByteParamAndCharReturnNative()
        assertByteParamAndDoubleReturnNative()
        assertByteParamAndFloatReturnNative()
        assertByteParamAndShortReturnNative()
        assertByteParamAndStringReturnNative()
        assertByteParamAndVoidReturnNative()

        assertByteParamAndIntReturnJVM()
        assertByteParamAndLongReturnJVM()
        assertByteParamAndBooleanReturnJVM()
        assertByteParamAndByteReturnJVM()
        assertByteParamAndCharReturnJVM()
        assertByteParamAndDoubleReturnJVM()
        assertByteParamAndFloatReturnJVM()
        assertByteParamAndShortReturnJVM()
        assertByteParamAndStringReturnJVM()
        assertByteParamAndVoidReturnJVM()
    }

    fun assertCharParam() {
        assertCharParamAndIntReturnNative()
        assertCharParamAndLongReturnNative()
        assertCharParamAndBooleanReturnNative()
        assertCharParamAndByteReturnNative()
        assertCharParamAndCharReturnNative()
        assertCharParamAndDoubleReturnNative()
        assertCharParamAndFloatReturnNative()
        assertCharParamAndShortReturnNative()
        assertCharParamAndStringReturnNative()
        assertCharParamAndVoidReturnNative()

        assertCharParamAndIntReturnJVM()
        assertCharParamAndLongReturnJVM()
        assertCharParamAndBooleanReturnJVM()
        assertCharParamAndByteReturnJVM()
        assertCharParamAndCharReturnJVM()
        assertCharParamAndDoubleReturnJVM()
        assertCharParamAndFloatReturnJVM()
        assertCharParamAndShortReturnJVM()
        assertCharParamAndStringReturnJVM()
        assertCharParamAndVoidReturnJVM()
    }

    fun assertDoubleParam() {
        assertDoubleParamAndIntReturnNative()
        assertDoubleParamAndLongReturnNative()
        assertDoubleParamAndBooleanReturnNative()
        assertDoubleParamAndByteReturnNative()
        assertDoubleParamAndCharReturnNative()
        assertDoubleParamAndDoubleReturnNative()
        assertDoubleParamAndFloatReturnNative()
        assertDoubleParamAndShortReturnNative()
        assertDoubleParamAndStringReturnNative()
        assertDoubleParamAndVoidReturnNative()

        assertDoubleParamAndIntReturnJVM()
        assertDoubleParamAndLongReturnJVM()
        assertDoubleParamAndBooleanReturnJVM()
        assertDoubleParamAndByteReturnJVM()
        assertDoubleParamAndCharReturnJVM()
        assertDoubleParamAndDoubleReturnJVM()
        assertDoubleParamAndFloatReturnJVM()
        assertDoubleParamAndShortReturnJVM()
        assertDoubleParamAndStringReturnJVM()
        assertDoubleParamAndVoidReturnJVM()
    }

    fun assertFloatParam() {
        assertFloatParamAndIntReturnNative()
        assertFloatParamAndLongReturnNative()
        assertFloatParamAndBooleanReturnNative()
        assertFloatParamAndByteReturnNative()
        assertFloatParamAndCharReturnNative()
        assertFloatParamAndDoubleReturnNative()
        assertFloatParamAndFloatReturnNative()
        assertFloatParamAndShortReturnNative()
        assertFloatParamAndStringReturnNative()
        assertFloatParamAndVoidReturnNative()

        assertFloatParamAndIntReturnJVM()
        assertFloatParamAndLongReturnJVM()
        assertFloatParamAndBooleanReturnJVM()
        assertFloatParamAndByteReturnJVM()
        assertFloatParamAndCharReturnJVM()
        assertFloatParamAndDoubleReturnJVM()
        assertFloatParamAndFloatReturnJVM()
        assertFloatParamAndShortReturnJVM()
        assertFloatParamAndStringReturnJVM()
        assertFloatParamAndVoidReturnJVM()
    }

    fun assertShortParam() {
        assertShortParamAndIntReturnNative()
        assertShortParamAndLongReturnNative()
        assertShortParamAndBooleanReturnNative()
        assertShortParamAndByteReturnNative()
        assertShortParamAndCharReturnNative()
        assertShortParamAndDoubleReturnNative()
        assertShortParamAndFloatReturnNative()
        assertShortParamAndShortReturnNative()
        assertShortParamAndStringReturnNative()
        assertShortParamAndVoidReturnNative()

        assertShortParamAndIntReturnJVM()
        assertShortParamAndLongReturnJVM()
        assertShortParamAndBooleanReturnJVM()
        assertShortParamAndByteReturnJVM()
        assertShortParamAndCharReturnJVM()
        assertShortParamAndDoubleReturnJVM()
        assertShortParamAndFloatReturnJVM()
        assertShortParamAndShortReturnJVM()
        assertShortParamAndStringReturnJVM()
        assertShortParamAndVoidReturnJVM()
    }

    fun assertStringParam() {
        assertStringParamAndIntReturnNative()
        assertStringParamAndLongReturnNative()
        assertStringParamAndBooleanReturnNative()
        assertStringParamAndByteReturnNative()
        assertStringParamAndCharReturnNative()
        assertStringParamAndDoubleReturnNative()
        assertStringParamAndFloatReturnNative()
        assertStringParamAndShortReturnNative()
        assertStringParamAndStringReturnNative()
        assertStringParamAndVoidReturnNative()

        assertStringParamAndIntReturnJVM()
        assertStringParamAndLongReturnJVM()
        assertStringParamAndBooleanReturnJVM()
        assertStringParamAndByteReturnJVM()
        assertStringParamAndCharReturnJVM()
        assertStringParamAndDoubleReturnJVM()
        assertStringParamAndFloatReturnJVM()
        assertStringParamAndShortReturnJVM()
        assertStringParamAndStringReturnJVM()
        assertStringParamAndVoidReturnJVM()
    }


    ////////////////////// NATIVE //////////////////////

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
    fun assertIntParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod1(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod2(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod3(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod4(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod5(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod6(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod7(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod8(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertIntParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod9(
    env: JNIEnv,
    _class: JClass,
    param0: jint
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with int param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod10(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod11(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod12(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod13(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod14(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod15(
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
    fun assertLongParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod16(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod17(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod18(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertLongParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod19(
    env: JNIEnv,
    _class: JClass,
    param0: jlong
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with long param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod20(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod21(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod22(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod23(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod24(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod25(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod26(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod27(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertBooleanParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod28(
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
    fun assertBooleanParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod29(
    env: JNIEnv,
    _class: JClass,
    param0: jboolean
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with boolean param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod30(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod31(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod32(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod33(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod34(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod35(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod36(
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
    fun assertByteParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod37(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod38(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertByteParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod39(
    env: JNIEnv,
    _class: JClass,
    param0: jbyte
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with byte param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod40(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod41(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod42(
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
    fun assertCharParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod43(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod44(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod45(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod46(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod47(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod48(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertCharParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod49(
    env: JNIEnv,
    _class: JClass,
    param0: jchar
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with char param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod50(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod51(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod52(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod53(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod54(
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
    fun assertDoubleParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod55(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod56(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod57(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod58(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertDoubleParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod59(
    env: JNIEnv,
    _class: JClass,
    param0: jdouble
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with double param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod60(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod61(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod62(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod63(
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
    fun assertFloatParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod64(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod65(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod66(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod67(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod68(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertFloatParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod69(
    env: JNIEnv,
    _class: JClass,
    param0: jfloat
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with float param and void return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod70(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod71(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod72(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod73(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod74(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod75(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod76(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod77(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jshort {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and short return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod78(
    env: JNIEnv,
    _class: JClass,
    param0: jshort
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with short param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertShortParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod79(
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
    fun assertStringParamAndIntReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod80(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jint {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and int return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndLongReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod81(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jlong {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and long return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndBooleanReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod82(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jboolean {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and boolean return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndByteReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod83(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jbyte {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and byte return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndCharReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod84(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jchar {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and char return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndDoubleReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod85(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jdouble {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and double return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndFloatReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod86(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jfloat {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and float return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndShortReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod87(
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

    @Test
    fun assertStringParamAndStringReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod88(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) -> jstring {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and string return, it should generate the correct rust signature"
        )
    }

    @Test
    fun assertStringParamAndVoidReturnNative() {
        val expectedRustSignature = """
pub extern "C" fn Java_com_devfigas_rustjni_sample_MainActivity_someMethod89(
    env: JNIEnv,
    _class: JClass,
    param0: jstring
) {
    """.trimIndent()

        assertRustFileContains(
            expectedRustSignature,
            "given a method with string param and void return, it should generate the correct rust signature"
        )
    }

    ///////////////// JVM Tests /////////////////

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
    fun assertIntParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod1(param0: Int): Long"
        else
            "private static native long someMethod1(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod2(param0: Int): Boolean"
        else
            "private static native boolean someMethod2(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod3(param0: Int): Byte"
        else
            "private static native byte someMethod3(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod4(param0: Int): Char"
        else
            "private static native char someMethod4(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod5(param0: Int): Double"
        else
            "private static native double someMethod5(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod6(param0: Int): Float"
        else
            "private static native float someMethod6(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod7(param0: Int): Short"
        else
            "private static native short someMethod7(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod8(param0: Int): String"
        else
            "private static native String someMethod8(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertIntParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod9(param0: Int)"
        else
            "private static native void someMethod9(int param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with int param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod10(param0: Long): Int"
        else
            "private static native int someMethod10(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod11(param0: Long): Long"
        else
            "private static native long someMethod11(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod12(param0: Long): Boolean"
        else
            "private static native boolean someMethod12(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod13(param0: Long): Byte"
        else
            "private static native byte someMethod13(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod14(param0: Long): Char"
        else
            "private static native char someMethod14(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod15(param0: Long): Double"
        else
            "private static native double someMethod15(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod16(param0: Long): Float"
        else
            "private static native float someMethod16(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod17(param0: Long): Short"
        else
            "private static native short someMethod17(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod18(param0: Long): String"
        else
            "private static native String someMethod18(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertLongParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod19(param0: Long)"
        else
            "private static native void someMethod19(long param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with long param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod20(param0: Boolean): Int"
        else
            "private static native int someMethod20(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod21(param0: Boolean): Long"
        else
            "private static native long someMethod21(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod22(param0: Boolean): Boolean"
        else
            "private static native boolean someMethod22(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod23(param0: Boolean): Byte"
        else
            "private static native byte someMethod23(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod24(param0: Boolean): Char"
        else
            "private static native char someMethod24(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod25(param0: Boolean): Double"
        else
            "private static native double someMethod25(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod26(param0: Boolean): Float"
        else
            "private static native float someMethod26(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod27(param0: Boolean): Short"
        else
            "private static native short someMethod27(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod28(param0: Boolean): String"
        else
            "private static native String someMethod28(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertBooleanParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod29(param0: Boolean)"
        else
            "private static native void someMethod29(boolean param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with boolean param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod30(param0: Byte): Int"
        else
            "private static native int someMethod30(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod31(param0: Byte): Long"
        else
            "private static native long someMethod31(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod32(param0: Byte): Boolean"
        else
            "private static native boolean someMethod32(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod33(param0: Byte): Byte"
        else
            "private static native byte someMethod33(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod34(param0: Byte): Char"
        else
            "private static native char someMethod34(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod35(param0: Byte): Double"
        else
            "private static native double someMethod35(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod36(param0: Byte): Float"
        else
            "private static native float someMethod36(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod37(param0: Byte): Short"
        else
            "private static native short someMethod37(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod38(param0: Byte): String"
        else
            "private static native String someMethod38(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertByteParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod39(param0: Byte)"
        else
            "private static native void someMethod39(byte param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with byte param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod40(param0: Char): Int"
        else
            "private static native int someMethod40(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod41(param0: Char): Long"
        else
            "private static native long someMethod41(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod42(param0: Char): Boolean"
        else
            "private static native boolean someMethod42(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod43(param0: Char): Byte"
        else
            "private static native byte someMethod43(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod44(param0: Char): Char"
        else
            "private static native char someMethod44(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod45(param0: Char): Double"
        else
            "private static native double someMethod45(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod46(param0: Char): Float"
        else
            "private static native float someMethod46(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod47(param0: Char): Short"
        else
            "private static native short someMethod47(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod48(param0: Char): String"
        else
            "private static native String someMethod48(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertCharParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod49(param0: Char)"
        else
            "private static native void someMethod49(char param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with char param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod50(param0: Double): Int"
        else
            "private static native int someMethod50(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod51(param0: Double): Long"
        else
            "private static native long someMethod51(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod52(param0: Double): Boolean"
        else
            "private static native boolean someMethod52(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod53(param0: Double): Byte"
        else
            "private static native byte someMethod53(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod54(param0: Double): Char"
        else
            "private static native char someMethod54(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod55(param0: Double): Double"
        else
            "private static native double someMethod55(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod56(param0: Double): Float"
        else
            "private static native float someMethod56(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod57(param0: Double): Short"
        else
            "private static native short someMethod57(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod58(param0: Double): String"
        else
            "private static native String someMethod58(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertDoubleParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod59(param0: Double)"
        else
            "private static native void someMethod59(double param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with double param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod60(param0: Float): Int"
        else
            "private static native int someMethod60(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod61(param0: Float): Long"
        else
            "private static native long someMethod61(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod62(param0: Float): Boolean"
        else
            "private static native boolean someMethod62(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod63(param0: Float): Byte"
        else
            "private static native byte someMethod63(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod64(param0: Float): Char"
        else
            "private static native char someMethod64(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod65(param0: Float): Double"
        else
            "private static native double someMethod65(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod66(param0: Float): Float"
        else
            "private static native float someMethod66(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod67(param0: Float): Short"
        else
            "private static native short someMethod67(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod68(param0: Float): String"
        else
            "private static native String someMethod68(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertFloatParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod69(param0: Float)"
        else
            "private static native void someMethod69(float param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with float param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod70(param0: Short): Int"
        else
            "private static native int someMethod70(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod71(param0: Short): Long"
        else
            "private static native long someMethod71(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod72(param0: Short): Boolean"
        else
            "private static native boolean someMethod72(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod73(param0: Short): Byte"
        else
            "private static native byte someMethod73(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod74(param0: Short): Char"
        else
            "private static native char someMethod74(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod75(param0: Short): Double"
        else
            "private static native double someMethod75(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod76(param0: Short): Float"
        else
            "private static native float someMethod76(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod77(param0: Short): Short"
        else
            "private static native short someMethod77(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod78(param0: Short): String"
        else
            "private static native String someMethod78(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertShortParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod79(param0: Short)"
        else
            "private static native void someMethod79(short param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with short param and void return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndIntReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod80(param0: String): Int"
        else
            "private static native int someMethod80(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and int return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndLongReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod81(param0: String): Long"
        else
            "private static native long someMethod81(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and long return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndBooleanReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod82(param0: String): Boolean"
        else
            "private static native boolean someMethod82(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and boolean return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndByteReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod83(param0: String): Byte"
        else
            "private static native byte someMethod83(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and byte return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndCharReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod84(param0: String): Char"
        else
            "private static native char someMethod84(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and char return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndDoubleReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod85(param0: String): Double"
        else
            "private static native double someMethod85(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and double return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndFloatReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod86(param0: String): Float"
        else
            "private static native float someMethod86(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and float return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndShortReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod87(param0: String): Short"
        else
            "private static native short someMethod87(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and short return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndStringReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod88(param0: String): String"
        else
            "private static native String someMethod88(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and string return, it should generate the correct JVM signature"
        )
    }

    @Test
    fun assertStringParamAndVoidReturnJVM() {
        val expectedRustSignature: String = if (isKotlin)
            "private external fun someMethod89(param0: String)"
        else
            "private static native void someMethod89(String param0);"

        assertJVMFileContains(
            expectedRustSignature,
            "given a method with string param and void return, it should generate the correct JVM signature"
        )
    }

}