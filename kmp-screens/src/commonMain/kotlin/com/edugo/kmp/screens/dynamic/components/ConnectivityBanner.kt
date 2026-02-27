package com.edugo.kmp.screens.dynamic.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.edugo.kmp.dynamicui.offline.SyncEngine
import com.edugo.kmp.resources.Strings

@Composable
fun ConnectivityBanner(
    isOnline: Boolean,
    pendingMutationCount: Int,
    syncState: SyncEngine.SyncState,
    modifier: Modifier = Modifier,
) {
    val showBanner = !isOnline || pendingMutationCount > 0 || syncState is SyncEngine.SyncState.Syncing

    AnimatedVisibility(
        visible = showBanner,
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier,
    ) {
        val backgroundColor: Color
        val text: String

        when {
            !isOnline -> {
                backgroundColor = Color(0xFFFFF3E0) // amber 50
                text = Strings.offline_banner
            }
            syncState is SyncEngine.SyncState.Syncing -> {
                backgroundColor = Color(0xFFE3F2FD) // blue 50
                text = Strings.offline_syncing(syncState.current, syncState.total)
            }
            pendingMutationCount > 0 -> {
                backgroundColor = Color(0xFFE3F2FD) // blue 50
                text = Strings.offline_pending(pendingMutationCount)
            }
            else -> {
                backgroundColor = Color(0xFFE3F2FD)
                text = ""
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (syncState is SyncEngine.SyncState.Syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (!isOnline) Color(0xFFE65100) else Color(0xFF1565C0),
            )
        }
    }
}
