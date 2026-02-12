import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":kmp-screens"))
                implementation(project(":kmp-design"))
                implementation(project(":kmp-resources"))
                implementation(project(":modules:di"))

                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation(libs.koin.core)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.slf4j.nop)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.edugo.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "EduGo"
            packageVersion = "1.0.0"
            description = "EduGo - Plataforma Educativa Multiplataforma"
            vendor = "EduGo Team"
        }
    }
}

// Task para exportar classpath para debug en VS Code
tasks.register("writeDesktopClasspath") {
    dependsOn("desktopMainClasses")
    val outputFile = rootProject.file(".vscode/desktop-classpath.txt")
    val jsonOutputFile = rootProject.file(".vscode/desktop-classpath.json")
    val launchFile = rootProject.file(".vscode/launch.json")
    doLast {
        val desktopTarget = kotlin.targets.getByName("desktop")
            as org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
        val mainCompilation = desktopTarget.compilations.getByName("main")
        val classesDirs = mainCompilation.output.classesDirs.files
        val runtimeCp = mainCompilation.runtimeDependencyFiles?.files ?: emptySet()
        val allFiles = classesDirs + runtimeCp
        val allPaths = allFiles.joinToString(System.getProperty("path.separator"))
        outputFile.parentFile.mkdirs()
        outputFile.writeText(allPaths)

        // Generar JSON array para launch.json classPaths
        val jsonArray = allFiles.joinToString(",\n") { "        \"${it.absolutePath}\"" }
        jsonOutputFile.writeText("[\n$jsonArray\n]")

        // Generar launch.json con classPaths embebidos
        val ws = rootProject.projectDir.absolutePath
        val classPathsJson = allFiles.joinToString(",\n") { "                \"${it.absolutePath}\"" }
        val sourcePaths = listOf(
            "platforms/desktop/app/src/desktopMain/kotlin",
            "kmp-screens/src/commonMain/kotlin",
            "kmp-design/src/commonMain/kotlin",
            "kmp-resources/src/commonMain/kotlin",
            "modules/di/src/commonMain/kotlin",
            "modules/auth/src/commonMain/kotlin",
            "modules/core/src/commonMain/kotlin",
            "modules/config/src/commonMain/kotlin",
            "modules/storage/src/commonMain/kotlin",
            "modules/network/src/commonMain/kotlin",
            "modules/foundation/src/commonMain/kotlin",
            "modules/logger/src/commonMain/kotlin",
            "modules/validation/src/commonMain/kotlin"
        )
        val sourcePathsJson = sourcePaths.joinToString(",\n") { "                \"$ws/$it\"" }

        val launchJson = """
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Desktop App - Debug (Direct Launch)",
            "request": "launch",
            "mainClass": "com.edugo.desktop.MainKt",
            "classPaths": [
$classPathsJson
            ],
            "sourcePaths": [
$sourcePathsJson
            ],
            "cwd": "$ws",
            "console": "integratedTerminal",
            "preLaunchTask": "build-desktop-classpath"
        },
        {
            "type": "java",
            "name": "Desktop App - Debug (Attach)",
            "request": "attach",
            "hostName": "localhost",
            "port": 5005,
            "timeout": 60000,
            "preLaunchTask": "build-and-launch-debug",
            "sourcePaths": [
$sourcePathsJson
            ]
        },
        {
            "type": "java",
            "name": "Desktop App - Attach Only",
            "request": "attach",
            "hostName": "localhost",
            "port": 5005,
            "timeout": 30000,
            "sourcePaths": [
$sourcePathsJson
            ]
        }
    ]
}
""".trimIndent()

        launchFile.writeText(launchJson)

        println("Classpath written to " + outputFile.absolutePath)
        println("JSON classpath written to " + jsonOutputFile.absolutePath)
        println("launch.json updated with " + allFiles.size + " classpath entries")
    }
}
