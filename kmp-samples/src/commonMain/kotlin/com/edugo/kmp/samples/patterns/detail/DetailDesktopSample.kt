@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailDesktopContent() {
    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Hero image + tags (40%)
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing4),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Imagen del curso",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Spacer(Modifier.height(Spacing.spacing4))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                sampleTags.forEach { tag ->
                    DSChip(label = tag)
                }
            }
        }

        // RIGHT: Details (60%)
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing6),
        ) {
            Text(
                text = "Calculo Diferencial",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.spacing1))

            Text(
                text = "Matematicas - Semestre 2",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = SAMPLE_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Spacing.spacing6))

            DSDivider()

            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = "Detalles",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            // Details grid (2 columns)
            sampleDetails.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing6),
                ) {
                    rowItems.forEach { (key, value) ->
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(Spacing.spacing3))
            }

            Spacer(Modifier.height(Spacing.spacing6))

            DSFilledButton(
                text = "Inscribirse",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun DetailDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DetailDesktopContent()
        }
    }
}

@Preview
@Composable
fun DetailDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DetailDesktopContent()
        }
    }
}
