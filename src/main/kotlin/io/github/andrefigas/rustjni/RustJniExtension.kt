package io.github.andrefigas.rustjni

import io.github.andrefigas.rustjni.AndroidTarget.AARCH64_LINUX_ANDROID
import io.github.andrefigas.rustjni.AndroidTarget.ARMV7_LINUX_ANDROIDEABI
import io.github.andrefigas.rustjni.AndroidTarget.I686_LINUX_ANDROID
import io.github.andrefigas.rustjni.AndroidTarget.X86_64_LINUX_ANDROID

open class RustJniExtension {
    // -- Settings for the user of the plugin
    /** The *name* of the Rust library. Same value as `package.name` in `Cargo.toml`. */
    // TODO: default empty, should be parsed from Cargo.toml if it exists
    var libName = "my_rust_lib"
    /** The *version* of the Rust library. Same value as `package.version` in `Cargo.toml`.
     *
     * Is only used to generate Rust project if it doesn't exist. */
    var libVersion = "0.1.0"
    /** The path to the Rust library, i.e. the directory where `Cargo.toml` is in,
     * relative to the project's root directory. */
    var rustPath = "./rust"
    /** The version of NDK to use.
     * This is one of the directories found in `{sdk.dir}/ndk/`.
     *
     * Automatically uses the latest version if a value is not provided. */
    var ndkVersion = ""
    /** Corresponds to the OS you are building with.
     *
     * Value is automatically assigned to the host OS. */
    var preBuilt = ""
    /** The **Class** in your project that will load the Rust library and have all the `native`/`extern` functions.
     *
     * This is used to generated Rust code and library loader in the named Class. */
    var jniHost = ""
        get() {
            if (field.isEmpty()) {
                throw org.gradle.api.GradleException("Must define `rustJni.jniHost` if code needs to be generated")
            }
            return field
        }
    /** Whether `native`/`extern` functions should be generated in the Class [jniHost].
     *
     * Default is `true`. */
    var exportFunctions = true
    var applyAsCompileDependency = true
    private var architectures: (ArchitectureListScope.() -> Unit)? = null

    fun architectures(architectures: ArchitectureListScope.() -> Unit) {
        this.architectures = architectures
    }

    internal val architecturesList: List<ArchitectureConfig>
        get() {
            val list = mutableListOf<ArchitectureConfig>()
            val scope = ArchitectureListScope(list)
            architectures?.let { scope.it() }
            return list.toList()
        }

}

class ArchitectureListScope(private val list: MutableList<ArchitectureConfig>) {

    fun custom(
        target: String, linker: String,
        ar: String = ArchitectureConfig.DEFAULT_AR
    ) {
        list.add(
            ArchitectureConfig(
                target,
                linker,
                ar
            )
        )
    }

    fun armv7_linux_androideabi(
        linker: String,
        ar: String = ArchitectureConfig.DEFAULT_AR
    ) {
        list.add(
            ArchitectureConfig(
                ARMV7_LINUX_ANDROIDEABI,
                linker,
                ar
            )
        )
    }

    fun aarch64_linux_android(
        linker: String,
        ar: String = ArchitectureConfig.DEFAULT_AR
    ) {
        list.add(
            ArchitectureConfig(
                AARCH64_LINUX_ANDROID,
                linker,
                ar
            )
        )
    }

    fun i686_linux_android(
        linker: String,
        ar: String = ArchitectureConfig.DEFAULT_AR
    ) {
        list.add(
            ArchitectureConfig(
                I686_LINUX_ANDROID,
                linker,
                ar
            )
        )
    }

    fun x86_64_linux_android(
        linker: String,
        ar: String = ArchitectureConfig.DEFAULT_AR
    ) {
        list.add(
            ArchitectureConfig(
                X86_64_LINUX_ANDROID,
                linker,
                ar
            )
        )
    }

}
