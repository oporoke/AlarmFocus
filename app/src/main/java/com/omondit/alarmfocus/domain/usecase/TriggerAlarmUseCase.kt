package com.omondit.alarmfocus.domain.usecase

import android.content.Context
import android.content.Intent
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.services.AlarmService

class TriggerAlarmUseCase(
    private val context: Context,
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke(alarmId: Long) {
        val alarm = alarmRepository.getAlarmById(alarmId)
        if (alarm != null && alarm.isEnabled) {
            // Mark as triggered in database
            alarmRepository.markAlarmTriggered(alarmId)

            // Start the alarm service
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                action = AlarmService.ACTION_START_ALARM
                putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
                putExtra(AlarmService.EXTRA_SOUND_URI, alarm.soundUri)
            }

            context.startForegroundService(serviceIntent)
        }
    }
}
