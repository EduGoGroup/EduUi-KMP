package com.edugo.kmp.screens.dynamic.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.progress.DSSkeleton

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing4),
    ) {
        repeat(2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
            ) {
                DSSkeleton(
                    modifier = Modifier.weight(1f),
                    height = 100.dp,
                )
                DSSkeleton(
                    modifier = Modifier.weight(1f),
                    height = 100.dp,
                )
            }
        }
    }
}
