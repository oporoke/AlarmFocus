package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omondit.alarmfocus.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MissionScreen(
    alarmId: Long,
    missionConfig: MissionConfig,
    onMissionCompleted: (MissionResult) -> Unit,
    onMissionFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val mission = remember { MissionFactory.createMission(missionConfig) }
    val missionSession = remember { MissionSession(alarmId, mission) }

    var currentChallenge by remember { mutableStateOf<Challenge?>(null) }
    var userAnswer by remember { mutableStateOf("") }
    var timeRemaining by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var showResult by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<ValidationResult?>(null) }
    var attempts by remember { mutableStateOf(0) }

    // Generate initial challenge
    LaunchedEffect(Unit) {
        delay(500) // Small delay for dramatic effect
        currentChallenge = missionSession.generateNewChallenge()
        timeRemaining = currentChallenge?.timeoutSeconds ?: 0
        isLoading = false
    }

    // Countdown timer
    LaunchedEffect(currentChallenge, timeRemaining) {
        if (timeRemaining > 0 && currentChallenge != null) {
            delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0 && currentChallenge != null) {
            // Time expired
            onMissionFailed()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (lastResult?.isCorrect == true) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else if (lastResult?.isCorrect == false) Color(0xFFF44336).copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            LoadingScreen()
        } else {
            currentChallenge?.let { challenge ->
                MissionContent(
                    challenge = challenge,
                    userAnswer = userAnswer,
                    timeRemaining = timeRemaining,
                    attempts = attempts,
                    lastResult = lastResult,
                    showResult = showResult,
                    onAnswerChange = { userAnswer = it },
                    onSubmitAnswer = {
                        scope.launch {
                            val result = missionSession.submitAnswer(userAnswer)
                            lastResult = result
                            attempts = missionSession.attempts
                            showResult = true

                            if (result.isCorrect) {
                                delay(1500) // Show success briefly
                                val missionResult = missionSession.getCompletionResult(true)
                                onMissionCompleted(missionResult)
                            } else {
                                delay(2000) // Show error longer
                                if (missionSession.isMaxAttemptsReached()) {
                                    onMissionFailed()
                                } else {
                                    // Generate new challenge (escalated if needed)
                                    showResult = false
                                    lastResult = null
                                    userAnswer = ""

                                    currentChallenge = missionSession.generateNewChallenge()
                                    timeRemaining = currentChallenge?.timeoutSeconds ?: 0
                                }
                            }
                        }
                    }
                )
            }
        }

        // Emergency dismiss button (top-right corner)
        EmergencyDismissButton(
            onEmergencyDismiss = onMissionFailed,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
private fun LoadingScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )

        Icon(
            Icons.Default.Psychology,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .scale(scale),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Preparing your challenge...",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Get ready to wake up your brain!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MissionContent(
    challenge: Challenge,
    userAnswer: String,
    timeRemaining: Int,
    attempts: Int,
    lastResult: ValidationResult?,
    showResult: Boolean,
    onAnswerChange: (String) -> Unit,
    onSubmitAnswer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with timer
        MissionHeader(
            timeRemaining = timeRemaining,
            attempts = attempts,
            maxAttempts = challenge.allowedAttempts
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Challenge question
        AnimatedVisibility(
            visible = !showResult,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            ChallengeQuestion(challenge.question)
        }

        // Result display
        AnimatedVisibility(
            visible = showResult && lastResult != null,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            lastResult?.let { result ->
                ResultDisplay(result)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Answer input (only show when not displaying result)
        AnimatedVisibility(
            visible = !showResult,
            enter = slideInVertically() + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            AnswerInput(
                answer = userAnswer,
                onAnswerChange = onAnswerChange,
                onSubmit = onSubmitAnswer,
                enabled = !showResult
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Motivational text
        Text(
            text = when {
                attempts == 0 -> "You can do this! Take your time and think clearly."
                attempts == 1 -> "Keep going! Check your calculation."
                else -> "Last chance! Focus and double-check your answer."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MissionHeader(
    timeRemaining: Int,
    attempts: Int,
    maxAttempts: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Attempts indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Attempt ${attempts + 1}/$maxAttempts",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Timer
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    timeRemaining > 60 -> MaterialTheme.colorScheme.primaryContainer
                    timeRemaining > 30 -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${timeRemaining / 60}:${String.format("%02d", timeRemaining % 60)}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ChallengeQuestion(question: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Calculate,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Solve this problem:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 36.sp
            )
        }
    }
}

@Composable
private fun ResultDisplay(result: ValidationResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isCorrect) {
                Color(0xFF4CAF50)
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val scale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ), label = "result_scale"
            )

            Icon(
                if (result.isCorrect) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale),
                tint = if (result.isCorrect) Color.White else MaterialTheme.colorScheme.onErrorContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (result.isCorrect) "Correct!" else "Incorrect",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (result.isCorrect) Color.White else MaterialTheme.colorScheme.onErrorContainer
            )

            Text(
                text = result.message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (result.isCorrect) Color.White.copy(alpha = 0.9f)
                else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnswerInput(
    answer: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean
) {
    Column {
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            label = { Text("Your Answer") },
            placeholder = { Text("Enter the result") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = enabled && answer.isNotBlank(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Submit Answer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmergencyDismissButton(
    onEmergencyDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmation by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showConfirmation = true },
        modifier = modifier.padding(16.dp)
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = "Emergency dismiss",
            tint = MaterialTheme.colorScheme.error
        )
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Emergency Dismiss") },
            text = {
                Text("Are you sure you want to dismiss this alarm? This should only be used in emergencies.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmation = false
                        onEmergencyDismiss()
                    }
                ) {
                    Text("Yes, Dismiss", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MissionTypeCard(info: MissionTypeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                info.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private data class MissionTypeInfo(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun getMissionTypeList() = listOf(
    MissionTypeInfo("Math Challenge", "Solve arithmetic problems", Icons.Default.Calculate),
    MissionTypeInfo("Barcode Scan", "Scan a specific barcode", Icons.Default.QrCodeScanner),
    MissionTypeInfo("Photo Match", "Take a matching photo", Icons.Default.PhotoCamera),
    MissionTypeInfo("Physical Activity", "Complete exercises", Icons.Default.DirectionsRun),
    MissionTypeInfo("Motivational Typing", "Type a quote", Icons.Default.Keyboard)
)
