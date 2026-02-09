plugins {
    id("kmp.ui.full")
}

android {
    namespace = "com.edugo.kmp.screens"
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":kmp-design"))
                implementation(project(":kmp-resources"))
                implementation(project(":modules:di"))
            }
        }
    }
}
