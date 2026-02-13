@file:Suppress("DEPRECATION")

package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.tokens.SurfaceOpacity
import com.edugo.kmp.resources.InitStringsForPreview
import com.edugo.kmp.resources.Strings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pantalla principal - Home después de login exitoso.
 *
 * Usa componentes DS: DSElevatedCard, DSOutlinedButton, DSFilledButton.
 */
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = Strings.home_welcome,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.spacing4)
            )

            Text(
                text = Strings.home_subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = SurfaceOpacity.high),
                modifier = Modifier.padding(bottom = Spacing.spacing12)
            )

            DSElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.spacing8),
            ) {
                Text(
                    text = Strings.home_card_title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = Spacing.spacing2)
                )
                Text(
                    text = Strings.home_card_description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = SurfaceOpacity.high)
                )
            }

            DSOutlinedButton(
                text = Strings.home_settings_button,
                onClick = onNavigateToSettings,
                leadingIcon = Icons.Filled.Settings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.spacing4)
            )

            DSFilledButton(
                text = Strings.home_logout_button,
                onClick = onLogout,
                leadingIcon = Icons.AutoMirrored.Filled.Logout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
private fun HomeScreenPreview() {
    InitStringsForPreview()
    EduGoTheme {
        HomeScreen(
            onNavigateToSettings = {},
            onLogout = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenDarkPreview() {
    InitStringsForPreview()
    EduGoTheme(darkTheme = true) {
        HomeScreen(
            onNavigateToSettings = {},
            onLogout = {},
        )
    }
}
