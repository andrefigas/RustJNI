package io.github.andrefigas.rustjni.reflection

/**
 * Enum class that represents the visibility of a methods
 * Since internal visibility is only available in Kotlin, it is not supported here
 */
enum class Visibility(private val label : String) {
    PUBLIC("public"),
    PROTECTED("protected"),
    PRIVATE("private"),
    // Default omits the visibility modifier and assume the default visibility of the Programming Language:
    // Java: package-private, Kotlin: public
    DEFAULT("");

    override fun toString() = label
    
}