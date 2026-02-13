package com.edugo.kmp.design.components.progress

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DSLinearProgress(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = progress,
            modifier = modifier,
        )
    } else {
        LinearProgressIndicator(modifier = modifier)
    }
}
