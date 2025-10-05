package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.domain.model.BarcodeData
import com.omondit.alarmfocus.domain.model.BarcodeManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BarcodeManagementScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val barcodeManager = remember { BarcodeManager(context) }

    var barcodes by remember { mutableStateOf(emptyList<BarcodeData>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showScanner by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf<BarcodeManager.BarcodeStats?>(null) }

    // Load barcodes on start
    LaunchedEffect(Unit) {
        barcodes = barcodeManager.getRegisteredBarcodes()
        stats = barcodeManager.getBarcodeStats()
    }

    // Refresh function
    fun refreshBarcodes() {
        scope.launch {
            barcodes = barcodeManager.getRegisteredBarcodes()
            stats = barcodeManager.getBarcodeStats()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Barcode Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Barcode")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statistics Card
        stats?.let { statistics ->
            StatsCard(statistics = statistics)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Error Message
        AnimatedVisibility(visible = errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { errorMessage = null }
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }

        // Barcodes List
        if (barcodes.isEmpty()) {
            EmptyBarcodeState(
                onAddBarcode = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(barcodes, key = { it.id }) { barcode ->
                    BarcodeCard(
                        barcode = barcode,
                        onEdit = { /* TODO: Edit barcode */ },
                        onDelete = { barcodeId ->
                            scope.launch {
                                val success = barcodeManager.removeBarcode(barcodeId)
                                if (success) {
                                    refreshBarcodes()
                                } else {
                                    errorMessage = "Failed to delete barcode"
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Barcode Dialog
    if (showAddDialog) {
        AddBarcodeDialog(
            onDismiss = { showAddDialog = false },
            onScanBarcode = {
                showAddDialog = false
                showScanner = true
            },
            onManualEntry = { displayName, location, description ->
                // For demo purposes, generate a random barcode
                scope.launch {
                    isLoading = true
                    val result = barcodeManager.registerBarcode(
                        code = "MANUAL_${System.currentTimeMillis()}",
                        type = BarcodeData.BarcodeType.CODE_128,
                        displayName = displayName,
                        location = location,
                        description = description
                    )

                    result.fold(
                        onSuccess = {
                            refreshBarcodes()
                            showAddDialog = false
                        },
                        onFailure = { error ->
                            errorMessage = error.message
                        }
                    )
                    isLoading = false
                }
            }
        )
    }

    // Scanner Screen
    if (showScanner) {
        BarcodeScannerForRegistration(
            onBarcodeScanned = { scannedCode, barcodeType ->
                scope.launch {
                    val result = barcodeManager.registerBarcode(
                        code = scannedCode,
                        type = barcodeType,
                        displayName = "Scanned Barcode ${barcodes.size + 1}"
                    )

                    result.fold(
                        onSuccess = {
                            refreshBarcodes()
                            showScanner = false
                        },
                        onFailure = { error ->
                            errorMessage = error.message
                            showScanner = false
                        }
                    )
                }
            },
            onCancel = { showScanner = false }
        )
    }
}

@Composable
private fun StatsCard(
    statistics: BarcodeManager.BarcodeStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Barcode Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    label = "Total Barcodes",
                    value = statistics.totalBarcodes.toString()
                )
                StatItem(
                    label = "Total Scans",
                    value = statistics.totalScans.toString()
                )
                StatItem(
                    label = "Avg per Barcode",
                    value = String.format("%.1f", statistics.averageScansPerBarcode)
                )
            }

            statistics.mostUsedBarcode?.let { mostUsed ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Most used: ${mostUsed.displayName} (${mostUsed.useCount} times)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun BarcodeCard(
    barcode: BarcodeData,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = barcode.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (barcode.location.isNotBlank()) {
                        Text(
                            text = "Location: ${barcode.location}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "Type: ${barcode.type.name.replace('_', ' ')}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Code: ${barcode.code.take(20)}${if (barcode.code.length > 20) "..." else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onEdit(barcode.id)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Usage stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Used ${barcode.useCount} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                barcode.lastUsed?.let { lastUsed ->
                    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = "Last: ${formatter.format(Date(lastUsed))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (barcode.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = barcode.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Barcode") },
            text = {
                Text("Are you sure you want to delete '${barcode.displayName}'? This cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(barcode.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyBarcodeState(
    onAddBarcode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.QrCode,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No Barcodes Registered",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Register barcodes to use for wake-up challenges. Scan items like toothpaste, coffee, or anything that will force you to get out of bed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddBarcode,
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add First Barcode")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBarcodeDialog(
    onDismiss: () -> Unit,
    onScanBarcode: () -> Unit,
    onManualEntry: (displayName: String, location: String, description: String) -> Unit
) {
    var showManualEntry by remember { mutableStateOf(false) }
    var displayName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Barcode") },
        text = {
            if (!showManualEntry) {
                Column {
                    Text("How would you like to add a barcode?")

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onScanBarcode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan with Camera")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { showManualEntry = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manual Entry")
                    }
                }
            } else {
                Column {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Display Name") },
                        placeholder = { Text("e.g., Toothpaste, Coffee") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Optional)") },
                        placeholder = { Text("e.g., Bathroom, Kitchen") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Additional notes") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            if (showManualEntry) {
                Button(
                    onClick = {
                        if (displayName.isNotBlank()) {
                            onManualEntry(displayName, location, description)
                        }
                    },
                    enabled = displayName.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = if (showManualEntry) {
                    { showManualEntry = false }
                } else {
                    onDismiss
                }
            ) {
                Text(if (showManualEntry) "Back" else "Cancel")
            }
        }
    )
}

@Composable
private fun BarcodeScannerForRegistration(
    onBarcodeScanned: (String, BarcodeData.BarcodeType) -> Unit,
    onCancel: () -> Unit
) {
    // Simplified scanner for registration - reuse the main scanner component
    // but adapted for registration purposes
    val dummyChallenge = com.omondit.alarmfocus.domain.model.Challenge(
        id = "registration",
        question = "Scan any barcode to register",
        correctAnswer = "",
        timeoutSeconds = 60
    )

    BarcodeScannerScreen(
        challenge = dummyChallenge,
        onBarcodeScanned = { scannedCode ->
            // For now, default to CODE_128 - in a real implementation,
            // you'd detect the actual barcode type from the scanner
            onBarcodeScanned(scannedCode, BarcodeData.BarcodeType.CODE_128)
        },
        onTimeout = onCancel,
        onCancel = onCancel
    )
}
