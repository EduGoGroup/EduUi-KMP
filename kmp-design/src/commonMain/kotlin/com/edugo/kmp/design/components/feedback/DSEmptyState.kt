package com.edugo.kmp.design.components.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Componente de estado vacio para cuando no hay datos que mostrar.
 * Muestra un icono grande, titulo, descripcion opcional y boton de accion opcional.
 */
@Composable
fun DSEmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.spacing8),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(Sizes.iconXXLarge),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.spacing4))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (description != null) {
            Spacer(Modifier.height(Spacing.spacing2))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }

        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(Spacing.spacing6))
            DSFilledButton(
                text = actionLabel,
                onClick = onAction,
            )
        }
    }
}

// --- Previews ---

@Preview
@Composable
private fun DSEmptyStateBasicPreview() {
    EduGoTheme {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Search,
                title = "Sin resultados",
                description = "No encontramos resultados para tu busqueda. Intenta con otros terminos.",
                actionLabel = "Limpiar filtros",
                onAction = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSEmptyStateNoActionPreview() {
    EduGoTheme {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Info,
                title = "Aun no tienes cursos",
                description = "Cuando te inscribas en un curso aparecera aqui.",
            )
        }
    }
}

@Preview
@Composable
private fun DSEmptyStateErrorPreview() {
    EduGoTheme {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Warning,
                title = "Sin conexion",
                description = "Verifica tu conexion a internet e intenta nuevamente.",
                actionLabel = "Reintentar",
                onAction = {},
            )
        }
    }
}

@Preview
@Composable
private fun DSEmptyStateDarkPreview() {
    EduGoTheme(darkTheme = true) {
        Surface {
            DSEmptyState(
                icon = Icons.Default.Search,
                title = "Sin resultados",
                description = "No encontramos resultados para tu busqueda.",
                actionLabel = "Limpiar filtros",
                onAction = {},
            )
        }
    }
}
