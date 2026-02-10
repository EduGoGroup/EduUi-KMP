package com.edugo.kmp.design.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors

/**
 * Dialog de alerta con estilos consistentes según el tipo de mensaje.
 */
@Composable
fun DSAlertDialog(
    title: String,
    message: String,
    type: MessageType = MessageType.INFO,
    confirmText: String,
    dismissText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val icon: ImageVector = when (type) {
        MessageType.INFO -> Icons.Default.Info
        MessageType.SUCCESS -> Icons.Default.CheckCircle
        MessageType.WARNING -> Icons.Default.Warning
        MessageType.ERROR -> Icons.Default.Error
    }

    val iconColor = when (type) {
        MessageType.INFO -> SemanticColors.info()
        MessageType.SUCCESS -> SemanticColors.success()
        MessageType.WARNING -> SemanticColors.warning()
        MessageType.ERROR -> SemanticColors.error()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
            )
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
            ) {
                Text(confirmText)
            }
        },
        dismissButton = if (dismissText != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        } else {
            null
        },
        modifier = modifier,
    )
}
