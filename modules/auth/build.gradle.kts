plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.auth"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
                api(project(":modules:logger"))
                implementation(project(":modules:core"))
                implementation(project(":modules:validation"))
                implementation(project(":modules:network"))
                implementation(project(":modules:storage"))
                implementation(libs.ktor.client.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.ktor.client.mock)
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.test)
                implementation(libs.turbine)
            }
        }
    }
}
