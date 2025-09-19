package com.omondit.alarmfocus.presentation.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import com.omondit.alarmfocus.domain.usecase.GetUpcomingAlarmsUseCase
import com.omondit.alarmfocus.presentation.viewmodel.AlarmStats
import com.omondit.alarmfocus.presentation.viewmodel.AlarmViewModel
import com.omondit.alarmfocus.presentation.viewmodel.UiEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmsScreen(
    viewModel: AlarmViewModel,
    onNavigateToCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.allAlarms.collectAsState()
    val upcomingAlarms by viewModel.upcomingAlarms.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val alarmStats by viewModel.alarmStats.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UiEvent.AlarmCreated -> {
                    // Could navigate to alarm details or show success
                }
                is UiEvent.Navigate -> {
                    // Handle navigation
                }
                is UiEvent.NavigateBack -> {
                    // Handle back navigation
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header Section
            AlarmListHeader(
                stats = alarmStats,
                nextAlarm = upcomingAlarms.firstOrNull(),
                onCreateClick = { showCreateDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Error Display
            uiState.error?.let { error ->
                ErrorCard(
                    error = error,
                    onDismiss = { viewModel.dismissError() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Alarms List
            if (alarms.isEmpty()) {
                EmptyState(onCreateClick = { showCreateDialog = true })
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Upcoming alarms section
                    if (upcomingAlarms.isNotEmpty()) {
                        item {
                            Text(
                                text = "Upcoming",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }

                    items(alarms.filter { it.isEnabled }, key = { it.id }) { alarm ->
                        val upcomingInfo = upcomingAlarms.find { it.alarm.id == alarm.id }

                        AlarmCard(
                            alarm = alarm,
                            upcomingInfo = upcomingInfo,
                            onToggle = { viewModel.toggleAlarm(alarm.id) },
                            onDelete = { viewModel.deleteAlarm(alarm.id) },
                            onSkip = { viewModel.skipNextAlarm(alarm.id) },
                            onDuplicate = { viewModel.duplicateAlarm(alarm.id) },
                            modifier = Modifier.animateContentSize()
                        )
                    }

                    // Disabled alarms section
                    val disabledAlarms = alarms.filter { !it.isEnabled }
                    if (disabledAlarms.isNotEmpty()) {
                        item {
                            Text(
                                text = "Disabled",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }

                        items(
                            items = disabledAlarms,
                            key = { it.id }
                        ) { alarm ->
                            AlarmCard(
                                alarm = alarm,
                                upcomingInfo = null,
                                onToggle = { viewModel.toggleAlarm(alarm.id) },
                                onDelete = { viewModel.deleteAlarm(alarm.id) },
                                onSkip = null, // Can't skip disabled alarms
                                onDuplicate = { viewModel.duplicateAlarm(alarm.id) },
                                modifier = Modifier.animateContentSize()
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = onNavigateToCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .semantics { contentDescription = "Create new alarm" }
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    // Create Dialog
    if (showCreateDialog) {
        QuickCreateAlarmDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { hour, minute, label, schedule ->
                viewModel.createAlarm(hour, minute, label, schedule)
                showCreateDialog = false
            }
        )
    }

    // Loading Overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun AlarmListHeader(
    stats: AlarmStats,
    nextAlarm: GetUpcomingAlarmsUseCase.UpcomingAlarm?,
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
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
                        text = "ADHD Focus Alarms",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "${stats.enabledAlarms} of ${stats.totalAlarms} alarms active",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }

                IconButton(onClick = onCreateClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Create alarm",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            nextAlarm?.let { upcoming ->
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Next: ${upcoming.alarm.getDisplayTime()} ${getTimeUntilText(upcoming.timeUntilNext)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            stats.overallSuccessRate?.let { rate ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Success rate: ${(rate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmCard(
    alarm: AlarmEntity,
    upcomingInfo: GetUpcomingAlarmsUseCase.UpcomingAlarm?,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onSkip: (() -> Unit)?,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* TODO: Navigate to alarm details */ },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (alarm.isEnabled) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
        )
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
                        text = alarm.getDisplayTime(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (alarm.isEnabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )

                    if (alarm.label.isNotBlank()) {
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (alarm.isEnabled) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    val schedule = alarm.getRepeatSchedule()
                    if (schedule.type != RepeatSchedule.RepeatType.ONCE) {
                        Text(
                            text = schedule.getDisplayText(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = if (alarm.isEnabled) 1f else 0.6f
                            )
                        )
                    }

                    // Show upcoming time for enabled alarms
                    upcomingInfo?.let { info ->
                        if (info.timeUntilNext != null) {
                            Text(
                                text = getTimeUntilText(info.timeUntilNext),
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    info.isOverdue -> MaterialTheme.colorScheme.error
                                    info.isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Menu button
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        // Toggle switch
                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { onToggle() },
                            modifier = Modifier.semantics {
                                contentDescription = if (alarm.isEnabled) {
                                    "Alarm enabled, tap to disable"
                                } else {
                                    "Alarm disabled, tap to enable"
                                }
                            }
                        )
                    }

                    // Dropdown menu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onSkip != null && alarm.isEnabled) {
                            DropdownMenuItem(
                                text = { Text("Skip next") },
                                onClick = {
                                    onSkip()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.SkipNext, contentDescription = null)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                onDuplicate()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CopyAll, contentDescription = null)
                            }
                        )

                        Divider()

                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onDelete()
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

            // Sound and mission info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (alarm.soundUri != null) Icons.Default.MusicNote else Icons.Default.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (alarm.isEnabled) 0.8f else 0.5f
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (alarm.soundUri != null) alarm.soundName else "Ultra Loud Default",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (alarm.isEnabled) 0.8f else 0.5f
                        )
                    )
                }

                if (alarm.missionType != "NONE") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Task,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(
                                alpha = if (alarm.isEnabled) 1f else 0.5f
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = alarm.missionType.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary.copy(
                                alpha = if (alarm.isEnabled) 1f else 0.5f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss error",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun EmptyState(onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AlarmOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No alarms set",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first ultra-loud ADHD alarm to ensure you never oversleep again!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onCreateClick,
            modifier = Modifier.height(48.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Alarm")
        }
    }
}

private fun getTimeUntilText(timeUntil: Long?): String {
    if (timeUntil == null) return ""

    val hours = timeUntil / (1000 * 60 * 60)
    val minutes = (timeUntil % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        timeUntil < 0 -> "Overdue"
        hours == 0L -> "in ${minutes}m"
        hours < 24 -> "in ${hours}h ${minutes}m"
        else -> {
            val days = hours / 24
            "in ${days}d ${hours % 24}h"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCreateAlarmDialog(
    onDismiss: () -> Unit,
    onCreate: (Int, Int, String, RepeatSchedule) -> Unit
) {
    var selectedHour by remember { mutableStateOf(7) }
    var selectedMinute by remember { mutableStateOf(0) }
    var alarmLabel by remember { mutableStateOf("Wake Up!") }
    var selectedSchedule by remember { mutableStateOf(RepeatSchedule.once()) }
    var showRepeatOptions by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Alarm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Time Picker Section
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hour", style = MaterialTheme.typography.bodySmall)
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = String.format("%02d", selectedHour),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    selectedHour = if (selectedHour == 0) 23 else selectedHour - 1
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease hour")
                            }
                            IconButton(
                                onClick = {
                                    selectedHour = if (selectedHour == 23) 0 else selectedHour + 1
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase hour")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(24.dp))

                    // Minute Picker
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Minute", style = MaterialTheme.typography.bodySmall)
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Text(
                                text = String.format("%02d", selectedMinute),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    selectedMinute = if (selectedMinute == 0) 59 else selectedMinute - 1
                                }
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease minute")
                            }
                            IconButton(
                                onClick = {
                                    selectedMinute = if (selectedMinute == 59) 0 else selectedMinute + 1
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase minute")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Label Input
                Text(
                    text = "Label",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = alarmLabel,
                    onValueChange = { alarmLabel = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter alarm label") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Repeat Options
                Text(
                    text = "Repeat",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Column {
                    RepeatOptionCard(
                        title = "Once",
                        subtitle = "One-time alarm",
                        selected = selectedSchedule.type == RepeatSchedule.RepeatType.ONCE,
                        onClick = { selectedSchedule = RepeatSchedule.once() }
                    )

                    RepeatOptionCard(
                        title = "Every day",
                        subtitle = "7 days a week",
                        selected = selectedSchedule.type == RepeatSchedule.RepeatType.DAILY,
                        onClick = { selectedSchedule = RepeatSchedule.daily() }
                    )

                    RepeatOptionCard(
                        title = "Weekdays",
                        subtitle = "Monday to Friday",
                        selected = selectedSchedule.type == RepeatSchedule.RepeatType.WEEKDAYS,
                        onClick = { selectedSchedule = RepeatSchedule.weekdays() }
                    )

                    RepeatOptionCard(
                        title = "Weekends",
                        subtitle = "Saturday and Sunday",
                        selected = selectedSchedule.type == RepeatSchedule.RepeatType.WEEKENDS,
                        onClick = { selectedSchedule = RepeatSchedule.weekends() }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(selectedHour, selectedMinute, alarmLabel, selectedSchedule)
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text("CREATE ALARM")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.height(48.dp)
            ) {
                Text("CANCEL")
            }
        }
    )
}

@Composable
private fun RepeatOptionCard(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
