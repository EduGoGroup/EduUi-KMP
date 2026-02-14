@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.inputs.DSSearchBar
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.selection.DSChip
import com.edugo.kmp.design.components.selection.DSChipVariant
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import com.edugo.kmp.samples.data.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SearchSampleContent(
    initialQuery: String = "",
    showResults: Boolean = true,
) {
    var query by remember { mutableStateOf(initialQuery) }
    var active by remember { mutableStateOf(false) }
    val filterChips = listOf("Matematicas", "Ciencias", "Historia", "Idiomas")
    var selectedChip by remember { mutableStateOf<String?>(null) }
    val results = if (showResults) SampleData.searchResults else emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.spacing4, vertical = Spacing.spacing4),
    ) {
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
        ) {
            items(filterChips) { chip ->
                DSChip(
                    label = chip,
                    variant = DSChipVariant.FILTER,
                    selected = selectedChip == chip,
                    onClick = {
                        selectedChip = if (selectedChip == chip) null else chip
                    },
                )
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recientes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Limpiar",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {},
            )
        }

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

        DSDivider(modifier = Modifier.padding(vertical = Spacing.spacing4))

        if (results.isNotEmpty()) {
            Text(
                text = "Resultados",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            results.forEach { result ->
                DSElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Spacing.spacing12),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.spacing3))
                Text(
                    text = "No se encontraron resultados",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.spacing2))
                Text(
                    text = "Intenta con otros terminos de busqueda",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun SearchPortraitLightPreview() {
    SamplePreview {
        Surface {
            SearchSampleContent(initialQuery = "mate", showResults = true)
        }
    }
}

@Preview
@Composable
fun SearchPortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            SearchSampleContent(initialQuery = "mate", showResults = true)
        }
    }
}

@Preview
@Composable
fun SearchLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SearchSampleContent(initialQuery = "mate", showResults = true)
        }
    }
}

@Preview
@Composable
fun SearchLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            SearchSampleContent(initialQuery = "mate", showResults = true)
        }
    }
}

@Preview
@Composable
fun SearchSampleEmptyPreview() {
    SamplePreview {
        Surface {
            SearchSampleContent(initialQuery = "xyz", showResults = false)
        }
    }
}

@Preview
@Composable
fun SearchSampleDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            SearchSampleContent(initialQuery = "mate", showResults = true)
        }
    }
}
