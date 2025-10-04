package com.omondit.alarmfocus.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.database.entities.SleepSessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.sqrt

class SleepTracker(private val context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "SleepTracker"
        private const val MOVEMENT_THRESHOLD = 2.5f // g-force threshold
        private const val DEEP_SLEEP_THRESHOLD = 5 // movements per hour for deep sleep
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val database = AppDatabase.getDatabase(context)
    private val sleepSessionDao = database.sleepSessionDao()

    private var isTracking = false
    private var currentSessionId: Long? = null
    private var movementCount = 0
    private var lastMovementTime = 0L
    private val movementHistory = mutableListOf<Long>()

    /**
     * Start tracking sleep
     */
    suspend fun startTracking() {
        if (isTracking) {
            Log.w(TAG, "Sleep tracking already active")
            return
        }

        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val today = dateFormat.format(Date())

        // Create new sleep session
        val session = SleepSessionEntity(
            startTime = System.currentTimeMillis(),
            date = today
        )

        currentSessionId = sleepSessionDao.insert(session)
        movementCount = 0
        movementHistory.clear()

        // Start accelerometer monitoring
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        isTracking = true
        Log.d(TAG, "Sleep tracking started")
    }

    /**
     * Stop tracking sleep
     */
    suspend fun stopTracking(alarmDismissalSuccess: Boolean? = null) {
        if (!isTracking || currentSessionId == null) {
            Log.w(TAG, "No active sleep tracking")
            return
        }

        val session = sleepSessionDao.getSessionById(currentSessionId!!)
        if (session != null) {
            val endTime = System.currentTimeMillis()
            val durationMinutes = ((endTime - session.startTime) / (60 * 1000)).toInt()

            // Calculate sleep quality based on movement patterns
            val (deepSleep, lightSleep, awake) = calculateSleepPhases(durationMinutes)
            val qualityScore = calculateQualityScore(deepSleep, lightSleep, awake, durationMinutes)

            val updatedSession = session.copy(
                endTime = endTime,
                durationMinutes = durationMinutes,
                qualityScore = qualityScore,
                movementCount = movementCount,
                deepSleepMinutes = deepSleep,
                lightSleepMinutes = lightSleep,
                awakeMinutes = awake,
                alarmDismissalSuccess = alarmDismissalSuccess
            )

            sleepSessionDao.update(updatedSession)
            Log.d(TAG, "Sleep session ended: ${durationMinutes}min, quality: $qualityScore")
        }

        // Stop accelerometer
        sensorManager.unregisterListener(this)
        isTracking = false
        currentSessionId = null
        movementCount = 0
        movementHistory.clear()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Calculate acceleration magnitude
            val acceleration = sqrt(x * x + y * y + z * z)

            // Detect significant movement
            if (acceleration > MOVEMENT_THRESHOLD) {
                val currentTime = System.currentTimeMillis()

                // Debounce movements (only count if > 10 seconds since last)
                if (currentTime - lastMovementTime > 10_000) {
                    movementCount++
                    movementHistory.add(currentTime)
                    lastMovementTime = currentTime
                    Log.d(TAG, "Movement detected: $movementCount")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    /**
     * Calculate sleep phases based on movement patterns
     */
    private fun calculateSleepPhases(totalMinutes: Int): Triple<Int, Int, Int> {
        if (movementHistory.isEmpty() || totalMinutes == 0) {
            return Triple(0, 0, 0)
        }

        // Group movements by hour
        val firstTime = movementHistory.first()
        val movementsPerHour = mutableMapOf<Int, Int>()

        movementHistory.forEach { timestamp ->
            val hourIndex = ((timestamp - firstTime) / (60 * 60 * 1000)).toInt()
            movementsPerHour[hourIndex] = (movementsPerHour[hourIndex] ?: 0) + 1
        }

        var deepSleepMinutes = 0
        var lightSleepMinutes = 0
        var awakeMinutes = 0

        val totalHours = (totalMinutes / 60).coerceAtLeast(1)

        for (hour in 0 until totalHours) {
            val movements = movementsPerHour[hour] ?: 0
            val hourMinutes = if (hour == totalHours - 1) totalMinutes % 60 else 60

            when {
                movements <= DEEP_SLEEP_THRESHOLD -> deepSleepMinutes += hourMinutes
                movements <= DEEP_SLEEP_THRESHOLD * 2 -> lightSleepMinutes += hourMinutes
                else -> awakeMinutes += hourMinutes
            }
        }

        return Triple(deepSleepMinutes, lightSleepMinutes, awakeMinutes)
    }

    /**
     * Calculate overall sleep quality score
     */
    private fun calculateQualityScore(
        deepSleep: Int,
        lightSleep: Int,
        awake: Int,
        totalDuration: Int
    ): Float {
        if (totalDuration == 0) return 0f

        // Ideal sleep: 30% deep, 60% light, 10% awake
        val deepSleepRatio = deepSleep.toFloat() / totalDuration
        val lightSleepRatio = lightSleep.toFloat() / totalDuration
        val awakeRatio = awake.toFloat() / totalDuration

        // Calculate score based on how close to ideal
        val deepScore = 1f - kotlin.math.abs(deepSleepRatio - 0.3f) / 0.3f
        val lightScore = 1f - kotlin.math.abs(lightSleepRatio - 0.6f) / 0.6f
        val awakeScore = 1f - kotlin.math.abs(awakeRatio - 0.1f) / 0.1f

        // Weight: deep sleep 40%, light sleep 40%, awake 20%
        val score = (deepScore * 0.4f + lightScore * 0.4f + awakeScore * 0.2f)

        return score.coerceIn(0f, 1f)
    }

    /**
     * Get recent sleep sessions
     */
    fun getRecentSessions(limit: Int = 7): Flow<List<SleepSessionEntity>> {
        return sleepSessionDao.getRecentSessions(limit)
    }

    /**
     * Get sleep stats for date range
     */
    suspend fun getSleepStats(startDate: String, endDate: String): SleepStats {
        val avgQuality = sleepSessionDao.getAverageQuality(startDate, endDate) ?: 0f
        val avgDuration = sleepSessionDao.getAverageDuration(startDate, endDate) ?: 0f

        return SleepStats(
            averageQualityScore = avgQuality,
            averageDurationMinutes = avgDuration.toInt()
        )
    }

    data class SleepStats(
        val averageQualityScore: Float,
        val averageDurationMinutes: Int
    )

    /**
     * Check if currently tracking
     */
    fun isCurrentlyTracking(): Boolean = isTracking
}
