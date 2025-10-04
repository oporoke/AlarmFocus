package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.presentation.theme.*
import com.omondit.alarmfocus.presentation.viewmodel.AlarmViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced Main Dashboard - ADHD-Optimized
 *
 * Visual Design:
 * - Gradient background for depth without overwhelm
 * - Large, rounded alarm cards with clear hierarchy
 * - Smooth animations for all interactions
 * - High contrast for instant recognition
 * - Generous spacing to reduce visual clutter
 */

@Composable
fun EnhancedAlarmsScreen(
    viewModel: AlarmViewModel,
    modifier: Modifier = Modifier
) {
    val alarms by viewModel.allAlarms.collectAsState()
    val haptics = LocalHapticFeedback.current
    val adhdColors = LocalADHDColors.current

    var showCreateDialog by remember { mutableStateOf(false) }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f + gradientOffset * 0.2f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Header
            HeroHeader(
                alarmsCount = alarms.size,
                nextAlarm = alarms.firstOrNull { it.isEnabled }
            )

            // Alarms List
            if (alarms.isEmpty()) {
                EnhancedEmptyState(
                    onCreateAlarm = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showCreateDialog = true
                    }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(alarms, key = { it.id }) { alarm ->
                        EnhancedAlarmCard(
                            alarm = alarm,
                            onToggle = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.toggleAlarm(alarm.id)
                            },
                            onEdit = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Navigate to edit
                            },
                            onDelete = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.deleteAlarm(alarm.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // FAB spacing
                    }
                }
            }
        }

        // Floating Action Button - Prominent and Accessible
        FloatingActionButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                showCreateDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(72.dp), // Large for easy tapping
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create new alarm",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun HeroHeader(
    alarmsCount: Int,
    nextAlarm: AlarmEntity?
) {
    val adhdColors = LocalADHDColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Greeting with time
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Alarm count badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = ADHDCustomShapes.PillButton,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.shadow(2.dp, ADHDCustomShapes.PillButton)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$alarmsCount ${if (alarmsCount == 1) "Alarm" else "Alarms"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Next alarm indicator
            nextAlarm?.let { alarm ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandHorizontally()
                ) {
                    Surface(
                        shape = ADHDCustomShapes.PillButton,
                        color = SuccessGreen.copy(alpha = 0.15f),
                        modifier = Modifier.shadow(2.dp, ADHDCustomShapes.PillButton)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Next: ${alarm.getDisplayTime()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = SuccessGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EnhancedAlarmCard(
    alarm: AlarmEntity,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val adhdColors = LocalADHDColors.current

    // Animated scale for toggle
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (alarm.isEnabled) 4.dp else 1.dp,
                shape = ADHDCustomShapes.AlarmCard
            ),
        shape = ADHDCustomShapes.AlarmCard,
        colors = CardDefaults.cardColors(
            containerColor = if (alarm.isEnabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Display
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = alarm.getDisplayTime(false),
                    style = ADHDTextStyles.TimeDisplay.copy(fontSize = 48.sp),
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    },
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Label
                    if (alarm.label.isNotBlank()) {
                        Text(
                            text = alarm.label,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Mission indicator
                    if (alarm.missionType != "NONE") {
                        Surface(
                            shape = ADHDCustomShapes.PillButton,
                            color = getMissionColor(alarm.missionType).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    getMissionIcon(alarm.missionType),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = getMissionColor(alarm.missionType)
                                )
                                Text(
                                    text = alarm.missionType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = getMissionColor(alarm.missionType),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Repeat days
                if (alarm.isRepeating()) {
                    EnhancedRepeatDaysChips(alarm.getRepeatSchedule().toJson())
                }
            }

            // Toggle Switch - Extra Large
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = {
                    isPressed = true
                    onToggle()
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(100)
                        isPressed = false
                    }
                },
                modifier = Modifier.scale(1.3f),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SuccessGreen,
                    checkedTrackColor = SuccessGreen.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
private fun EnhancedEmptyState(onCreateAlarm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.AlarmOff,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Alarms Yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Create your first alarm to get started on better mornings",
            style = ADHDTextStyles.CalmText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateAlarm,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(56.dp),
            shape = ADHDCustomShapes.PillButton,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Create Alarm",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedRepeatDaysChips(repeatJson: String) {
    // Simple visualization of repeat pattern
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

fun getMissionColor(missionType: String): Color {
    return when (missionType) {
        "MATH" -> ADHDColors.MissionMath
        "BARCODE" -> ADHDColors.MissionBarcode
        "PHOTO" -> ADHDColors.MissionPhoto
        "ACTIVITY" -> ADHDColors.MissionActivity
        "TYPING" -> ADHDColors.MissionTyping
        else -> CalmBlue500
    }
}

fun getMissionIcon(missionType: String): ImageVector {
    return when (missionType) {
        "MATH" -> Icons.Default.Calculate
        "BARCODE" -> Icons.Default.QrCodeScanner
        "PHOTO" -> Icons.Default.PhotoCamera
        "ACTIVITY" -> Icons.Default.DirectionsRun
        "TYPING" -> Icons.Default.Keyboard
        else -> Icons.Default.Task
    }
}
