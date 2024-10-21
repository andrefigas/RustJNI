plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
    mavenCentral()
    google()
}

version = "0.0.1"
group = "io.github.andrefigas.rustjni-test"

gradlePlugin {
    plugins {
        create("rustJniPlugin") {
            id = "io.github.andrefigas.rustjni-test"
            implementationClass = "io.github.andrefigas.rustjni.test.RustJNITest"
            displayName = "Rust JNI Gradle Plugin"
            description = "A Gradle plugin that simplifies the creation and compilation of Rust code integrated with Android applications via JNI."
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(localGroovy())
    compileOnly("com.android.tools.build:gradle:8.1.1")
}

// Function to insert the plugin line into the build.gradle.kts file
fun insertPluginLine(projectDir: File, pluginLine: String) {
    val buildGradleFile = File(projectDir, "app/build.gradle.kts")
    val trimmedPlugin = pluginLine.trim().replace("\n", "").replace("\r", "")

    if (buildGradleFile.exists()) {
        val buildGradleContent = buildGradleFile.readText()

        // Escape special characters in the plugin line for use in the regex
        val escapedPluginLine = Regex.escape(trimmedPlugin)

        // Check if the plugin is already present by using the plugin line passed as an argument
        if (!Regex(escapedPluginLine).containsMatchIn(buildGradleContent)) {
            // Ensure there is a newline after the opening brace
            val updatedContent = buildGradleContent.replaceFirst(
                Regex("""plugins\s*\{\s*"""), // Match "plugins {" and ensure a newline is added
                """plugins {
    
    """ // Add an explicit newline and indentation
            )

            // Insert the plugin line after the first brace
            val finalContent = updatedContent.replaceFirst(
                Regex("""plugins\s*\{\s*"""),
                """plugins {
    $trimmedPlugin
    """
            )

            // Write the modified content back to the build.gradle.kts file
            buildGradleFile.writeText(finalContent)
            println("Plugin line successfully inserted!")
        } else {
            println("The plugin is already present in build.gradle.kts.")
        }
    } else {
        throw GradleException("build.gradle.kts file not found!")
    }
}

// Function to remove the plugin line from the build.gradle.kts file
fun removePluginLine(projectDir: File, pluginLine: String) {
    val buildGradleFile = File(projectDir, "app/build.gradle.kts")
    val trimmedPlugin = pluginLine.trim().replace("\n", "").replace("\r", "")

    if (buildGradleFile.exists()) {
        val buildGradleContent = buildGradleFile.readText()

        // Escape special characters in the plugin line for use in the regex
        val escapedPluginLine = Regex.escape(trimmedPlugin)

        // Create a flexible regex to capture the plugin line, allowing variations in spaces and line breaks,
        // but preserving surrounding newlines
        val pluginRegex = Regex("""^\s*$escapedPluginLine\s*$\n?""", RegexOption.MULTILINE)

        // Check if the plugin is present
        if (pluginRegex.containsMatchIn(buildGradleContent)) {
            // Remove only the plugin line and leave the surrounding structure intact
            val updatedContent = buildGradleContent.replace(pluginRegex, "")

            // Write the modified content back to build.gradle.kts
            buildGradleFile.writeText(updatedContent.trimEnd() + "\n") // Ensures proper formatting with a newline at the end
            println("Plugin line successfully removed!")
        } else {
            println("The plugin line was not found in build.gradle.kts.")
        }
    } else {
        throw GradleException("build.gradle.kts file not found!")
    }
}

// Function to execute a gradle task in another project
fun executeGradleTask(projectDir: File, taskName: String) {
    val gradleCmd = if (System.getProperty("os.name").toLowerCase().contains("windows")) {
        "gradlew.bat"
    } else {
        "./gradlew"
    }

    val gradleWrapperFile = File(projectDir, gradleCmd)
    if (!gradleWrapperFile.exists()) {
        throw GradleException("Gradle wrapper not found in the project directory")
    }

    // Ensure execution permissions on Unix-like systems
    if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
        exec {
            commandLine("chmod", "+x", gradleWrapperFile.absolutePath)
        }
    }

    // Execute the Gradle task
    exec {
        workingDir = projectDir
        commandLine(gradleWrapperFile.absolutePath, taskName)
    }
}

tasks.register("executeTest") {
    doLast {
        // Define the project directories and task names
        val kotlinDir = file("$projectDir/../sample/kotlin")
        val javaDir = file("$projectDir/../sample/java")
        val pluginDir = file("$projectDir/../gradle-plugin")
        val pluginLine = """id("io.github.andrefigas.rustjni-test") version "0.0.1""""
        val testTaskKotlin = ":app:rust-jni-compile-test"
        val testTaskJava = ":app:rust-jni-compile-test"
        val publishLocalTask = "publishToMavenLocal"

        // Step 1: Publish local dependencies
        println("Publishing test plugin to Maven local...")
        tasks.getByPath(publishLocalTask).actions.forEach {
            it.execute(this)
        }

        executeGradleTask(pluginDir, publishLocalTask)

        // Step 2: Insert the plugin line in both Kotlin and Java sample projects
        println("Inserting plugin line in Kotlin project...")
        insertPluginLine(kotlinDir, pluginLine)

        println("Inserting plugin line in Java project...")
        insertPluginLine(javaDir, pluginLine)

        // Step 3: Execute the test tasks in both Kotlin and Java projects
        println("🦀 Executing test task in Kotlin project...")
        executeGradleTask(kotlinDir, testTaskKotlin)

        println("🦀 Executing test task in Java project...")
        executeGradleTask(javaDir, testTaskJava)

        // Step 4: Remove the plugin line after the tasks are executed
        println("Removing plugin line in Kotlin project...")
        removePluginLine(kotlinDir, pluginLine)

        println("Removing plugin line in Java project...")
        removePluginLine(javaDir, pluginLine)
    }

}



