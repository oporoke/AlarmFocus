// Fix for SettingsScreen.kt to properly integrate DiagnosticsScreen

package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDiagnostics by remember { mutableStateOf(false) }
    var showSoundPicker by remember { mutableStateOf(false) }
    var showBarcodeManagement by remember { mutableStateOf(false) }
    var showPhotoManagement by remember { mutableStateOf(false) }
    var showDeviceAdminOnboarding by remember { mutableStateOf(false) }

    // Sound picker dialog
    if (showSoundPicker) {
        com.omondit.alarmfocus.presentation.ui.dialogs.SoundPickerDialog(
            currentSoundUri = null,
            onSoundSelected = { uri ->
                // Handle sound selection
                showSoundPicker = false
            },
            onDismiss = { showSoundPicker = false }
        )
    }

    // Device admin onboarding
    if (showDeviceAdminOnboarding) {
        val deviceAdminManager = remember { com.omondit.alarmfocus.utils.DeviceAdminManager(context) }
        DeviceAdminOnboardingScreen(
            onEnableDeviceAdmin = {
                deviceAdminManager.requestDeviceAdminActivation()
                showDeviceAdminOnboarding = false
            },
            onSkip = { showDeviceAdminOnboarding = false }
        )
    } else if (showDiagnostics) {
        // Initialize repository for diagnostics
        val database = AppDatabase.getDatabase(context)
        val repository = AlarmRepositoryImpl(database.alarmDao())

        DiagnosticsScreen(
            alarmRepository = repository,
            modifier = modifier
        )
    } else if (showBarcodeManagement) {
        com.omondit.alarmfocus.presentation.ui.screens.BarcodeManagementScreen(
            onNavigateBack = { showBarcodeManagement = false }
        )
    } else if (showPhotoManagement) {
        // For photo management, we'll show a photo gallery/management screen
        // PhotoCaptureScreen is meant for mission challenges, create a simpler management screen
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showPhotoManagement = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Photo Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Photo management screen - Use mission setup to register reference photos",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Settings options
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    SettingsItem(
                        icon = Icons.Default.Healing,
                        title = "System Diagnostics",
                        description = "Check alarm system health and troubleshoot issues",
                        onClick = { showDiagnostics = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.QrCode,
                        title = "Manage Barcodes",
                        description = "Add and manage barcodes for wake-up challenges",
                        onClick = { showBarcodeManagement = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.PhotoLibrary,
                        title = "Manage Photos",
                        description = "Add and manage reference photos",
                        onClick = { showPhotoManagement = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.FormatQuote,
                        title = "Manage Quotes",
                        description = "Add custom motivational quotes",
                        onClick = { /* Navigate to quote management */ }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.VolumeUp,
                        title = "Sound Settings",
                        description = "Manage custom alarm sounds",
                        onClick = { showSoundPicker = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Security,
                        title = "Alarm Protection",
                        description = "Enable device admin for anti-uninstall protection",
                        onClick = { showDeviceAdminOnboarding = true }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "About",
                        description = "App version and information",
                        onClick = { /* Show about dialog */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
