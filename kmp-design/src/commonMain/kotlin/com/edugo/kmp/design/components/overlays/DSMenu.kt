package com.edugo.kmp.design.components.overlays

import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.Sizes

data class DSMenuItem(
    val text: String,
    val icon: ImageVector? = null,
    val enabled: Boolean = true,
    val isDivider: Boolean = false,
)

@Composable
fun DSMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<DSMenuItem>,
    onItemClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            if (item.isDivider) {
                HorizontalDivider()
            } else {
                DropdownMenuItem(
                    text = { Text(item.text) },
                    onClick = {
                        onItemClick(index)
                        onDismissRequest()
                    },
                    enabled = item.enabled,
                    leadingIcon = item.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.iconLarge),
                            )
                        }
                    },
                )
            }
        }
    }
}
