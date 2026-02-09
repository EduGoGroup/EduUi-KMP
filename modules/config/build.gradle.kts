plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.config"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
            }
        }
    }
}
