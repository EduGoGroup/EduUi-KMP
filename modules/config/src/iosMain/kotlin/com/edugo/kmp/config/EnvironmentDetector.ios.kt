package com.edugo.kmp.config

import platform.Foundation.NSBundle

internal actual fun detectPlatformEnvironment(): Environment {
    // Strategy 1: Read from Info.plist (set via Xcode build schemes)
    val infoDictionary = NSBundle.mainBundle.infoDictionary
    val plistValue = infoDictionary?.get("AppEnvironment") as? String
    if (plistValue != null) {
        return Environment.fromString(plistValue) ?: Environment.DEV
    }

    // Strategy 2: Conservative default to DEV
    // Rationale: better to hit DEV accidentally than PROD
    return Environment.DEV
}
