package com.omondit.alarmfocus.domain.usecase

import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.Mission
import com.omondit.alarmfocus.domain.model.MissionConfig
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.utils.AlarmScheduler
import com.omondit.alarmfocus.utils.AlarmValidator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class CreateAlarmUseCaseTest {

    private lateinit var repository: AlarmRepository
    private lateinit var scheduler: AlarmScheduler
    private lateinit var validator: AlarmValidator
    private lateinit var useCase: CreateAlarmUseCase

    @Before
    fun setup() {
        repository = mock(AlarmRepository::class.java)
        scheduler = mock(AlarmScheduler::class.java)
        validator = mock(AlarmValidator::class.java)
        useCase = CreateAlarmUseCase(repository, scheduler, validator)
    }

    @Test
    fun `invoke should create alarm with valid params`() = runBlocking {
        val time = AlarmTime(10, 30)
        val label = "Test Alarm"
        val missionConfig = MissionConfig(Mission.MissionType.MATH, Mission.Difficulty.EASY)
        val repeatSchedule = RepeatSchedule.WEEKDAYS

        `when`(validator.validateAlarmTime(time)).thenReturn(AlarmValidator.ValidationResult.Valid)
        `when`(validator.canScheduleExactAlarms()).thenReturn(true)

        val result = useCase.invoke(time, label, missionConfig, repeatSchedule)

        assertTrue(result is CreateAlarmUseCase.Result.Success)
        verify(repository).createRepeatingAlarm(any(), any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `invoke should fail with invalid time`() = runBlocking {
        val time = AlarmTime(25, 70) // Invalid time
        val label = "Test Alarm"
        val missionConfig = MissionConfig()
        val repeatSchedule = RepeatSchedule.DAILY

        `when`(validator.validateAlarmTime(time))
            .thenReturn(AlarmValidator.ValidationResult.Invalid("Invalid time"))

        val result = useCase.invoke(time, label, missionConfig, repeatSchedule)

        assertTrue(result is CreateAlarmUseCase.Result.ValidationError)
        verify(repository, never()).createRepeatingAlarm(any(), any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `invoke should fail without exact alarm permission`() = runBlocking {
        val time = AlarmTime(10, 30)
        val label = "Test Alarm"
        val missionConfig = MissionConfig()
        val repeatSchedule = RepeatSchedule.DAILY

        `when`(validator.validateAlarmTime(time)).thenReturn(AlarmValidator.ValidationResult.Valid)
        `when`(validator.canScheduleExactAlarms()).thenReturn(false)

        val result = useCase.invoke(time, label, missionConfig, repeatSchedule)

        assertTrue(result is CreateAlarmUseCase.Result.PermissionError)
        verify(repository, never()).createRepeatingAlarm(any(), any(), any(), any(), any(), any(), any())
    }
}
