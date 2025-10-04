package com.omondit.alarmfocus.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * AES-256 Encryption Manager
 *
 * Provides encryption/decryption for sensitive user data:
 * - Custom alarm sound URIs
 * - Reference photos for photo mission
 * - Barcode strings
 * - User preferences
 */
class EncryptionManager(private val context: Context) {

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "adhd_alarm_secure_prefs"
        private const val KEY_SOUND_URIS = "encrypted_sound_uris"
        private const val KEY_BARCODES = "encrypted_barcodes"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Encrypt and save a custom alarm sound URI
     */
    fun saveEncryptedSoundUri(alarmId: Long, uri: String, soundName: String) {
        val key = "${KEY_SOUND_URIS}_${alarmId}"
        val value = "$uri|$soundName" // Store URI and name together
        encryptedPrefs.edit().putString(key, value).apply()
    }

    /**
     * Retrieve and decrypt a custom alarm sound URI
     */
    fun getDecryptedSoundUri(alarmId: Long): Pair<String?, String?> {
        val key = "${KEY_SOUND_URIS}_${alarmId}"
        val value = encryptedPrefs.getString(key, null)
        return if (value != null) {
            val parts = value.split("|")
            Pair(parts.getOrNull(0), parts.getOrNull(1))
        } else {
            Pair(null, null)
        }
    }

    /**
     * Delete encrypted sound URI
     */
    fun deleteEncryptedSoundUri(alarmId: Long) {
        val key = "${KEY_SOUND_URIS}_${alarmId}"
        encryptedPrefs.edit().remove(key).apply()
    }

    /**
     * Encrypt and save barcode data
     */
    fun saveEncryptedBarcode(barcodeId: Long, barcodeData: String, label: String) {
        val key = "${KEY_BARCODES}_${barcodeId}"
        val value = "$barcodeData|$label"
        encryptedPrefs.edit().putString(key, value).apply()
    }

    /**
     * Retrieve and decrypt barcode data
     */
    fun getDecryptedBarcode(barcodeId: Long): Pair<String?, String?> {
        val key = "${KEY_BARCODES}_${barcodeId}"
        val value = encryptedPrefs.getString(key, null)
        return if (value != null) {
            val parts = value.split("|")
            Pair(parts.getOrNull(0), parts.getOrNull(1))
        } else {
            Pair(null, null)
        }
    }

    /**
     * Delete encrypted barcode
     */
    fun deleteEncryptedBarcode(barcodeId: Long) {
        val key = "${KEY_BARCODES}_${barcodeId}"
        encryptedPrefs.edit().remove(key).apply()
    }

    /**
     * Get all encrypted barcode IDs
     */
    fun getAllBarcodeIds(): List<Long> {
        return encryptedPrefs.all.keys
            .filter { it.startsWith(KEY_BARCODES) }
            .mapNotNull { it.substringAfterLast("_").toLongOrNull() }
    }

    /**
     * Encrypt a file (for photo mission reference images)
     */
    fun encryptFile(sourceFile: File, encryptedFileName: String): File {
        val encryptedFile = File(context.filesDir, encryptedFileName)

        val encryptedFileWrapper = EncryptedFile.Builder(
            context,
            encryptedFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        // Copy source to encrypted file
        sourceFile.inputStream().use { input ->
            encryptedFileWrapper.openFileOutput().use { output ->
                input.copyTo(output)
            }
        }

        return encryptedFile
    }

    /**
     * Decrypt a file
     */
    fun decryptFile(encryptedFile: File): InputStream {
        val encryptedFileWrapper = EncryptedFile.Builder(
            context,
            encryptedFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        return encryptedFileWrapper.openFileInput()
    }

    /**
     * Save encrypted reference photo
     */
    fun saveEncryptedPhoto(alarmId: Long, photoFile: File): String {
        val encryptedFileName = "photo_${alarmId}_${System.currentTimeMillis()}.enc"
        encryptFile(photoFile, encryptedFileName)

        // Store encrypted file path
        val key = "photo_path_$alarmId"
        encryptedPrefs.edit().putString(key, encryptedFileName).apply()

        return encryptedFileName
    }

    /**
     * Get encrypted reference photo path
     */
    fun getEncryptedPhotoPath(alarmId: Long): String? {
        val key = "photo_path_$alarmId"
        return encryptedPrefs.getString(key, null)
    }

    /**
     * Delete encrypted photo
     */
    fun deleteEncryptedPhoto(alarmId: Long) {
        val encryptedFileName = getEncryptedPhotoPath(alarmId)
        if (encryptedFileName != null) {
            val file = File(context.filesDir, encryptedFileName)
            file.delete()

            val key = "photo_path_$alarmId"
            encryptedPrefs.edit().remove(key).apply()
        }
    }

    /**
     * Clear all encrypted data (for app reset/uninstall)
     */
    fun clearAllEncryptedData() {
        // Delete all encrypted photos
        context.filesDir.listFiles()?.filter { it.extension == "enc" }?.forEach { it.delete() }

        // Clear encrypted preferences
        encryptedPrefs.edit().clear().apply()
    }

    /**
     * Save encrypted preference
     */
    fun saveEncryptedPref(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    /**
     * Get decrypted preference
     */
    fun getDecryptedPref(key: String, defaultValue: String? = null): String? {
        return encryptedPrefs.getString(key, defaultValue)
    }

    /**
     * Check if encryption is available
     */
    fun isEncryptionAvailable(): Boolean {
        return try {
            masterKey
            true
        } catch (e: Exception) {
            false
        }
    }
}
