package io.github.andrefigas.rustjni

import io.github.andrefigas.rustjni.AndroidTarget.AARCH64_LINUX_ANDROID
import io.github.andrefigas.rustjni.AndroidTarget.ARMV7_LINUX_ANDROIDEABI
import io.github.andrefigas.rustjni.AndroidTarget.I686_LINUX_ANDROID
import io.github.andrefigas.rustjni.AndroidTarget.X86_64_LINUX_ANDROID

open class RustJniExtension {
    var libName: String = "my_rust_lib"
    var libVersion: String = "0.1.0"
    var ndkVersion: String = ""
    var preBuilt: String = ""
    var jniHost: String = defaultJniHost
    var exportFunctions = true
    var applyAsCompileDependency = true

    companion object {
        internal const val defaultJniHost = "com.yourpackage.YorClass"

        fun shouldSkipAddingMethods(jniHost: String, extension: RustJniExtension): Boolean {
            return jniHost == RustJniExtension.defaultJniHost || !extension.exportFunctions
        }
    }

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
