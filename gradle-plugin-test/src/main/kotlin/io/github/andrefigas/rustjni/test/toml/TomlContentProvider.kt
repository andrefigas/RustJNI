package io.github.andrefigas.rustjni.test.toml

class TomlContentProvider(ndkPath: String) {

    val osName = System.getProperty("os.name").toLowerCase()
    val defaultPrebuilt = when {
        osName.contains("win") -> "windows-x86_64"
        osName.contains("mac") -> "darwin-x86_64"
        osName.contains("linux") -> "linux-x86_64"
        else -> throw org.gradle.api.GradleException("Unsupported operating system: $osName")
    }

    private val binPath = "$ndkPath/27.1.12297006/toolchains/llvm/prebuilt/$defaultPrebuilt/bin"

    val armv7_linux_androideabi = "[target.armv7-linux-androideabi]\n" +
            "ar = \"$binPath/llvm-ar\"\n" +
            "linker = \"$binPath/armv7a-linux-androideabi21-clang\""

    val aarch64_linux_android = "[target.aarch64-linux-android]\n" +
            "ar = \"$binPath/llvm-ar\"\n" +
            "linker = \"$binPath/aarch64-linux-android21-clang\""

    val i686_linux_android = "[target.i686-linux-android]\n" +
            "ar = \"$binPath/llvm-ar\"\n" +
            "linker = \"$binPath/i686-linux-android21-clang\""

    val x86_64_linux_android = "[target.x86_64-linux-android]\n" +
            "ar = \"$binPath/llvm-ar\"\n" +
            "linker = \"$binPath/x86_64-linux-android21-clang\""

    val all = listOf(
        armv7_linux_androideabi,
        aarch64_linux_android,
        i686_linux_android,
        x86_64_linux_android
    )

    fun cargoConfig(): String {
        return buildString {
            appendLine("#<RustJNI>")
            appendLine("#auto-generated code")
            all.forEach {
                appendLine(it)
                appendLine()
            }
            appendLine("#</RustJNI>")
        }.trimStart()
    }
}
