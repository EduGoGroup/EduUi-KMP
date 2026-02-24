package com.edugo.kmp.screens.dynamic.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.config.AppConfig
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.network.EduGoHttpClient
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.compose.koinInject

@Serializable
private data class SchoolItem(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("code") val code: String = "",
    @SerialName("city") val city: String = "",
    @SerialName("is_active") val isActive: Boolean = true,
)

/**
 * School selector screen shown when a super_admin navigates to a
 * school-scoped section without having a school in their context.
 */
@Composable
fun SchoolSelectorScreen(
    onSchoolSelected: (schoolId: String, schoolName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Default EduGoHttpClient has AuthInterceptor (auto-adds bearer token)
    val httpClient = koinInject<EduGoHttpClient>()
    val appConfig = koinInject<AppConfig>()
    val authService = koinInject<AuthService>()
    val scope = rememberCoroutineScope()

    var schools by remember { mutableStateOf<List<SchoolItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSwitching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val url = "${appConfig.adminApiBaseUrl}/api/v1/schools"

        when (val result = httpClient.getSafe<List<SchoolItem>>(url)) {
            is Result.Success -> {
                schools = result.data.filter { it.isActive }
            }
            is Result.Failure -> {
                error = result.error
            }
            is Result.Loading -> { /* already loading */ }
        }
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Seleccionar escuela",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Selecciona una escuela para continuar a esta seccion",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(16.dp))

        when {
            isLoading || isSwitching -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    DSLinearProgress()
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error cargando escuelas: $error",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            schools.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay escuelas disponibles")
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(schools, key = { it.id }) { school ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isSwitching = true
                                    scope.launch {
                                        when (val switchResult = authService.switchContext(school.id)) {
                                            is Result.Success -> {
                                                onSchoolSelected(school.id, school.name)
                                            }
                                            is Result.Failure -> {
                                                isSwitching = false
                                                error = "Error al cambiar de escuela: ${switchResult.error}"
                                            }
                                            is Result.Loading -> { /* wait */ }
                                        }
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Column {
                                    Text(
                                        text = school.name,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    if (school.city.isNotBlank()) {
                                        Text(
                                            text = school.city,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
