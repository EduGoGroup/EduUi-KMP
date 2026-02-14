@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun DialogsCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Dialogs Catalog", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(Spacing.spacing2))
        Text(
            "Los dialogs se muestran aqui como contenido inline dentro de cards para visualizar su estructura sin bloquear la UI.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        // --- DSAlertDialog - INFO ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSAlertDialog", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("INFO", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            AlertDialogContent(
                type = MessageType.INFO,
                title = "Informacion",
                message = "Tu sesion se cerrara automaticamente en 5 minutos.",
                confirmText = "Entendido",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("SUCCESS", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            AlertDialogContent(
                type = MessageType.SUCCESS,
                title = "Guardado exitoso",
                message = "Los cambios se han guardado correctamente.",
                confirmText = "Aceptar",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("WARNING", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            AlertDialogContent(
                type = MessageType.WARNING,
                title = "Atencion",
                message = "Esta accion no se puede deshacer. Deseas continuar?",
                confirmText = "Continuar",
                dismissText = "Cancelar",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("ERROR", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            AlertDialogContent(
                type = MessageType.ERROR,
                title = "Error",
                message = "No se pudo conectar con el servidor. Verifica tu conexion.",
                confirmText = "Reintentar",
                dismissText = "Cancelar",
            )
        }

        // --- DSBasicDialog ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSBasicDialog", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With confirm and dismiss", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                Text("Confirmar eliminacion", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.spacing2))
                Text(
                    "Estas seguro de eliminar este elemento? Esta accion no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(Spacing.spacing4))
                Row(modifier = Modifier.align(Alignment.End)) {
                    TextButton(onClick = {}) { Text("Cancelar") }
                    Spacer(Modifier.width(Spacing.spacing2))
                    TextButton(onClick = {}) { Text("Eliminar") }
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Confirm only", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                Text("Aviso", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(Spacing.spacing2))
                Text(
                    "Tu perfil ha sido actualizado correctamente.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(Spacing.spacing4))
                TextButton(
                    onClick = {},
                    modifier = Modifier.align(Alignment.End),
                ) { Text("Aceptar") }
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Composable
private fun AlertDialogContent(
    type: MessageType,
    title: String,
    message: String,
    confirmText: String,
    dismissText: String? = null,
) {
    val icon = when (type) {
        MessageType.INFO -> Icons.Default.Info
        MessageType.SUCCESS -> Icons.Default.CheckCircle
        MessageType.WARNING -> Icons.Default.Warning
        MessageType.ERROR -> Icons.Default.Error
    }
    val iconColor = when (type) {
        MessageType.INFO -> SemanticColors.info()
        MessageType.SUCCESS -> SemanticColors.success()
        MessageType.WARNING -> SemanticColors.warning()
        MessageType.ERROR -> SemanticColors.error()
    }

    Column(
        modifier = Modifier.padding(Spacing.spacing4),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(Sizes.iconXLarge),
        )
        Spacer(Modifier.height(Spacing.spacing3))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.spacing2))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.spacing4))
        Row(modifier = Modifier.align(Alignment.End)) {
            if (dismissText != null) {
                TextButton(onClick = {}) { Text(dismissText) }
                Spacer(Modifier.width(Spacing.spacing2))
            }
            TextButton(onClick = {}) { Text(confirmText) }
        }
    }
}

@Preview
@Composable
fun DialogsCatalogPreview() {
    SamplePreview {
        Surface {
            DialogsCatalog()
        }
    }
}

@Preview
@Composable
fun DialogsCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            DialogsCatalog()
        }
    }
}
