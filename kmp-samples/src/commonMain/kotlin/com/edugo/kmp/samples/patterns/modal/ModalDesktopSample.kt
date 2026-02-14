@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSRadioButton
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun ModalDesktopContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing6),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing6),
    ) {
        Text(
            text = "Variantes de Modal - Desktop",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // --- Center Dialog (wider) ---
        Text(
            text = "Center Dialog (600dp max)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        // Simulated overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center,
        ) {
            DSElevatedCard(
                modifier = Modifier.widthIn(max = 600.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.width(Spacing.spacing3))
                    Text(
                        text = "Confirmar Eliminacion",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(Modifier.height(Spacing.spacing4))

                Text(
                    text = "Esta seguro de eliminar este elemento? Esta accion no se puede deshacer. " +
                        "Todos los datos asociados seran eliminados permanentemente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.spacing6))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    DSOutlinedButton(
                        text = "Cancelar",
                        onClick = {},
                    )
                    Spacer(Modifier.width(Spacing.spacing2))
                    DSFilledButton(
                        text = "Eliminar",
                        onClick = {},
                    )
                }
            }
        }

        // --- Side Sheet ---
        Text(
            text = "Side Sheet (panel derecho 400dp)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
        ) {
            // Background content (simulated)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Contenido principal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Side sheet panel
            Surface(
                modifier = Modifier
                    .width(400.dp)
                    .fillMaxHeight(),
                shadowElevation = 8.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(Spacing.spacing4)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Nuevo Elemento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }

                    DSDivider()

                    Spacer(Modifier.height(Spacing.spacing4))

                    DSOutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = "Nombre",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.spacing3))

                    DSOutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = "Descripcion",
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.spacing3))

                    DSOutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = "Categoria",
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(Spacing.spacing6))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        DSOutlinedButton(
                            text = "Cancelar",
                            onClick = {},
                        )
                        Spacer(Modifier.width(Spacing.spacing2))
                        DSFilledButton(
                            text = "Guardar",
                            onClick = {},
                        )
                    }
                }
            }
        }

        // --- Bottom Sheet (wider, desktop sizing) ---
        Text(
            text = "Bottom Sheet (desktop sizing)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        var selectedOption by remember { mutableStateOf(1) }
        val options = listOf("Opcion A", "Opcion B", "Opcion C")

        DSElevatedCard(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = "Seleccionar Opcion",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
            ) {
                options.forEachIndexed { index, option ->
                    DSRadioButton(
                        selected = selectedOption == index,
                        onClick = { selectedOption = index },
                        label = option,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.spacing4))
            DSDivider()
            Spacer(Modifier.height(Spacing.spacing3))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                DSOutlinedButton(
                    text = "Cancelar",
                    onClick = {},
                )
                Spacer(Modifier.width(Spacing.spacing2))
                DSFilledButton(
                    text = "Aceptar",
                    onClick = {},
                )
            }
        }

        // --- Form Dialog (desktop) ---
        Text(
            text = "Form Dialog (desktop)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        FormDialogInlineCard()

        Spacer(Modifier.height(Spacing.spacing4))
    }
}

// --- Previews ---

@Preview
@Composable
fun ModalDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ModalDesktopContent()
        }
    }
}

@Preview
@Composable
fun ModalDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ModalDesktopContent()
        }
    }
}
