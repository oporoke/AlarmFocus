package com.omondit.alarmfocus.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Manages reference photos for photo comparison mission with AES-256 encryption
 */
class PhotoManager(private val context: Context) {

    private val encryptionManager = EncryptionManager(context)

    companion object {
        private const val SIMILARITY_THRESHOLD = 0.70f // 70% match required
        private const val MAX_PHOTO_SIZE_MB = 5
        private const val COMPRESSED_QUALITY = 85
    }

    /**
     * Save reference photo with encryption
     */
    suspend fun saveReferencePhoto(alarmId: Long, photoUri: Uri): Result<String> {
        return try {
            // Load and compress photo
            val bitmap = loadBitmapFromUri(photoUri)
                ?: return Result.failure(Exception("Failed to load photo"))

            // Compress to reduce file size
            val tempFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSED_QUALITY, out)
            }

            // Validate file size
            if (tempFile.length() > MAX_PHOTO_SIZE_MB * 1024 * 1024) {
                tempFile.delete()
                return Result.failure(Exception("Photo size exceeds ${MAX_PHOTO_SIZE_MB}MB limit"))
            }

            // Encrypt and save
            val encryptedFileName = encryptionManager.saveEncryptedPhoto(alarmId, tempFile)
            tempFile.delete()

            Result.success(encryptedFileName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Load reference photo (decrypted)
     */
    fun loadReferencePhoto(alarmId: Long): Bitmap? {
        val encryptedFileName = encryptionManager.getEncryptedPhotoPath(alarmId) ?: return null
        val encryptedFile = File(context.filesDir, encryptedFileName)

        if (!encryptedFile.exists()) return null

        return try {
            val inputStream = encryptionManager.decryptFile(encryptedFile)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Compare captured photo with reference photo using ML Kit
     */
    suspend fun comparePhotos(alarmId: Long, capturedBitmap: Bitmap): Result<Float> {
        return try {
            val referenceBitmap = loadReferencePhoto(alarmId)
                ?: return Result.failure(Exception("Reference photo not found"))

            val referenceLabels = extractLabels(referenceBitmap)
            val capturedLabels = extractLabels(capturedBitmap)

            val similarityScore = calculateSimilarity(referenceLabels, capturedLabels)

            Result.success(similarityScore)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if photos match (≥70% similarity)
     */
    suspend fun photosMatch(alarmId: Long, capturedBitmap: Bitmap): Boolean {
        val result = comparePhotos(alarmId, capturedBitmap)
        return result.getOrNull()?.let { it >= SIMILARITY_THRESHOLD } ?: false
    }

    /**
     * Delete reference photo
     */
    fun deleteReferencePhoto(alarmId: Long) {
        encryptionManager.deleteEncryptedPhoto(alarmId)
    }

    /**
     * Extract image labels using ML Kit
     */
    private suspend fun extractLabels(bitmap: Bitmap): Map<String, Float> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        return suspendCoroutine { continuation ->
            labeler.process(inputImage)
                .addOnSuccessListener { labels ->
                    val labelMap = labels.associate { it.text.lowercase() to it.confidence }
                    continuation.resume(labelMap)
                }
                .addOnFailureListener {
                    continuation.resume(emptyMap())
                }
        }
    }

    /**
     * Calculate similarity between two sets of labels
     */
    private fun calculateSimilarity(
        referenceLabels: Map<String, Float>,
        capturedLabels: Map<String, Float>
    ): Float {
        if (referenceLabels.isEmpty() || capturedLabels.isEmpty()) return 0f

        // Calculate Jaccard similarity with confidence weighting
        val allLabels = (referenceLabels.keys + capturedLabels.keys).toSet()
        var intersection = 0f
        var union = 0f

        allLabels.forEach { label ->
            val refConfidence = referenceLabels[label] ?: 0f
            val capConfidence = capturedLabels[label] ?: 0f

            intersection += minOf(refConfidence, capConfidence)
            union += maxOf(refConfidence, capConfidence)
        }

        return if (union > 0) intersection / union else 0f
    }

    /**
     * Load bitmap from URI
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if reference photo exists
     */
    fun hasReferencePhoto(alarmId: Long): Boolean {
        return encryptionManager.getEncryptedPhotoPath(alarmId) != null
    }

    /**
     * Get reference photo file size
     */
    fun getReferencePhotoSize(alarmId: Long): Long {
        val encryptedFileName = encryptionManager.getEncryptedPhotoPath(alarmId) ?: return 0
        val file = File(context.filesDir, encryptedFileName)
        return if (file.exists()) file.length() else 0
    }
}
