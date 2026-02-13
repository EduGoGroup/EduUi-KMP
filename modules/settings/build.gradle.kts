plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.settings"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:storage"))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
