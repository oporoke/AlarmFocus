package com.omondit.alarmfocus.domain.model

import org.junit.Assert.*
import org.junit.Test

class MissionConfigTest {

    @Test
    fun `default config should have NONE type`() {
        val config = MissionConfig()

        assertEquals(Mission.MissionType.NONE, config.type)
        assertEquals(Mission.Difficulty.EASY, config.difficulty)
        assertTrue(config.parameters.isEmpty())
    }

    @Test
    fun `toJson should serialize config correctly`() {
        val config = MissionConfig(
            type = Mission.MissionType.MATH,
            difficulty = Mission.Difficulty.MEDIUM,
            parameters = mapOf("timeout" to "60", "attempts" to "3")
        )

        val json = config.toJson()

        assertTrue(json.contains("MATH"))
        assertTrue(json.contains("MEDIUM"))
        assertTrue(json.contains("timeout"))
        assertTrue(json.contains("attempts"))
    }

    @Test
    fun `fromJson should deserialize config correctly`() {
        val json = """
            {
                "type": "MATH",
                "difficulty": "HARD",
                "parameters": {
                    "max_attempts": "5"
                }
            }
        """.trimIndent()

        val config = MissionConfig.fromJson(json)

        assertEquals(Mission.MissionType.MATH, config.type)
        assertEquals(Mission.Difficulty.HARD, config.difficulty)
        assertEquals("5", config.parameters["max_attempts"])
    }

    @Test
    fun `fromJson should handle invalid JSON gracefully`() {
        val invalidJson = "{ invalid json }"

        val config = MissionConfig.fromJson(invalidJson)

        // Should return default config on parse failure
        assertEquals(Mission.MissionType.NONE, config.type)
        assertEquals(Mission.Difficulty.EASY, config.difficulty)
    }

    @Test
    fun `fromJson should handle missing fields`() {
        val json = "{}"

        val config = MissionConfig.fromJson(json)

        assertEquals(Mission.MissionType.NONE, config.type)
        assertEquals(Mission.Difficulty.EASY, config.difficulty)
        assertTrue(config.parameters.isEmpty())
    }

    @Test
    fun `roundtrip serialization should preserve data`() {
        val original = MissionConfig(
            type = Mission.MissionType.BARCODE,
            difficulty = Mission.Difficulty.MEDIUM,
            parameters = mapOf("key1" to "value1", "key2" to "value2")
        )

        val json = original.toJson()
        val deserialized = MissionConfig.fromJson(json)

        assertEquals(original.type, deserialized.type)
        assertEquals(original.difficulty, deserialized.difficulty)
        assertEquals(original.parameters["key1"], deserialized.parameters["key1"])
        assertEquals(original.parameters["key2"], deserialized.parameters["key2"])
    }
}
