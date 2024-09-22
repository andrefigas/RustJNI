package com.rustjni.plugin

data class ArchitectureConfig(val target : String,
                              val linker: String,
                              val ar: String = DEFAULT_AR
){

    companion object {
        const val DEFAULT_AR = "llvm-ar"
    }

}