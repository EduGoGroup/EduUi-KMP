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
    }
}
