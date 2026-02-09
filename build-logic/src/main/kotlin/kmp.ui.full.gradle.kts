@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

val VERSION_CATALOG_NAME = "libs"
val COMPILE_SDK = 36
val MIN_SDK = 24
val JVM_TARGET = 17

val libs: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named(VERSION_CATALOG_NAME)

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.fromTarget(JVM_TARGET.toString()))
            }
        }
    }

    jvm("desktop") {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.fromTarget(JVM_TARGET.toString()))
            }
        }
    }

    wasmJs {
        browser()
        binaries.library()
    }

    val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
    if (enableIos) {
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(compose.materialIconsExtended)

                implementation(libs.findLibrary("koin-core")
                    .orElseThrow { IllegalStateException("Library 'koin-core' not found") })
                implementation(libs.findLibrary("koin-compose")
                    .orElseThrow { IllegalStateException("Library 'koin-compose' not found") })
                implementation(libs.findLibrary("koin-compose-viewmodel")
                    .orElseThrow { IllegalStateException("Library 'koin-compose-viewmodel' not found") })

                implementation(libs.findLibrary("androidx-lifecycle-viewmodel")
                    .orElseThrow { IllegalStateException("Library 'androidx-lifecycle-viewmodel' not found") })
                implementation(libs.findLibrary("androidx-lifecycle-runtime-compose")
                    .orElseThrow { IllegalStateException("Library 'androidx-lifecycle-runtime-compose' not found") })

                implementation(libs.findLibrary("kotlinx-coroutines-core")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-core' not found") })

                implementation(libs.findLibrary("kotlinx-serialization-json")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-serialization-json' not found") })
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.findLibrary("kotlinx-coroutines-test")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-test' not found") })
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.preview)

                implementation(libs.findLibrary("androidx-activity-compose")
                    .orElseThrow { IllegalStateException("Library 'androidx-activity-compose' not found") })

                implementation(libs.findLibrary("kotlinx-coroutines-android")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-android' not found") })

                implementation(libs.findLibrary("koin-android")
                    .orElseThrow { IllegalStateException("Library 'koin-android' not found") })
            }
        }

        val androidUnitTest by getting {
            dependencies {
                implementation(libs.findLibrary("kotlin-test-junit")
                    .orElseThrow { IllegalStateException("Library 'kotlin-test-junit' not found") })
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                implementation(libs.findLibrary("kotlinx-coroutines-swing")
                    .orElseThrow { IllegalStateException("Library 'kotlinx-coroutines-swing' not found") })
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.findLibrary("kotlin-test-junit")
                    .orElseThrow { IllegalStateException("Library 'kotlin-test-junit' not found") })
            }
        }

        val wasmJsMain by getting

        val wasmJsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        if (enableIos) {
            val iosX64Main by getting
            val iosArm64Main by getting
            val iosSimulatorArm64Main by getting
            val iosMain by creating {
                dependsOn(commonMain)
                iosX64Main.dependsOn(this)
                iosArm64Main.dependsOn(this)
                iosSimulatorArm64Main.dependsOn(this)
            }

            val iosX64Test by getting
            val iosArm64Test by getting
            val iosSimulatorArm64Test by getting
            val iosTest by creating {
                dependsOn(commonTest)
                iosX64Test.dependsOn(this)
                iosArm64Test.dependsOn(this)
                iosSimulatorArm64Test.dependsOn(this)
            }
        }
    }

    jvmToolchain(JVM_TARGET)
}

configure<LibraryExtension> {
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=androidx.compose.ui.ExperimentalComposeUiApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
}
