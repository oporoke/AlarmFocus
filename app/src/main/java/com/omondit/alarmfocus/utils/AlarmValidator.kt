package com.omondit.alarmfocus.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File

/**
 * Validates alarm configurations and audio files
 * Ensures reliability for ADHD users who depend on consistent alarm behavior
 */
class AlarmValidator(private val context: Context) {

    companion object {
        private const val TAG = "AlarmValidator"
        private const val MAX_SOUND_DURATION_MS = 300_000 // 5 minutes
        private const val MIN_SOUND_DURATION_MS = 1_000 // 1 second
        private const val MAX_FILE_SIZE_MB = 10
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )

    /**
     * Validate alarm time configuration
     */
    fun validateAlarmTime(hour: Int, minute: Int): ValidationResult {
        val errors = mutableListOf<String>()

        if (hour < 0 || hour > 23) {
            errors.add("Hour must be between 0 and 23")
        }

        if (minute < 0 || minute > 59) {
            errors.add("Minute must be between 0 and 59")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validate custom alarm sound file
     */
    fun validateSoundFile(uri: Uri): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            // Check duration
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L

            if (duration == 0L) {
                errors.add("Unable to determine audio file duration")
            } else {
                if (duration < MIN_SOUND_DURATION_MS) {
                    errors.add("Audio file is too short (minimum 1 second required)")
                }
                if (duration > MAX_SOUND_DURATION_MS) {
                    warnings.add("Audio file is very long (${duration / 1000}s). Consider shorter files for better performance.")
                }
            }

            // Check if it's actually audio
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            if (mimeType == null || !mimeType.startsWith("audio/")) {
                errors.add("File is not a valid audio file")
            }

            // Check file size
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                if (sizeIndex != -1 && it.moveToFirst()) {
                    val size = it.getLong(sizeIndex)
                    val sizeMB = size / (1024 * 1024)
                    if (sizeMB > MAX_FILE_SIZE_MB) {
                        warnings.add("File is large (${sizeMB}MB). This may impact performance.")
                    }
                }
            }

            retriever.release()

        } catch (e: Exception) {
            Log.e(TAG, "Error validating sound file", e)
            errors.add("Unable to validate audio file: ${e.message}")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Validate system state for reliable alarm triggering
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun validateSystemState(): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Check battery optimization
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            warnings.add("Battery optimization is enabled. This may affect alarm reliability.")
        }

        // Check storage space
        val availableSpace = File(context.filesDir.absolutePath).freeSpace
        val availableSpaceMB = availableSpace / (1024 * 1024)
        if (availableSpaceMB < 50) {
            warnings.add("Low storage space (${availableSpaceMB}MB available). Consider freeing up space.")
        }

        // Check if alarm permissions are properly set
        val permissionManager = PermissionManager(context as androidx.activity.ComponentActivity)
        if (!permissionManager.areAllPermissionsGranted()) {
            errors.add("Required permissions are missing. Alarms may not work properly.")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Validate alarm label for accessibility
     */
    fun validateAlarmLabel(label: String): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (label.isBlank()) {
            warnings.add("Consider adding a label to help identify this alarm")
        }

        if (label.length > 50) {
            warnings.add("Label is quite long. Shorter labels are easier to read.")
        }

        // Check for potentially confusing characters
        val problematicChars = label.filter { it.isISOControl() || it.category == CharCategory.FORMAT }
        if (problematicChars.isNotEmpty()) {
            errors.add("Label contains invalid characters")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }
}