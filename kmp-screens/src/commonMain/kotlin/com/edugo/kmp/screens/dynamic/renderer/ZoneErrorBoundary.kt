package com.edugo.kmp.screens.dynamic.renderer

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSTextButton
import com.edugo.kmp.logger.DefaultLogger

class ZoneErrorState {
    var error: Throwable? by mutableStateOf(null)
}

val LocalZoneError = compositionLocalOf<ZoneErrorState?> { null }

@Composable
fun ZoneErrorBoundary(
    zoneId: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val errorState = remember { ZoneErrorState() }
    var retryCount by remember { mutableStateOf(0) }

    val currentError = errorState.error
    if (currentError != null) {
        LaunchedEffect(currentError) {
            DefaultLogger.e("ZoneErrorBoundary", "Zone '$zoneId' failed: ${currentError.message}")
        }
        ZoneErrorPlaceholder(
            zoneId = zoneId,
            errorMessage = currentError.message ?: "Error desconocido",
            onRetry = {
                errorState.error = null
                retryCount++
            },
            modifier = modifier,
        )
    } else {
        key(retryCount) {
            CompositionLocalProvider(LocalZoneError provides errorState) {
                content()
            }
        }
    }
}

@Composable
fun ZoneErrorPlaceholder(
    zoneId: String,
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
            ),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.spacing3),
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing1),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(Sizes.iconSmall),
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = "Error en zona: $zoneId",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
                DSTextButton(
                    text = "Reintentar",
                    onClick = onRetry,
                    leadingIcon = Icons.Default.Refresh,
                )
            }
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
