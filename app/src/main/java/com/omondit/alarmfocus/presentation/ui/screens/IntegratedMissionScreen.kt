package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.domain.model.*
import com.omondit.alarmfocus.utils.QuoteManager
import kotlinx.coroutines.launch

@Composable
fun IntegratedMissionScreen(
    alarmId: Long,
    missionConfig: MissionConfig,
    onMissionCompleted: (MissionResult) -> Unit,
    onMissionFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    when (missionConfig.type) {
        Mission.MissionType.MATH -> {
            MissionScreen(
                alarmId = alarmId,
                missionConfig = missionConfig,
                onMissionCompleted = onMissionCompleted,
                onMissionFailed = onMissionFailed,
                modifier = modifier
            )
        }

        Mission.MissionType.BARCODE -> {
            BarcodeMissionScreen(
                alarmId = alarmId,
                missionConfig = missionConfig,
                onMissionCompleted = onMissionCompleted,
                onMissionFailed = onMissionFailed,
                modifier = modifier
            )
        }

        Mission.MissionType.PHOTO -> {
            PhotoMissionScreen(
                alarmId = alarmId,
                missionConfig = missionConfig,
                onMissionCompleted = onMissionCompleted,
                onMissionFailed = onMissionFailed,
                modifier = modifier
            )
        }

        Mission.MissionType.ACTIVITY -> {
            ActivityMissionScreen(
                alarmId = alarmId,
                missionConfig = missionConfig,
                onMissionCompleted = onMissionCompleted,
                onMissionFailed = onMissionFailed,
                modifier = modifier
            )
        }

        Mission.MissionType.TYPING -> {
            TypingMissionScreen(
                alarmId = alarmId,
                missionConfig = missionConfig,
                onMissionCompleted = onMissionCompleted,
                onMissionFailed = onMissionFailed,
                modifier = modifier
            )
        }

        Mission.MissionType.NONE -> {
            // Immediately complete mission for no-mission type
            LaunchedEffect(Unit) {
                val result = MissionResult(
                    success = true,
                    completionTime = 0,
                    attempts = 0,
                    score = 1.0f
                )
                onMissionCompleted(result)
            }
        }
    }
}

@Composable
fun PhotoMissionScreen(
    alarmId: Long,
    missionConfig: MissionConfig,
    onMissionCompleted: (MissionResult) -> Unit,
    onMissionFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mission = remember {
        val photoManager = PhotoManager(context)
        val registeredPhotos = photoManager.getRegisteredPhotos()
        PhotoMission(missionConfig.difficulty, missionConfig, registeredPhotos)
    }
    val missionSession = remember { MissionSession(alarmId, mission) }

    var currentChallenge by remember { mutableStateOf<Challenge?>(null) }
    var showNoPhotos by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(0) }

    // Generate initial challenge
    LaunchedEffect(Unit) {
        currentChallenge = missionSession.generateNewChallenge()
        timeRemaining = currentChallenge?.timeoutSeconds ?: 0
        if (currentChallenge?.timeoutSeconds == 0) {
            showNoPhotos = true
        }
    }

    // Timeout enforcement
    LaunchedEffect(currentChallenge, timeRemaining) {
        if (timeRemaining > 0 && currentChallenge != null && !showNoPhotos) {
            kotlinx.coroutines.delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0 && currentChallenge != null && !showNoPhotos) {
            onMissionFailed()
        }
    }

    when {
        showNoPhotos -> {
            NoPhotosRegisteredScreen(
                onNavigateToManagement = onMissionFailed,
                onSkipMission = onMissionFailed,
                modifier = modifier
            )
        }

        currentChallenge != null -> {
            PhotoCaptureScreen(
                challenge = currentChallenge!!,
                onPhotoTaken = { filePath ->
                    scope.launch {
                        val result = missionSession.submitAnswer(filePath)

                        if (result.isCorrect) {
                            val photoManager = PhotoManager(context)
                            val photoId = currentChallenge!!.data["photoId"] as? String
                            photoId?.let { photoManager.updatePhotoUsage(it) }

                            val missionResult = missionSession.getCompletionResult(true)
                            onMissionCompleted(missionResult)
                        } else {
                            if (missionSession.isMaxAttemptsReached()) {
                                onMissionFailed()
                            } else {
                                currentChallenge = missionSession.generateNewChallenge()
                            }
                        }
                    }
                },
                onTimeout = onMissionFailed,
                onCancel = onMissionFailed,
                modifier = modifier
            )
        }

        else -> {
            LaunchedEffect(Unit) { onMissionFailed() }
        }
    }
}

@Composable
fun ActivityMissionScreen(
    alarmId: Long,
    missionConfig: MissionConfig,
    onMissionCompleted: (MissionResult) -> Unit,
    onMissionFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mission = remember { ActivityMission(missionConfig.difficulty, missionConfig) }
    val missionSession = remember { MissionSession(alarmId, mission) }
    val challenge = remember { missionSession.generateNewChallenge() }
    var timeRemaining by remember { mutableStateOf(challenge.timeoutSeconds) }

    // Timeout enforcement
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            kotlinx.coroutines.delay(1000)
            timeRemaining--
        } else if (timeRemaining == 0) {
            onMissionFailed()
        }
    }

    ActivityMissionScreen(
        challenge = challenge,
        onMissionCompleted = { finalCount ->
            scope.launch {
                val result = missionSession.submitAnswer(finalCount.toString())
                val missionResult = if (result.isCorrect) {
                    missionSession.getCompletionResult(true)
                } else {
                    missionSession.getCompletionResult(false)
                }

                if (result.isCorrect) {
                    onMissionCompleted(missionResult)
                } else {
                    onMissionFailed()
                }
            }
        },
        onTimeout = onMissionFailed,
        onCancel = onMissionFailed,
        modifier = modifier
    )
}

@Composable
fun TypingMissionScreen(
    alarmId: Long,
    missionConfig: MissionConfig,
    onMissionCompleted: (MissionResult) -> Unit,
    onMissionFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mission = remember {
        val quoteManager = QuoteManager(context)
        TypingMission(missionConfig.difficulty, missionConfig, quoteManager)
    }
    val missionSession = remember { MissionSession(alarmId, mission) }
    val challenge = remember { missionSession.generateNewChallenge() }

    TypingMissionScreen(
        challenge = challenge,
        onMissionCompleted = { accuracy ->
            scope.launch {
                // For typing missions, we consider it successful if accuracy meets requirement
                val missionResult = missionSession.getCompletionResult(true)
                onMissionCompleted(missionResult)
            }
        },
        onTimeout = onMissionFailed,
        onCancel = onMissionFailed,
        modifier = modifier
    )
}

@Composable
private fun NoPhotosRegisteredScreen(
    onNavigateToManagement: () -> Unit,
    onSkipMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.PhotoCamera,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Reference Photos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "You need to register reference photos before using photo missions. Add photos of locations you want to visit to dismiss alarms.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToManagement,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register Photos", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onSkipMission,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Skip This Time")
        }
    }
}

@Composable
fun BarcodeMissionScreen(
    alarmId: Long,
    missionConfig: MissionConfig,
    onMissionCompleted: (MissionResult) -> Unit,
    onMissionFailed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mission = remember {
        val barcodeManager = BarcodeManager(context)
        val registeredBarcodes = barcodeManager.getRegisteredBarcodes()
        BarcodeMission(missionConfig.difficulty, missionConfig, registeredBarcodes)
    }
    val missionSession = remember { MissionSession(alarmId, mission) }

    var currentChallenge by remember { mutableStateOf<Challenge?>(null) }
    var showNoBarcodes by remember { mutableStateOf(false) }

    // Generate initial challenge
    LaunchedEffect(Unit) {
        currentChallenge = missionSession.generateNewChallenge()
        // Check if no barcodes are registered
        if (currentChallenge?.timeoutSeconds == 0) {
            showNoBarcodes = true
        }
    }

    when {
        showNoBarcodes -> {
            NoBarcodeRegisteredScreen(
                onNavigateToManagement = {
                    // In a real implementation, this would navigate to barcode management
                    onMissionFailed()
                },
                onSkipMission = onMissionFailed,
                modifier = modifier
            )
        }

        currentChallenge != null -> {
            BarcodeScannerScreen(
                challenge = currentChallenge!!,
                onBarcodeScanned = { scannedCode ->
                    scope.launch {
                        val result = missionSession.submitAnswer(scannedCode)

                        if (result.isCorrect) {
                            // Update barcode usage
                            val barcodeManager = BarcodeManager(context)
                            val barcodeId = currentChallenge!!.data["barcodeId"] as? String
                            barcodeId?.let { id ->
                                barcodeManager.updateBarcodeUsage(id)
                            }

                            val missionResult = missionSession.getCompletionResult(true)
                            onMissionCompleted(missionResult)
                        } else {
                            if (missionSession.isMaxAttemptsReached()) {
                                onMissionFailed()
                            } else {
                                // Generate new challenge for retry
                                currentChallenge = missionSession.generateNewChallenge()
                            }
                        }
                    }
                },
                onTimeout = onMissionFailed,
                onCancel = onMissionFailed,
                modifier = modifier
            )
        }

        else -> {
            // Loading state or error
            LaunchedEffect(Unit) {
                onMissionFailed()
            }
        }
    }
}

@Composable
private fun NoBarcodeRegisteredScreen(
    onNavigateToManagement: () -> Unit,
    onSkipMission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            androidx.compose.material.icons.Icons.Default.QrCode,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        androidx.compose.material3.Text(
            text = "No Barcodes Registered",
            style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.Text(
            text = "You need to register at least one barcode before using barcode missions. Go to barcode management to add barcodes first.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        androidx.compose.material3.Button(
            onClick = onNavigateToManagement,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            androidx.compose.material3.Icon(
                androidx.compose.material.icons.Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.Text(
                text = "Register Barcodes",
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.TextButton(
            onClick = onSkipMission,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text("Skip This Time")
        }
    }
}
