package com.edugo.kmp.design.components.lists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.AnimationDuration
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * ListItem expandible con animacion de chevron y contenido colapsable.
 * Envuelve DSListItem con un trailing icon animado que rota 180 grados al expandir.
 */
@Composable
fun DSExpandableListItem(
    headlineContent: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    supportingContent: String? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    expandedContent: @Composable () -> Unit = {},
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
        label = "chevron_rotation",
    )

    Column(modifier = modifier) {
        ListItem(
            headlineContent = { Text(headlineContent) },
            modifier = Modifier.clickable(onClick = onToggle),
            supportingContent = supportingContent?.let { { Text(it) } },
            leadingContent = leadingContent,
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Colapsar" else "Expandir",
                    modifier = Modifier.rotate(rotationAngle),
                )
            },
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
            ),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
            ),
        ) {
            expandedContent()
        }
    }
}

// --- Previews ---

@Preview
@Composable
private fun DSExpandableListItemCollapsedPreview() {
    EduGoTheme {
        Surface {
            DSExpandableListItem(
                headlineContent = "Notificaciones",
                supportingContent = "Configurar alertas y sonidos",
                expanded = false,
                onToggle = {},
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
private fun DSExpandableListItemExpandedPreview() {
    EduGoTheme {
        Surface {
            DSExpandableListItem(
                headlineContent = "Notificaciones",
                supportingContent = "Configurar alertas y sonidos",
                expanded = true,
                onToggle = {},
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                    )
                },
            ) {
                Column(modifier = Modifier.padding(start = Spacing.spacing14)) {
                    DSListItem(
                        headlineText = "Push",
                        trailingContent = { Text("Activado") },
                    )
                    DSListItem(
                        headlineText = "Email",
                        trailingContent = { Text("Desactivado") },
                    )
                    DSListItem(
                        headlineText = "Sonido",
                        trailingContent = { Text("Activado") },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun DSExpandableListItemDarkPreview() {
    EduGoTheme(darkTheme = true) {
        Surface {
            Column {
                DSExpandableListItem(
                    headlineContent = "Apariencia",
                    expanded = true,
                    onToggle = {},
                ) {
                    Column(modifier = Modifier.padding(start = Spacing.spacing14)) {
                        DSListItem(headlineText = "Tema: Oscuro")
                        DSListItem(headlineText = "Tamano de texto: Normal")
                    }
                }
                DSExpandableListItem(
                    headlineContent = "Privacidad",
                    supportingContent = "Permisos y seguridad",
                    expanded = false,
                    onToggle = {},
                )
            }
        }
    }
}
