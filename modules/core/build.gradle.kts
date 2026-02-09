plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.core"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:foundation"))
            }
        }
    }
}
