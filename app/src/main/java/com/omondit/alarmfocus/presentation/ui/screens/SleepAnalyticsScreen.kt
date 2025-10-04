package com.omondit.alarmfocus.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.presentation.viewmodel.SleepViewModel

/**
 * D14: Sleep Analytics Screen
 *
 * Displays sleep tracking data with:
 * - 7-day average quality
 * - Weekly trend visualization
 * - Sleep phase breakdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepAnalyticsScreen(
    viewModel: SleepViewModel,
    modifier: Modifier = Modifier
) {
    val sleepSessions by viewModel.recentSessions.collectAsState()
    val averageQuality by viewModel.averageQuality.collectAsState()
    val weeklyData by viewModel.weeklyData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sleep Analytics") },
                actions = {
                    IconButton(onClick = { viewModel.refreshData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Quality Score Card
                QualityScoreCard(averageQuality)
            }

            item {
                // 7-Day Trend
                Text(
                    text = "Weekly Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                WeeklyTrendChart(weeklyData)
            }

            item {
                // Recent Sessions
                Text(
                    text = "Recent Sessions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (sleepSessions.isEmpty()) {
                item {
                    EmptySleepState()
                }
            } else {
                items(sleepSessions.take(5)) { session ->
                    SleepSessionCard(session)
                }
            }
        }
    }
}

@Composable
private fun QualityScoreCard(averageQuality: Float) {
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
                Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sleep Quality",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(averageQuality * 100).toInt()}%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "7-day average",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun WeeklyTrendChart(weeklyData: List<SleepViewModel.DailySleepData>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data for past 7 days",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Simple bar chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { dayData ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Quality bar
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height((dayData.quality * 150).dp.coerceIn(10.dp, 150.dp))
                                    .background(
                                        color = getQualityColor(dayData.quality),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day label
                            Text(
                                text = dayData.dayOfWeek,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepSessionCard(session: com.omondit.alarmfocus.data.database.entities.SleepSessionEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${(session.qualityScore * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getQualityColor(session.qualityScore)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sleep phases
            SleepPhaseRow("Deep Sleep", session.deepSleepMinutes, Color(0xFF4CAF50))
            SleepPhaseRow("Light Sleep", session.lightSleepMinutes, Color(0xFF42A5F5))
            SleepPhaseRow("Awake", session.awakeMinutes, Color(0xFFFFA726))

            // Alarm success indicator
            session.alarmDismissalSuccess?.let { success ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (success) "✓ Alarm dismissed" else "✗ Alarm snoozed/failed",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (success) Color(0xFF4CAF50) else Color(0xFFFFA726)
                    )
                }
            }
        }
    }
}

@Composable
private fun SleepPhaseRow(label: String, minutes: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            "$minutes min",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptySleepState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Bedtime,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Sleep Data Yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sleep tracking will automatically record your sleep patterns",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getQualityColor(quality: Float): Color {
    return when {
        quality >= 0.8f -> Color(0xFF4CAF50) // Excellent - Green
        quality >= 0.6f -> Color(0xFF42A5F5) // Good - Blue
        quality >= 0.4f -> Color(0xFFFFA726) // Fair - Orange
        else -> Color(0xFFEF5350) // Poor - Red
    }
}
