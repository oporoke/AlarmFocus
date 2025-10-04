package com.omondit.alarmfocus.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_sessions")
data class SleepSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long, // Timestamp when sleep started
    val endTime: Long? = null, // Timestamp when sleep ended
    val durationMinutes: Int = 0,
    val qualityScore: Float = 0f, // 0.0 to 1.0
    val movementCount: Int = 0, // Number of movements detected
    val deepSleepMinutes: Int = 0,
    val lightSleepMinutes: Int = 0,
    val awakeMinutes: Int = 0,
    val date: String, // YYYY-MM-DD format
    val notes: String = "",
    val alarmDismissalSuccess: Boolean? = null // Did they successfully dismiss alarm next morning
) {
    fun getSleepQualityCategory(): SleepQuality {
        return when {
            qualityScore >= 0.8f -> SleepQuality.EXCELLENT
            qualityScore >= 0.6f -> SleepQuality.GOOD
            qualityScore >= 0.4f -> SleepQuality.FAIR
            else -> SleepQuality.POOR
        }
    }

    enum class SleepQuality {
        EXCELLENT, GOOD, FAIR, POOR
    }
}
