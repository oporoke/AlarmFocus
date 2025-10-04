package com.omondit.alarmfocus.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Use SDK 28 for Robolectric compatibility
class EncryptionManagerTest {

    private lateinit var encryptionManager: EncryptionManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        encryptionManager = EncryptionManager(context)
    }

    @Test
    fun `save and retrieve encrypted sound URI`() {
        val alarmId = 1L
        val uri = "content://media/external/audio/1234"
        val soundName = "Custom Alarm.mp3"

        encryptionManager.saveEncryptedSoundUri(alarmId, uri, soundName)
        val (retrievedUri, retrievedName) = encryptionManager.getDecryptedSoundUri(alarmId)

        assertEquals(uri, retrievedUri)
        assertEquals(soundName, retrievedName)
    }

    @Test
    fun `save and retrieve encrypted barcode`() {
        val barcodeId = 1L
        val barcodeData = "1234567890"
        val label = "Kitchen Item"

        encryptionManager.saveEncryptedBarcode(barcodeId, barcodeData, label)
        val (retrievedData, retrievedLabel) = encryptionManager.getDecryptedBarcode(barcodeId)

        assertEquals(barcodeData, retrievedData)
        assertEquals(label, retrievedLabel)
    }

    @Test
    fun `delete encrypted sound URI should work`() {
        val alarmId = 2L
        encryptionManager.saveEncryptedSoundUri(alarmId, "test_uri", "test_name")
        encryptionManager.deleteEncryptedSoundUri(alarmId)

        val (uri, name) = encryptionManager.getDecryptedSoundUri(alarmId)
        assertNull(uri)
        assertNull(name)
    }

    @Test
    fun `delete encrypted barcode should work`() {
        val barcodeId = 3L
        encryptionManager.saveEncryptedBarcode(barcodeId, "test_data", "test_label")
        encryptionManager.deleteEncryptedBarcode(barcodeId)

        val (data, label) = encryptionManager.getDecryptedBarcode(barcodeId)
        assertNull(data)
        assertNull(label)
    }

    @Test
    fun `encryption should be available`() {
        assertTrue(encryptionManager.isEncryptionAvailable())
    }

    @Test
    fun `save and retrieve encrypted preference`() {
        val key = "test_key"
        val value = "test_value"

        encryptionManager.saveEncryptedPref(key, value)
        val retrieved = encryptionManager.getDecryptedPref(key)

        assertEquals(value, retrieved)
    }

    @Test
    fun `get non-existent encrypted data should return null`() {
        val (uri, name) = encryptionManager.getDecryptedSoundUri(999L)
        assertNull(uri)
        assertNull(name)

        val (data, label) = encryptionManager.getDecryptedBarcode(999L)
        assertNull(data)
        assertNull(label)
    }

    @Test
    fun `get all barcode IDs should work`() {
        encryptionManager.saveEncryptedBarcode(1L, "data1", "label1")
        encryptionManager.saveEncryptedBarcode(2L, "data2", "label2")

        val ids = encryptionManager.getAllBarcodeIds()
        assertTrue(ids.contains(1L))
        assertTrue(ids.contains(2L))
    }
}
