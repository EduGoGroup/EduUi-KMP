plugins {
    id("kmp.logic.core")
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
