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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.auth.model.SchoolInfo
import com.edugo.kmp.auth.service.AuthService
import com.edugo.kmp.design.components.progress.DSLinearProgress
import com.edugo.kmp.dynamicui.sync.DataSyncService
import com.edugo.kmp.foundation.result.Result
import kotlinx.coroutines.launch
import com.edugo.kmp.resources.Strings
import org.koin.compose.koinInject

/**
 * Pantalla de selección de escuela mostrada post-login cuando el usuario
 * tiene acceso a múltiples escuelas. Permite seleccionar una escuela,
 * ejecutar switchContext + fullSync, y navegar al dashboard.
 */
@Composable
fun SchoolSelectionScreen(
    schools: List<SchoolInfo>,
    onSyncComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val authService = koinInject<AuthService>()
    val dataSyncService = koinInject<DataSyncService>()
    val scope = rememberCoroutineScope()

    var isSyncing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))

            Text(
                text = Strings.school_selection_title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = Strings.school_selection_description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))

            if (isSyncing) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        DSLinearProgress()
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = Strings.syncing_data,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                return@Surface
            }

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(schools, key = { it.id }) { school ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isSyncing = true
                                error = null
                                scope.launch {
                                    when (val switchResult = authService.switchContext(school.id)) {
                                        is Result.Success -> {
                                            // Light sync: menu+perms (~200ms) then navigate immediately
                                            dataSyncService.syncMenuAndPermissions()
                                            onSyncComplete()
                                            // Screens load in background
                                            dataSyncService.syncScreens()
                                        }
                                        is Result.Failure -> {
                                            isSyncing = false
                                            error = "Error al seleccionar escuela: ${switchResult.error}"
                                        }
                                        is Result.Loading -> {}
                                    }
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            Column {
                                Text(
                                    text = school.name,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
