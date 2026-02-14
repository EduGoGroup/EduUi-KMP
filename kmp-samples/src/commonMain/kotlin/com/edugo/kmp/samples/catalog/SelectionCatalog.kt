@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.selection.DSCheckbox
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.design.components.selection.DSChipVariant
import com.edugo.kmp.design.components.selection.DSRadioButton
import com.edugo.kmp.design.components.selection.DSSlider
import com.edugo.kmp.design.components.selection.DSSwitch
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Selection Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSCheckbox ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSCheckbox", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Checked", style = MaterialTheme.typography.labelMedium)
        var checked1 by remember { mutableStateOf(true) }
        DSCheckbox(
            checked = checked1,
            onCheckedChange = { checked1 = it },
            label = "Opcion seleccionada",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Unchecked", style = MaterialTheme.typography.labelMedium)
        var checked2 by remember { mutableStateOf(false) }
        DSCheckbox(
            checked = checked2,
            onCheckedChange = { checked2 = it },
            label = "Opcion no seleccionada",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled checked", style = MaterialTheme.typography.labelMedium)
        DSCheckbox(
            checked = true,
            onCheckedChange = {},
            label = "Deshabilitado seleccionado",
            enabled = false,
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled unchecked", style = MaterialTheme.typography.labelMedium)
        DSCheckbox(
            checked = false,
            onCheckedChange = {},
            label = "Deshabilitado no seleccionado",
            enabled = false,
        )

        // --- DSRadioButton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSRadioButton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Selected", style = MaterialTheme.typography.labelMedium)
        DSRadioButton(
            selected = true,
            onClick = {},
            label = "Opcion seleccionada",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Unselected", style = MaterialTheme.typography.labelMedium)
        DSRadioButton(
            selected = false,
            onClick = {},
            label = "Opcion no seleccionada",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSRadioButton(
            selected = true,
            onClick = {},
            label = "Deshabilitado",
            enabled = false,
        )

        // --- DSSwitch ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSSwitch", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("On", style = MaterialTheme.typography.labelMedium)
        var switchOn by remember { mutableStateOf(true) }
        DSSwitch(
            checked = switchOn,
            onCheckedChange = { switchOn = it },
            label = "Notificaciones push",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Off", style = MaterialTheme.typography.labelMedium)
        var switchOff by remember { mutableStateOf(false) }
        DSSwitch(
            checked = switchOff,
            onCheckedChange = { switchOff = it },
            label = "Modo oscuro",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled on", style = MaterialTheme.typography.labelMedium)
        DSSwitch(
            checked = true,
            onCheckedChange = {},
            label = "Deshabilitado activo",
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled off", style = MaterialTheme.typography.labelMedium)
        DSSwitch(
            checked = false,
            onCheckedChange = {},
            label = "Deshabilitado inactivo",
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSSlider ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSSlider", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With value 0.5", style = MaterialTheme.typography.labelMedium)
        var sliderValue by remember { mutableFloatStateOf(0.5f) }
        DSSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            label = "Volumen: ${(sliderValue * 100).toInt()}%",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With label", style = MaterialTheme.typography.labelMedium)
        var sliderValue2 by remember { mutableFloatStateOf(0.3f) }
        DSSlider(
            value = sliderValue2,
            onValueChange = { sliderValue2 = it },
            label = "Tamano de texto",
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSChip ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSChip", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("ASSIST", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2)) {
            DSChip(label = "Assist", variant = DSChipVariant.ASSIST)
            DSChip(label = "Con icono", variant = DSChipVariant.ASSIST, leadingIcon = Icons.Default.Star)
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("FILTER (selected / not selected)", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2)) {
            var filterSelected by remember { mutableStateOf(true) }
            DSChip(
                label = "Seleccionado",
                variant = DSChipVariant.FILTER,
                selected = filterSelected,
                onClick = { filterSelected = !filterSelected },
                leadingIcon = Icons.Default.Check,
            )
            DSChip(
                label = "No seleccionado",
                variant = DSChipVariant.FILTER,
                selected = false,
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("INPUT (selected / not selected)", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2)) {
            DSChip(
                label = "Seleccionado",
                variant = DSChipVariant.INPUT,
                selected = true,
                leadingIcon = Icons.Default.Favorite,
            )
            DSChip(
                label = "No seleccionado",
                variant = DSChipVariant.INPUT,
                selected = false,
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("SUGGESTION", style = MaterialTheme.typography.labelMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2)) {
            DSChip(label = "Matematicas", variant = DSChipVariant.SUGGESTION)
            DSChip(label = "Fisica", variant = DSChipVariant.SUGGESTION)
            DSChip(label = "Historia", variant = DSChipVariant.SUGGESTION)
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

// --- Phone ---
@Preview
@Composable
fun SelectionCatalogPreview() {
    SamplePreview {
        Surface {
            SelectionCatalog()
        }
    }
}

@Preview
@Composable
fun SelectionCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            SelectionCatalog()
        }
    }
}

// --- Tablet ---
@Preview
@Composable
fun SelectionCatalogTabletPreview() {
    SampleDevicePreview(device = DeviceSize.TABLET_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SelectionCatalog()
        }
    }
}

// --- Desktop ---
@Preview
@Composable
fun SelectionCatalogDesktopPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SelectionCatalog()
        }
    }
}
