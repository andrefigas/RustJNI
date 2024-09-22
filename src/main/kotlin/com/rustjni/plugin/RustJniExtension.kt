package com.rustjni.plugin

import com.rustjni.plugin.AndroidTarget.AARCH64_LINUX_ANDROID
import com.rustjni.plugin.AndroidTarget.ARMV7_LINUX_ANDROIDEABI
import com.rustjni.plugin.AndroidTarget.I686_LINUX_ANDROID
import com.rustjni.plugin.AndroidTarget.X86_64_LINUX_ANDROID

open class RustJniExtension {
    var libName: String = "my_rust_lib"
    var libVersion: String = "0.1.0"
    var preBuilt: String = ""
    var jniHost: String = defaultJniHost
    var exportFunctions = true

    companion object {
        internal const val defaultJniHost = "com.yourpackage.YorClass"
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
