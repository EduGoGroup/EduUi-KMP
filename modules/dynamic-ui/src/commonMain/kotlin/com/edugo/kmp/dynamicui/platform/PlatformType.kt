package com.edugo.kmp.dynamicui.platform

/**
 * Represents the platform type for adapting the UI.
 */
enum class PlatformType {
    ANDROID,
    IOS,
    DESKTOP,
    WEB
}

/**
 * Window size class for adaptive layouts, based on Material Design guidelines.
 */
enum class WindowSizeClass {
    COMPACT,   // < 600dp (phones)
    MEDIUM,    // 600-840dp (tablets)
    EXPANDED   // > 840dp (desktop/large tablets)
}

/**
 * Provides the current platform type at runtime.
 *
 * Since the dynamic-ui module doesn't have platform-specific source sets,
 * this uses a mutable object that should be set during app initialization.
 *
 * Example usage:
 * ```
 * // In Android Application.onCreate():
 * PlatformDetector.current = PlatformType.ANDROID
 *
 * // In iOS app init:
 * PlatformDetector.current = PlatformType.IOS
 * ```
 */
object PlatformDetector {
    var current: PlatformType = PlatformType.ANDROID
}
