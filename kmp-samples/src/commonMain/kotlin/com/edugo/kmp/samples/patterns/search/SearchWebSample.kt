@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.inputs.DSSearchBar
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.design.components.selection.DSChipVariant
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun SearchWebContent() {
    var query by remember { mutableStateOf("mate") }
    var active by remember { mutableStateOf(false) }
    val filterChips = listOf("Matematicas", "Ciencias", "Historia", "Idiomas")
    var selectedChip by remember { mutableStateOf<String?>(null) }
    val results = SampleData.searchResults

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Busqueda Responsiva - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Compact (list)
        Text(
            text = "Compact (lista)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.spacing2)) {
                DSSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    placeholder = "Buscar cursos...",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.spacing3))

                results.take(4).forEach { result ->
                    DSListItem(
                        headlineText = result.title,
                        supportingText = result.description,
                        trailingContent = {
                            Text(
                                text = "${"★".repeat(result.rating.toInt())} ${result.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                    DSDivider()
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing8))

        // Section: Expanded (grid + filters)
        Text(
            text = "Expanded (grilla + filtros)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.spacing2)) {
                DSSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    placeholder = "Buscar cursos...",
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(Spacing.spacing3))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
                ) {
                    // Left: Filters (25%)
                    Column(modifier = Modifier.weight(0.25f)) {
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(Modifier.height(Spacing.spacing2))

                        filterChips.forEach { chip ->
                            DSChip(
                                label = chip,
                                variant = DSChipVariant.FILTER,
                                selected = selectedChip == chip,
                                onClick = {
                                    selectedChip = if (selectedChip == chip) null else chip
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = Spacing.spacing2),
                            )
                        }

                        Spacer(Modifier.height(Spacing.spacing4))

                        Text(
                            text = "Recientes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(Modifier.height(Spacing.spacing2))

                        SampleData.recentSearches.forEach { search ->
                            DSListItem(
                                headlineText = search,
                                leadingContent = {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                },
                            )
                        }
                    }

                    // Right: Results grid (75%)
                    Column(modifier = Modifier.weight(0.75f)) {
                        Text(
                            text = "${results.size} resultados",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(Modifier.height(Spacing.spacing2))

                        results.take(6).chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
                            ) {
                                rowItems.forEach { result ->
                                    DSElevatedCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = Spacing.spacing3),
                                    ) {
                                        Text(
                                            text = result.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Spacer(Modifier.height(Spacing.spacing1))
                                        Text(
                                            text = result.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Spacer(Modifier.height(Spacing.spacing1))
                                        Text(
                                            text = "${"★".repeat(result.rating.toInt())}${"☆".repeat(5 - result.rating.toInt())} ${result.rating}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun SearchWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SearchWebContent()
        }
    }
}

@Preview
@Composable
fun SearchWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SearchWebContent()
        }
    }
}
