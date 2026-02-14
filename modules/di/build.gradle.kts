plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.di"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.koin.core)
                implementation(project(":modules:foundation"))
                implementation(project(":modules:logger"))
                implementation(project(":modules:core"))
                implementation(project(":modules:validation"))
                implementation(project(":modules:network"))
                implementation(project(":modules:storage"))
                implementation(project(":modules:config"))
                implementation(project(":modules:auth"))
                implementation(project(":modules:settings"))
                implementation(project(":modules:dynamic-ui"))
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.koin.test)
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
