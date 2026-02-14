@file:Suppress("DEPRECATION")

package com.edugo.kmp.design.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.components.overlays.DSSnackbar as NewDSSnackbar
import com.edugo.kmp.design.components.overlays.DSSnackbarHost as NewDSSnackbarHost

/**
 * @deprecated Moved to components.overlays package. Use [com.edugo.kmp.design.components.overlays.DSSnackbar] instead.
 */
@Deprecated(
    "Moved to overlays package",
    ReplaceWith(
        "com.edugo.kmp.design.components.overlays.DSSnackbar(message, modifier, type, actionLabel, onActionClick, null, duration)",
        "com.edugo.kmp.design.components.overlays.DSSnackbar",
    ),
)
@Composable
fun DSSnackbar(
    message: String,
    type: MessageType = MessageType.INFO,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    modifier: Modifier = Modifier,
) {
    NewDSSnackbar(
        message = message,
        modifier = modifier,
        messageType = type,
        actionLabel = actionLabel,
        onAction = onActionClick,
        duration = duration,
    )
}

/**
 * @deprecated Moved to components.overlays package. Use [com.edugo.kmp.design.components.overlays.DSSnackbarHost] instead.
 */
@Deprecated(
    "Moved to overlays package",
    ReplaceWith(
        "com.edugo.kmp.design.components.overlays.DSSnackbarHost(hostState, modifier, messageType)",
        "com.edugo.kmp.design.components.overlays.DSSnackbarHost",
    ),
)
@Composable
fun DSSnackbarHost(
    hostState: SnackbarHostState,
    messageType: MessageType = MessageType.INFO,
    modifier: Modifier = Modifier,
) {
    NewDSSnackbarHost(
        hostState = hostState,
        modifier = modifier,
        messageType = messageType,
    )
}
