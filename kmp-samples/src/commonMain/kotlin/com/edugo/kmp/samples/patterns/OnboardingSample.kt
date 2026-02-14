@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSTextButton
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OnboardingPageContent(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    activePageIndex: Int,
    totalPages: Int,
    buttonText: String,
    modifier: Modifier = Modifier,
    showSkipButton: Boolean = true,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.spacing6, vertical = Spacing.spacing8),
    ) {
        // Top row with skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            if (showSkipButton) {
                DSTextButton(
                    text = "Omitir",
                    onClick = {},
                )
            }
        }

        // Centered content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            icon()

            Spacer(Modifier.height(Spacing.spacing8))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(Spacing.spacing3))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Page indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.spacing6),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(totalPages) { index ->
                val color = if (index == activePageIndex) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = Spacing.spacing1)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }

        // Bottom button
        DSFilledButton(
            text = buttonText,
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun OnboardingPage1Content(
    modifier: Modifier = Modifier,
) {
    OnboardingPageContent(
        icon = {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMassive),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = "Bienvenido a EduGo",
        description = "Tu plataforma de aprendizaje personalizada",
        activePageIndex = 0,
        totalPages = 4,
        buttonText = "Siguiente",
        showSkipButton = true,
        modifier = modifier,
    )
}

@Composable
fun OnboardingLastPageContent(
    modifier: Modifier = Modifier,
) {
    OnboardingPageContent(
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(Sizes.iconMassive),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = "Listo para empezar!",
        description = "Tu cuenta esta configurada",
        activePageIndex = 3,
        totalPages = 4,
        buttonText = "Comenzar",
        showSkipButton = false,
        modifier = modifier,
    )
}

// --- Previews ---

@Preview
@Composable
fun OnboardingSamplePage1Preview() {
    SamplePreview {
        Surface {
            OnboardingPage1Content()
        }
    }
}

@Preview
@Composable
fun OnboardingSampleLastPagePreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            OnboardingLastPageContent()
        }
    }
}
