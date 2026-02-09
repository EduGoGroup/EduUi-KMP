plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.storage"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
                implementation(project(":modules:logger"))
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.coroutines)
                implementation(libs.multiplatform.settings.make.observable)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.multiplatform.settings.test)
            }
        }
    }
}
