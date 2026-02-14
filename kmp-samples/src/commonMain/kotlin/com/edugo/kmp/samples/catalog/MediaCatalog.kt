@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.media.DSAvatar
import com.edugo.kmp.design.components.media.DSBadge
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.media.DSInsetDivider
import com.edugo.kmp.design.components.media.DSVerticalDivider
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Media Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSAvatar ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSAvatar", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("All sizes with initials", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing3),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSAvatar(initials = "AG", size = Sizes.Avatar.small)
                Spacer(Modifier.height(Spacing.spacing1))
                Text("small", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSAvatar(initials = "BL", size = Sizes.Avatar.medium)
                Spacer(Modifier.height(Spacing.spacing1))
                Text("medium", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSAvatar(initials = "CR", size = Sizes.Avatar.large)
                Spacer(Modifier.height(Spacing.spacing1))
                Text("large", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSAvatar(initials = "DM", size = Sizes.Avatar.xlarge)
                Spacer(Modifier.height(Spacing.spacing1))
                Text("xlarge", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DSAvatar(initials = "EF", size = Sizes.Avatar.xxlarge)
                Spacer(Modifier.height(Spacing.spacing1))
                Text("xxlarge", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icon", style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.spacing3),
            verticalArrangement = Arrangement.spacedBy(Spacing.spacing3),
        ) {
            DSAvatar(icon = Icons.Default.Person, size = Sizes.Avatar.small)
            DSAvatar(icon = Icons.Default.Person, size = Sizes.Avatar.medium)
            DSAvatar(icon = Icons.Default.Person, size = Sizes.Avatar.large)
            DSAvatar(icon = Icons.Default.Person, size = Sizes.Avatar.xlarge)
            DSAvatar(icon = Icons.Default.Person, size = Sizes.Avatar.xxlarge)
        }

        // --- DSBadge ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSBadge", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("No count (dot badge)", style = MaterialTheme.typography.labelMedium)
        DSBadge {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Count = 3", style = MaterialTheme.typography.labelMedium)
        DSBadge(count = 3) {
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Count = 99", style = MaterialTheme.typography.labelMedium)
        DSBadge(count = 99) {
            Icon(Icons.Default.Email, contentDescription = "Email")
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Count = 150 (shows 99+)", style = MaterialTheme.typography.labelMedium)
        DSBadge(count = 150) {
            Icon(Icons.Default.Email, contentDescription = "Email")
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Count = 0 (no badge shown)", style = MaterialTheme.typography.labelMedium)
        DSBadge(count = 0) {
            Icon(Icons.Default.Star, contentDescription = "Star")
        }

        // --- DSDivider ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSDivider", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("DSDivider (full width)", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Spacing.spacing2))
        DSDivider()

        Spacer(Modifier.height(Spacing.spacing4))
        Text("DSInsetDivider (with start padding)", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Spacing.spacing2))
        DSInsetDivider()

        Spacer(Modifier.height(Spacing.spacing4))
        Text("DSVerticalDivider (in a Row)", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(Spacing.spacing2))
        Row(
            modifier = Modifier.height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Seccion A", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(Spacing.spacing4))
            DSVerticalDivider()
            Spacer(Modifier.width(Spacing.spacing4))
            Text("Seccion B", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(Spacing.spacing4))
            DSVerticalDivider()
            Spacer(Modifier.width(Spacing.spacing4))
            Text("Seccion C", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun MediaCatalogPreview() {
    SamplePreview {
        Surface {
            MediaCatalog()
        }
    }
}

@Preview
@Composable
fun MediaCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            MediaCatalog()
        }
    }
}
