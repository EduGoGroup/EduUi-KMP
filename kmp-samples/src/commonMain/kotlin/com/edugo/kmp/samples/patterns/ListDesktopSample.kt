@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSOutlinedButton
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import com.edugo.kmp.samples.data.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun ListDesktopSampleContent() {
    val contacts = SampleData.contacts
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedContact = contacts[selectedIndex]

    Row(modifier = Modifier.fillMaxSize()) {
        // LEFT: List panel
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight(),
        ) {
            DSTopAppBar(title = "Contactos")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                contacts.forEachIndexed { index, contact ->
                    val isSelected = index == selectedIndex
                    val bgColor = if (isSelected) {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                        MaterialTheme.colorScheme.surface
                    }

                    DSListItem(
                        headlineText = contact.name,
                        supportingText = contact.email,
                        leadingContent = {
                            DSAvatar(initials = contact.initials)
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.iconLarge),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = { selectedIndex = index },
                        modifier = Modifier.background(bgColor),
                    )
                    DSDivider()
                }
            }
        }

        // RIGHT: Detail panel
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(Spacing.spacing8),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(Spacing.spacing8))

            DSAvatar(
                initials = selectedContact.initials,
                size = Sizes.Avatar.xxlarge,
            )

            Spacer(Modifier.height(Spacing.spacing4))

            Text(
                text = selectedContact.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(Spacing.spacing6))

            // Contact info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.iconLarge),
                )
                Text(
                    text = selectedContact.email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.spacing2))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(Sizes.iconLarge),
                )
                Text(
                    text = "+34 600 123 456",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.spacing8))

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            ) {
                DSFilledButton(
                    text = "Enviar mensaje",
                    onClick = {},
                    leadingIcon = Icons.Default.Send,
                )
                DSOutlinedButton(
                    text = "Editar",
                    onClick = {},
                    leadingIcon = Icons.Default.Edit,
                )
            }
        }
    }
}

@Preview
@Composable
fun ListDesktopSamplePreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ListDesktopSampleContent()
        }
    }
}

@Preview
@Composable
fun ListDesktopSampleDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ListDesktopSampleContent()
        }
    }
}
