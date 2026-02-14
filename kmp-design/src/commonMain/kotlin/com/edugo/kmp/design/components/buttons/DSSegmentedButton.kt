package com.edugo.kmp.design.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.design.Sizes
import com.edugo.kmp.design.Spacing
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Representa un item dentro de un segmented button.
 */
data class DSSegmentedButtonItem(
    val label: String,
    val icon: ImageVector? = null,
)

/**
 * Segmented button de seleccion unica.
 * Permite al usuario elegir una opcion de un grupo de opciones.
 */
@Composable
fun DSSingleChoiceSegmentedButton(
    items: List<DSSegmentedButtonItem>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        items.forEachIndexed { index, item ->
            SegmentedButton(
                selected = index == selectedIndex,
                onClick = { onSelected(index) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = items.size,
                ),
                icon = {
                    SegmentedButtonDefaults.Icon(active = index == selectedIndex) {
                        item.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.iconMedium),
                            )
                        }
                    }
                },
            ) {
                Text(item.label)
            }
        }
    }
}

/**
 * Segmented button de seleccion multiple.
 * Permite al usuario elegir una o mas opciones de un grupo.
 */
@Composable
fun DSMultiChoiceSegmentedButton(
    items: List<DSSegmentedButtonItem>,
    selectedIndices: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    MultiChoiceSegmentedButtonRow(modifier = modifier) {
        items.forEachIndexed { index, item ->
            val isSelected = index in selectedIndices
            SegmentedButton(
                checked = isSelected,
                onCheckedChange = {
                    val newSelection = selectedIndices.toMutableSet()
                    if (isSelected) {
                        newSelection.remove(index)
                    } else {
                        newSelection.add(index)
                    }
                    onSelectionChange(newSelection)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = items.size,
                ),
                icon = {
                    SegmentedButtonDefaults.Icon(active = isSelected) {
                        item.icon?.let { icon ->
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(Sizes.iconMedium),
                            )
                        }
                    }
                },
            ) {
                Text(item.label)
            }
        }
    }
}

// --- Previews ---

@Preview
@Composable
private fun DSSingleChoiceSegmentedButtonPreview() {
    EduGoTheme {
        Surface {
            DSSingleChoiceSegmentedButton(
                items = listOf(
                    DSSegmentedButtonItem("Dia"),
                    DSSegmentedButtonItem("Semana"),
                    DSSegmentedButtonItem("Mes"),
                ),
                selectedIndex = 1,
                onSelected = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSingleChoiceWithIconsPreview() {
    EduGoTheme {
        Surface {
            DSSingleChoiceSegmentedButton(
                items = listOf(
                    DSSegmentedButtonItem("Inicio", Icons.Default.Home),
                    DSSegmentedButtonItem("Favoritos", Icons.Default.Favorite),
                    DSSegmentedButtonItem("Config", Icons.Default.Settings),
                ),
                selectedIndex = 0,
                onSelected = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSMultiChoiceSegmentedButtonPreview() {
    EduGoTheme {
        Surface {
            DSMultiChoiceSegmentedButton(
                items = listOf(
                    DSSegmentedButtonItem("Lunes"),
                    DSSegmentedButtonItem("Martes"),
                    DSSegmentedButtonItem("Miercoles"),
                    DSSegmentedButtonItem("Jueves"),
                ),
                selectedIndices = setOf(0, 2),
                onSelectionChange = {},
                modifier = Modifier.padding(Spacing.spacing4),
            )
        }
    }
}

@Preview
@Composable
private fun DSSegmentedButtonDarkPreview() {
    EduGoTheme(darkTheme = true) {
        Surface {
            Column(modifier = Modifier.padding(Spacing.spacing4)) {
                DSSingleChoiceSegmentedButton(
                    items = listOf(
                        DSSegmentedButtonItem("Light"),
                        DSSegmentedButtonItem("Dark"),
                        DSSegmentedButtonItem("System"),
                    ),
                    selectedIndex = 1,
                    onSelected = {},
                )
                Spacer(Modifier.height(Spacing.spacing4))
                DSMultiChoiceSegmentedButton(
                    items = listOf(
                        DSSegmentedButtonItem("A", Icons.Default.Star),
                        DSSegmentedButtonItem("B", Icons.Default.Favorite),
                        DSSegmentedButtonItem("C", Icons.Default.Check),
                    ),
                    selectedIndices = setOf(0, 2),
                    onSelectionChange = {},
                )
            }
        }
    }
}
