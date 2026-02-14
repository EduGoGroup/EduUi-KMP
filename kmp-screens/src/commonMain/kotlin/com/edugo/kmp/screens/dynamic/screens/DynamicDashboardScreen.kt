package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.auth.service.currentUser
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
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

    val user = authService.authState.value.currentUser
    val screenKey = when {
        user?.hasRole("teacher") == true -> "dashboard-teacher"
        user?.hasRole("admin") == true -> "dashboard-teacher"
        else -> "dashboard-student"
    }

    DynamicScreen(
        screenKey = screenKey,
        viewModel = viewModel,
        onNavigate = onNavigate,
        modifier = modifier,
    )
}
