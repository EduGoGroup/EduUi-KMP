package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import org.koin.compose.koinInject

/**
 * Detalle de un material educativo.
 *
 * Usa DynamicScreen con screenKey="material-detail".
 * Recibe el ID del material como parametro y carga datos desde /v1/materials/{id}.
 * Soporta acciones: download, take quiz, go back via custom events.
 */
@Composable
fun DynamicMaterialDetailScreen(
    materialId: String,
    onNavigate: (String, Map<String, String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinInject<DynamicScreenViewModel>()

    DynamicScreen(
        screenKey = "material-detail",
        viewModel = viewModel,
        onNavigate = { screenKey, params ->
            if (screenKey == "back") {
                onBack()
            } else {
                onNavigate(screenKey, params)
            }
        },
        modifier = modifier,
        placeholders = mapOf("materialId" to materialId),
    )
}
