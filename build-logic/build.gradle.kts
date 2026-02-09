plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.20")
    implementation("com.android.tools.build:gradle:8.12.0")
    implementation("org.jetbrains.compose:org.jetbrains.compose.gradle.plugin:1.9.0")
    implementation("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.2.20")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.2.20")
    implementation("org.jetbrains.kotlinx:kover-gradle-plugin:0.8.3")
}
