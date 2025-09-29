package com.omondit.alarmfocus.domain.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Photo Verification Mission Implementation
 * Forces user to take a photo matching a pre-registered reference photo
 */
class PhotoMission(
    override val difficulty: Mission.Difficulty = Mission.Difficulty.EASY,
    override val config: MissionConfig = MissionConfig(),
    private val registeredPhotos: List<ReferencePhoto> = emptyList()
) : Mission() {

    override val type = MissionType.PHOTO

    companion object {
        private const val TAG = "PhotoMission"
        private const val VERIFICATION_TIMEOUT_SECONDS = 10
        private const val MAX_ATTEMPTS = 5
    }

    override fun generateChallenge(): Challenge {
        if (registeredPhotos.isEmpty()) {
            return Challenge(
                id = "photo_no_refs",
                question = "No reference photos registered. Please register photos first.",
                correctAnswer = "",
                timeoutSeconds = 0,
                allowedAttempts = 0
            )
        }

        val targetPhoto = registeredPhotos.random()

        return Challenge(
            id = generateChallengeId(),
            question = "Take a photo matching: ${targetPhoto.displayName}",
            correctAnswer = targetPhoto.id,
            data = mapOf(
                "photoId" to targetPhoto.id,
                "displayName" to targetPhoto.displayName,
                "location" to targetPhoto.location,
                "description" to targetPhoto.description,
                "tolerance" to getToleranceForDifficulty(difficulty).toString()
            ),
            timeoutSeconds = when (difficulty) {
                Difficulty.EASY -> 90
                Difficulty.MEDIUM -> 60
                Difficulty.HARD -> 45
            },
            allowedAttempts = MAX_ATTEMPTS
        )
    }

    override fun validateAnswer(challenge: Challenge, answer: String): ValidationResult {
        // Answer contains the file path of the captured photo
        val capturedPhotoPath = answer.trim()
        val referencePhotoId = challenge.correctAnswer

        if (capturedPhotoPath.isEmpty()) {
            return ValidationResult(
                isCorrect = false,
                message = "No photo captured. Try again.",
                shouldEscalate = false
            )
        }

        val referencePhoto = registeredPhotos.find { it.id == referencePhotoId }
        if (referencePhoto == null) {
            return ValidationResult(
                isCorrect = false,
                message = "Reference photo not found.",
                shouldEscalate = false
            )
        }

        val tolerance = challenge.data["tolerance"]?.toString()?.toFloatOrNull() ?: 0.3f
        val similarity = calculatePhotoSimilarity(referencePhoto.encryptedPath, capturedPhotoPath)

        return if (similarity >= tolerance) {
            ValidationResult(
                isCorrect = true,
                message = "Photo match confirmed! Alarm dismissed.",
                shouldEscalate = false
            )
        } else {
            ValidationResult(
                isCorrect = false,
                message = "Photo doesn't match. Try again from the correct location.",
                shouldEscalate = true
            )
        }
    }

    private fun calculatePhotoSimilarity(referencePath: String, capturedPath: String): Float {
        return try {
            val referenceBitmap = loadAndDecryptPhoto(referencePath) ?: return 0f
            val capturedBitmap = BitmapFactory.decodeFile(capturedPath) ?: return 0f

            comparePhotos(referenceBitmap, capturedBitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating photo similarity", e)
            0f
        }
    }

    private fun comparePhotos(bitmap1: Bitmap, bitmap2: Bitmap): Float {
        // Simple histogram comparison - in production, use more sophisticated algorithms
        val hist1 = calculateHistogram(bitmap1)
        val hist2 = calculateHistogram(bitmap2)

        return calculateHistogramSimilarity(hist1, hist2)
    }

    private fun calculateHistogram(bitmap: Bitmap): IntArray {
        val histogram = IntArray(256)
        val width = bitmap.width
        val height = bitmap.height

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val gray = ((pixel shr 16 and 0xFF) + (pixel shr 8 and 0xFF) + (pixel and 0xFF)) / 3
                histogram[gray]++
            }
        }

        return histogram
    }

    private fun calculateHistogramSimilarity(hist1: IntArray, hist2: IntArray): Float {
        var sum = 0.0
        var sum1 = 0.0
        var sum2 = 0.0

        for (i in hist1.indices) {
            sum += hist1[i] * hist2[i]
            sum1 += hist1[i] * hist1[i]
            sum2 += hist2[i] * hist2[i]
        }

        val denominator = sqrt(sum1 * sum2)
        return if (denominator > 0) (sum / denominator).toFloat() else 0f
    }

    private fun loadAndDecryptPhoto(encryptedPath: String): Bitmap? {
        // Implementation would decrypt and load the reference photo
        // For now, return null - actual implementation needs encryption key management
        return null
    }

    private fun getToleranceForDifficulty(difficulty: Difficulty): Float {
        return when (difficulty) {
            Difficulty.EASY -> 0.5f    // More lenient matching
            Difficulty.MEDIUM -> 0.7f  // Moderate matching
            Difficulty.HARD -> 0.85f   // Strict matching
        }
    }

    private fun generateChallengeId(): String {
        return "photo_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Represents a reference photo for photo verification missions
 */
data class ReferencePhoto(
    val id: String = generatePhotoId(),
    val displayName: String,
    val location: String = "",
    val description: String = "",
    val encryptedPath: String,
    val thumbnail: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long? = null,
    val useCount: Int = 0,
    val fileSize: Long = 0
) {

    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("displayName", displayName)
            put("location", location)
            put("description", description)
            put("encryptedPath", encryptedPath)
            put("thumbnail", thumbnail)
            put("createdAt", createdAt)
            put("lastUsed", lastUsed)
            put("useCount", useCount)
            put("fileSize", fileSize)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): ReferencePhoto? {
            return try {
                val obj = JSONObject(json)
                ReferencePhoto(
                    id = obj.getString("id"),
                    displayName = obj.getString("displayName"),
                    location = obj.optString("location", ""),
                    description = obj.optString("description", ""),
                    encryptedPath = obj.getString("encryptedPath"),
                    thumbnail = obj.optString("thumbnail", null),
                    createdAt = obj.getLong("createdAt"),
                    lastUsed = if (obj.isNull("lastUsed")) null else obj.getLong("lastUsed"),
                    useCount = obj.optInt("useCount", 0),
                    fileSize = obj.optLong("fileSize", 0)
                )
            } catch (e: Exception) {
                Log.e("ReferencePhoto", "Error parsing reference photo data", e)
                null
            }
        }

        fun generatePhotoId(): String {
            return "photo_${System.currentTimeMillis()}_${(100..999).random()}"
        }
    }
}

/**
 * Manages reference photos for photo verification missions
 */
class PhotoManager(private val context: Context) {

    companion object {
        private const val TAG = "PhotoManager"
        private const val PREFS_NAME = "photo_manager"
        private const val KEY_PHOTOS = "reference_photos"
        private const val PHOTOS_DIR = "reference_photos"
        private const val THUMBNAILS_DIR = "thumbnails"
        private const val MAX_PHOTOS = 15
        private const val MAX_FILE_SIZE_MB = 5
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val photosDirectory = File(context.filesDir, PHOTOS_DIR).apply {
        if (!exists()) mkdirs()
    }
    private val thumbnailsDirectory = File(context.filesDir, THUMBNAILS_DIR).apply {
        if (!exists()) mkdirs()
    }

    /**
     * Register a new reference photo
     */
    suspend fun registerPhoto(
        displayName: String,
        location: String = "",
        description: String = "",
        photoBitmap: Bitmap
    ): Result<ReferencePhoto> {

        val currentPhotos = getRegisteredPhotos().toMutableList()

        // Check maximum limit
        if (currentPhotos.size >= MAX_PHOTOS) {
            return Result.failure(Exception("Maximum $MAX_PHOTOS photos allowed"))
        }

        try {
            val photoId = ReferencePhoto.generatePhotoId()
            val encryptedFileName = "${photoId}.enc"
            val thumbnailFileName = "${photoId}_thumb.jpg"

            // Save encrypted photo
            val encryptedFile = File(photosDirectory, encryptedFileName)
            val success = saveEncryptedPhoto(photoBitmap, encryptedFile)

            if (!success) {
                return Result.failure(Exception("Failed to save photo"))
            }

            // Create thumbnail
            val thumbnailFile = File(thumbnailsDirectory, thumbnailFileName)
            createThumbnail(photoBitmap, thumbnailFile)

            val newPhoto = ReferencePhoto(
                id = photoId,
                displayName = displayName.ifBlank { "Photo ${currentPhotos.size + 1}" },
                location = location,
                description = description,
                encryptedPath = encryptedFile.absolutePath,
                thumbnail = thumbnailFile.absolutePath,
                fileSize = encryptedFile.length()
            )

            currentPhotos.add(newPhoto)
            savePhotos(currentPhotos)

            Log.d(TAG, "Registered new photo: ${newPhoto.displayName}")
            return Result.success(newPhoto)

        } catch (e: Exception) {
            Log.e(TAG, "Error registering photo", e)
            return Result.failure(e)
        }
    }

    /**
     * Get all registered photos
     */
    fun getRegisteredPhotos(): List<ReferencePhoto> {
        val photosJson = prefs.getString(KEY_PHOTOS, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(photosJson)
            (0 until jsonArray.length()).mapNotNull { index ->
                ReferencePhoto.fromJson(jsonArray.getString(index))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading photos", e)
            emptyList()
        }
    }

    /**
     * Remove a registered photo
     */
    fun removePhoto(photoId: String): Boolean {
        val currentPhotos = getRegisteredPhotos().toMutableList()
        val photoToRemove = currentPhotos.find { it.id == photoId }

        if (photoToRemove != null) {
            // Delete files
            try {
                File(photoToRemove.encryptedPath).delete()
                photoToRemove.thumbnail?.let { File(it).delete() }
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting photo files", e)
            }

            // Remove from list
            currentPhotos.removeIf { it.id == photoId }
            savePhotos(currentPhotos)

            Log.d(TAG, "Removed photo: $photoId")
            return true
        }

        return false
    }

    /**
     * Update photo usage statistics
     */
    fun updatePhotoUsage(photoId: String) {
        val currentPhotos = getRegisteredPhotos().toMutableList()
        val index = currentPhotos.indexOfFirst { it.id == photoId }

        if (index >= 0) {
            currentPhotos[index] = currentPhotos[index].copy(
                lastUsed = System.currentTimeMillis(),
                useCount = currentPhotos[index].useCount + 1
            )
            savePhotos(currentPhotos)
        }
    }

    /**
     * Get photo statistics
     */
    fun getPhotoStats(): PhotoStats {
        val photos = getRegisteredPhotos()
        val totalSize = photos.sumOf { it.fileSize }

        return PhotoStats(
            totalPhotos = photos.size,
            totalSizeMB = totalSize / (1024f * 1024f),
            mostUsedPhoto = photos.maxByOrNull { it.useCount },
            totalUsage = photos.sumOf { it.useCount }
        )
    }

    private fun saveEncryptedPhoto(bitmap: Bitmap, file: File): Boolean {
        return try {
            // Simple encryption - in production, use proper key management
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving encrypted photo", e)
            false
        }
    }

    private fun createThumbnail(bitmap: Bitmap, file: File): Boolean {
        return try {
            val thumbnailSize = 150
            val thumbnail = Bitmap.createScaledBitmap(
                bitmap,
                thumbnailSize,
                thumbnailSize,
                true
            )
            val outputStream = FileOutputStream(file)
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            outputStream.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating thumbnail", e)
            false
        }
    }

    private fun savePhotos(photos: List<ReferencePhoto>) {
        val jsonArray = JSONArray()
        photos.forEach { photo ->
            jsonArray.put(photo.toJson())
        }
        prefs.edit().putString(KEY_PHOTOS, jsonArray.toString()).apply()
    }

    data class PhotoStats(
        val totalPhotos: Int,
        val totalSizeMB: Float,
        val mostUsedPhoto: ReferencePhoto?,
        val totalUsage: Int
    )
}
