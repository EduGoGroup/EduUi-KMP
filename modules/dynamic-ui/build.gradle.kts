plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.dynamicui"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
                implementation(project(":modules:network"))
                implementation(project(":modules:storage"))
                implementation(project(":modules:auth"))
                implementation(libs.koin.core)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.test)
                implementation(libs.turbine)
            }
        }
    }
}
