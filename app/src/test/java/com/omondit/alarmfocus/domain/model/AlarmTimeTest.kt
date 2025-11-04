package com.omondit.alarmfocus.domain.model

import org.junit.Assert.*
import org.junit.Test

class AlarmTimeTest {

    @Test
    fun `AlarmTime should validate correct hours and minutes`() {
        val time = AlarmTime(10, 30)
        assertEquals(10, time.hour)
        assertEquals(30, time.minute)
    }

    @Test
    fun `isValid should return true for valid time`() {
        val validTimes = listOf(
            AlarmTime(0, 0),
            AlarmTime(12, 30),
            AlarmTime(23, 59)
        )

        validTimes.forEach { time ->
            assertTrue("Time $time should be valid", time.isValid())
        }
    }

    @Test
    fun `isValid should return false for invalid time`() {
        val invalidTimes = listOf(
            AlarmTime(-1, 0),
            AlarmTime(24, 0),
            AlarmTime(12, -1),
            AlarmTime(12, 60)
        )

        invalidTimes.forEach { time ->
            assertFalse("Time $time should be invalid", time.isValid())
        }
    }

    @Test
    fun `toMillisToday should calculate correct time for today`() {
        val time = AlarmTime(14, 30) // 2:30 PM
        val millis = time.toMillisToday()

        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = millis

        assertEquals(14, calendar.get(java.util.Calendar.HOUR_OF_DAY))
        assertEquals(30, calendar.get(java.util.Calendar.MINUTE))
        assertEquals(0, calendar.get(java.util.Calendar.SECOND))
    }

    @Test
    fun `formatted should return correct 12-hour format`() {
        assertEquals("12:00 AM", AlarmTime(0, 0).formatted())
        assertEquals("12:30 AM", AlarmTime(0, 30).formatted())
        assertEquals("1:00 PM", AlarmTime(13, 0).formatted())
        assertEquals("11:59 PM", AlarmTime(23, 59).formatted())
    }

    @Test
    fun `formatted24Hour should return correct 24-hour format`() {
        assertEquals("00:00", AlarmTime(0, 0).formatted24Hour())
        assertEquals("09:05", AlarmTime(9, 5).formatted24Hour())
        assertEquals("23:59", AlarmTime(23, 59).formatted24Hour())
    }

    @Test
    fun `compareTo should order times correctly`() {
        val time1 = AlarmTime(9, 0)
        val time2 = AlarmTime(9, 30)
        val time3 = AlarmTime(10, 0)

        assertTrue(time1 < time2)
        assertTrue(time2 < time3)
        assertTrue(time1 < time3)
        assertEquals(0, time1.compareTo(AlarmTime(9, 0)))
    }
}
