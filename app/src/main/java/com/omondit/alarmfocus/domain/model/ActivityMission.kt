package com.omondit.alarmfocus.domain.model

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

/**
 * Physical Activity Mission Implementation
 * Detects physical movements like shakes, jumping jacks, or squats
 */
class ActivityMission(
    override val difficulty: Mission.Difficulty = Mission.Difficulty.EASY,
    override val config: MissionConfig = MissionConfig()
) : Mission() {

    override val type = MissionType.ACTIVITY

    companion object {
        private const val TAG = "ActivityMission"
        private const val COMPLETION_WINDOW_SECONDS = 30
        private const val FORCE_THRESHOLD_2G = 2.0f * 9.81f // 2g in m/s²
    }

    override fun generateChallenge(escalationLevel: Int): Challenge {
        val activityType = getActivityTypeForDifficulty(difficulty)
        val baseCount = getRequiredCountForDifficulty(difficulty)

        // Escalate by increasing required count
        val escalatedCount = baseCount + (escalationLevel * 2)
        val timeReduction = escalationLevel * 3

        return Challenge(
            id = generateChallengeId(),
            question = "Complete $escalatedCount ${activityType.displayName}",
            correctAnswer = escalatedCount.toString(),
            data = mapOf(
                "activityType" to activityType.name,
                "requiredCount" to escalatedCount.toString(),
                "forceThreshold" to FORCE_THRESHOLD_2G.toString(),
                "timeWindow" to COMPLETION_WINDOW_SECONDS.toString(),
                "escalation_level" to escalationLevel
            ),
            timeoutSeconds = (when (difficulty) {
                Difficulty.EASY -> 60   // More time for easy
                Difficulty.MEDIUM -> 45
                Difficulty.HARD -> 30   // Less time for hard
            } - timeReduction).coerceAtLeast(20),
            allowedAttempts = 3
        )
    }

    override fun validateAnswer(challenge: Challenge, answer: String): ValidationResult {
        val detectedCount = answer.toIntOrNull() ?: 0
//        val requiredCount = challenge.data["requiredCount"]?.toIntOrNull() ?: 10
        val requiredCount = (challenge.data["requiredCount"] as? String)?.toIntOrNull() ?: 10

        return if (detectedCount >= requiredCount) {
            ValidationResult(
                isCorrect = true,
                message = "Great job! You completed $detectedCount movements. Alarm dismissed!",
                shouldEscalate = false
            )
        } else {
            ValidationResult(
                isCorrect = false,
                message = "Only $detectedCount detected. Need $requiredCount ${getActivityDisplayName(challenge)}.",
                shouldEscalate = true
            )
        }
    }

    private fun getActivityTypeForDifficulty(difficulty: Difficulty): ActivityType {
        return when (difficulty) {
            Difficulty.EASY -> ActivityType.SHAKE
            Difficulty.MEDIUM -> ActivityType.JUMPING_JACKS
            Difficulty.HARD -> ActivityType.SQUATS
        }
    }

    private fun getRequiredCountForDifficulty(difficulty: Difficulty): Int {
        return when (difficulty) {
            Difficulty.EASY -> 10    // 10 shakes
            Difficulty.MEDIUM -> 8   // 8 jumping jacks
            Difficulty.HARD -> 5     // 5 squats
        }
    }

    private fun getActivityDisplayName(challenge: Challenge): String {
        val activityType = challenge.data["activityType"]?.let {
            ActivityType.valueOf(it.toString())
        } ?: ActivityType.SHAKE
        return activityType.displayName
    }

    private fun generateChallengeId(): String {
        return "activity_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    enum class ActivityType(val displayName: String, val description: String) {
        SHAKE("shakes", "Hold phone and shake it vigorously"),
        JUMPING_JACKS("jumping jacks", "Hold phone while doing jumping jacks"),
        SQUATS("squats", "Hold phone while doing squats")
    }
}

/**
 * Motion detector using accelerometer data
 */
class MotionDetector(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : SensorEventListener {

    companion object {
        private const val TAG = "MotionDetector"
        private const val SHAKE_THRESHOLD = 2.5f * 9.81f // 2.5g
        private const val JUMP_THRESHOLD = 1.8f * 9.81f  // 1.8g
        private const val SQUAT_THRESHOLD = 1.5f * 9.81f // 1.5g
        private const val MOTION_TIMEOUT_MS = 500L // Minimum time between detections
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _motionCount = MutableStateFlow(0)
    val motionCount: StateFlow<Int> = _motionCount

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive

    private val _currentActivity = MutableStateFlow<ActivityMission.ActivityType?>(null)
    val currentActivity: StateFlow<ActivityMission.ActivityType?> = _currentActivity

    private var lastMotionTime = 0L
    private var isDetecting = false

    fun startDetection(activityType: ActivityMission.ActivityType) {
        if (accelerometer == null) {
            Log.e(TAG, "Accelerometer not available")
            return
        }

        _currentActivity.value = activityType
        _motionCount.value = 0
        lastMotionTime = 0L
        isDetecting = true
        _isActive.value = true

        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )

        Log.d(TAG, "Started detecting ${activityType.displayName}")
    }

    fun stopDetection() {
        isDetecting = false
        _isActive.value = false
        sensorManager.unregisterListener(this)

        Log.d(TAG, "Stopped motion detection")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isDetecting || event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastMotionTime < MOTION_TIMEOUT_MS) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt(x * x + y * y + z * z)
        val activityType = _currentActivity.value ?: return

        val threshold = when (activityType) {
            ActivityMission.ActivityType.SHAKE -> SHAKE_THRESHOLD
            ActivityMission.ActivityType.JUMPING_JACKS -> JUMP_THRESHOLD
            ActivityMission.ActivityType.SQUATS -> SQUAT_THRESHOLD
        }

        if (acceleration > threshold) {
            lastMotionTime = currentTime
            val newCount = _motionCount.value + 1
            _motionCount.value = newCount

            Log.d(TAG, "Motion detected: ${activityType.displayName} #${newCount} (${acceleration}m/s²)")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this implementation
    }

    fun resetCount() {
        _motionCount.value = 0
    }

    fun getCurrentCount(): Int = _motionCount.value
}

/**
 * Session manager for activity missions
 */
class ActivityMissionSession(
    private val context: Context,
    private val challenge: Challenge,
    private val onCountUpdate: (Int, Int) -> Unit, // current, required
    private val onCompleted: (Int) -> Unit, // final count
    private val onTimeout: () -> Unit
) {

    private val motionDetector = MotionDetector(context)
    private val requiredCount = (challenge.data["requiredCount"] as? String)?.toIntOrNull() ?: 10
    private val timeWindow = (challenge.data["requiredCount"] as? String)?.toIntOrNull() ?: 30
    private val activityType = challenge.data["activityType"]?.let {
        ActivityMission.ActivityType.valueOf(it.toString())
    } ?: ActivityMission.ActivityType.SHAKE

    private var startTime = 0L
    private var isActive = false

    fun startSession() {
        if (isActive) return

        isActive = true
        startTime = System.currentTimeMillis()

        // Start motion detection
        motionDetector.startDetection(activityType)

        // Monitor progress
        CoroutineScope(Dispatchers.Main).launch {
            motionDetector.motionCount.collect { count ->
                onCountUpdate(count, requiredCount)

                if (count >= requiredCount) {
                    completeSession()
                }
            }
        }

        // Timeout handler
        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay((timeWindow * 1000).toLong())
            if (isActive) {
                timeoutSession()
            }
        }
    }

    fun stopSession() {
        isActive = false
        motionDetector.stopDetection()
    }

    private fun completeSession() {
        if (!isActive) return

        val finalCount = motionDetector.getCurrentCount()
        stopSession()
        onCompleted(finalCount)
    }

    private fun timeoutSession() {
        if (!isActive) return

        stopSession()
        onTimeout()
    }

    fun getActivityType(): ActivityMission.ActivityType = activityType
    fun getRequiredCount(): Int = requiredCount
    fun getTimeWindow(): Int = timeWindow
}
