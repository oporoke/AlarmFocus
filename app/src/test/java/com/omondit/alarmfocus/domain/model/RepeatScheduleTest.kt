package com.omondit.alarmfocus.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class RepeatScheduleTest {

    @Test
    fun `once schedule should return correct display text`() {
        val schedule = RepeatSchedule.once()
        assertEquals("Once", schedule.getDisplayText())
    }

    @Test
    fun `daily schedule should return correct display text`() {
        val schedule = RepeatSchedule.daily()
        assertEquals("Every day", schedule.getDisplayText())
    }

    @Test
    fun `weekdays schedule should include Monday to Friday`() {
        val schedule = RepeatSchedule.weekdays()
        assertTrue(schedule.days.contains(Calendar.MONDAY))
        assertTrue(schedule.days.contains(Calendar.TUESDAY))
        assertTrue(schedule.days.contains(Calendar.WEDNESDAY))
        assertTrue(schedule.days.contains(Calendar.THURSDAY))
        assertTrue(schedule.days.contains(Calendar.FRIDAY))
        assertFalse(schedule.days.contains(Calendar.SATURDAY))
        assertFalse(schedule.days.contains(Calendar.SUNDAY))
    }

    @Test
    fun `weekends schedule should include Saturday and Sunday`() {
        val schedule = RepeatSchedule.weekends()
        assertTrue(schedule.days.contains(Calendar.SATURDAY))
        assertTrue(schedule.days.contains(Calendar.SUNDAY))
        assertFalse(schedule.days.contains(Calendar.MONDAY))
        assertFalse(schedule.days.contains(Calendar.TUESDAY))
    }

    @Test
    fun `custom schedule with all days should behave like daily`() {
        val schedule = RepeatSchedule.custom(listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
        ))
        assertEquals(RepeatSchedule.RepeatType.DAILY, schedule.type)
    }

    @Test
    fun `custom schedule with weekdays should behave like weekdays`() {
        val schedule = RepeatSchedule.custom(listOf(
            Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
            Calendar.THURSDAY, Calendar.FRIDAY
        ))
        assertEquals(RepeatSchedule.RepeatType.WEEKDAYS, schedule.type)
    }

    @Test
    fun `JSON serialization should round-trip correctly`() {
        val original = RepeatSchedule.weekdays()
        val json = original.toJson()
        val deserialized = RepeatSchedule.fromJson(json)
        assertEquals(original.type, deserialized.type)
        assertEquals(original.days, deserialized.days)
    }

    @Test
    fun `empty custom schedule should default to once`() {
        val schedule = RepeatSchedule.custom(emptyList())
        assertEquals(RepeatSchedule.RepeatType.ONCE, schedule.type)
    }

    @Test
    fun `isRepeating should return false for once type`() {
        val schedule = RepeatSchedule.once()
        assertFalse(schedule.isRepeating())
    }

    @Test
    fun `isRepeating should return true for daily type`() {
        val schedule = RepeatSchedule.daily()
        assertTrue(schedule.isRepeating())
    }
}
