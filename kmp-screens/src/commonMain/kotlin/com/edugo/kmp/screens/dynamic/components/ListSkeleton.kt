package com.edugo.kmp.screens.dynamic.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.progress.DSSkeleton

@Composable
fun ListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.spacing4),
        verticalArrangement = Arrangement.spacedBy(Spacing.spacing3),
    ) {
        repeat(5) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DSSkeleton(
                    modifier = Modifier.size(48.dp),
                    height = 48.dp,
                )
                Spacer(modifier = Modifier.width(Spacing.spacing3))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                ) {
                    DSSkeleton(height = 16.dp)
                    DSSkeleton(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        height = 12.dp,
                    )
                }
            }
        }
    }
}
