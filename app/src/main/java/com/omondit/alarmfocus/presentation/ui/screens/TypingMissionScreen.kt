package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omondit.alarmfocus.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TypingMissionScreen(
    challenge: Challenge,
    onMissionCompleted: (Float) -> Unit, // Pass accuracy
    onTimeout: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val targetText = challenge.correctAnswer
    val author = challenge.data["author"] as? String ?: "Unknown"
    val category = challenge.data["category"] as? String ?: "Motivation"
    val requiredAccuracy = (challenge.data["timeWindow"] as? String)?.toIntOrNull() ?: 0.95f

    var userInput by remember { mutableStateOf("") }
    var timeRemaining by remember { mutableStateOf(challenge.timeoutSeconds) }
    var currentAccuracy by remember { mutableStateOf(0f) }
    var showResult by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var attemptCount by remember { mutableStateOf(0) }

    // Calculate real-time accuracy
    LaunchedEffect(userInput) {
        if (userInput.isNotEmpty()) {
            currentAccuracy = calculateTypingAccuracy(targetText, userInput)
        }
    }

    // Countdown timer
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0 && !showResult) {
            delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0 && !showResult) {
            onTimeout()
        }
    }

    fun submitAttempt() {
        attemptCount++
        val accuracy = calculateTypingAccuracy(targetText, userInput)

        if (accuracy > 0.95f) {
            isSuccess = true
            showResult = true
            scope.launch {
                delay(2000)
                onMissionCompleted(accuracy)
            }
        } else if (attemptCount >= challenge.allowedAttempts) {
            isSuccess = false
            showResult = true
            scope.launch {
                delay(2000)
                onTimeout()
            }
        } else {
            // Show feedback and allow retry
            userInput = ""
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.1f)
                else if (showResult && !isSuccess) Color(0xFFF44336).copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface
            )
    ) {
        if (showResult) {
            ResultScreen(
                isSuccess = isSuccess,
                accuracy = currentAccuracy,
                requiredAccuracy = 0.95f,
                targetText = targetText,
                author = author
            )
        } else {
            TypingInterface(
                targetText = targetText,
                author = author,
                category = category,
                userInput = userInput,
                onInputChange = { userInput = it },
                currentAccuracy = currentAccuracy,
                requiredAccuracy = 0.95f,
                timeRemaining = timeRemaining,
                attemptCount = attemptCount,
                maxAttempts = challenge.allowedAttempts,
                onSubmit = { submitAttempt() },
                onCancel = onCancel
            )
        }
    }
}

@Composable
private fun TypingInterface(
    targetText: String,
    author: String,
    category: String,
    userInput: String,
    onInputChange: (String) -> Unit,
    currentAccuracy: Float,
    requiredAccuracy: Float,
    timeRemaining: Int,
    attemptCount: Int,
    maxAttempts: Int,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header with timer and attempt counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Attempt counter
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Attempt ${attemptCount + 1}/$maxAttempts",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Timer
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            timeRemaining <= 15 -> MaterialTheme.colorScheme.errorContainer
                            timeRemaining <= 30 -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Text(
                        text = "${timeRemaining}s",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mission instruction
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Keyboard,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Type this motivational quote",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Accuracy required: ${(requiredAccuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quote display
        QuoteDisplayCard(
            text = targetText,
            author = author,
            category = category,
            userInput = userInput
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Typing input
        TypingInputSection(
            userInput = userInput,
            onInputChange = onInputChange,
            onSubmit = onSubmit,
            enabled = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Accuracy display
        AccuracyIndicator(
            currentAccuracy = currentAccuracy,
            requiredAccuracy = requiredAccuracy,
            wordProgress = calculateWordProgress(targetText, userInput)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = userInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentAccuracy >= requiredAccuracy) {
                    Color(0xFF4CAF50)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            if (currentAccuracy >= requiredAccuracy) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Perfect! Submit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = "Submit (${(currentAccuracy * 100).toInt()}%)",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuoteDisplayCard(
    text: String,
    author: String,
    category: String,
    userInput: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Category badge
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quote text with highlighting
            val highlightedText = buildAnnotatedString {
                val words = text.split(" ")
                val typedWords = userInput.split(" ")

                words.forEachIndexed { index, word ->
                    val typedWord = typedWords.getOrNull(index)

                    when {
                        typedWord == null -> {
                            // Not yet typed
                            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                append(word)
                            }
                        }
                        typedWord.lowercase() == word.lowercase() -> {
                            // Correctly typed
                            withStyle(SpanStyle(
                                color = Color(0xFF4CAF50),
                                background = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            )) {
                                append(word)
                            }
                        }
                        else -> {
                            // Incorrectly typed
                            withStyle(SpanStyle(
                                color = Color(0xFFF44336),
                                background = Color(0xFFF44336).copy(alpha = 0.1f)
                            )) {
                                append(word)
                            }
                        }
                    }

                    if (index < words.size - 1) {
                        append(" ")
                    }
                }
            }

            Text(
                text = highlightedText,
                style = MaterialTheme.typography.headlineSmall,
                lineHeight = 32.sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Author
            Text(
                text = "— $author",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypingInputSection(
    userInput: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your typing:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Start typing the quote here...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSubmit() }
                ),
                enabled = enabled,
                minLines = 3,
                maxLines = 6,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun AccuracyIndicator(
    currentAccuracy: Float,
    requiredAccuracy: Float,
    wordProgress: Pair<Int, Int> // current words, total words
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                currentAccuracy >= requiredAccuracy -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                currentAccuracy >= 0.8f -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Accuracy: ${(currentAccuracy * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Words: ${wordProgress.first}/${wordProgress.second}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = currentAccuracy.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    currentAccuracy >= requiredAccuracy -> Color(0xFF4CAF50)
                    currentAccuracy >= 0.8f -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (currentAccuracy >= requiredAccuracy) {
                        "Excellent accuracy!"
                    } else {
                        "Need ${(requiredAccuracy * 100).toInt()}% accuracy"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        currentAccuracy >= requiredAccuracy -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (currentAccuracy >= requiredAccuracy) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultScreen(
    isSuccess: Boolean,
    accuracy: Float,
    requiredAccuracy: Float,
    targetText: String,
    author: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "result")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )

        Icon(
            if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = if (isSuccess) "Success" else "Failed",
            modifier = Modifier
                .size(80.dp)
                .scale(scale),
            tint = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSuccess) "Excellent Work!" else "Almost There!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSuccess) {
                "You achieved ${(accuracy * 100).toInt()}% accuracy!"
            } else {
                "You reached ${(accuracy * 100).toInt()}% accuracy. Needed ${(requiredAccuracy * 100).toInt()}%."
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "\"${targetText}\"",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "— $author",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isSuccess) {
                "You're motivated and ready to tackle the day! Alarm dismissed."
            } else {
                "Keep practicing your typing for better results next time."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions
private fun calculateTypingAccuracy(original: String, typed: String): Float {
    val originalWords = original.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
    val typedWords = typed.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }

    val maxLength = maxOf(originalWords.size, typedWords.size)
    if (maxLength == 0) return 0f

    var correctWords = 0
    for (i in 0 until maxLength) {
        val originalWord = originalWords.getOrNull(i) ?: ""
        val typedWord = typedWords.getOrNull(i) ?: ""

        if (originalWord == typedWord) {
            correctWords++
        }
    }

    return correctWords.toFloat() / maxLength
}

private fun calculateWordProgress(original: String, typed: String): Pair<Int, Int> {
    val originalWords = original.split(" ").filter { it.isNotBlank() }
    val typedWords = typed.split(" ").filter { it.isNotBlank() }

    return Pair(typedWords.size, originalWords.size)
}
