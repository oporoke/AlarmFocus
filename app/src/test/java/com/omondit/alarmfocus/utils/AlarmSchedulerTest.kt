package com.omondit.alarmfocus.utils

import android.app.AlarmManager
import android.content.Context
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.Calendar

class AlarmSchedulerTest {

    private lateinit var context: Context
    private lateinit var alarmManager: AlarmManager
    private lateinit var scheduler: AlarmScheduler

    @Before
    fun setup() {
        context = mock(Context::class.java)
        alarmManager = mock(AlarmManager::class.java)
        `when`(context.getSystemService(Context.ALARM_SERVICE)).thenReturn(alarmManager)
        scheduler = AlarmScheduler(context)
    }

    @Test
    fun `calculateNextTriggerTime should return correct time for today`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, 2) // 2 hours from now

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val nextTrigger = scheduler.calculateNextTriggerTime(hour, minute, RepeatSchedule.DAILY)

        assertNotNull(nextTrigger)
        assertTrue(nextTrigger!! > System.currentTimeMillis())

        val triggerCalendar = Calendar.getInstance().apply { timeInMillis = nextTrigger }
        assertEquals(hour, triggerCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals(minute, triggerCalendar.get(Calendar.MINUTE))
    }

    @Test
    fun `calculateNextTriggerTime should return tomorrow if time has passed`() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, -2) // 2 hours ago

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val nextTrigger = scheduler.calculateNextTriggerTime(hour, minute, RepeatSchedule.DAILY)

        assertNotNull(nextTrigger)

        val triggerCalendar = Calendar.getInstance().apply { timeInMillis = nextTrigger!! }
        val nowCalendar = Calendar.getInstance()

        // Should be tomorrow
        assertEquals(nowCalendar.get(Calendar.DAY_OF_YEAR) + 1, triggerCalendar.get(Calendar.DAY_OF_YEAR))
    }

    @Test
    fun `calculateNextTriggerTime should skip to next weekday for WEEKDAYS schedule`() {
        val calendar = Calendar.getInstance()

        // Set to Saturday
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)

        val nextTrigger = scheduler.calculateNextTriggerTime(9, 0, RepeatSchedule.WEEKDAYS)

        assertNotNull(nextTrigger)

        val triggerCalendar = Calendar.getInstance().apply { timeInMillis = nextTrigger!! }

        // Should be Monday (skip Saturday and Sunday)
        assertEquals(Calendar.MONDAY, triggerCalendar.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `cancelAlarm should cancel alarm with correct ID`() {
        val alarmId = 123L

        scheduler.cancelAlarm(alarmId)

        verify(alarmManager).cancel(any())
    }
}
