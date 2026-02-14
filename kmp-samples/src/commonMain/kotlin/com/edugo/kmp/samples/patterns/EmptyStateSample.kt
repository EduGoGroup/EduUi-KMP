@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EmptyStateSampleContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        // Variant 1: No data
        Text(
            text = "Sin datos",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        DSElevatedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            DSEmptyState(
                icon = Icons.Default.Info,
                title = "Aun no tienes cursos",
                description = "Explora nuestro catalogo",
                actionLabel = "Explorar Cursos",
                onAction = {},
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Variant 2: Network error
        Text(
            text = "Error de red",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        DSElevatedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            DSEmptyState(
                icon = Icons.Default.Warning,
                title = "Sin conexion",
                description = "Verifica tu conexion a internet",
                actionLabel = "Reintentar",
                onAction = {},
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Variant 3: No results
        Text(
            text = "Sin resultados",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        DSElevatedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            DSEmptyState(
                icon = Icons.Default.Search,
                title = "Sin resultados",
                description = "No encontramos resultados para tu busqueda",
                actionLabel = "Limpiar filtros",
                onAction = {},
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Variant 4: First use
        Text(
            text = "Primer uso",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(Spacing.spacing2))
        DSElevatedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            DSEmptyState(
                icon = Icons.Default.CheckCircle,
                title = "Comienza tu primer curso!",
                description = "Miles de cursos te esperan",
                actionLabel = "Empezar",
                onAction = {},
            )
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun EmptyStateSamplePreview() {
    SamplePreview {
        Surface {
            EmptyStateSampleContent()
        }
    }
}

@Preview
@Composable
fun EmptyStateSampleDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            EmptyStateSampleContent()
        }
    }
}
