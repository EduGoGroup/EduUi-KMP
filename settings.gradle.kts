pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EduGoKmpModules"

// Sprint 1 modules
include(":modules:foundation")
include(":modules:logger")
include(":modules:core")
include(":modules:validation")
