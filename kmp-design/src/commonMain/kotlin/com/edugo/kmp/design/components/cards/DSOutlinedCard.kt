package com.edugo.kmp.design.components.cards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.tokens.CardSpacing

@Composable
fun DSOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick != null) {
        OutlinedCard(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
        ) {
            Column(modifier = Modifier.padding(CardSpacing.internalPadding)) {
                content()
            }
        }
    } else {
        OutlinedCard(modifier = modifier) {
            Column(modifier = Modifier.padding(CardSpacing.internalPadding)) {
                content()
            }
        }
    }
}
