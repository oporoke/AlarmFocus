package com.omondit.alarmfocus.presentation.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.domain.model.Mission
import com.omondit.alarmfocus.domain.model.MissionConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionSettingsDialog(
    currentConfig: MissionConfig,
    onDismiss: () -> Unit,
    onConfigChanged: (MissionConfig) -> Unit
) {
    var selectedType by remember { mutableStateOf(currentConfig.type) }
    var selectedDifficulty by remember { mutableStateOf(currentConfig.difficulty) }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(0.95f)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wake-Up Mission",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Choose a challenge to ensure you're fully awake",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Mission Type Selection
                Text(
                    text = "Mission Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(getMissionTypeOptions()) { option ->
                        MissionTypeCard(
                            option = option,
                            selected = selectedType == option.type,
                            onClick = { selectedType = option.type }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Selection (only for missions that support it)
                AnimatedVisibility(visible = selectedType != Mission.MissionType.NONE) {
                    Column {
                        Text(
                            text = "Difficulty Level",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Mission.Difficulty.values().forEach { difficulty ->
                                DifficultyChip(
                                    difficulty = difficulty,
                                    selected = selectedDifficulty == difficulty,
                                    onClick = { selectedDifficulty = difficulty },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Difficulty explanation
                        Text(
                            text = getDifficultyExplanation(selectedType, selectedDifficulty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Advanced Settings Toggle
                TextButton(
                    onClick = { showAdvancedSettings = !showAdvancedSettings }
                ) {
                    Icon(
                        if (showAdvancedSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Advanced Settings")
                }

                AnimatedVisibility(visible = showAdvancedSettings) {
                    AdvancedSettingsSection(selectedType)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val newConfig = MissionConfig(
                                type = selectedType,
                                difficulty = selectedDifficulty,
                                parameters = getParametersForType(selectedType)
                            )
                            onConfigChanged(newConfig)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionTypeCard(
    option: MissionTypeOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                option.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (option.comingSoon) {
                    Text(
                        text = "Coming Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (selected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun DifficultyChip(
    difficulty: Mission.Difficulty,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(difficulty.displayName) },
        modifier = modifier
    )
}

@Composable
private fun AdvancedSettingsSection(missionType: Mission.MissionType) {
    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        when (missionType) {
            Mission.MissionType.MATH -> {
                Text(
                    text = "Math Challenge Settings",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Example advanced settings for math missions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Time Limit")
                    Text("60 seconds", style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Max Attempts")
                    Text("3 attempts", style = MaterialTheme.typography.bodySmall)
                }
            }
            else -> {
                Text(
                    text = "No advanced settings available for this mission type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class MissionTypeOption(
    val type: Mission.MissionType,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val comingSoon: Boolean = false
)

private fun getMissionTypeOptions(): List<MissionTypeOption> {
    return listOf(
        MissionTypeOption(
            type = Mission.MissionType.NONE,
            title = "No Mission",
            description = "Dismiss alarm immediately (not recommended for heavy sleepers)",
            icon = Icons.Default.DoNotDisturb
        ),
        MissionTypeOption(
            type = Mission.MissionType.MATH,
            title = "Math Challenge",
            description = "Solve arithmetic problems to wake up your brain",
            icon = Icons.Default.Calculate
        ),
        MissionTypeOption(
            type = Mission.MissionType.BARCODE,
            title = "Barcode Scan",
            description = "Scan a barcode to force yourself out of bed",
            icon = Icons.Default.QrCodeScanner
        ),
        MissionTypeOption(
            type = Mission.MissionType.PHOTO,
            title = "Photo Match",
            description = "Take a photo matching a reference image",
            icon = Icons.Default.PhotoCamera,
            comingSoon = false
        ),
        MissionTypeOption(
            type = Mission.MissionType.ACTIVITY,
            title = "Physical Activity",
            description = "Complete jumping jacks or squats",
            icon = Icons.Default.DirectionsRun,
            comingSoon = false
        ),
        MissionTypeOption(
            type = Mission.MissionType.TYPING,
            title = "Motivational Typing",
            description = "Type a motivational quote with high accuracy",
            icon = Icons.Default.Keyboard,
            comingSoon = false
        )
    )
}

private fun getDifficultyExplanation(type: Mission.MissionType, difficulty: Mission.Difficulty): String {
    return when (type) {
        Mission.MissionType.MATH -> when (difficulty) {
            Mission.Difficulty.EASY -> "Simple addition and subtraction with numbers up to 50"
            Mission.Difficulty.MEDIUM -> "Addition, subtraction, and multiplication with larger numbers"
            Mission.Difficulty.HARD -> "Complex operations including division with 3-digit numbers"
        }
        Mission.MissionType.BARCODE -> when (difficulty) {
            Mission.Difficulty.EASY -> "60 seconds to scan barcode, flash auto-enabled"
            Mission.Difficulty.MEDIUM -> "45 seconds to scan barcode"
            Mission.Difficulty.HARD -> "30 seconds to scan barcode, more precision required"
        }
        Mission.MissionType.PHOTO -> when (difficulty) {
            Mission.Difficulty.EASY -> "70% similarity match required, 60 seconds timeout"
            Mission.Difficulty.MEDIUM -> "80% similarity match required, 45 seconds timeout"
            Mission.Difficulty.HARD -> "90% similarity match required, 30 seconds timeout"
        }
        Mission.MissionType.ACTIVITY -> when (difficulty) {
            Mission.Difficulty.EASY -> "Complete 10 jumping jacks or squats"
            Mission.Difficulty.MEDIUM -> "Complete 20 jumping jacks or squats"
            Mission.Difficulty.HARD -> "Complete 30 jumping jacks or squats"
        }
        Mission.MissionType.TYPING -> when (difficulty) {
            Mission.Difficulty.EASY -> "Type a short quote with 85% accuracy"
            Mission.Difficulty.MEDIUM -> "Type a medium quote with 90% accuracy"
            Mission.Difficulty.HARD -> "Type a long quote with 95% accuracy"
        }
        Mission.MissionType.NONE -> "No mission challenge will be required to dismiss the alarm"
    }
}

private fun getParametersForType(type: Mission.MissionType): Map<String, String> {
    return when (type) {
        Mission.MissionType.MATH -> mapOf(
            "max_attempts" to "3",
            "timeout_seconds" to "60"
        )
        else -> emptyMap()
    }
}
