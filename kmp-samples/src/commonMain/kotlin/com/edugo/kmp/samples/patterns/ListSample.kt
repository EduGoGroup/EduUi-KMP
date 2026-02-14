@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.feedback.DSEmptyState
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.samples.SamplePreview
import com.edugo.kmp.samples.data.Contact
import com.edugo.kmp.samples.data.SampleData
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ListSampleContent(
    contacts: List<Contact>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            DSTopAppBar(
                title = "Contactos",
                actions = {
                    DSIconButton(
                        icon = Icons.Default.Search,
                        contentDescription = "Buscar",
                        onClick = {},
                    )
                    DSIconButton(
                        icon = Icons.Default.Add,
                        contentDescription = "Agregar",
                        onClick = {},
                    )
                },
            )
        },
    ) { paddingValues ->
        if (contacts.isEmpty()) {
            DSEmptyState(
                icon = Icons.Default.People,
                title = "Sin contactos",
                description = "Agrega tu primer contacto para comenzar.",
                actionLabel = "Agregar contacto",
                onAction = {},
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
            ) {
                val grouped = contacts.groupBy { it.name.first().uppercaseChar() }
                    .toList()
                    .sortedBy { it.first }

                grouped.forEach { (letter, sectionContacts) ->
                    // Section header
                    Text(
                        text = letter.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(
                                horizontal = Spacing.spacing4,
                                vertical = Spacing.spacing2,
                            ),
                    )

                    sectionContacts.forEach { contact ->
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
                            onClick = {},
                        )
                    }
                }
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
fun ListSamplePreview() {
    SamplePreview {
        Surface {
            ListSampleContent(contacts = SampleData.contacts)
        }
    }
}

@Preview
@Composable
fun ListSampleEmptyPreview() {
    SamplePreview {
        Surface {
            ListSampleContent(contacts = emptyList())
        }
    }
}

@Preview
@Composable
fun ListSampleDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            ListSampleContent(contacts = SampleData.contacts)
        }
    }
}
