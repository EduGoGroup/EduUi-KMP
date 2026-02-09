plugins {
    id("kmp.android")
}

android {
    namespace = "com.edugo.kmp.logger"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:foundation"))
                implementation(project(":modules:core"))
                implementation(libs.kermit)
            }
        }
    }
}
