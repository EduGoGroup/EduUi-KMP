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

// Sprint 2 modules
include(":modules:config")
include(":modules:storage")
include(":modules:network")

// Sprint 3 modules
include(":modules:auth")
include(":modules:di")

// Sprint 4 modules (UI Layer)
include(":kmp-design")
include(":kmp-resources")
include(":kmp-screens")

// Sprint 4 platform apps
include(":platforms:mobile:app")
include(":platforms:desktop:app")
include(":platforms:web:app")
