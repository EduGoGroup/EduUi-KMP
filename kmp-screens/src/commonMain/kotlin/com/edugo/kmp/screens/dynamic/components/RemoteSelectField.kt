package com.edugo.kmp.screens.dynamic.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.edugo.kmp.dynamicui.viewmodel.DynamicScreenViewModel.SelectOptionsState
import com.edugo.kmp.resources.Strings

@Composable
fun RemoteSelectField(
    fieldKey: String,
    label: String?,
    placeholder: String?,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    optionsState: SelectOptionsState?,
    onLoadOptions: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    LaunchedEffect(fieldKey) {
        onLoadOptions()
    }

    when (optionsState) {
        is SelectOptionsState.Loading, null -> {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = label?.let { { Text(it) } },
                placeholder = { Text(Strings.select_loading) },
                enabled = false,
                readOnly = true,
                modifier = modifier.fillMaxWidth(),
            )
        }

        is SelectOptionsState.Success -> {
            SelectField(
                label = label,
                placeholder = placeholder,
                options = optionsState.options,
                selectedValue = selectedValue,
                onValueChange = onValueChange,
                isError = isError,
                supportingText = supportingText,
                modifier = modifier,
            )
        }

        is SelectOptionsState.Error -> {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = label?.let { { Text(it) } },
                placeholder = { Text(Strings.select_load_error) },
                supportingText = { Text(optionsState.message) },
                isError = true,
                enabled = false,
                readOnly = true,
                modifier = modifier.fillMaxWidth(),
            )
        }
    }
}
