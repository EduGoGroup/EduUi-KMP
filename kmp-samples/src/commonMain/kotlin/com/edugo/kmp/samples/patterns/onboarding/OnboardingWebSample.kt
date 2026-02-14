@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.onboarding

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

private data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val onboardingSteps = listOf(
    OnboardingStep(
        icon = Icons.Default.Star,
        title = "Bienvenido a EduGo",
        description = "Tu plataforma de aprendizaje personalizada",
    ),
    OnboardingStep(
        icon = Icons.Default.MenuBook,
        title = "Cursos para ti",
        description = "Descubre cursos adaptados a tus intereses y nivel",
    ),
    OnboardingStep(
        icon = Icons.Default.Notifications,
        title = "Mantente al dia",
        description = "Recibe notificaciones sobre tu progreso y novedades",
    ),
    OnboardingStep(
        icon = Icons.Default.CheckCircle,
        title = "Listo para empezar!",
        description = "Tu cuenta esta configurada",
    ),
)

@Composable
private fun OnboardingWebContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Onboarding - Web",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing8))

        // Horizontal layout: steps side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing4),
        ) {
            onboardingSteps.forEach { step ->
                DSElevatedCard(
                    modifier = Modifier.weight(1f),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.spacing4),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = step.icon,
                            contentDescription = null,
                            modifier = Modifier.size(Sizes.iconXLarge),
                            tint = MaterialTheme.colorScheme.primary,
                        )

                        Spacer(Modifier.height(Spacing.spacing4))

                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(Modifier.height(Spacing.spacing2))

                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Page indicators
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(onboardingSteps.size) { index ->
                val color = if (index == 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = Spacing.spacing1)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))

        DSFilledButton(
            text = "Comenzar",
            onClick = {},
            modifier = Modifier.width(300.dp),
        )
    }
}

// --- Previews ---

@Preview
@Composable
fun OnboardingWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingWebContent()
        }
    }
}

@Preview
@Composable
fun OnboardingWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingWebContent()
        }
    }
}
