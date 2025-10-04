package com.omondit.alarmfocus.presentation.ui.screens

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omondit.alarmfocus.data.database.entities.AppUsageEntity
import com.omondit.alarmfocus.utils.AppUsageMonitor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun AppUsageScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appUsageMonitor = remember { AppUsageMonitor(context) }

    var hasPermission by remember { mutableStateOf(appUsageMonitor.hasUsageStatsPermission()) }
    var selectedTab by remember { mutableStateOf(0) }
    var usageData by remember { mutableStateOf<List<AppUsageEntity>>(emptyList()) }
    var categoryBreakdown by remember { mutableStateOf<List<Pair<AppUsageEntity.AppCategory, Long>>>(emptyList()) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())

    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            appUsageMonitor.collectTodayUsage()

            // Collect usage data
            appUsageMonitor.getUsageForDateRange(today, today).collect { data ->
                usageData = data
            }

            // Get category breakdown
            val breakdown = appUsageMonitor.getCategoryBreakdown(today)
            categoryBreakdown = breakdown.map { usage -> usage.category to usage.totalTime }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (!hasPermission) {
            PermissionRequestCard(
                onRequestPermission = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                },
                onCheckPermission = {
                    hasPermission = appUsageMonitor.hasUsageStatsPermission()
                }
            )
        } else {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Apps") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Categories") }
                )
            }

            when (selectedTab) {
                0 -> AppUsageList(usageData = usageData)
                1 -> CategoryBreakdownList(categoryBreakdown = categoryBreakdown)
            }
        }
    }
}

@Composable
fun PermissionRequestCard(
    onRequestPermission: () -> Unit,
    onCheckPermission: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Usage Access Permission Required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To monitor app usage and help you stay focused, we need permission to access usage stats.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onCheckPermission) {
                Text("I've granted permission")
            }
        }
    }
}

@Composable
fun AppUsageList(usageData: List<AppUsageEntity>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (usageData.isEmpty()) {
            item {
                Text(
                    text = "No usage data available",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(usageData) { app ->
                AppUsageItem(app = app)
            }
        }
    }
}

@Composable
fun AppUsageItem(app: AppUsageEntity) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = app.category.name.replace("_", " "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDuration(app.usageTimeMillis),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = getCategoryColor(app.category)
                )
                Text(
                    text = "${app.launchCount} launches",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategoryBreakdownList(categoryBreakdown: List<Pair<AppUsageEntity.AppCategory, Long>>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (categoryBreakdown.isEmpty()) {
            item {
                Text(
                    text = "No usage data available",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            val totalTime = categoryBreakdown.sumOf { it.second }

            items(categoryBreakdown) { (category, time) ->
                CategoryUsageCard(
                    category = category,
                    usageTime = time,
                    percentage = if (totalTime > 0) (time.toFloat() / totalTime * 100) else 0f
                )
            }
        }
    }
}

@Composable
fun CategoryUsageCard(
    category: AppUsageEntity.AppCategory,
    usageTime: Long,
    percentage: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category),
                        contentDescription = null,
                        tint = getCategoryColor(category),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDuration(usageTime),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${percentage.toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = getCategoryColor(category),
            )
        }
    }
}

fun formatDuration(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "<1m"
    }
}

fun getCategoryColor(category: AppUsageEntity.AppCategory): Color {
    return when (category) {
        AppUsageEntity.AppCategory.SOCIAL_MEDIA -> Color(0xFFE91E63)
        AppUsageEntity.AppCategory.PRODUCTIVITY -> Color(0xFF4CAF50)
        AppUsageEntity.AppCategory.GAMES -> Color(0xFF9C27B0)
        AppUsageEntity.AppCategory.COMMUNICATION -> Color(0xFF2196F3)
        AppUsageEntity.AppCategory.ENTERTAINMENT -> Color(0xFFFF9800)
        AppUsageEntity.AppCategory.UTILITIES -> Color(0xFF607D8B)
        AppUsageEntity.AppCategory.EDUCATION -> Color(0xFF00BCD4)
        AppUsageEntity.AppCategory.OTHER -> Color(0xFF9E9E9E)
    }
}

fun getCategoryIcon(category: AppUsageEntity.AppCategory): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        AppUsageEntity.AppCategory.SOCIAL_MEDIA -> Icons.Default.Person
        AppUsageEntity.AppCategory.PRODUCTIVITY -> Icons.Default.Work
        AppUsageEntity.AppCategory.GAMES -> Icons.Default.SportsEsports
        AppUsageEntity.AppCategory.COMMUNICATION -> Icons.Default.Chat
        AppUsageEntity.AppCategory.ENTERTAINMENT -> Icons.Default.Movie
        AppUsageEntity.AppCategory.UTILITIES -> Icons.Default.Build
        AppUsageEntity.AppCategory.EDUCATION -> Icons.Default.School
        AppUsageEntity.AppCategory.OTHER -> Icons.Default.Apps
    }
}
