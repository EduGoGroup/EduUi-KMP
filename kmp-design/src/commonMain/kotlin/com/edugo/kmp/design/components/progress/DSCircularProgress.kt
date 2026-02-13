package com.edugo.kmp.design.components.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.edugo.kmp.design.Sizes

@Composable
fun DSCircularProgress(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    size: Dp = Sizes.progressLarge,
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = progress,
            modifier = modifier,
        )
    } else {
        CircularProgressIndicator(modifier = modifier)
    }
}
