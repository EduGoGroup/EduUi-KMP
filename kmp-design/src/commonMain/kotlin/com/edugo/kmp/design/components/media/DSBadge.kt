package com.edugo.kmp.design.components.media

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DSBadge(
    modifier: Modifier = Modifier,
    count: Int? = null,
    content: @Composable () -> Unit,
) {
    BadgedBox(
        badge = {
            if (count != null && count > 0) {
                Badge { Text(if (count > 99) "99+" else count.toString()) }
            } else if (count == null) {
                Badge()
            }
        },
        modifier = modifier,
        content = { content() },
    )
}
