package com.edugo.kmp.config

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * iOS implementation: loads from Bundle resources, falls back to DefaultConfigs.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun loadResourceAsString(path: String): String? {
    try {
        val parts = path.split("/")
        val fileName = parts.lastOrNull()?.substringBeforeLast(".")
        val fileExt = parts.lastOrNull()?.substringAfterLast(".")
        val directory = parts.dropLast(1).joinToString("/").takeIf { it.isNotEmpty() }

        if (fileName != null && fileExt != null) {
            val resourcePath = NSBundle.mainBundle.pathForResource(
                name = fileName,
                ofType = fileExt,
                inDirectory = directory
            )

            if (resourcePath != null) {
                val content = NSString.stringWithContentsOfFile(
                    path = resourcePath,
                    encoding = NSUTF8StringEncoding,
                    error = null
                ) as? String

                if (content != null) return content
            }
        }
    } catch (_: Exception) {
        // Bundle resource not found, fall through to fallback
    }

    return DefaultConfigs.get(path)
}
