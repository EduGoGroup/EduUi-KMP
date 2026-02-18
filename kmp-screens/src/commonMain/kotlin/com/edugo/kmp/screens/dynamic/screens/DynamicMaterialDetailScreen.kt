package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.action.ActionResult
import com.edugo.kmp.dynamicui.model.ActionType
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * Detalle de un material educativo.
 *
 * Usa DynamicScreen con screenKey="material-detail".
 * Recibe el ID del material como parametro y carga datos desde /v1/materials/{id}.
 * Soporta acciones: download, take quiz, go back.
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
        onNavigate = onNavigate,
        modifier = modifier,
        placeholders = mapOf("materialId" to materialId),
        onAction = { action, item, scope ->
            if (action.type == ActionType.NAVIGATE_BACK) {
                onBack()
            } else {
                scope.launch {
                    val result = viewModel.executeAction(action, item)
                    if (result is ActionResult.NavigateTo) {
                        onNavigate(result.screenKey, result.params)
                    }
                }
            }
        },
    )
}
