package com.omondit.alarmfocus.domain.usecase

import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class GetUpcomingAlarmsUseCase(
    private val repository: AlarmRepository
) {

    data class UpcomingAlarm(
        val alarm: AlarmEntity,
        val nextTriggerTime: Long?,
        val timeUntilNext: Long?,
        val isToday: Boolean,
        val isOverdue: Boolean
    )

    operator fun invoke(): Flow<List<UpcomingAlarm>> {
        return repository.getEnabledAlarms().map { alarms ->
            val now = System.currentTimeMillis()
            val todayCalendar = Calendar.getInstance()

            alarms.mapNotNull { alarm ->
                val schedule = alarm.getRepeatSchedule()
                val alarmTime = alarm.toAlarmTime()
                val nextTriggerTime = schedule.getNextOccurrence(alarmTime.toMillisToday())

                if (nextTriggerTime != null) {
                    val timeUntilNext = nextTriggerTime - now
                    val triggerCalendar = Calendar.getInstance().apply { timeInMillis = nextTriggerTime }
                    val isToday = isSameDay(todayCalendar, triggerCalendar)
                    val isOverdue = nextTriggerTime < now

                    UpcomingAlarm(
                        alarm = alarm,
                        nextTriggerTime = nextTriggerTime,
                        timeUntilNext = timeUntilNext,
                        isToday = isToday,
                        isOverdue = isOverdue
                    )
                } else null
            }.sortedBy { it.nextTriggerTime }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
