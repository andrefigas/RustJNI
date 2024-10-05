package io.github.andrefigas.rustjni

object AndroidTarget {
    const val ARMV7_LINUX_ANDROIDEABI = "armv7-linux-androideabi"
    const val AARCH64_LINUX_ANDROID = "aarch64-linux-android"
    const val I686_LINUX_ANDROID = "i686-linux-android"
    const val X86_64_LINUX_ANDROID = "x86_64-linux-android"

    val ALL = listOf(
        ARMV7_LINUX_ANDROIDEABI,
        AARCH64_LINUX_ANDROID,
        I686_LINUX_ANDROID,
        X86_64_LINUX_ANDROID
    )
}