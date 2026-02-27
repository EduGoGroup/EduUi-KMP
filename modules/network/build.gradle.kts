plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.network"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
                api(project(":modules:logger"))
                implementation(project(":modules:core"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.encoding)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.ktor.client.mock)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val enableIos = findProperty("enableIos")?.toString()?.toBoolean() ?: false
        if (enableIos) {
            val iosMain by getting {
                dependencies {
                    implementation(libs.ktor.client.darwin)
                }
            }
        }
    }
}
