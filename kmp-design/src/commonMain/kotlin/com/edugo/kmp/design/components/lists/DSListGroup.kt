package com.edugo.kmp.design.components.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.media.DSDivider

@Composable
fun DSListGroup(
    modifier: Modifier = Modifier,
    header: String? = null,
    showDividers: Boolean = true,
    items: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        if (header != null) {
            Text(
                text = header,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    horizontal = Spacing.spacing4,
                    vertical = Spacing.spacing2,
                ),
            )
        }
        items()
        if (showDividers) {
            DSDivider()
        }
    }
}
