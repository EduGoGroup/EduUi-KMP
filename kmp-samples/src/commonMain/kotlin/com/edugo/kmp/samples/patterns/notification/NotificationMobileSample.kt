@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.navigation.DSTopAppBarVariant
import com.edugo.kmp.design.components.overlays.DSSnackbar
import com.edugo.kmp.design.components.overlays.DSToast
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview

@Composable
private fun NotificationMobileSampleContent() {
    Scaffold(
        topBar = {
            DSTopAppBar(
                title = "Notificaciones",
                variant = DSTopAppBarVariant.SMALL,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.spacing4),
        ) {
            Spacer(Modifier.height(Spacing.spacing4))

            // Snackbar section
            Text(
                text = "Snackbar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            Text(
                text = "Informacion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.spacing1))
            DSSnackbar(
                message = "Informacion actualizada correctamente",
                messageType = MessageType.INFO,
                actionLabel = "Ver",
                onAction = {},
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Text(
                text = "Exito",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.spacing1))
            DSSnackbar(
                message = "Curso completado exitosamente",
                messageType = MessageType.SUCCESS,
                actionLabel = "Deshacer",
                onAction = {},
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Text(
                text = "Advertencia",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.spacing1))
            DSSnackbar(
                message = "Conexion inestable",
                messageType = MessageType.WARNING,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Text(
                text = "Error",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.spacing1))
            DSSnackbar(
                message = "Error al guardar los cambios",
                messageType = MessageType.ERROR,
                actionLabel = "Reintentar",
                onAction = {},
            )

            Spacer(Modifier.height(Spacing.spacing6))
            DSDivider()
            Spacer(Modifier.height(Spacing.spacing4))

            // Toast section
            Text(
                text = "Toast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            ) {
                DSToast(
                    message = "Progreso guardado automaticamente",
                    visible = true,
                    onDismiss = {},
                    durationMs = Long.MAX_VALUE,
                    alignment = Alignment.BottomCenter,
                )
            }

            Spacer(Modifier.height(Spacing.spacing6))
            DSDivider()
            Spacer(Modifier.height(Spacing.spacing4))

            // Banner section
            Text(
                text = "Banner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = SemanticColors.infoContainer(),
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(Spacing.spacing4),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = SemanticColors.onInfoContainer(),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nueva version disponible",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SemanticColors.onInfoContainer(),
                        )
                        Text(
                            text = "Actualiza la app para obtener las ultimas mejoras",
                            style = MaterialTheme.typography.bodySmall,
                            color = SemanticColors.onInfoContainer(),
                        )
                    }
                    DSTextButton(
                        text = "Actualizar",
                        onClick = {},
                    )
                }
            }

            Spacer(Modifier.height(Spacing.spacing4))
        }
    }
}

@Preview
@Composable
fun NotificationMobilePortraitLightPreview() {
    SamplePreview {
        Surface {
            NotificationMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun NotificationMobilePortraitDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            NotificationMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun NotificationMobileLandscapeLightPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationMobileSampleContent()
        }
    }
}

@Preview
@Composable
fun NotificationMobileLandscapeDarkPreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            NotificationMobileSampleContent()
        }
    }
}
