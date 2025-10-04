package com.omondit.alarmfocus.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omondit.alarmfocus.data.database.dao.SleepSessionDao
import com.omondit.alarmfocus.data.database.entities.SleepSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Sleep Analytics (D14)
 *
 * Manages sleep tracking data and provides analytics for display
 */
class SleepViewModel(
    private val sleepSessionDao: SleepSessionDao
) : ViewModel() {

    private val _recentSessions = MutableStateFlow<List<SleepSessionEntity>>(emptyList())
    val recentSessions: StateFlow<List<SleepSessionEntity>> = _recentSessions.asStateFlow()

    private val _averageQuality = MutableStateFlow(0f)
    val averageQuality: StateFlow<Float> = _averageQuality.asStateFlow()

    private val _weeklyData = MutableStateFlow<List<DailySleepData>>(emptyList())
    val weeklyData: StateFlow<List<DailySleepData>> = _weeklyData.asStateFlow()

    init {
        loadSleepData()
    }

    private fun loadSleepData() {
        viewModelScope.launch {
            // Load last 7 days of sleep sessions
            val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }
            val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            sleepSessionDao.getSessionsInDateRange(startDate, endDate).collect { sessions ->
                _recentSessions.value = sessions

                // Calculate average quality
                val avgQuality = if (sessions.isNotEmpty()) {
                    sessions.map { it.qualityScore }.average().toFloat()
                } else {
                    0f
                }
                _averageQuality.value = avgQuality

                // Group by date for weekly chart
                _weeklyData.value = groupByDate(sessions)
            }
        }
    }

    private fun groupByDate(sessions: List<SleepSessionEntity>): List<DailySleepData> {
        val grouped = sessions.groupBy { it.date }
        val calendar = Calendar.getInstance()

        return (0..6).map { daysAgo ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

            val daySessions = grouped[date] ?: emptyList()
            val avgQuality = if (daySessions.isNotEmpty()) {
                daySessions.map { it.qualityScore }.average().toFloat()
            } else {
                0f
            }

            val totalDeepSleep = daySessions.sumOf { it.deepSleepMinutes }
            val totalLightSleep = daySessions.sumOf { it.lightSleepMinutes }
            val totalAwake = daySessions.sumOf { it.awakeMinutes }

            DailySleepData(
                date = date,
                quality = avgQuality,
                deepSleepMinutes = totalDeepSleep,
                lightSleepMinutes = totalLightSleep,
                awakeMinutes = totalAwake,
                sessionCount = daySessions.size
            )
        }.reversed() // Oldest to newest
    }

    fun refreshData() {
        loadSleepData()
    }

    data class DailySleepData(
        val date: String,
        val quality: Float,
        val deepSleepMinutes: Int,
        val lightSleepMinutes: Int,
        val awakeMinutes: Int,
        val sessionCount: Int
    ) {
        val totalMinutes: Int
            get() = deepSleepMinutes + lightSleepMinutes + awakeMinutes

        val dayOfWeek: String
            get() {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = dateFormat.parse(date) ?: return ""
                return SimpleDateFormat("EEE", Locale.getDefault()).format(date)
            }
    }
}
