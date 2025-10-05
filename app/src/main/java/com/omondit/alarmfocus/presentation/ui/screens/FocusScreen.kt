package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.presentation.viewmodel.FocusViewModel
import com.omondit.alarmfocus.utils.FocusModeManager

@Composable
fun FocusScreen() {
    val context = LocalContext.current
    val viewModel = FocusViewModel(FocusModeManager(context))
    val uiState by viewModel.uiState.collectAsState()

    FocusScreenContent(
        isSessionActive = uiState.isSessionActive,
        activeSessionName = uiState.activeSession?.name,
        onStartQuickSession = { duration -> viewModel.startQuickSession(duration) },
        onStopSession = { viewModel.stopFocusSession() }
    )
}

@Composable
fun FocusScreenContent(
    isSessionActive: Boolean,
    activeSessionName: String?,
    onStartQuickSession: (Int) -> Unit,
    onStopSession: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Focus Mode",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .semantics { contentDescription = "Focus mode screen title" }
                .padding(bottom = 24.dp)
        )

        // Focus Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (isSessionActive) Icons.Filled.CheckCircle else Icons.Filled.DoNotDisturb,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp),
                    tint = if (isSessionActive) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )

                Text(
                    text = if (isSessionActive) {
                        "Focus Mode Active"
                    } else {
                        "Focus Mode Inactive"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isSessionActive) {
                        "Session: ${activeSessionName ?: "Unknown"}"
                    } else {
                        "Block distracting apps and stay focused on your goals"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                if (isSessionActive) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onStopSession) {
                        Icon(Icons.Filled.Stop, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Stop Focus Session")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Focus Options
        Text(
            text = "Quick Focus Sessions",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FocusOptionCard(
                    duration = "15 minutes",
                    description = "Short focus session",
                    enabled = !isSessionActive,
                    onClick = { onStartQuickSession(15) }
                )
            }
            item {
                FocusOptionCard(
                    duration = "30 minutes",
                    description = "Standard work session",
                    enabled = !isSessionActive,
                    onClick = { onStartQuickSession(30) }
                )
            }
            item {
                FocusOptionCard(
                    duration = "1 hour",
                    description = "Deep work session",
                    enabled = !isSessionActive,
                    onClick = { onStartQuickSession(60) }
                )
            }
            item {
                FocusOptionCard(
                    duration = "2 hours",
                    description = "Extended focus session",
                    enabled = !isSessionActive,
                    onClick = { onStartQuickSession(120) }
                )
            }
        }
    }
}

@Composable
fun FocusOptionCard(
    duration: String,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = duration,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                }
            )
        }
    }
}
