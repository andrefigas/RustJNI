package io.github.andrefigas.rustjni.reflection.primitive

internal object PrimitiveRust {
    const val RS_JSTRING = "jstring"
    const val RS_JINT = "jint"
    const val RS_JLONG = "jlong"
    const val RS_JBOOLEAN = "jboolean"
    const val RS_JBYTE = "jbyte"
    const val RS_JCHAR = "jchar"
    const val RS_JDOUBLE = "jdouble"
    const val RS_JFLOAT = "jfloat"
    const val RS_JSHORT = "jshort"
    const val RS_JOBJECT = "jobject"
    const val RS_JCLASS = "jclass"
    const val RS_VOID = ""

    // Function 'any' that performs some operation with the provided type
    fun any(type: String): String {
        // Return a sample value based on the provided type
        return when (type) {
            RS_JSTRING -> "Sample string"
            RS_JINT -> "42"
            RS_JLONG -> "1234567890"
            RS_JBOOLEAN -> "true"
            RS_JBYTE -> "42"
            RS_JCHAR -> "'A' as jchar"
            RS_JDOUBLE -> "3.14159"
            RS_JFLOAT -> "2.71828"
            RS_JSHORT -> "32767"
            RS_JOBJECT -> "null object"
            RS_JCLASS -> "class object"
            RS_VOID -> "void"
            else -> "Unknown type"
        }
    }

    // Function 'mock' that creates a pair of the provided type and its corresponding 'any' value
    fun mock(type: String) = type to any(type)

}
