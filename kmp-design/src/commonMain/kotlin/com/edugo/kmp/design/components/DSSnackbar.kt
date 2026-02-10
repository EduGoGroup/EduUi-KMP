package com.edugo.kmp.design.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors

/**
 * Snackbar con estilos consistentes según el tipo de mensaje.
 */
@Composable
fun DSSnackbar(
    message: String,
    type: MessageType = MessageType.INFO,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (type) {
        MessageType.INFO -> SemanticColors.infoContainer()
        MessageType.SUCCESS -> SemanticColors.successContainer()
        MessageType.WARNING -> SemanticColors.warningContainer()
        MessageType.ERROR -> SemanticColors.errorContainer()
    }

    val contentColor = when (type) {
        MessageType.INFO -> SemanticColors.onInfoContainer()
        MessageType.SUCCESS -> SemanticColors.onSuccessContainer()
        MessageType.WARNING -> SemanticColors.onWarningContainer()
        MessageType.ERROR -> SemanticColors.onErrorContainer()
    }

    Snackbar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        action = if (actionLabel != null && onActionClick != null) {
            {
                TextButton(onClick = onActionClick) {
                    Text(actionLabel, color = contentColor)
                }
            }
        } else {
            null
        },
    ) {
        Text(message)
    }
}

/**
 * Host para mostrar snackbars con el estilo del design system.
 */
@Composable
fun DSSnackbarHost(
    hostState: SnackbarHostState,
    messageType: MessageType = MessageType.INFO,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        DSSnackbar(
            message = snackbarData.visuals.message,
            type = messageType,
            actionLabel = snackbarData.visuals.actionLabel,
            onActionClick = { snackbarData.performAction() },
            duration = snackbarData.visuals.duration,
        )
    }
}
