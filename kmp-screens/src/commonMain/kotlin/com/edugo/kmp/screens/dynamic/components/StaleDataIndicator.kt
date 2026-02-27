package com.edugo.kmp.screens.dynamic.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edugo.kmp.resources.Strings

@Composable
fun StaleDataIndicator(
    isStale: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isStale) return

    SuggestionChip(
        onClick = { },
        label = {
            Text(
                text = Strings.stale_data_indicator,
                style = MaterialTheme.typography.labelSmall,
            )
        },
        modifier = modifier.padding(horizontal = 4.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = Color(0xFFFFF8E1),
            labelColor = Color(0xFFF57F17),
        ),
    )
}
