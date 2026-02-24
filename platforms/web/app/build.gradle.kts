@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Generate build-time environment constant: ./gradlew wasmJsBrowserDevelopmentRun -Penv=STAGING
val generateBuildConfig by tasks.registering {
    val envValue = project.findProperty("env")?.toString() ?: ""
    inputs.property("env", envValue)
    val outputDir = layout.buildDirectory.dir("generated/buildConfig")
    outputs.dir(outputDir)
    doLast {
        val env = inputs.properties["env"] as String
        val dir = outputDir.get().asFile
        dir.mkdirs()
        dir.resolve("BuildConfig.kt").writeText(
            """
            package com.edugo.web

            internal const val BUILD_ENVIRONMENT: String = "$env"
            """.trimIndent()
        )
    }
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = devServer?.copy(port = 8080)
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            kotlin.srcDir(generateBuildConfig.map { layout.buildDirectory.dir("generated/buildConfig") })
            dependencies {
                implementation(project(":kmp-screens"))
                implementation(project(":kmp-design"))
                implementation(project(":kmp-resources"))
                implementation(project(":modules:config"))
                implementation(project(":modules:di"))
                implementation(project(":modules:dynamic-ui"))

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)

                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}
