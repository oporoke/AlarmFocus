package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.utils.AlarmDiagnostics
import kotlinx.coroutines.launch

@Composable
fun DiagnosticsScreen(
    alarmRepository: AlarmRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val diagnostics = remember { AlarmDiagnostics(context, alarmRepository) }

    var systemHealth by remember { mutableStateOf<AlarmDiagnostics.SystemHealth?>(null) }
    var batteryStatus by remember { mutableStateOf<AlarmDiagnostics.BatteryStatus?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showLogs by remember { mutableStateOf(false) }
    var logs by remember { mutableStateOf("") }

    // Perform initial health check
    LaunchedEffect(Unit) {
        isLoading = true
        systemHealth = diagnostics.performHealthCheck()
        batteryStatus = diagnostics.getBatteryStatus()
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "System Diagnostics",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Monitor alarm system health and troubleshoot issues",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        systemHealth = diagnostics.performHealthCheck()
                        batteryStatus = diagnostics.getBatteryStatus()
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Refresh")
            }

            OutlinedButton(
                onClick = {
                    if (!showLogs) {
                        scope.launch {
                            logs = diagnostics.getDiagnosticLogs()
                            showLogs = true
                        }
                    } else {
                        showLogs = false
                    }
                }
            ) {
                Text(if (showLogs) "Hide Logs" else "View Logs")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Overall Health Status
                item {
                    systemHealth?.let { health ->
                        OverallHealthCard(health)
                    }
                }

                // Battery Status
                item {
                    batteryStatus?.let { battery ->
                        BatteryStatusCard(battery)
                    }
                }

                // Individual Diagnostic Results
                systemHealth?.results?.let { results ->
                    items(results) { result ->
                        DiagnosticResultCard(
                            result = result,
                            onActionClick = { action ->
                                try {
                                    context.startActivity(action.intent)
                                } catch (e: Exception) {
                                    // Handle case where intent can't be started
                                }
                            }
                        )
                    }
                }

                // Logs Section
                if (showLogs) {
                    item {
                        LogsCard(
                            logs = logs,
                            onClearLogs = {
                                scope.launch {
                                    diagnostics.clearLogs()
                                    logs = "Logs cleared"
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverallHealthCard(health: AlarmDiagnostics.SystemHealth) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (health.overallStatus) {
                AlarmDiagnostics.HealthStatus.HEALTHY -> MaterialTheme.colorScheme.primaryContainer
                AlarmDiagnostics.HealthStatus.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                AlarmDiagnostics.HealthStatus.CRITICAL -> MaterialTheme.colorScheme.errorContainer
                AlarmDiagnostics.HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Overall System Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = health.overallStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    when (health.overallStatus) {
                        AlarmDiagnostics.HealthStatus.HEALTHY -> Icons.Default.CheckCircle
                        AlarmDiagnostics.HealthStatus.WARNING -> Icons.Default.Warning
                        AlarmDiagnostics.HealthStatus.CRITICAL -> Icons.Default.Error
                        AlarmDiagnostics.HealthStatus.UNKNOWN -> Icons.Default.Help
                    },
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = when (health.overallStatus) {
                        AlarmDiagnostics.HealthStatus.HEALTHY -> MaterialTheme.colorScheme.onPrimaryContainer
                        AlarmDiagnostics.HealthStatus.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                        AlarmDiagnostics.HealthStatus.CRITICAL -> MaterialTheme.colorScheme.onErrorContainer
                        AlarmDiagnostics.HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (health.criticalIssues > 0) {
                    Text(
                        text = "${health.criticalIssues} critical",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (health.warnings > 0) {
                    Text(
                        text = "${health.warnings} warnings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                Text(
                    text = "Last checked: ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(health.lastChecked))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BatteryStatusCard(battery: AlarmDiagnostics.BatteryStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                battery.isCritical -> MaterialTheme.colorScheme.errorContainer
                battery.isLow -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Battery Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${battery.level}% • ${if (battery.isCharging) "Charging" else "Not charging"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (battery.isLow) {
                    Text(
                        text = "Low battery may affect alarm reliability",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Icon(
                if (battery.isCharging) Icons.Default.BatteryChargingFull
                else when {
                    battery.isCritical -> Icons.Default.BatteryAlert
                    battery.isLow -> Icons.Default.Battery2Bar
                    battery.level > 80 -> Icons.Default.BatteryFull
                    else -> Icons.Default.Battery4Bar
                },
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DiagnosticResultCard(
    result: AlarmDiagnostics.DiagnosticResult,
    onActionClick: (AlarmDiagnostics.DiagnosticAction) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (result.status) {
                                AlarmDiagnostics.HealthStatus.HEALTHY -> Icons.Default.CheckCircle
                                AlarmDiagnostics.HealthStatus.WARNING -> Icons.Default.Warning
                                AlarmDiagnostics.HealthStatus.CRITICAL -> Icons.Default.Error
                                AlarmDiagnostics.HealthStatus.UNKNOWN -> Icons.Default.Help
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (result.status) {
                                AlarmDiagnostics.HealthStatus.HEALTHY -> Color(0xFF4CAF50)
                                AlarmDiagnostics.HealthStatus.WARNING -> Color(0xFFFF9800)
                                AlarmDiagnostics.HealthStatus.CRITICAL -> MaterialTheme.colorScheme.error
                                AlarmDiagnostics.HealthStatus.UNKNOWN -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = result.category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = result.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (result.actionable && result.action != null) {
                    TextButton(
                        onClick = { onActionClick(result.action) }
                    ) {
                        Text(result.action.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsCard(
    logs: String,
    onClearLogs: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Diagnostic Logs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TextButton(
                    onClick = onClearLogs
                ) {
                    Text("Clear Logs")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = logs.ifEmpty { "No logs available" },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
