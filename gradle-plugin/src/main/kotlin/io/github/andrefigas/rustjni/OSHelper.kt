package io.github.andrefigas.rustjni

import java.io.File

object OSHelper {

    fun isWindows(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("win")
    }

    fun isMac(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("mac")
    }

    fun isLinux(): Boolean {
        return System.getProperty("os.name").toLowerCase().contains("linux")
    }

    fun doubleSeparatorIfNeeded(path: String): String {
        return if (isWindows()) {
            path.replace(File.separator, "${File.separator}${File.separator}")
        } else {
            path
        }
    }

    fun addLinkerExtensionIfNeeded(linker: String): String {
        val windowsLinkerExt = ".cmd"
        return if (isWindows() && !linker.endsWith(windowsLinkerExt)) {
            linker + windowsLinkerExt
        } else {
            linker
        }
    }

}