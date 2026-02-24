package com.edugo.kmp.design.components.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.tokens.AnimationDuration

data class DSDrawerSection(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val children: List<DSDrawerChild> = emptyList(),
)

data class DSDrawerChild(
    val key: String,
    val label: String,
    val icon: ImageVector? = null,
    val selectedIcon: ImageVector? = null,
)

@Composable
fun DSExpandableDrawerContent(
    sections: List<DSDrawerSection>,
    selectedKey: String?,
    expandedKeys: Set<String>,
    onSectionClick: (DSDrawerSection) -> Unit,
    onChildClick: (DSDrawerChild) -> Unit,
    onExpandToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable (ColumnScope.() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = null,
) {
    PermanentDrawerSheet(
        modifier = modifier.width(280.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
        ) {
            if (header != null) {
                header()
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                sections.forEach { section ->
                    if (section.children.isEmpty()) {
                        // Leaf section - no children, direct click
                        NavigationDrawerItem(
                            label = { Text(section.label) },
                            selected = selectedKey == section.key,
                            onClick = { onSectionClick(section) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedKey == section.key) {
                                        section.selectedIcon ?: section.icon
                                    } else {
                                        section.icon
                                    },
                                    contentDescription = section.label,
                                )
                            },
                            modifier = Modifier.padding(horizontal = Spacing.spacing3),
                        )
                    } else {
                        // Parent section with children - expandable
                        val isExpanded = expandedKeys.contains(section.key)
                        val rotationAngle by animateFloatAsState(
                            targetValue = if (isExpanded) 180f else 0f,
                            animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
                            label = "chevron_${section.key}",
                        )

                        NavigationDrawerItem(
                            label = { Text(section.label) },
                            selected = false,
                            onClick = { onExpandToggle(section.key) },
                            icon = {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.label,
                                )
                            },
                            badge = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                                    modifier = Modifier.rotate(rotationAngle),
                                )
                            },
                            modifier = Modifier.padding(horizontal = Spacing.spacing3),
                        )

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically(
                                animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
                            ),
                            exit = shrinkVertically(
                                animationSpec = tween(durationMillis = AnimationDuration.medium1.toInt()),
                            ),
                        ) {
                            Column {
                                section.children.forEach { child ->
                                    NavigationDrawerItem(
                                        label = { Text(child.label) },
                                        selected = selectedKey == child.key,
                                        onClick = { onChildClick(child) },
                                        icon = if (child.icon != null) {
                                            {
                                                Icon(
                                                    imageVector = if (selectedKey == child.key) {
                                                        child.selectedIcon ?: child.icon
                                                    } else {
                                                        child.icon
                                                    },
                                                    contentDescription = child.label,
                                                )
                                            }
                                        } else null,
                                        modifier = Modifier.padding(
                                            start = Spacing.spacing14,
                                            end = Spacing.spacing3,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (footer != null) {
                footer()
            }
        }
    }
}
