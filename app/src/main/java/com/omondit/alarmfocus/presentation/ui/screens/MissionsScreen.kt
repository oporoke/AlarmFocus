package com.omondit.alarmfocus.presentation.ui.screens

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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun MissionsScreen() {
    val context = LocalContext.current
    var showSleepAnalytics by remember { mutableStateOf(false) }
    var showAppUsage by remember { mutableStateOf(false) }

    // Navigate to screens
    if (showSleepAnalytics) {
        val database = AppDatabase.getDatabase(context)
        val viewModel = remember {
            com.omondit.alarmfocus.presentation.viewmodel.SleepViewModel(
                database.sleepSessionDao()
            )
        }
        // Wrap in column with back button
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showSleepAnalytics = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Sleep Analytics",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            SleepAnalyticsScreen(viewModel = viewModel)
        }
    } else if (showAppUsage) {
        // Wrap in column with back button
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAppUsage = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "App Usage",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            AppUsageScreen()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Features & Tracking",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .semantics { contentDescription = "Features and tracking screen title" }
                    .padding(bottom = 24.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
            item {
                MissionCard(
                    title = "Math Challenge",
                    description = "Solve arithmetic problems to dismiss alarm",
                    icon = Icons.Filled.Calculate,
                    enabled = true
                )
            }
            item {
                MissionCard(
                    title = "Barcode Scanner",
                    description = "Scan a specific barcode to turn off alarm",
                    icon = Icons.Filled.QrCode2,
                    enabled = true
                )
            }
            item {
                MissionCard(
                    title = "Photo Verification",
                    description = "Take a photo matching your reference image",
                    icon = Icons.Filled.PhotoCamera,
                    enabled = true
                )
            }
            item {
                MissionCard(
                    title = "Physical Activity",
                    description = "Complete jumping jacks or squats",
                    icon = Icons.Filled.FitnessCenter,
                    enabled = true
                )
            }
            item {
                MissionCard(
                    title = "Motivational Quote",
                    description = "Type an inspirational quote accurately",
                    icon = Icons.Filled.FormatQuote,
                    enabled = true
                )
            }

            // Tracking & Analytics Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Analytics & Tracking",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            item {
                MissionCard(
                    title = "Sleep Analytics",
                    description = "Track your sleep patterns and quality",
                    icon = Icons.Filled.Bedtime,
                    enabled = true,
                    onClick = { showSleepAnalytics = true }
                )
            }

            item {
                MissionCard(
                    title = "App Usage Monitoring",
                    description = "Monitor your phone usage throughout the day",
                    icon = Icons.Filled.PhoneAndroid,
                    enabled = true,
                    onClick = { showAppUsage = true }
                )
            }
        }
    }
    }
}

@Composable
fun MissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(end = 16.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (enabled) description else "Coming in future updates",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
