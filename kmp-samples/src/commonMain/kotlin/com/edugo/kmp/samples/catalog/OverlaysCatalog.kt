@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.overlays.DSMenu
import com.edugo.kmp.design.components.overlays.DSMenuItem
import com.edugo.kmp.design.components.overlays.DSPlainTooltip
import com.edugo.kmp.design.components.overlays.DSRichTooltip
import com.edugo.kmp.design.components.overlays.DSSnackbar
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlaysCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Overlays Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSSnackbar ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSSnackbar", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("INFO", style = MaterialTheme.typography.labelMedium)
        DSSnackbar(
            message = "Informacion actualizada correctamente",
            messageType = MessageType.INFO,
            actionLabel = "Ver",
            onAction = {},
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("SUCCESS", style = MaterialTheme.typography.labelMedium)
        DSSnackbar(
            message = "Guardado exitosamente",
            messageType = MessageType.SUCCESS,
            actionLabel = "Deshacer",
            onAction = {},
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("WARNING", style = MaterialTheme.typography.labelMedium)
        DSSnackbar(
            message = "Conexion inestable",
            messageType = MessageType.WARNING,
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("ERROR", style = MaterialTheme.typography.labelMedium)
        DSSnackbar(
            message = "Error al guardar los cambios",
            messageType = MessageType.ERROR,
            actionLabel = "Reintentar",
            onAction = {},
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With dismiss", style = MaterialTheme.typography.labelMedium)
        DSSnackbar(
            message = "Mensaje con boton de cerrar",
            messageType = MessageType.INFO,
            onDismiss = {},
        )

        // --- DSMenu ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSMenu", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Expanded menu (tap icon to toggle)", style = MaterialTheme.typography.labelMedium)
        Box {
            var menuExpanded by remember { mutableStateOf(false) }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Menu")
            }
            DSMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                items = listOf(
                    DSMenuItem("Editar", icon = Icons.Default.Edit),
                    DSMenuItem("Copiar", icon = Icons.Default.ContentCopy),
                    DSMenuItem("Compartir", icon = Icons.Default.Share),
                    DSMenuItem(text = "", isDivider = true),
                    DSMenuItem("Eliminar", icon = Icons.Default.Delete),
                ),
                onItemClick = { menuExpanded = false },
            )
        }

        // --- DSPlainTooltip ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSPlainTooltip", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Long press the icon to see tooltip", style = MaterialTheme.typography.labelMedium)
        DSPlainTooltip(tooltipText = "Esta es informacion adicional") {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
        }

        // --- DSRichTooltip ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSRichTooltip", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Long press the icon to see rich tooltip", style = MaterialTheme.typography.labelMedium)
        DSRichTooltip(
            title = "Titulo del tooltip",
            text = "Este tooltip contiene informacion mas detallada con titulo y una accion.",
            actionText = "Ver mas",
            onAction = {},
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Without action", style = MaterialTheme.typography.labelMedium)
        DSRichTooltip(
            title = "Ayuda",
            text = "Mantiene presionado un elemento para ver opciones adicionales.",
        ) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Info, contentDescription = "Help")
            }
        }

        // --- DSToast note ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSToast", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(Spacing.spacing4))
        Text(
            "DSToast requiere un contenedor Box(fillMaxSize) y un estado visible/onDismiss. Se usa en contexto de pantalla completa y no es representable inline en este catalogo.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun OverlaysCatalogPreview() {
    SamplePreview {
        Surface {
            OverlaysCatalog()
        }
    }
}

@Preview
@Composable
fun OverlaysCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            OverlaysCatalog()
        }
    }
}
