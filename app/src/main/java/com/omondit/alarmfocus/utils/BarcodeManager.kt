package com.omondit.alarmfocus.utils

import android.content.Context

/**
 * Manages registered barcodes for barcode scan mission with AES-256 encryption
 */
class BarcodeManager(private val context: Context) {

    private val encryptionManager = EncryptionManager(context)
    private var nextId = 1L

    data class RegisteredBarcode(
        val id: Long,
        val barcodeData: String,
        val label: String,
        val format: String,
        val createdAt: Long = System.currentTimeMillis()
    )

    init {
        // Load next ID from encrypted prefs
        nextId = encryptionManager.getDecryptedPref("barcode_next_id", "1")?.toLongOrNull() ?: 1L
    }

    /**
     * Register a new barcode with encryption
     */
    fun registerBarcode(barcodeData: String, label: String, format: String = "UNKNOWN"): RegisteredBarcode {
        val id = nextId++

        // Save encrypted barcode
        encryptionManager.saveEncryptedBarcode(id, barcodeData, label)

        // Save format separately
        encryptionManager.saveEncryptedPref("barcode_format_$id", format)

        // Save creation timestamp
        encryptionManager.saveEncryptedPref("barcode_created_$id", System.currentTimeMillis().toString())

        // Update next ID
        encryptionManager.saveEncryptedPref("barcode_next_id", nextId.toString())

        return RegisteredBarcode(id, barcodeData, label, format)
    }

    /**
     * Get all registered barcodes
     */
    fun getAllBarcodes(): List<RegisteredBarcode> {
        return encryptionManager.getAllBarcodeIds().mapNotNull { id ->
            val (data, label) = encryptionManager.getDecryptedBarcode(id)
            if (data != null && label != null) {
                val format = encryptionManager.getDecryptedPref("barcode_format_$id", "UNKNOWN") ?: "UNKNOWN"
                val createdAt = encryptionManager.getDecryptedPref("barcode_created_$id", "0")?.toLongOrNull() ?: 0L
                RegisteredBarcode(id, data, label, format, createdAt)
            } else {
                null
            }
        }
    }

    /**
     * Get barcode by ID
     */
    fun getBarcodeById(id: Long): RegisteredBarcode? {
        val (data, label) = encryptionManager.getDecryptedBarcode(id)
        return if (data != null && label != null) {
            val format = encryptionManager.getDecryptedPref("barcode_format_$id", "UNKNOWN") ?: "UNKNOWN"
            val createdAt = encryptionManager.getDecryptedPref("barcode_created_$id", "0")?.toLongOrNull() ?: 0L
            RegisteredBarcode(id, data, label, format, createdAt)
        } else {
            null
        }
    }

    /**
     * Check if scanned barcode matches any registered barcode
     */
    fun isRegisteredBarcode(scannedData: String): Boolean {
        return getAllBarcodes().any { it.barcodeData == scannedData }
    }

    /**
     * Get registered barcode by scanned data
     */
    fun getRegisteredBarcode(scannedData: String): RegisteredBarcode? {
        return getAllBarcodes().firstOrNull { it.barcodeData == scannedData }
    }

    /**
     * Delete registered barcode
     */
    fun deleteBarcode(id: Long) {
        encryptionManager.deleteEncryptedBarcode(id)
        encryptionManager.saveEncryptedPref("barcode_format_$id", "")
        encryptionManager.saveEncryptedPref("barcode_created_$id", "")
    }

    /**
     * Update barcode label
     */
    fun updateBarcodeLabel(id: Long, newLabel: String): Boolean {
        val barcode = getBarcodeById(id) ?: return false
        encryptionManager.saveEncryptedBarcode(id, barcode.barcodeData, newLabel)
        return true
    }

    /**
     * Get registered barcodes count
     */
    fun getRegisteredCount(): Int {
        return getAllBarcodes().size
    }

    /**
     * Clear all registered barcodes
     */
    fun clearAllBarcodes() {
        getAllBarcodes().forEach { deleteBarcode(it.id) }
        nextId = 1L
        encryptionManager.saveEncryptedPref("barcode_next_id", "1")
    }
}
