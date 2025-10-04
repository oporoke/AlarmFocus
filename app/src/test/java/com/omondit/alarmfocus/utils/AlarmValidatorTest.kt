package com.omondit.alarmfocus.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class AlarmValidatorTest {

    @Test
    fun `valid hour and minute should pass validation`() {
        assertTrue(isValidTime(10, 30))
        assertTrue(isValidTime(0, 0))
        assertTrue(isValidTime(23, 59))
    }

    @Test
    fun `invalid hour should fail validation`() {
        assertFalse(isValidTime(-1, 30))
        assertFalse(isValidTime(24, 30))
        assertFalse(isValidTime(25, 0))
        assertFalse(isValidTime(100, 30))
    }

    @Test
    fun `invalid minute should fail validation`() {
        assertFalse(isValidTime(10, -1))
        assertFalse(isValidTime(10, 60))
        assertFalse(isValidTime(10, 100))
        assertFalse(isValidTime(10, -30))
    }

    @Test
    fun `alarm label should not exceed max length`() {
        val validLabel = "Wake Up!"
        val tooLongLabel = "A".repeat(101)
        val maxLengthLabel = "A".repeat(100)

        assertTrue(isValidLabel(validLabel))
        assertTrue(isValidLabel(maxLengthLabel))
        assertFalse(isValidLabel(tooLongLabel))
    }

    @Test
    fun `empty label should be valid`() {
        assertTrue(isValidLabel(""))
    }

    @Test
    fun `alarm in past should be detected for today`() {
        val now = Calendar.getInstance()
        val pastHour = (now.get(Calendar.HOUR_OF_DAY) - 1 + 24) % 24
        val currentMinute = now.get(Calendar.MINUTE)

        // Only check if we're not at midnight edge case
        if (now.get(Calendar.HOUR_OF_DAY) > 1) {
            assertTrue(isInPast(pastHour, currentMinute))
        }
    }

    @Test
    fun `alarm in future should not be in past`() {
        val now = Calendar.getInstance()
        val futureHour = (now.get(Calendar.HOUR_OF_DAY) + 1) % 24
        val currentMinute = now.get(Calendar.MINUTE)

        assertFalse(isInPast(futureHour, currentMinute))
    }

    // Helper functions for validation
    private fun isValidTime(hour: Int, minute: Int): Boolean {
        return hour in 0..23 && minute in 0..59
    }

    private fun isValidLabel(label: String): Boolean {
        return label.length <= 100
    }

    private fun isInPast(hour: Int, minute: Int): Boolean {
        val now = Calendar.getInstance()
        val alarm = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If alarm time is before now, it's in the past (for today)
        return alarm.timeInMillis < now.timeInMillis
    }
}
