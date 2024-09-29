package io.github.andrefigas.rustjni

import io.github.andrefigas.rustjni.OSHelper.isWindows

data class ArchitectureConfig(val target : String,
                              val linker: String,
                              val ar: String = DEFAULT_AR
){

    companion object {

        val DEFAULT_AR = if (isWindows()) "llvm-ar.exe" else "llvm-ar"
    }

}