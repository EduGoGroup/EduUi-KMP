package com.edugo.kmp.design.components.selection

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.Sizes

enum class DSChipVariant { ASSIST, FILTER, INPUT, SUGGESTION }

@Composable
fun DSChip(
    label: String,
    modifier: Modifier = Modifier,
    variant: DSChipVariant = DSChipVariant.ASSIST,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    onDismiss: (() -> Unit)? = null,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    val iconSlot: (@Composable () -> Unit)? = leadingIcon?.let { icon ->
        {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMedium),
            )
        }
    }

    when (variant) {
        DSChipVariant.ASSIST -> AssistChip(
            onClick = onClick,
            label = { Text(label) },
            modifier = modifier,
            enabled = enabled,
            leadingIcon = iconSlot,
        )
        DSChipVariant.FILTER -> FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label) },
            modifier = modifier,
            enabled = enabled,
            leadingIcon = iconSlot,
        )
        DSChipVariant.INPUT -> InputChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label) },
            modifier = modifier,
            enabled = enabled,
            leadingIcon = iconSlot,
        )
        DSChipVariant.SUGGESTION -> SuggestionChip(
            onClick = onClick,
            label = { Text(label) },
            modifier = modifier,
            enabled = enabled,
            icon = iconSlot,
        )
    }
}
