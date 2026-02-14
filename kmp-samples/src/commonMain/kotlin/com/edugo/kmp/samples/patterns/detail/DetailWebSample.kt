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
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailWebContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Detalle Responsivo - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Compact (hero on top)
        Text(
            text = "Compact (hero arriba)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.spacing2)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Imagen del curso",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }

                Spacer(Modifier.height(Spacing.spacing3))

                Text(
                    text = "Calculo Diferencial",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Matematicas - Semestre 2",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(Spacing.spacing2))

                Text(
                    text = SAMPLE_DESCRIPTION,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                DSFilledButton(
                    text = "Inscribirse",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Spacer(Modifier.height(Spacing.spacing8))

        // Section: Expanded (hero on side)
        Text(
            text = "Expanded (hero al lado)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Spacing.spacing2),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
            ) {
                // Left: Hero + tags (40%)
                Column(modifier = Modifier.weight(0.4f)) {
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

                    Spacer(Modifier.height(Spacing.spacing3))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                        verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                    ) {
                        sampleTags.forEach { tag ->
                            DSChip(label = tag)
                        }
                    }
                }

                // Right: Content (60%)
                Column(modifier = Modifier.weight(0.6f)) {
                    Text(
                        text = "Calculo Diferencial",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = "Matematicas - Semestre 2",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(Spacing.spacing3))

                    Text(
                        text = SAMPLE_DESCRIPTION,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(Modifier.height(Spacing.spacing4))

                    DSDivider()

                    Spacer(Modifier.height(Spacing.spacing3))

                    sampleDetails.forEach { (key, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.spacing1),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Spacer(Modifier.height(Spacing.spacing4))

                    DSFilledButton(
                        text = "Inscribirse",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun DetailWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DetailWebContent()
        }
    }
}

@Preview
@Composable
fun DetailWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            DetailWebContent()
        }
    }
}
