package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Alpha
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.resources.InitStringsForPreview
import com.edugo.kmp.resources.Strings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Pantalla principal - Home después de login exitoso.
 *
 * Muestra bienvenida, info card, botón de settings y logout.
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
                .padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = Strings.home_welcome,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Spacing.m)
            )

            Text(
                text = Strings.home_subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = Alpha.subtle),
                modifier = Modifier.padding(bottom = Spacing.xxl)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.xl),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.l)
                ) {
                    Text(
                        text = Strings.home_card_title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = Spacing.xs)
                    )
                    Text(
                        text = Strings.home_card_description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.muted)
                    )
                }
            }

            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.m)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = Strings.home_settings_button,
                    modifier = Modifier.size(Sizes.iconMedium)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(Strings.home_settings_button)
            }

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = Strings.home_logout_button,
                    modifier = Modifier.size(Sizes.iconMedium)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(Strings.home_logout_button)
            }
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
