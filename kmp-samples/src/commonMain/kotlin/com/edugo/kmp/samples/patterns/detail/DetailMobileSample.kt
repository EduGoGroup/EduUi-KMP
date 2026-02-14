@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailViewSampleContent(
    title: String,
    subtitle: String,
    tags: List<String>,
    description: String,
    details: List<Pair<String, String>>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DSTopAppBar(
                title = "Detalle",
                navigationIcon = {
                    DSIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        onClick = {},
                    )
                },
                actions = {
                    DSIconButton(
                        icon = Icons.Default.MoreVert,
                        contentDescription = "Mas opciones",
                        onClick = {},
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
        ) {
            // Hero image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Imagen del curso",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = Spacing.spacing6),
            ) {
                Spacer(Modifier.height(Spacing.spacing4))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(Spacing.spacing1))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.spacing4))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                    verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                ) {
                    tags.forEach { tag ->
                        DSChip(label = tag)
                    }
                }

                Spacer(Modifier.height(Spacing.spacing4))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(Spacing.spacing6))

                DSDivider()

                Spacer(Modifier.height(Spacing.spacing4))

                // Seccion Detalles
                Text(
                    text = "Detalles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                details.forEach { (key, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.spacing2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = key,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.spacing8))

                DSFilledButton(
                    text = "Inscribirse",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.spacing6))
            }
        }
    }
}

// --- Preview Data ---

internal val sampleTags = listOf("Matematicas", "Calculo", "Nivel Intermedio")

internal val sampleDetails = listOf(
    "Duracion" to "12 semanas",
    "Nivel" to "Intermedio",
    "Instructor" to "Prof. Maria Lopez",
    "Estudiantes" to "234 inscritos",
    "Idioma" to "Espanol",
)

internal const val SAMPLE_DESCRIPTION =
    "Este curso cubre los fundamentos del calculo diferencial e integral. " +
        "Aprenderas sobre limites, derivadas, integrales y sus aplicaciones " +
        "en problemas del mundo real. Incluye ejercicios practicos y " +
        "evaluaciones semanales para reforzar el aprendizaje."

// --- Previews ---

@Preview
@Composable
fun DetailPortraitLightPreview() {
    SamplePreview {
        Surface {
            DetailViewSampleContent(
                title = "Calculo Diferencial",
                subtitle = "Matematicas - Semestre 2",
                tags = sampleTags,
                description = SAMPLE_DESCRIPTION,
                details = sampleDetails,
            )
        }
    }
}

@Preview
@Composable
fun DetailPortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            DetailViewSampleContent(
                title = "Calculo Diferencial",
                subtitle = "Matematicas - Semestre 2",
                tags = sampleTags,
                description = SAMPLE_DESCRIPTION,
                details = sampleDetails,
            )
        }
    }
}

@Preview
@Composable
fun DetailLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DetailViewSampleContent(
                title = "Calculo Diferencial",
                subtitle = "Matematicas - Semestre 2",
                tags = sampleTags,
                description = SAMPLE_DESCRIPTION,
                details = sampleDetails,
            )
        }
    }
}

@Preview
@Composable
fun DetailLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DetailViewSampleContent(
                title = "Calculo Diferencial",
                subtitle = "Matematicas - Semestre 2",
                tags = sampleTags,
                description = SAMPLE_DESCRIPTION,
                details = sampleDetails,
            )
        }
    }
}
