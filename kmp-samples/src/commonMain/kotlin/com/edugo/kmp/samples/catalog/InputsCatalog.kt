@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.edugo.kmp.design.components.inputs.DSFilledTextField
import com.edugo.kmp.design.components.inputs.DSOutlinedTextField
import com.edugo.kmp.design.components.inputs.DSPasswordField
import com.edugo.kmp.design.components.inputs.DSSearchBar
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputsCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Inputs Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSOutlinedTextField ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSOutlinedTextField", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Empty with label", style = MaterialTheme.typography.labelMedium)
        var outlined1 by remember { mutableStateOf("") }
        DSOutlinedTextField(
            value = outlined1,
            onValueChange = { outlined1 = it },
            label = "Nombre",
            placeholder = "Ingresa tu nombre",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With value", style = MaterialTheme.typography.labelMedium)
        var outlined2 by remember { mutableStateOf("Juan Perez") }
        DSOutlinedTextField(
            value = outlined2,
            onValueChange = { outlined2 = it },
            label = "Nombre completo",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With error and supporting text", style = MaterialTheme.typography.labelMedium)
        DSOutlinedTextField(
            value = "correo-invalido",
            onValueChange = {},
            label = "Email",
            isError = true,
            supportingText = "Formato de email invalido",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSOutlinedTextField(
            value = "No editable",
            onValueChange = {},
            label = "Campo deshabilitado",
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With leading icon", style = MaterialTheme.typography.labelMedium)
        var outlinedIcon by remember { mutableStateOf("") }
        DSOutlinedTextField(
            value = outlinedIcon,
            onValueChange = { outlinedIcon = it },
            label = "Email",
            placeholder = "usuario@ejemplo.com",
            leadingIcon = Icons.Default.Email,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With trailing icon", style = MaterialTheme.typography.labelMedium)
        var outlinedTrailing by remember { mutableStateOf("") }
        DSOutlinedTextField(
            value = outlinedTrailing,
            onValueChange = { outlinedTrailing = it },
            label = "Usuario",
            trailingIcon = Icons.Default.Person,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With supporting text", style = MaterialTheme.typography.labelMedium)
        var outlinedSupporting by remember { mutableStateOf("") }
        DSOutlinedTextField(
            value = outlinedSupporting,
            onValueChange = { outlinedSupporting = it },
            label = "Telefono",
            supportingText = "Incluye codigo de pais (+57)",
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSFilledTextField ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSFilledTextField", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Empty with label", style = MaterialTheme.typography.labelMedium)
        var filled1 by remember { mutableStateOf("") }
        DSFilledTextField(
            value = filled1,
            onValueChange = { filled1 = it },
            label = "Nombre",
            placeholder = "Ingresa tu nombre",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With value", style = MaterialTheme.typography.labelMedium)
        var filled2 by remember { mutableStateOf("Juan Perez") }
        DSFilledTextField(
            value = filled2,
            onValueChange = { filled2 = it },
            label = "Nombre completo",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With error", style = MaterialTheme.typography.labelMedium)
        DSFilledTextField(
            value = "dato-invalido",
            onValueChange = {},
            label = "Campo",
            isError = true,
            supportingText = "Este campo es obligatorio",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSFilledTextField(
            value = "No editable",
            onValueChange = {},
            label = "Deshabilitado",
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With icons", style = MaterialTheme.typography.labelMedium)
        var filledIcon by remember { mutableStateOf("") }
        DSFilledTextField(
            value = filledIcon,
            onValueChange = { filledIcon = it },
            label = "Buscar",
            leadingIcon = Icons.Default.Search,
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSPasswordField ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSPasswordField", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Empty", style = MaterialTheme.typography.labelMedium)
        var password1 by remember { mutableStateOf("") }
        DSPasswordField(
            value = password1,
            onValueChange = { password1 = it },
            label = "Contrasena",
            placeholder = "Ingresa tu contrasena",
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With value", style = MaterialTheme.typography.labelMedium)
        var password2 by remember { mutableStateOf("miPassword123") }
        DSPasswordField(
            value = password2,
            onValueChange = { password2 = it },
            label = "Contrasena",
            leadingIcon = Icons.Default.Lock,
            modifier = Modifier.fillMaxWidth(),
        )

        // --- DSSearchBar ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSSearchBar", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Empty", style = MaterialTheme.typography.labelMedium)
        var searchQuery1 by remember { mutableStateOf("") }
        var searchActive1 by remember { mutableStateOf(false) }
        DSSearchBar(
            query = searchQuery1,
            onQueryChange = { searchQuery1 = it },
            onSearch = { searchActive1 = false },
            active = searchActive1,
            onActiveChange = { searchActive1 = it },
            placeholder = "Buscar cursos...",
        )

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With query", style = MaterialTheme.typography.labelMedium)
        var searchQuery2 by remember { mutableStateOf("Matematicas") }
        var searchActive2 by remember { mutableStateOf(false) }
        DSSearchBar(
            query = searchQuery2,
            onQueryChange = { searchQuery2 = it },
            onSearch = { searchActive2 = false },
            active = searchActive2,
            onActiveChange = { searchActive2 = it },
            placeholder = "Buscar cursos...",
        )

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun InputsCatalogPreview() {
    SamplePreview {
        Surface {
            InputsCatalog()
        }
    }
}

@Preview
@Composable
fun InputsCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            InputsCatalog()
        }
    }
}
