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
@Config(sdk = [28])
class BarcodeManagerTest {

    private lateinit var barcodeManager: BarcodeManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        barcodeManager = BarcodeManager(context)
        // Clear any existing data
        barcodeManager.clearAllBarcodes()
    }

    @Test
    fun `register barcode should create new entry`() {
        val barcodeData = "1234567890"
        val label = "Test Product"
        val format = "EAN_13"

        val registered = barcodeManager.registerBarcode(barcodeData, label, format)

        assertEquals(barcodeData, registered.barcodeData)
        assertEquals(label, registered.label)
        assertEquals(format, registered.format)
        assertTrue(registered.id > 0)
    }

    @Test
    fun `get all barcodes should return registered barcodes`() {
        barcodeManager.registerBarcode("111", "Item 1", "CODE_128")
        barcodeManager.registerBarcode("222", "Item 2", "QR_CODE")

        val all = barcodeManager.getAllBarcodes()

        assertEquals(2, all.size)
        assertTrue(all.any { it.label == "Item 1" })
        assertTrue(all.any { it.label == "Item 2" })
    }

    @Test
    fun `is registered barcode should detect existing codes`() {
        val barcodeData = "9876543210"
        barcodeManager.registerBarcode(barcodeData, "Test", "EAN_13")

        assertTrue(barcodeManager.isRegisteredBarcode(barcodeData))
        assertFalse(barcodeManager.isRegisteredBarcode("unknown"))
    }

    @Test
    fun `delete barcode should remove entry`() {
        val registered = barcodeManager.registerBarcode("123", "Test", "EAN_8")
        val id = registered.id

        barcodeManager.deleteBarcode(id)

        assertNull(barcodeManager.getBarcodeById(id))
        assertEquals(0, barcodeManager.getRegisteredCount())
    }

    @Test
    fun `update barcode label should work`() {
        val registered = barcodeManager.registerBarcode("456", "Old Label", "CODE_39")
        val id = registered.id

        barcodeManager.updateBarcodeLabel(id, "New Label")

        val updated = barcodeManager.getBarcodeById(id)
        assertEquals("New Label", updated?.label)
    }

    @Test
    fun `clear all barcodes should remove all entries`() {
        barcodeManager.registerBarcode("111", "Item 1", "EAN_13")
        barcodeManager.registerBarcode("222", "Item 2", "EAN_13")

        barcodeManager.clearAllBarcodes()

        assertEquals(0, barcodeManager.getRegisteredCount())
        assertTrue(barcodeManager.getAllBarcodes().isEmpty())
    }
}
