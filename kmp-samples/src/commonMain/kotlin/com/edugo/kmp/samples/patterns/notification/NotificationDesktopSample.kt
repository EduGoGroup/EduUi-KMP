@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.MessageType
import com.edugo.kmp.design.SemanticColors
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSTextButton
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.overlays.DSSnackbar
import com.edugo.kmp.design.components.overlays.DSToast
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun NotificationDesktopSampleContent() {
    val notifications = SampleData.notifications

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: Main content area (70%)
        Box(
            modifier = Modifier
                .weight(0.7f)
                .fillMaxHeight()
                .padding(Spacing.spacing6),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Contenido Principal",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.spacing2))
                Text(
                    text = "Las notificaciones aparecen en el panel derecho",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // RIGHT: Notification panel (30%)
        Column(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.spacing4),
        ) {
            // Banner at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SemanticColors.infoContainer(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(Spacing.spacing3),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = SemanticColors.onInfoContainer(),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Actualizacion disponible",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SemanticColors.onInfoContainer(),
                        )
                        Text(
                            text = "Version 2.1 lista para instalar",
                            style = MaterialTheme.typography.bodySmall,
                            color = SemanticColors.onInfoContainer(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.spacing4))

            // Notification list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                DSTextButton(
                    text = "Marcar leidas",
                    onClick = {},
                )
            }

            Spacer(Modifier.height(Spacing.spacing2))

            notifications.forEach { (title, description, time) ->
                DSListItem(
                    headlineText = title,
                    supportingText = description,
                    leadingContent = {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    },
                    trailingContent = {
                        Text(
                            text = time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                DSDivider()
            }

            Spacer(Modifier.height(Spacing.spacing4))

            // Snackbar stack at bottom
            Text(
                text = "Snackbar (posicion inferior)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            DSSnackbar(
                message = "Curso completado exitosamente",
                messageType = MessageType.SUCCESS,
                actionLabel = "Ver certificado",
                onAction = {},
            )

            Spacer(Modifier.height(Spacing.spacing2))

            // Toast area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
            ) {
                DSToast(
                    message = "Guardado automaticamente",
                    visible = true,
                    onDismiss = {},
                    durationMs = Long.MAX_VALUE,
                    alignment = Alignment.BottomCenter,
                )
            }
        }
    }
}

@Preview
@Composable
fun NotificationDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationDesktopSampleContent()
        }
    }
}

@Preview
@Composable
fun NotificationDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationDesktopSampleContent()
        }
    }
}
