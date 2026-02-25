package com.edugo.kmp.screens.dynamic.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import kotlinx.coroutines.delay

/**
 * Componente de búsqueda modular con debounce.
 *
 * Filtra items client-side sobre la lista ya cargada.
 * Diseñado para ser reemplazado por búsqueda server-side en el futuro.
 */
@Composable
fun SearchBarComponent(
    query: String,
    onQueryChanged: (String) -> Unit,
    placeholder: String = "Buscar...",
    debounceMs: Long = 300L,
    modifier: Modifier = Modifier,
) {
    var localQuery by remember { mutableStateOf(query) }

    LaunchedEffect(localQuery) {
        if (localQuery != query) {
            delay(debounceMs)
            onQueryChanged(localQuery)
        }
    }

    LaunchedEffect(query) {
        if (query != localQuery) {
            localQuery = query
        }
    }

    DSOutlinedTextField(
        value = localQuery,
        onValueChange = { localQuery = it },
        placeholder = placeholder,
        leadingIcon = Icons.Default.Search,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Filtra una lista de JsonObjects basándose en un query de búsqueda.
 * Busca en todos los valores string primitivos del objeto.
 */
fun filterItems(
    items: List<kotlinx.serialization.json.JsonObject>,
    query: String,
): List<kotlinx.serialization.json.JsonObject> {
    if (query.isBlank()) return items
    val lowerQuery = query.lowercase()
    return items.filter { item ->
        item.values.any { value ->
            when (value) {
                is kotlinx.serialization.json.JsonPrimitive -> {
                    value.isString && value.content.lowercase().contains(lowerQuery)
                }
                else -> false
            }
        }
    }
}
