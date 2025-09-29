package com.omondit.alarmfocus.domain.model

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

/**
 * Barcode/QR Code Mission Implementation
 * Forces user to scan a specific barcode to dismiss alarm
 */
class BarcodeMission(
    override val difficulty: Mission.Difficulty = Mission.Difficulty.EASY,
    override val config: MissionConfig = MissionConfig(),
    private val registeredBarcodes: List<BarcodeData> = emptyList()
) : Mission() {

    override val type = MissionType.BARCODE

    companion object {
        private const val TAG = "BarcodeMission"
        private const val VERIFICATION_TIMEOUT_SECONDS = 10
        private const val MAX_SCAN_ATTEMPTS = 5
    }

    override fun generateChallenge(): Challenge {
        if (registeredBarcodes.isEmpty()) {
            return Challenge(
                id = "barcode_no_codes",
                question = "No barcodes registered. Please register a barcode first.",
                correctAnswer = "",
                timeoutSeconds = 0,
                allowedAttempts = 0
            )
        }

        // Select a random registered barcode for the challenge
        val targetBarcode = registeredBarcodes.random()

        return Challenge(
            id = generateChallengeId(),
            question = "Scan the barcode: ${targetBarcode.displayName}",
            correctAnswer = targetBarcode.code,
            data = mapOf(
                "barcodeId" to targetBarcode.id,
                "displayName" to targetBarcode.displayName,
                "location" to targetBarcode.location,
                "type" to targetBarcode.type.name
            ),
            timeoutSeconds = when (difficulty) {
                Difficulty.EASY -> 60  // More time for easy mode
                Difficulty.MEDIUM -> 45
                Difficulty.HARD -> 30  // Less time for hard mode
            },
            allowedAttempts = MAX_SCAN_ATTEMPTS
        )
    }

    override fun validateAnswer(challenge: Challenge, answer: String): ValidationResult {
        val expectedCode = challenge.correctAnswer
        val scannedCode = answer.trim()

        return if (scannedCode == expectedCode) {
            ValidationResult(
                isCorrect = true,
                message = "Correct barcode scanned! Alarm dismissed.",
                shouldEscalate = false
            )
        } else if (scannedCode.isEmpty()) {
            ValidationResult(
                isCorrect = false,
                message = "No barcode detected. Try again.",
                shouldEscalate = false
            )
        } else {
            ValidationResult(
                isCorrect = false,
                message = "Wrong barcode. Scan: ${challenge.data["displayName"]}",
                shouldEscalate = true
            )
        }
    }

    private fun generateChallengeId(): String {
        return "barcode_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Represents a registered barcode for wake-up missions
 */
data class BarcodeData(
    val id: String = generateBarcodeId(),
    val code: String,
    val type: BarcodeType,
    val displayName: String,
    val location: String = "",
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long? = null,
    val useCount: Int = 0
) {

    enum class BarcodeType {
        QR_CODE,
        CODE_128,
        CODE_39,
        EAN_13,
        EAN_8,
        UPC_A,
        UPC_E,
        UNKNOWN
    }

    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("code", code)
            put("type", type.name)
            put("displayName", displayName)
            put("location", location)
            put("description", description)
            put("createdAt", createdAt)
            put("lastUsed", lastUsed)
            put("useCount", useCount)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): BarcodeData? {
            return try {
                val obj = JSONObject(json)
                BarcodeData(
                    id = obj.getString("id"),
                    code = obj.getString("code"),
                    type = BarcodeType.valueOf(obj.getString("type")),
                    displayName = obj.getString("displayName"),
                    location = obj.optString("location", ""),
                    description = obj.optString("description", ""),
                    createdAt = obj.getLong("createdAt"),
                    lastUsed = if (obj.isNull("lastUsed")) null else obj.getLong("lastUsed"),
                    useCount = obj.optInt("useCount", 0)
                )
            } catch (e: Exception) {
                Log.e("BarcodeData", "Error parsing barcode data", e)
                null
            }
        }

        private fun generateBarcodeId(): String {
            return "bc_${System.currentTimeMillis()}_${(100..999).random()}"
        }
    }
}

/**
 * Manages registered barcodes for missions
 */
class BarcodeManager(private val context: Context) {

    companion object {
        private const val TAG = "BarcodeManager"
        private const val PREFS_NAME = "barcode_manager"
        private const val KEY_BARCODES = "registered_barcodes"
        private const val MAX_BARCODES = 20
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Register a new barcode for wake-up missions
     */
    fun registerBarcode(
        code: String,
        type: BarcodeData.BarcodeType,
        displayName: String,
        location: String = "",
        description: String = ""
    ): Result<BarcodeData> {

        val currentBarcodes = getRegisteredBarcodes().toMutableList()

        // Check if barcode already exists
        if (currentBarcodes.any { it.code == code }) {
            return Result.failure(Exception("Barcode already registered"))
        }

        // Check maximum limit
        if (currentBarcodes.size >= MAX_BARCODES) {
            return Result.failure(Exception("Maximum $MAX_BARCODES barcodes allowed"))
        }

        // Create new barcode
        val newBarcode = BarcodeData(
            code = code,
            type = type,
            displayName = displayName.ifBlank { "Barcode ${currentBarcodes.size + 1}" },
            location = location,
            description = description
        )

        // Add to list and save
        currentBarcodes.add(newBarcode)
        saveBarcodes(currentBarcodes)

        Log.d(TAG, "Registered new barcode: ${newBarcode.displayName}")
        return Result.success(newBarcode)
    }

    /**
     * Get all registered barcodes
     */
    fun getRegisteredBarcodes(): List<BarcodeData> {
        val barcodesJson = prefs.getString(KEY_BARCODES, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(barcodesJson)
            (0 until jsonArray.length()).mapNotNull { index ->
                BarcodeData.fromJson(jsonArray.getString(index))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading barcodes", e)
            emptyList()
        }
    }

    /**
     * Remove a registered barcode
     */
    fun removeBarcode(barcodeId: String): Boolean {
        val currentBarcodes = getRegisteredBarcodes().toMutableList()
        val removed = currentBarcodes.removeIf { it.id == barcodeId }

        if (removed) {
            saveBarcodes(currentBarcodes)
            Log.d(TAG, "Removed barcode: $barcodeId")
        }

        return removed
    }

    /**
     * Update barcode usage statistics
     */
    fun updateBarcodeUsage(barcodeId: String) {
        val currentBarcodes = getRegisteredBarcodes().toMutableList()
        val index = currentBarcodes.indexOfFirst { it.id == barcodeId }

        if (index >= 0) {
            currentBarcodes[index] = currentBarcodes[index].copy(
                lastUsed = System.currentTimeMillis(),
                useCount = currentBarcodes[index].useCount + 1
            )
            saveBarcodes(currentBarcodes)
        }
    }

    /**
     * Get barcode by ID
     */
    fun getBarcodeById(barcodeId: String): BarcodeData? {
        return getRegisteredBarcodes().find { it.id == barcodeId }
    }

    /**
     * Update barcode details
     */
    fun updateBarcode(barcodeId: String, displayName: String?, location: String?, description: String?): Boolean {
        val currentBarcodes = getRegisteredBarcodes().toMutableList()
        val index = currentBarcodes.indexOfFirst { it.id == barcodeId }

        if (index >= 0) {
            currentBarcodes[index] = currentBarcodes[index].copy(
                displayName = displayName ?: currentBarcodes[index].displayName,
                location = location ?: currentBarcodes[index].location,
                description = description ?: currentBarcodes[index].description
            )
            saveBarcodes(currentBarcodes)
            return true
        }

        return false
    }

    /**
     * Get statistics about registered barcodes
     */
    fun getBarcodeStats(): BarcodeStats {
        val barcodes = getRegisteredBarcodes()
        return BarcodeStats(
            totalBarcodes = barcodes.size,
            mostUsedBarcode = barcodes.maxByOrNull { it.useCount },
            totalScans = barcodes.sumOf { it.useCount },
            averageScansPerBarcode = if (barcodes.isNotEmpty()) {
                barcodes.sumOf { it.useCount }.toFloat() / barcodes.size
            } else 0f
        )
    }

    private fun saveBarcodes(barcodes: List<BarcodeData>) {
        val jsonArray = JSONArray()
        barcodes.forEach { barcode ->
            jsonArray.put(barcode.toJson())
        }
        prefs.edit().putString(KEY_BARCODES, jsonArray.toString()).apply()
    }

    data class BarcodeStats(
        val totalBarcodes: Int,
        val mostUsedBarcode: BarcodeData?,
        val totalScans: Int,
        val averageScansPerBarcode: Float
    )
}
