package com.edugo.kmp.design.components.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.SemanticColors
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Snackbar con estilos consistentes segun el tipo de mensaje.
 * Usa colores semanticos del design system para cada variante.
 */
@Composable
fun DSSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    messageType: MessageType = MessageType.INFO,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    val containerColor = when (messageType) {
        MessageType.INFO -> SemanticColors.infoContainer()
        MessageType.SUCCESS -> SemanticColors.successContainer()
        MessageType.WARNING -> SemanticColors.warningContainer()
        MessageType.ERROR -> SemanticColors.errorContainer()
    }

    val contentColor = when (messageType) {
        MessageType.INFO -> SemanticColors.onInfoContainer()
        MessageType.SUCCESS -> SemanticColors.onSuccessContainer()
        MessageType.WARNING -> SemanticColors.onWarningContainer()
        MessageType.ERROR -> SemanticColors.onErrorContainer()
    }

    Snackbar(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        dismissAction = if (onDismiss != null) {
            {
                TextButton(onClick = onDismiss) {
                    Text("X", color = contentColor)
                }
            }
        } else {
            null
        },
        action = if (actionLabel != null && onAction != null) {
            {
                TextButton(onClick = onAction) {
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
    modifier: Modifier = Modifier,
    messageType: MessageType = MessageType.INFO,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    ) { snackbarData ->
        DSSnackbar(
            message = snackbarData.visuals.message,
            messageType = messageType,
            actionLabel = snackbarData.visuals.actionLabel,
            onAction = { snackbarData.performAction() },
            duration = snackbarData.visuals.duration,
        )
    }
}

// --- Previews ---

@Preview
@Composable
private fun DSSnackbarInfoPreview() {
    EduGoTheme {
        Surface {
            DSSnackbar(
                message = "Informacion actualizada correctamente",
                messageType = MessageType.INFO,
                actionLabel = "Ver",
                onAction = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarSuccessPreview() {
    EduGoTheme {
        Surface {
            DSSnackbar(
                message = "Guardado exitosamente",
                messageType = MessageType.SUCCESS,
                actionLabel = "Deshacer",
                onAction = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarWarningPreview() {
    EduGoTheme {
        Surface {
            DSSnackbar(
                message = "Conexion inestable",
                messageType = MessageType.WARNING,
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarErrorPreview() {
    EduGoTheme {
        Surface {
            DSSnackbar(
                message = "Error al guardar los cambios",
                messageType = MessageType.ERROR,
                actionLabel = "Reintentar",
                onAction = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSnackbarAllVariantsPreview() {
    EduGoTheme {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                MessageType.entries.forEach { type ->
                    DSSnackbar(
                        message = "Mensaje de tipo ${type.name}",
                        messageType = type,
                        actionLabel = "Accion",
                        onAction = {},
                    )
                    Spacer(Modifier.height(Spacing.spacing2))
                }
            }
        }
    }
}

@Preview
@Composable
private fun DSSnackbarAllVariantsDarkPreview() {
    EduGoTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                MessageType.entries.forEach { type ->
                    DSSnackbar(
                        message = "Mensaje de tipo ${type.name}",
                        messageType = type,
                        actionLabel = "Accion",
                        onAction = {},
                    )
                    Spacer(Modifier.height(Spacing.spacing2))
                }
            }
        }
    }
}
