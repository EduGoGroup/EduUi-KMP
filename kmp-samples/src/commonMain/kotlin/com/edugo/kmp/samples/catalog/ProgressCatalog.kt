@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.progress.DSCircularProgress
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.design.components.progress.DSSkeleton
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ProgressCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Progress Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSCircularProgress ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSCircularProgress", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Indeterminate", style = MaterialTheme.typography.labelMedium)
        DSCircularProgress()

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Determinate (0.3 / 0.7 / 1.0)", style = MaterialTheme.typography.labelMedium)
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing6),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSCircularProgress(progress = { 0.3f })
                Spacer(Modifier.height(Spacing.spacing1))
                Text("30%", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSCircularProgress(progress = { 0.7f })
                Spacer(Modifier.height(Spacing.spacing1))
                Text("70%", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSCircularProgress(progress = { 1.0f })
                Spacer(Modifier.height(Spacing.spacing1))
                Text("100%", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Small size", style = MaterialTheme.typography.labelMedium)
        DSCircularProgress(
            modifier = Modifier.size(Sizes.progressSmall),
            size = Sizes.progressSmall,
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Large size", style = MaterialTheme.typography.labelMedium)
        DSCircularProgress(
            modifier = Modifier.size(Sizes.progressLarge),
            size = Sizes.progressLarge,
        )

        // --- DSLinearProgress ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSLinearProgress", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Indeterminate", style = MaterialTheme.typography.labelMedium)
        DSLinearProgress(modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Determinate 30%", style = MaterialTheme.typography.labelMedium)
        DSLinearProgress(
            progress = { 0.3f },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Determinate 70%", style = MaterialTheme.typography.labelMedium)
        DSLinearProgress(
            progress = { 0.7f },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Determinate 100%", style = MaterialTheme.typography.labelMedium)
        DSLinearProgress(
            progress = { 1.0f },
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSSkeleton ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSSkeleton", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Text line skeleton", style = MaterialTheme.typography.labelMedium)
        DSSkeleton(modifier = Modifier.fillMaxWidth(), height = 16.dp)

        Spacer(Modifier.height(Spacing.spacing2))
        DSSkeleton(modifier = Modifier.fillMaxWidth(0.8f), height = 16.dp)

        Spacer(Modifier.height(Spacing.spacing2))
        DSSkeleton(modifier = Modifier.fillMaxWidth(0.6f), height = 16.dp)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Card skeleton", style = MaterialTheme.typography.labelMedium)
        DSSkeleton(modifier = Modifier.fillMaxWidth(), height = 120.dp)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Avatar skeleton", style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            DSSkeleton(
                modifier = Modifier.size(Sizes.Avatar.large),
                height = Sizes.Avatar.large,
                cornerRadius = Sizes.Avatar.large / 2,
            )
            Spacer(Modifier.width(Spacing.spacing3))
            Column {
                DSSkeleton(modifier = Modifier.width(150.dp), height = 14.dp)
                Spacer(Modifier.height(Spacing.spacing2))
                DSSkeleton(modifier = Modifier.width(100.dp), height = 12.dp)
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("List item skeleton", style = MaterialTheme.typography.labelMedium)
        repeat(3) {
            Row(
                modifier = Modifier.padding(vertical = Spacing.spacing2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DSSkeleton(
                    modifier = Modifier.size(Sizes.Avatar.medium),
                    height = Sizes.Avatar.medium,
                    cornerRadius = Sizes.Avatar.medium / 2,
                )
                Spacer(Modifier.width(Spacing.spacing3))
                Column(modifier = Modifier.weight(1f)) {
                    DSSkeleton(modifier = Modifier.fillMaxWidth(0.7f), height = 14.dp)
                    Spacer(Modifier.height(Spacing.spacing1))
                    DSSkeleton(modifier = Modifier.fillMaxWidth(0.5f), height = 12.dp)
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun ProgressCatalogPreview() {
    SamplePreview {
        Surface {
            ProgressCatalog()
        }
    }
}

@Preview
@Composable
fun ProgressCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            ProgressCatalog()
        }
    }
}
