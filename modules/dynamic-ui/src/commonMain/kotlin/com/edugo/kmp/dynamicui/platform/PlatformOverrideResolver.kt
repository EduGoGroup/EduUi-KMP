package com.edugo.kmp.dynamicui.platform

import com.edugo.kmp.dynamicui.model.PlatformOverrides
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * Resolves and applies platformOverrides for a ScreenDefinition.
 *
 * Overrides can modify:
 * - distribution: layout distribution of the screen (centered-card, side-by-side, etc.)
 * - maxWidth: maximum width in dp
 * - zones: zone-specific overrides
 */
object PlatformOverrideResolver {

    fun resolve(
        screen: ScreenDefinition,
        platform: PlatformType = PlatformDetector.current
    ): ResolvedOverrides {
        val overrides = screen.template.platformOverrides ?: return ResolvedOverrides()

        val platformJson = when (platform) {
            PlatformType.ANDROID -> overrides.android
            PlatformType.IOS -> overrides.ios
            PlatformType.DESKTOP -> overrides.desktop
            PlatformType.WEB -> overrides.web
        } ?: return ResolvedOverrides()

        return ResolvedOverrides(
            distribution = platformJson["distribution"]?.jsonPrimitive?.content,
            maxWidth = platformJson["maxWidth"]?.jsonPrimitive?.intOrNull,
            zoneOverrides = platformJson["zones"] as? JsonObject
        )
    }
}

data class ResolvedOverrides(
    val distribution: String? = null,
    val maxWidth: Int? = null,
    val zoneOverrides: JsonObject? = null
)
