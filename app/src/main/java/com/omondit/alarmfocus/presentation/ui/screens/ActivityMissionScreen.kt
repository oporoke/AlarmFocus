package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omondit.alarmfocus.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActivityMissionScreen(
    challenge: Challenge,
    onMissionCompleted: (Int) -> Unit, // Pass final count
    onTimeout: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentCount by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(challenge.timeoutSeconds) }
    var isActive by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    val requiredCount = (challenge.data["timeWindow"] as? String)?.toIntOrNull() ?: 10
    val activityType = challenge.data["activityType"]?.let {
        ActivityMission.ActivityType.valueOf(it.toString())
    } ?: ActivityMission.ActivityType.SHAKE

    // Activity session
    val activitySession = remember {
        ActivityMissionSession(
            context = context,
            challenge = challenge,
            onCountUpdate = { current, _ ->
                currentCount = current
            },
            onCompleted = { finalCount ->
                showSuccess = true
                scope.launch {
                    delay(1500)
                    onMissionCompleted(finalCount)
                }
            },
            onTimeout = onTimeout
        )
    }

    // Start session automatically
    LaunchedEffect(Unit) {
        delay(2000) // 2-second countdown
        isActive = true
        activitySession.startSession()
    }

    // Countdown timer
    LaunchedEffect(timeRemaining, isActive) {
        if (timeRemaining > 0 && isActive) {
            delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0 && isActive) {
            onTimeout()
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            activitySession.stopSession()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (showSuccess) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surface
            )
    ) {
        if (!isActive) {
            // Countdown screen
            CountdownScreen(
                activityType = activityType,
                requiredCount = requiredCount
            )
        } else {
            // Active mission screen
            ActivityTrackingScreen(
                activityType = activityType,
                currentCount = currentCount,
                requiredCount = requiredCount,
                timeRemaining = timeRemaining,
                showSuccess = showSuccess,
                onCancel = {
                    activitySession.stopSession()
                    onCancel()
                }
            )
        }
    }
}

@Composable
private fun CountdownScreen(
    activityType: ActivityMission.ActivityType,
    requiredCount: Int
) {
    var countdown by remember { mutableStateOf(3) }

    LaunchedEffect(Unit) {
        while (countdown > 0) {
            delay(1000)
            countdown--
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "countdown")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )

        Icon(
            getIconForActivity(activityType),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .scale(scale),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Get Ready!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Complete ${requiredCount} ${activityType.displayName}",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = activityType.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (countdown > 0) {
            Card(
                modifier = Modifier.size(80.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = countdown.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = "GO!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActivityTrackingScreen(
    activityType: ActivityMission.ActivityType,
    currentCount: Int,
    requiredCount: Int,
    timeRemaining: Int,
    showSuccess: Boolean,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with timer and cancel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        timeRemaining <= 10 -> MaterialTheme.colorScheme.errorContainer
                        timeRemaining <= 20 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                )
            ) {
                Text(
                    text = "${timeRemaining}s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Activity icon with animation
        val infiniteTransition = rememberInfiniteTransition(label = "activity")
        val iconScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (showSuccess) 1.2f else 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(if (showSuccess) 500 else 800),
                repeatMode = RepeatMode.Reverse
            ), label = "icon_scale"
        )

        Icon(
            getIconForActivity(activityType),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .scale(iconScale),
            tint = if (showSuccess) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress display
        AnimatedVisibility(
            visible = !showSuccess,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ProgressDisplay(
                currentCount = currentCount,
                requiredCount = requiredCount,
                activityType = activityType
            )
        }

        // Success display
        AnimatedVisibility(
            visible = showSuccess,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            SuccessDisplay(
                finalCount = currentCount,
                activityType = activityType
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Instructions
        if (!showSuccess) {
            InstructionsCard(activityType = activityType)
        }
    }
}

@Composable
private fun ProgressDisplay(
    currentCount: Int,
    requiredCount: Int,
    activityType: ActivityMission.ActivityType
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large count display
        Text(
            text = currentCount.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 72.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "of $requiredCount ${activityType.displayName}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress bar
        LinearProgressIndicator(
            progress = (currentCount.toFloat() / requiredCount).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${((currentCount.toFloat() / requiredCount) * 100).toInt()}% Complete",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessDisplay(
    finalCount: Int,
    activityType: ActivityMission.ActivityType
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Excellent!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Text(
            text = "You completed ${finalCount} ${activityType.displayName}",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Great job getting your body moving! Alarm dismissed.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InstructionsCard(
    activityType: ActivityMission.ActivityType
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Instructions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = when (activityType) {
                    ActivityMission.ActivityType.SHAKE -> "Hold your phone firmly and shake it back and forth vigorously. Each strong shake will be counted."
                    ActivityMission.ActivityType.JUMPING_JACKS -> "Hold your phone and do jumping jacks. The up and down motion will be detected."
                    ActivityMission.ActivityType.SQUATS -> "Hold your phone and do squats. The up and down motion of squatting will be counted."
                },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Make sure to hold your phone securely during the activity!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun getIconForActivity(activityType: ActivityMission.ActivityType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (activityType) {
        ActivityMission.ActivityType.SHAKE -> Icons.Default.Vibration
        ActivityMission.ActivityType.JUMPING_JACKS -> Icons.Default.DirectionsRun
        ActivityMission.ActivityType.SQUATS -> Icons.Default.FitnessCenter
    }
}
