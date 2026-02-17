package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel
import com.edugo.kmp.screens.dynamic.DynamicScreen
import org.koin.compose.koinInject

/**
 * Lista de materiales con paginacion infinita.
 *
 * Usa DynamicScreen con screenKey="materials-list".
 * La accion item_click navega a material-detail con ID del item.
 * Los datos se cargan desde /v1/materials via DataLoader.
 */
@Composable
fun DynamicMaterialsListScreen(
    onNavigate: (String, Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DynamicScreenViewModel = koinInject(),
) {

    DynamicScreen(
        screenKey = "materials-list",
        viewModel = viewModel,
        onNavigate = onNavigate,
        modifier = modifier,
    )
}
