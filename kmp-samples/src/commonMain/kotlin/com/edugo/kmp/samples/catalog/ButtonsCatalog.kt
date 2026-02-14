@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSElevatedButton
import com.edugo.kmp.design.components.buttons.DSExtendedFloatingActionButton
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSFloatingActionButton
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.buttons.DSIconButtonVariant
import com.edugo.kmp.design.components.buttons.DSMultiChoiceSegmentedButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.buttons.DSSegmentedButtonItem
import com.edugo.kmp.design.components.buttons.DSSingleChoiceSegmentedButton
import com.edugo.kmp.design.components.buttons.DSSmallFloatingActionButton
import com.edugo.kmp.design.components.buttons.DSTextButton
import com.edugo.kmp.design.components.buttons.DSTonalButton
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ButtonsCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Buttons Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSFilledButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSFilledButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Normal", style = MaterialTheme.typography.labelMedium)
        DSFilledButton(text = "Filled Button", onClick = {})

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSFilledButton(text = "Disabled", onClick = {}, enabled = false)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Loading", style = MaterialTheme.typography.labelMedium)
        DSFilledButton(text = "Loading...", onClick = {}, loading = true)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With leading icon", style = MaterialTheme.typography.labelMedium)
        DSFilledButton(text = "Favorito", onClick = {}, leadingIcon = Icons.Default.Favorite)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With trailing icon", style = MaterialTheme.typography.labelMedium)
        DSFilledButton(text = "Compartir", onClick = {}, trailingIcon = Icons.Default.Share)

        // --- DSOutlinedButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSOutlinedButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Normal", style = MaterialTheme.typography.labelMedium)
        DSOutlinedButton(text = "Outlined Button", onClick = {})

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSOutlinedButton(text = "Disabled", onClick = {}, enabled = false)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icon", style = MaterialTheme.typography.labelMedium)
        DSOutlinedButton(text = "Editar", onClick = {}, leadingIcon = Icons.Default.Edit)

        // --- DSElevatedButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSElevatedButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Normal", style = MaterialTheme.typography.labelMedium)
        DSElevatedButton(text = "Elevated Button", onClick = {})

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSElevatedButton(text = "Disabled", onClick = {}, enabled = false)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icon", style = MaterialTheme.typography.labelMedium)
        DSElevatedButton(text = "Favorito", onClick = {}, leadingIcon = Icons.Default.Star)

        // --- DSTonalButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSTonalButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Normal", style = MaterialTheme.typography.labelMedium)
        DSTonalButton(text = "Tonal Button", onClick = {})

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSTonalButton(text = "Disabled", onClick = {}, enabled = false)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icon", style = MaterialTheme.typography.labelMedium)
        DSTonalButton(text = "Configuracion", onClick = {}, leadingIcon = Icons.Default.Settings)

        // --- DSTextButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSTextButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Normal", style = MaterialTheme.typography.labelMedium)
        DSTextButton(text = "Text Button", onClick = {})

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSTextButton(text = "Disabled", onClick = {}, enabled = false)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icon", style = MaterialTheme.typography.labelMedium)
        DSTextButton(text = "Inicio", onClick = {}, leadingIcon = Icons.Default.Home)

        // --- DSIconButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSIconButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4)) {
            DSIconButtonVariant.entries.forEach { variant ->
                Column {
                    Text(variant.name, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(Spacing.spacing1))
                    DSIconButton(
                        icon = Icons.Default.Favorite,
                        contentDescription = variant.name,
                        onClick = {},
                        variant = variant,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4)) {
            DSIconButton(
                icon = Icons.Default.Favorite,
                contentDescription = "Disabled standard",
                onClick = {},
                enabled = false,
                variant = DSIconButtonVariant.STANDARD,
            )
            DSIconButton(
                icon = Icons.Default.Favorite,
                contentDescription = "Disabled filled",
                onClick = {},
                enabled = false,
                variant = DSIconButtonVariant.FILLED,
            )
        }

        // --- FABs ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("Floating Action Buttons", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("DSFloatingActionButton", style = MaterialTheme.typography.labelMedium)
        DSFloatingActionButton(
            icon = Icons.Default.Add,
            contentDescription = "Add",
            onClick = {},
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("DSSmallFloatingActionButton", style = MaterialTheme.typography.labelMedium)
        DSSmallFloatingActionButton(
            icon = Icons.Default.Add,
            contentDescription = "Add",
            onClick = {},
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("DSExtendedFloatingActionButton", style = MaterialTheme.typography.labelMedium)
        DSExtendedFloatingActionButton(
            text = "Nuevo",
            icon = Icons.Default.Add,
            contentDescription = "Add",
            onClick = {},
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Collapsed Extended FAB", style = MaterialTheme.typography.labelMedium)
        DSExtendedFloatingActionButton(
            text = "Nuevo",
            icon = Icons.Default.Add,
            contentDescription = "Add",
            onClick = {},
            expanded = false,
        )

        // --- SegmentedButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSSegmentedButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Single Choice", style = MaterialTheme.typography.labelMedium)
        var singleSelected by remember { mutableIntStateOf(0) }
        DSSingleChoiceSegmentedButton(
            items = listOf(
                DSSegmentedButtonItem("Dia"),
                DSSegmentedButtonItem("Semana"),
                DSSegmentedButtonItem("Mes"),
            ),
            selectedIndex = singleSelected,
            onSelected = { singleSelected = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Single Choice with icons", style = MaterialTheme.typography.labelMedium)
        var singleIconSelected by remember { mutableIntStateOf(1) }
        DSSingleChoiceSegmentedButton(
            items = listOf(
                DSSegmentedButtonItem("Inicio", Icons.Default.Home),
                DSSegmentedButtonItem("Favoritos", Icons.Default.Favorite),
                DSSegmentedButtonItem("Config", Icons.Default.Settings),
            ),
            selectedIndex = singleIconSelected,
            onSelected = { singleIconSelected = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Multi Choice", style = MaterialTheme.typography.labelMedium)
        var multiSelected by remember { mutableStateOf(setOf(0, 2)) }
        DSMultiChoiceSegmentedButton(
            items = listOf(
                DSSegmentedButtonItem("Lun"),
                DSSegmentedButtonItem("Mar"),
                DSSegmentedButtonItem("Mie"),
                DSSegmentedButtonItem("Jue"),
            ),
            selectedIndices = multiSelected,
            onSelectionChange = { multiSelected = it },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun ButtonsCatalogPreview() {
    SamplePreview {
        Surface {
            ButtonsCatalog()
        }
    }
}

@Preview
@Composable
fun ButtonsCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            ButtonsCatalog()
        }
    }
}
