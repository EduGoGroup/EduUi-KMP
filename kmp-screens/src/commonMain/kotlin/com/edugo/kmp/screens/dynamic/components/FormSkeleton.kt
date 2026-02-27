package com.edugo.kmp.screens.dynamic.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.progress.DSSkeleton

@Composable
fun FormSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing4),
    ) {
        repeat(3) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                DSSkeleton(
                    modifier = Modifier.fillMaxWidth(0.3f),
                    height = 14.dp,
                )
                DSSkeleton(height = 56.dp)
            }
        }
    }
}
