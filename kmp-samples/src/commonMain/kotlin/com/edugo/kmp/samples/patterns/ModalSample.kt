@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSRadioButton
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun ModalSampleContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing6),
    ) {
        Text(
            text = "Variantes de Modal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // --- Bottom Sheet ---
        Text(
            text = "Bottom Sheet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        BottomSheetInlineCard()

        // --- Alert Dialog ---
        Text(
            text = "Alert Dialog",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        AlertDialogInlineCard()

        // --- Fullscreen Dialog ---
        Text(
            text = "Fullscreen Dialog",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        FullscreenDialogInlineCard()

        // --- Form Dialog ---
        Text(
            text = "Form Dialog",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        FormDialogInlineCard()

        Spacer(Modifier.height(Spacing.spacing4))
    }
}

@Composable
private fun BottomSheetInlineCard() {
    var selectedOption by remember { mutableStateOf(1) }
    val options = listOf("Opcion A", "Opcion B", "Opcion C")

    DSElevatedCard(modifier = Modifier.fillMaxWidth()) {
        // Drag handle
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))

        Text(
            text = "Seleccionar Opcion",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing3))

        options.forEachIndexed { index, option ->
            DSRadioButton(
                selected = selectedOption == index,
                onClick = { selectedOption = index },
                label = option,
                modifier = Modifier.fillMaxWidth(),
            )
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
}

@Composable
private fun AlertDialogInlineCard() {
    DSElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
            text = "Esta seguro de eliminar este elemento? Esta accion no se puede deshacer.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = {}) {
                Text("Cancelar")
            }
            Spacer(Modifier.width(Spacing.spacing2))
            DSFilledButton(
                text = "Eliminar",
                onClick = {},
            )
        }
    }
}

@Composable
private fun FullscreenDialogInlineCard() {
    DSElevatedCard(modifier = Modifier.fillMaxWidth()) {
        // Simulated top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Close, contentDescription = null)
            }
            Text(
                text = "Nuevo Elemento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {}) {
                Text("Guardar")
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
    }
}

@Composable
private fun FormDialogInlineCard() {
    DSElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Agregar Comentario",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing4))

        DSOutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = "Escribe tu comentario aqui...",
            singleLine = false,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
        )

        Spacer(Modifier.height(Spacing.spacing4))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = {}) {
                Text("Cancelar")
            }
            Spacer(Modifier.width(Spacing.spacing2))
            DSFilledButton(
                text = "Enviar",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
fun ModalSamplePreview() {
    SamplePreview {
        Surface {
            ModalSampleContent()
        }
    }
}

@Preview
@Composable
fun ModalSampleDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            ModalSampleContent()
        }
    }
}
