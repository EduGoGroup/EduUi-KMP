@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.lists.DSExpandableListItem
import com.edugo.kmp.design.components.lists.DSListGroup
import com.edugo.kmp.design.components.lists.DSListItem
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ListsCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Lists Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSListItem ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSListItem", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Headline only", style = MaterialTheme.typography.labelMedium)
        DSListItem(headlineText = "Elemento simple")

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Headline + supporting", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Ana Garcia",
            supportingText = "ana.garcia@email.com",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Headline + supporting + overline", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Curso de Matematicas",
            supportingText = "Fundamentos de algebra y calculo",
            overlineText = "CURSO ACTIVO",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With leading icon", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Notificaciones",
            supportingText = "Gestionar alertas",
            leadingContent = {
                Icon(Icons.Default.Notifications, contentDescription = null)
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With leading DSAvatar", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Bruno Lopez",
            supportingText = "bruno.lopez@email.com",
            leadingContent = {
                DSAvatar(initials = "BL", size = Sizes.Avatar.medium)
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With trailing icon", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Configuracion",
            leadingContent = {
                Icon(Icons.Default.Person, contentDescription = null)
            },
            trailingContent = {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With trailing text", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Idioma",
            trailingContent = {
                Text("Espanol", style = MaterialTheme.typography.bodyMedium)
            },
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Clickable", style = MaterialTheme.typography.labelMedium)
        DSListItem(
            headlineText = "Elemento clickable",
            supportingText = "Toca para ver detalles",
            leadingContent = {
                Icon(Icons.Default.Star, contentDescription = null)
            },
            trailingContent = {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            },
            onClick = {},
        )

        // --- DSListGroup ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSListGroup", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With header text", style = MaterialTheme.typography.labelMedium)
        DSListGroup(header = "Contactos recientes") {
            DSListItem(
                headlineText = "Ana Garcia",
                supportingText = "ana.garcia@email.com",
                leadingContent = {
                    DSAvatar(initials = "AG", size = Sizes.Avatar.medium)
                },
            )
            DSListItem(
                headlineText = "Bruno Lopez",
                supportingText = "bruno.lopez@email.com",
                leadingContent = {
                    DSAvatar(initials = "BL", size = Sizes.Avatar.medium)
                },
            )
            DSListItem(
                headlineText = "Carmen Rodriguez",
                supportingText = "carmen.rod@email.com",
                leadingContent = {
                    DSAvatar(initials = "CR", size = Sizes.Avatar.medium)
                },
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Without header and no dividers", style = MaterialTheme.typography.labelMedium)
        DSListGroup(showDividers = false) {
            DSListItem(
                headlineText = "Elemento 1",
                leadingContent = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
            )
            DSListItem(
                headlineText = "Elemento 2",
                leadingContent = {
                    Icon(Icons.Default.Star, contentDescription = null)
                },
            )
        }

        // --- DSExpandableListItem ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSExpandableListItem", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Collapsed", style = MaterialTheme.typography.labelMedium)
        var expanded1 by remember { mutableStateOf(false) }
        DSExpandableListItem(
            headlineContent = "Notificaciones",
            supportingContent = "Configurar alertas y sonidos",
            expanded = expanded1,
            onToggle = { expanded1 = !expanded1 },
            leadingContent = {
                Icon(Icons.Default.Notifications, contentDescription = null)
            },
        ) {
            Column(modifier = Modifier.padding(start = Spacing.spacing14)) {
                DSListItem(headlineText = "Push", trailingContent = { Text("Activado") })
                DSListItem(headlineText = "Email", trailingContent = { Text("Desactivado") })
                DSListItem(headlineText = "Sonido", trailingContent = { Text("Activado") })
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Expanded", style = MaterialTheme.typography.labelMedium)
        var expanded2 by remember { mutableStateOf(true) }
        DSExpandableListItem(
            headlineContent = "Apariencia",
            expanded = expanded2,
            onToggle = { expanded2 = !expanded2 },
        ) {
            Column(modifier = Modifier.padding(start = Spacing.spacing14)) {
                DSListItem(headlineText = "Tema: Claro")
                DSListItem(headlineText = "Tamano de texto: Normal")
                DSListItem(headlineText = "Densidad: Comoda")
            }
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun ListsCatalogPreview() {
    SamplePreview {
        Surface {
            ListsCatalog()
        }
    }
}

@Preview
@Composable
fun ListsCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            ListsCatalog()
        }
    }
}
