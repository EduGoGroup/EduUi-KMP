@file:Suppress("DEPRECATION")
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.edugo.kmp.samples.patterns.dynamic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.buttons.DSFilledButton
import com.edugo.kmp.design.components.buttons.DSIconButton
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.inputs.DSPasswordField
import com.edugo.kmp.design.components.media.DSDivider
import com.edugo.kmp.design.components.navigation.DSTopAppBar
import com.edugo.kmp.design.components.selection.DSCheckbox
import com.edugo.kmp.design.components.selection.DSRadioButton
import com.edugo.kmp.design.components.selection.DSSwitch

/**
 * Renderizador de formularios dinamicos.
 *
 * Recibe una [DynamicFormDefinition] (parseada de JSON) y dibuja
 * automaticamente todos los campos usando componentes del Design System.
 *
 * El estado de cada campo se mantiene en un mapa [fieldId -> valor].
 */
@Composable
fun DynamicFormRenderer(
    definition: DynamicFormDefinition,
    onSubmit: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fieldValues = remember { mutableStateMapOf<String, String>() }

    // Inicializar valores por defecto
    definition.sections.forEach { section ->
        section.fields.forEach { field ->
            if (field.id !in fieldValues) {
                fieldValues[field.id] = field.defaultValue
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            DSTopAppBar(
                title = definition.title,
                navigationIcon = {
                    DSIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        onClick = {},
                    )
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = Spacing.spacing6),
        ) {
            Spacer(Modifier.height(Spacing.spacing4))

            definition.sections.forEachIndexed { index, section ->
                // Titulo de seccion
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(Modifier.height(Spacing.spacing3))

                // Renderizar cada campo de la seccion
                section.fields.forEach { field ->
                    DynamicFieldRenderer(
                        field = field,
                        value = fieldValues[field.id] ?: "",
                        onValueChange = { fieldValues[field.id] = it },
                    )
                    Spacer(Modifier.height(Spacing.spacing3))
                }

                // Separador entre secciones
                if (index < definition.sections.lastIndex) {
                    Spacer(Modifier.height(Spacing.spacing3))
                    DSDivider()
                    Spacer(Modifier.height(Spacing.spacing4))
                }
            }

            Spacer(Modifier.height(Spacing.spacing6))

            DSFilledButton(
                text = definition.submitLabel,
                onClick = { onSubmit(fieldValues.toMap()) },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.spacing6))
        }
    }
}

/**
 * Renderiza un campo individual segun su [FieldType].
 * Mapea cada tipo de campo al componente DS correspondiente.
 */
@Composable
private fun DynamicFieldRenderer(
    field: DynamicField,
    value: String,
    onValueChange: (String) -> Unit,
) {
    when (field.type) {
        FieldType.TEXT, FieldType.DATE -> {
            DSOutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = buildLabel(field),
                placeholder = field.placeholder,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldType.EMAIL -> {
            DSOutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = buildLabel(field),
                placeholder = field.placeholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldType.NUMBER -> {
            DSOutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() || it == '.' }) {
                        onValueChange(newValue)
                    }
                },
                label = buildLabel(field),
                placeholder = field.placeholder,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldType.PASSWORD -> {
            DSPasswordField(
                value = value,
                onValueChange = onValueChange,
                label = buildLabel(field),
                placeholder = field.placeholder,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldType.TEXTAREA -> {
            DSOutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = buildLabel(field),
                placeholder = field.placeholder,
                singleLine = false,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldType.CHECKBOX -> {
            DSCheckbox(
                checked = value == "true",
                onCheckedChange = { onValueChange(it.toString()) },
                label = field.label,
            )
        }

        FieldType.SWITCH -> {
            DSSwitch(
                checked = value == "true",
                onCheckedChange = { onValueChange(it.toString()) },
                label = field.label,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        FieldType.RADIO -> {
            Text(
                text = buildLabel(field),
                style = MaterialTheme.typography.bodyMedium,
            )
            Column {
                field.options.forEach { option ->
                    DSRadioButton(
                        selected = value == option,
                        onClick = { onValueChange(option) },
                        label = option,
                    )
                }
            }
        }

        FieldType.SELECT -> {
            // Simulamos un select con RadioButtons en columna
            // (en produccion se usaria un DropdownMenu o ExposedDropdownMenu)
            Text(
                text = buildLabel(field),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.spacing2),
            ) {
                field.options.forEach { option ->
                    DSRadioButton(
                        selected = value == option,
                        onClick = { onValueChange(option) },
                        label = option,
                    )
                }
            }
        }
    }
}

private fun buildLabel(field: DynamicField): String =
    if (field.required) "${field.label} *" else field.label
