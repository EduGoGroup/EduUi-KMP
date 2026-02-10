package com.edugo.kmp.screens.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.resources.InitStringsForPreview
import com.edugo.kmp.resources.Strings
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Opciones de tema disponibles.
 */
enum class ThemeOption {
    LIGHT,
    DARK,
    SYSTEM,
}

/**
 * Pantalla de configuración.
 *
 * Permite seleccionar tema (Light/Dark/System) y cerrar sesión.
 * Implementación simplificada sin ViewModel.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTheme by remember { mutableStateOf(ThemeOption.SYSTEM) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(Strings.settings_title) },
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
                .padding(Spacing.m),
        ) {
            Text(
                text = Strings.settings_theme_section,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = Spacing.xs),
            )

            Column(modifier = Modifier.selectableGroup()) {
                ThemeOptionRow(
                    label = Strings.settings_theme_light,
                    selected = selectedTheme == ThemeOption.LIGHT,
                    onClick = { selectedTheme = ThemeOption.LIGHT },
                )

                ThemeOptionRow(
                    label = Strings.settings_theme_dark,
                    selected = selectedTheme == ThemeOption.DARK,
                    onClick = { selectedTheme = ThemeOption.DARK },
                )

                ThemeOptionRow(
                    label = Strings.settings_theme_system,
                    selected = selectedTheme == ThemeOption.SYSTEM,
                    onClick = { selectedTheme = ThemeOption.SYSTEM },
                )
            }

            Spacer(modifier = Modifier.height(Spacing.l))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.iconMedium)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(Strings.settings_logout_button)
            }
        }
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Spacer(modifier = Modifier.width(Spacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
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
