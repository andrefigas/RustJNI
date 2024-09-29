package io.github.andrefigas.rustjni

data class ArchitectureConfig(val target : String,
                              val linker: String,
                              val ar: String = DEFAULT_AR
){

    companion object {
        private fun isWindows(): Boolean {
            return System.getProperty("os.name").toLowerCase().contains("win")
        }

        val DEFAULT_AR = if (isWindows()) "llvm-ar.exe" else "llvm-ar"
    }

}