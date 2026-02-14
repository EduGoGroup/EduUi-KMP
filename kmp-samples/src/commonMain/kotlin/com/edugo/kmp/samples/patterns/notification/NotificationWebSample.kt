@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.overlays.DSSnackbar
import com.edugo.kmp.design.components.overlays.DSToast
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun NotificationWebSampleContent() {
    val notifications = SampleData.notifications

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Notificaciones Responsivas - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Compact layout
        Text(
            text = "Compact (ancho completo - inferior)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.spacing2),
                verticalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            ) {
                // Notification list stacked
                notifications.take(3).forEach { (title, description, time) ->
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

                // Snackbar full-width at bottom
                Text(
                    text = "Snackbar (ancho completo)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DSSnackbar(
                    message = "Tienes 3 notificaciones sin leer",
                    messageType = MessageType.INFO,
                    actionLabel = "Ver todas",
                    onAction = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                // Toast at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                ) {
                    DSToast(
                        message = "Notificacion marcada como leida",
                        visible = true,
                        onDismiss = {},
                        durationMs = Long.MAX_VALUE,
                        alignment = Alignment.BottomCenter,
                    )
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing8))

        // Section: Expanded layout
        Text(
            text = "Expanded (esquina inferior derecha)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.spacing2),
            ) {
                // Two columns: notifications (60%) + snackbar position (40%)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
                ) {
                    // Notification list (60%)
                    Column(modifier = Modifier.weight(0.6f)) {
                        Text(
                            text = "Lista de Notificaciones",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                    }

                    // Snackbar/Toast position (40%) - bottom-right corner style
                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "Posicion: Esquina inferior derecha",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.height(Spacing.spacing2))

                        DSSnackbar(
                            message = "Certificado descargado",
                            messageType = MessageType.SUCCESS,
                            actionLabel = "Abrir",
                            onAction = {},
                            modifier = Modifier.widthIn(max = 400.dp),
                        )

                        Spacer(Modifier.height(Spacing.spacing2))

                        DSSnackbar(
                            message = "Error de conexion",
                            messageType = MessageType.ERROR,
                            actionLabel = "Reintentar",
                            onAction = {},
                            modifier = Modifier.widthIn(max = 400.dp),
                        )

                        Spacer(Modifier.height(Spacing.spacing2))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                        ) {
                            DSToast(
                                message = "Cambios guardados",
                                visible = true,
                                onDismiss = {},
                                durationMs = Long.MAX_VALUE,
                                alignment = Alignment.BottomEnd,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun NotificationWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationWebSampleContent()
        }
    }
}

@Preview
@Composable
fun NotificationWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationWebSampleContent()
        }
    }
}
