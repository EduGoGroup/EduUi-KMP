package com.edugo.kmp.core.platform

import platform.UIKit.UIDevice

/**
 * iOS implementation of Platform.
 */
public actual object Platform {
    actual val name: String = "iOS"

    actual val osVersion: String
        get() = UIDevice.currentDevice.systemVersion

    actual val isDebug: Boolean = false
}
