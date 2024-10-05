package io.github.andrefigas.rustjni

import org.gradle.api.Project
import java.io.File

internal object AndroidSettings {

    internal fun configureAndroidSourceSets(project: Project, extension: RustJniExtension) {
        project.afterEvaluate {
            val androidExtension = project.extensions.findByName("android")
            if (androidExtension is com.android.build.gradle.BaseExtension) {
                androidExtension.sourceSets.getByName("main").apply {
                    val buildRust = File(buildDir, "rust")
                    val buildSrcList = mutableListOf<String>()
                    buildSrcList.add(buildRust.toString())
                    buildSrcList.addAll(jniLibs.srcDirs.map { it.toString() })
                    jniLibs.setSrcDirs(buildSrcList)
                }
                project.logger.lifecycle("Configured jniLibs.srcDirs: ${androidExtension.sourceSets.getByName("main").jniLibs.srcDirs}")
            } else {
                throw org.gradle.api.GradleException("Android extension not found in project")
            }
        }
    }

    internal fun configureNdkAbiFilters(project: Project, extension: RustJniExtension) {
        project.afterEvaluate {
            val androidExtension = project.extensions.findByName("android")

            if (androidExtension is com.android.build.gradle.BaseExtension) {
                // Log the architectures list for debugging
                project.logger.lifecycle("Architectures list: ${extension.architecturesList.map { it.target }}")

                // Map architecture targets to corresponding ABI filters
                val pluginAbiFilters = extension.architecturesList.mapNotNull { archConfig ->
                    when (archConfig.target) {
                        AndroidTarget.ARMV7_LINUX_ANDROIDEABI -> "armeabi-v7a"
                        AndroidTarget.AARCH64_LINUX_ANDROID -> "arm64-v8a"
                        AndroidTarget.I686_LINUX_ANDROID -> "x86"
                        AndroidTarget.X86_64_LINUX_ANDROID -> "x86_64"
                        else -> {
                            project.logger.lifecycle("Unknown architecture target: ${archConfig.target}")
                            null
                        }
                    }
                }

                if (pluginAbiFilters.isNotEmpty()) {
                    androidExtension.defaultConfig {
                        ndk {
                            abiFilters.addAll(pluginAbiFilters)
                            project.logger.lifecycle("Configured ndk.abiFilters: $pluginAbiFilters")
                        }
                    }
                } else {
                    project.logger.lifecycle("No valid architectures found to configure ndk.abiFilters.")
                }
            } else {
                throw org.gradle.api.GradleException("Android extension not found in project")
            }
        }
    }

}