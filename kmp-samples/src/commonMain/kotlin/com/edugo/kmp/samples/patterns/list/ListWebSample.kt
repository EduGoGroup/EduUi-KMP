@file:Suppress("DEPRECATION")
package com.edugo.kmp.samples.patterns.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.data.Contact
import com.edugo.kmp.samples.data.SampleData

@Composable
private fun ListWebSampleContent() {
    val contacts = SampleData.contacts

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text(
            text = "Contactos - Vista Web Responsiva",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(Spacing.spacing4))

        // Section: Compact (1 column)
        Text(
            text = "Compact (1 columna)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
        ) {
            contacts.take(3).forEach { contact ->
                ContactCard(contact = contact)
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Medium (2 columns)
        Text(
            text = "Medium (2 columnas)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        val mediumContacts = contacts.take(4)
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
        ) {
            for (i in mediumContacts.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                ) {
                    ContactCard(
                        contact = mediumContacts[i],
                        modifier = Modifier.weight(1f),
                    )
                    if (i + 1 < mediumContacts.size) {
                        ContactCard(
                            contact = mediumContacts[i + 1],
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))

        // Section: Expanded (3 columns)
        Text(
            text = "Expanded (3 columnas)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(Modifier.height(Spacing.spacing2))

        val expandedContacts = contacts.take(6)
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing2),
        ) {
            for (i in expandedContacts.indices step 3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
                ) {
                    ContactCard(
                        contact = expandedContacts[i],
                        modifier = Modifier.weight(1f),
                    )
                    if (i + 1 < expandedContacts.size) {
                        ContactCard(
                            contact = expandedContacts[i + 1],
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    if (i + 2 < expandedContacts.size) {
                        ContactCard(
                            contact = expandedContacts[i + 2],
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    modifier: Modifier = Modifier,
) {
    DSOutlinedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.spacing4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DSAvatar(
                initials = contact.initials,
                size = Sizes.Avatar.large,
            )

            Spacer(Modifier.height(Spacing.spacing2))

            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(Spacing.spacing1))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing1),
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.iconSmall),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = contact.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(Spacing.spacing1))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing1),
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.iconSmall),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "+34 600 123 456",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview
@Composable
fun ListWebDesktopLightPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ListWebSampleContent()
        }
    }
}

@Preview
@Composable
fun ListWebDesktopDarkPreview() {
    SampleDevicePreview(device = DeviceSize.DESKTOP, darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize()) {
            ListWebSampleContent()
        }
    }
}
