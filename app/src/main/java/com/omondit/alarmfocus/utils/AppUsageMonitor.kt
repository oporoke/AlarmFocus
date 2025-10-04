package com.omondit.alarmfocus.utils

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.database.entities.AppUsageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AppUsageMonitor(private val context: Context) {

    companion object {
        private const val TAG = "AppUsageMonitor"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager
    private val database = AppDatabase.getDatabase(context)
    private val appUsageDao = database.appUsageDao()

    /**
     * Check if usage access permission is granted
     */
    fun hasUsageStatsPermission(): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR, -1)

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            calendar.timeInMillis,
            System.currentTimeMillis()
        )

        return stats != null && stats.isNotEmpty()
    }

    /**
     * Collect app usage stats for today and store in database
     */
    suspend fun collectTodayUsage() {
        try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            if (usageStats.isNullOrEmpty()) {
                Log.w(TAG, "No usage stats available")
                return
            }

            val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val today = dateFormat.format(System.currentTimeMillis())

            val usageEntities = usageStats.mapNotNull { stats ->
                if (stats.totalTimeInForeground > 0) {
                    createAppUsageEntity(stats, today)
                } else null
            }

            appUsageDao.insertAll(usageEntities)
            Log.d(TAG, "Collected usage for ${usageEntities.size} apps")
        } catch (e: Exception) {
            Log.e(TAG, "Error collecting usage stats", e)
        }
    }

    /**
     * Get usage stats for a specific date range
     */
    fun getUsageForDateRange(startDate: String, endDate: String) = appUsageDao.getUsageByDateRange(startDate, endDate)

    /**
     * Get total usage time for a category on a specific date
     */
    suspend fun getCategoryUsage(date: String, category: AppUsageEntity.AppCategory): Long {
        return appUsageDao.getTotalUsageByCategory(date, category) ?: 0L
    }

    /**
     * Get app name from package name
     */
    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    /**
     * Create AppUsageEntity from UsageStats
     */
    private fun createAppUsageEntity(stats: UsageStats, date: String): AppUsageEntity {
        val packageName = stats.packageName
        val appName = getAppName(packageName)
        val category = AppUsageEntity.categorizeApp(packageName)

        return AppUsageEntity(
            packageName = packageName,
            appName = appName,
            category = category,
            usageTimeMillis = stats.totalTimeInForeground,
            launchCount = stats.lastTimeUsed.toInt(),
            lastUsedTimestamp = stats.lastTimeUsed,
            date = date
        )
    }

    /**
     * Clean up old usage data (older than 30 days)
     */
    suspend fun cleanupOldData() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -30)

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val cutoffDate = dateFormat.format(calendar.time)

        appUsageDao.deleteOldData(cutoffDate)
        Log.d(TAG, "Cleaned up usage data older than $cutoffDate")
    }

    /**
     * Get category breakdown for a specific date
     */
    suspend fun getCategoryBreakdown(date: String): List<CategoryUsageResult> {
        val rawData = appUsageDao.getCategoryUsageForDate(date)
        return rawData.map { CategoryUsageResult(it.category, it.totalTime) }
    }

    data class CategoryUsageResult(
        val category: AppUsageEntity.AppCategory,
        val totalTime: Long
    )

    /**
     * Schedule periodic usage collection
     */
    fun schedulePeriodicCollection(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            while (true) {
                collectTodayUsage()
                // Wait 1 hour before next collection
                kotlinx.coroutines.delay(60 * 60 * 1000)
            }
        }
    }
}
