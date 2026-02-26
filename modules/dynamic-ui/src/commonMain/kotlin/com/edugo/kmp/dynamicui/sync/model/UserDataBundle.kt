package com.edugo.kmp.dynamicui.sync.model

import com.edugo.kmp.auth.model.MenuResponse
import com.edugo.kmp.auth.model.UserContext
import com.edugo.kmp.dynamicui.model.ScreenDefinition
import kotlinx.datetime.Instant

data class UserDataBundle(
    val menu: MenuResponse,
    val permissions: List<String>,
    val screens: Map<String, ScreenDefinition>,
    val availableContexts: List<UserContext>,
    val hashes: Map<String, String>,
    val syncedAt: Instant,
)
