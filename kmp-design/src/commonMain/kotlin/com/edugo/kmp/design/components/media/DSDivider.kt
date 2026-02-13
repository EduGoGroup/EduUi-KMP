package com.edugo.kmp.design.components.media

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing

@Composable
fun DSDivider(
    modifier: Modifier = Modifier,
    inset: Dp = 0.dp,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = inset),
    )
}

@Composable
fun DSVerticalDivider(
    modifier: Modifier = Modifier,
) {
    VerticalDivider(modifier = modifier)
}

@Composable
fun DSInsetDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier.padding(start = Spacing.spacing4),
    )
}
