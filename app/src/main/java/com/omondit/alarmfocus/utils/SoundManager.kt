package com.omondit.alarmfocus.utils

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * Manages custom alarm sounds with encryption and validation
 * Handles file upload, validation, storage, and playback
 */
class SoundManager(private val context: Context) {

    companion object {
        private const val TAG = "SoundManager"
        private const val SOUNDS_DIR = "custom_sounds"
        private const val MAX_FILE_SIZE_MB = 10
        private const val ENCRYPTION_ALGORITHM = "AES"
        private const val PREFS_NAME = "sound_manager_prefs"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
    }

    data class SoundFile(
        val id: String,
        val originalName: String,
        val displayName: String,
        val filePath: String,
        val duration: Long,
        val fileSize: Long,
        val isEncrypted: Boolean,
        val createdAt: Long = System.currentTimeMillis()
    )

    data class SoundValidationResult(
        val isValid: Boolean,
        val soundFile: SoundFile? = null,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )

    private val soundsDirectory: File by lazy {
        File(context.filesDir, SOUNDS_DIR).apply {
            if (!exists()) mkdirs()
        }
    }

    private val encryptionKey: SecretKey by lazy {
        getOrCreateEncryptionKey()
    }

    /**
     * Upload and validate a custom sound file
     */
    suspend fun uploadSoundFile(
        uri: Uri,
        displayName: String? = null
    ): SoundValidationResult = withContext(Dispatchers.IO) {

        try {
            // Get file info
            val (originalName, fileSize) = getFileInfo(uri)
                ?: return@withContext SoundValidationResult(
                    false,
                    errors = listOf("Could not read file information")
                )

            // Validate file size
            if (fileSize > MAX_FILE_SIZE_MB * 1024 * 1024) {
                return@withContext SoundValidationResult(
                    false,
                    errors = listOf("File too large (${fileSize / 1024 / 1024}MB). Maximum size is ${MAX_FILE_SIZE_MB}MB.")
                )
            }

            // Validate audio format and get metadata
            val audioMetadata = validateAudioFile(uri)
            if (!audioMetadata.isValid) {
                return@withContext SoundValidationResult(
                    false,
                    errors = audioMetadata.errors,
                    warnings = audioMetadata.warnings
                )
            }

            // Generate unique ID and file path
            val soundId = generateSoundId(originalName)
            val fileName = "${soundId}.enc" // Encrypted file extension
            val destinationFile = File(soundsDirectory, fileName)

            // Copy and encrypt file
            val success = copyAndEncryptFile(uri, destinationFile)
            if (!success) {
                return@withContext SoundValidationResult(
                    false,
                    errors = listOf("Failed to save sound file")
                )
            }

            // Create sound file object
            val soundFile = SoundFile(
                id = soundId,
                originalName = originalName,
                displayName = displayName ?: originalName.substringBeforeLast('.'),
                filePath = destinationFile.absolutePath,
                duration = audioMetadata.duration,
                fileSize = fileSize,
                isEncrypted = true
            )

            Log.i(TAG, "Successfully uploaded sound: ${soundFile.displayName}")

            SoundValidationResult(
                isValid = true,
                soundFile = soundFile,
                warnings = audioMetadata.warnings
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error uploading sound file", e)
            SoundValidationResult(
                false,
                errors = listOf("Upload failed: ${e.message}")
            )
        }
    }

    /**
     * Get list of all custom sound files
     */
    fun getCustomSounds(): List<SoundFile> {
        return try {
            soundsDirectory.listFiles()?.mapNotNull { file ->
                try {
                    // Parse file info from filename and metadata
                    // This is a simplified version - in production, you'd store metadata separately
                    val soundId = file.nameWithoutExtension
                    SoundFile(
                        id = soundId,
                        originalName = "Custom Sound",
                        displayName = file.nameWithoutExtension.replace("_", " "),
                        filePath = file.absolutePath,
                        duration = 0L, // Would be stored in metadata
                        fileSize = file.length(),
                        isEncrypted = true
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Error reading sound file: ${file.name}", e)
                    null
                }
            } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting custom sounds", e)
            emptyList()
        }
    }

    /**
     * Delete a custom sound file
     */
    suspend fun deleteSoundFile(soundId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(soundsDirectory, "${soundId}.enc")
            val deleted = file.delete()
            if (deleted) {
                Log.i(TAG, "Deleted sound file: $soundId")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting sound file: $soundId", e)
            false
        }
    }

    /**
     * Create a MediaPlayer for a custom sound
     */
    suspend fun createMediaPlayer(soundFile: SoundFile): MediaPlayer? = withContext(Dispatchers.IO) {
        try {
            val tempFile = decryptToTempFile(soundFile)
            if (tempFile != null) {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(tempFile.absolutePath)
                mediaPlayer.prepareAsync()
                mediaPlayer
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error creating media player for ${soundFile.id}", e)
            null
        }
    }

    /**
     * Preview a sound file
     */
    suspend fun previewSound(soundFile: SoundFile, onComplete: () -> Unit) = withContext(Dispatchers.IO) {
        try {
            val mediaPlayer = createMediaPlayer(soundFile)
            mediaPlayer?.apply {
                setOnCompletionListener {
                    release()
                    onComplete()
                }
                setOnPreparedListener { player ->
                    player.start()
                    // Stop after 10 seconds for preview
                    player.seekTo(0)
                }
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error previewing sound", e)
        }
    }

    /**
     * Get storage usage information
     */
    fun getStorageUsage(): StorageInfo {
        val totalSize = soundsDirectory.listFiles()?.sumOf { it.length() } ?: 0L
        val fileCount = soundsDirectory.listFiles()?.size ?: 0
        val availableSpace = soundsDirectory.freeSpace

        return StorageInfo(
            totalSize = totalSize,
            fileCount = fileCount,
            availableSpace = availableSpace
        )
    }

    private fun getFileInfo(uri: Uri): Pair<String, Long>? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "unknown.mp3"
                    val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L

                    Pair(name, size)
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file info", e)
            null
        }
    }

    private fun validateAudioFile(uri: Uri): AudioValidationResult {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 0L

            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)

            retriever.release()

            val errors = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            if (duration == 0L) {
                errors.add("Invalid audio file or unable to read duration")
            } else if (duration < 1000) {
                errors.add("Audio file too short (minimum 1 second)")
            } else if (duration > 300000) {
                warnings.add("Audio file is very long (${duration/1000}s)")
            }

            if (mimeType?.startsWith("audio/") != true) {
                errors.add("File is not a valid audio format")
            }

            AudioValidationResult(
                isValid = errors.isEmpty(),
                duration = duration,
                errors = errors,
                warnings = warnings
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error validating audio file", e)
            AudioValidationResult(
                false,
                errors = listOf("Audio validation failed: ${e.message}")
            )
        }
    }

    private suspend fun copyAndEncryptFile(uri: Uri, destination: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
                cipher.init(Cipher.ENCRYPT_MODE, encryptionKey)

                FileOutputStream(destination).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } > 0) {
                        val encryptedBytes = cipher.update(buffer, 0, bytesRead)
                        if (encryptedBytes != null) {
                            output.write(encryptedBytes)
                        }
                    }
                    val finalBytes = cipher.doFinal()
                    if (finalBytes != null) {
                        output.write(finalBytes)
                    }
                }
                inputStream.close()
                true
            } else false
        } catch (e: Exception) {
            Log.e(TAG, "Error copying and encrypting file", e)
            false
        }
    }

    private suspend fun decryptToTempFile(soundFile: SoundFile): File? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(soundFile.filePath)
            if (!sourceFile.exists()) return@withContext null

            val tempFile = File.createTempFile("sound_${soundFile.id}", ".tmp", context.cacheDir)

            val cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey)

            sourceFile.inputStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val encryptedData = input.readBytes()
                    val decryptedData = cipher.doFinal(encryptedData)
                    output.write(decryptedData)
                }
            }

            tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting sound file", e)
            null
        }
    }

    private fun generateSoundId(fileName: String): String {
        val timestamp = System.currentTimeMillis()
        val hash = MessageDigest.getInstance("MD5")
            .digest("$fileName$timestamp".toByteArray())
            .joinToString("") { "%02x".format(it) }
        return hash.take(8)
    }

    private fun getOrCreateEncryptionKey(): SecretKey {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keyString = prefs.getString(KEY_ENCRYPTION_KEY, null)

        return if (keyString != null) {
            SecretKeySpec(android.util.Base64.decode(keyString, android.util.Base64.DEFAULT), ENCRYPTION_ALGORITHM)
        } else {
            // Generate new key
            val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM)
            keyGenerator.init(256)
            val newKey = keyGenerator.generateKey()

            // Save key
            val encodedKey = android.util.Base64.encodeToString(newKey.encoded, android.util.Base64.DEFAULT)
            prefs.edit().putString(KEY_ENCRYPTION_KEY, encodedKey).apply()

            newKey
        }
    }

    data class AudioValidationResult(
        val isValid: Boolean,
        val duration: Long = 0,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )

    data class StorageInfo(
        val totalSize: Long,
        val fileCount: Int,
        val availableSpace: Long
    ) {
        val totalSizeMB: Double get() = totalSize / (1024.0 * 1024.0)
        val availableSpaceMB: Double get() = availableSpace / (1024.0 * 1024.0)
    }
}
