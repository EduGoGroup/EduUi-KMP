package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.activeContext
import com.edugo.kmp.auth.service.currentUser
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

/**
 * Dashboard screen integrada con AuthService.
 *
 * Selecciona el screenKey segun el rol del usuario:
 * - "dashboard-teacher" para profesores
 * - "dashboard-student" para estudiantes
 *
 * Los KPIs se cargan automaticamente via DataLoader desde /v1/stats/global.
 */
@Composable
fun DynamicDashboardScreen(
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val authService = koinInject<AuthService>()
    val viewModel = koinInject<DynamicScreenViewModel>()

    val authState = authService.authState.value
    val context = authState.activeContext
    val user = authState.currentUser

    val screenKey = when {
        context?.hasRole("teacher") == true -> "dashboard-teacher"
        context?.hasRole("admin") == true -> "dashboard-teacher"
        else -> "dashboard-student"
    }

    val placeholders = remember(user, context) {
        buildPlaceholders(user, context)
    }

    DynamicScreen(
        screenKey = screenKey,
        viewModel = viewModel,
        onNavigate = onNavigate,
        modifier = modifier,
        placeholders = placeholders,
    )
}

private fun buildPlaceholders(
    user: com.edugo.kmp.auth.model.AuthUserInfo?,
    context: com.edugo.kmp.auth.model.UserContext?
): Map<String, String> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val dateFormatted = "${now.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${now.dayOfMonth}, ${now.year}"

    return buildMap {
        put("today_date", dateFormatted)
        user?.let {
            put("user.firstName", it.firstName)
            put("user.lastName", it.lastName)
            put("user.fullName", it.fullName)
            put("user.email", it.email)
            put("user.initials", it.getInitials())
        }
        context?.let {
            put("context.roleName", it.roleName)
            put("context.schoolName", it.schoolName ?: "")
        }
    }
}
