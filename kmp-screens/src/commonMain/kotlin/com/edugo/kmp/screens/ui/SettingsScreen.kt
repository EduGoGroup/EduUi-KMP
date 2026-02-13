@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.selection.DSRadioButton
import com.edugo.kmp.resources.InitStringsForPreview
import com.edugo.kmp.resources.Strings
import com.edugo.kmp.settings.model.ThemeOption
import com.edugo.kmp.settings.theme.ThemeService
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * Pantalla de configuración.
 *
 * Usa componentes DS: DSTopAppBar, DSRadioButton, DSFilledButton.
 * Se conecta a [ThemeService] para aplicar y persistir la preferencia de tema.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeService = koinInject<ThemeService>()
    val selectedTheme by themeService.themePreference.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            DSTopAppBar(
                title = Strings.settings_title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Strings.back_button
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.spacing4),
        ) {
            Text(
                text = Strings.settings_theme_section,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.spacing2),
            )

            Column(modifier = Modifier.selectableGroup()) {
                DSRadioButton(
                    label = Strings.settings_theme_light,
                    selected = selectedTheme == ThemeOption.LIGHT,
                    onClick = { themeService.setThemePreference(ThemeOption.LIGHT) },
                )

                DSRadioButton(
                    label = Strings.settings_theme_dark,
                    selected = selectedTheme == ThemeOption.DARK,
                    onClick = { themeService.setThemePreference(ThemeOption.DARK) },
                )

                DSRadioButton(
                    label = Strings.settings_theme_system,
                    selected = selectedTheme == ThemeOption.SYSTEM,
                    onClick = { themeService.setThemePreference(ThemeOption.SYSTEM) },
                )
            }

            Spacer(modifier = Modifier.height(Spacing.spacing6))

            DSFilledButton(
                text = Strings.settings_logout_button,
                onClick = onLogout,
                leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    InitStringsForPreview()
    EduGoTheme {
        SettingsScreen(
            onBack = {},
            onLogout = {},
        )
    }
}

@Preview
@Composable
private fun SettingsScreenDarkPreview() {
    InitStringsForPreview()
    EduGoTheme(darkTheme = true) {
        SettingsScreen(
            onBack = {},
            onLogout = {},
        )
    }
}
