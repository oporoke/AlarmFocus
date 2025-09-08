package com.omondit.alarmfocus.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.model.AlarmTime
import com.omondit.alarmfocus.domain.model.RepeatSchedule
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.domain.usecase.CreateAlarmUseCase
import com.omondit.alarmfocus.domain.usecase.DeleteAlarmUseCase
import com.omondit.alarmfocus.domain.usecase.GetUpcomingAlarmsUseCase
import com.omondit.alarmfocus.domain.usecase.ToggleAlarmUseCase
import com.omondit.alarmfocus.utils.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val toggleAlarmUseCase: ToggleAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val getUpcomingAlarmsUseCase: GetUpcomingAlarmsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    // All alarms for management
    val allAlarms: StateFlow<List<AlarmEntity>> = repository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Upcoming alarms with timing information
    val upcomingAlarms = getUpcomingAlarmsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Quick stats
    val alarmStats: StateFlow<AlarmStats> = allAlarms
        .combine(upcomingAlarms) { all, upcoming ->
            AlarmStats(
                totalAlarms = all.size,
                enabledAlarms = all.count { it.isEnabled },
                nextAlarmTime = upcoming.firstOrNull()?.nextTriggerTime,
                overallSuccessRate = all.mapNotNull {
                    if (it.snoozeCount + it.successfulWakeups > 0) it.getSuccessRate() else null
                }.let { rates ->
                    if (rates.isNotEmpty()) rates.average().toFloat() else null
                }
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AlarmStats()
        )

    init {
        // Load initial state
        refreshSystemPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createAlarm(
        hour: Int,
        minute: Int,
        label: String,
        schedule: RepeatSchedule = RepeatSchedule.once(),
        soundUri: String? = null,
        vibrationEnabled: Boolean = true
    ) {
        viewModelScope.launch {
            updateState { it.copy(isLoading = true) }

            try {
                val time = AlarmTime(hour, minute)
                val result = createAlarmUseCase(
                    time = time,
                    label = label,
                    schedule = schedule,
                    soundUri = soundUri,
                    vibrationEnabled = vibrationEnabled
                )

                result.fold(
                    onSuccess = { alarmId ->
                        updateState { it.copy(isLoading = false) }
                        sendEvent(UiEvent.ShowSnackbar("Alarm created successfully"))
                        sendEvent(UiEvent.AlarmCreated(alarmId))
                    },
                    onFailure = { error ->
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to create alarm"
                            )
                        }
                    }
                )

            } catch (e: Exception) {
                updateState {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun toggleAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val result = toggleAlarmUseCase(alarmId)

                result.fold(
                    onSuccess = { enabled ->
                        val message = if (enabled) "Alarm enabled" else "Alarm disabled"
                        sendEvent(UiEvent.ShowSnackbar(message))
                    },
                    onFailure = { error ->
                        updateState {
                            it.copy(error = error.message ?: "Failed to toggle alarm")
                        }
                    }
                )

            } catch (e: Exception) {
                updateState {
                    it.copy(error = e.message ?: "Failed to toggle alarm")
                }
            }
        }
    }

    fun deleteAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val result = deleteAlarmUseCase(alarmId)

                result.fold(
                    onSuccess = {
                        sendEvent(UiEvent.ShowSnackbar("Alarm deleted"))
                    },
                    onFailure = { error ->
                        updateState {
                            it.copy(error = error.message ?: "Failed to delete alarm")
                        }
                    }
                )

            } catch (e: Exception) {
                updateState {
                    it.copy(error = e.message ?: "Failed to delete alarm")
                }
            }
        }
    }

    fun skipNextAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                repository.setSkipNextAlarm(alarmId, true)
                sendEvent(UiEvent.ShowSnackbar("Next alarm occurrence skipped"))

            } catch (e: Exception) {
                updateState {
                    it.copy(error = e.message ?: "Failed to skip alarm")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun duplicateAlarm(alarmId: Long) {
        viewModelScope.launch {
            try {
                val originalAlarm = repository.getAlarmById(alarmId)
                if (originalAlarm != null) {
                    val time = originalAlarm.toAlarmTime()
                    val schedule = originalAlarm.getRepeatSchedule()

                    createAlarm(
                        hour = time.hour,
                        minute = time.minute,
                        label = "${originalAlarm.label} (Copy)",
                        schedule = schedule,
                        soundUri = originalAlarm.soundUri,
                        vibrationEnabled = originalAlarm.vibrationEnabled
                    )
                }

            } catch (e: Exception) {
                updateState {
                    it.copy(error = e.message ?: "Failed to duplicate alarm")
                }
            }
        }
    }

    fun refreshSystemPermissions() {
        viewModelScope.launch {
            // This would integrate with PermissionManager
            // For now, just update the UI state
            updateState { it.copy(systemReady = true) }
        }
    }

    fun dismissError() {
        updateState { it.copy(error = null) }
    }

    fun dismissMessage() {
        // Events are one-time, no state to clear
    }

    private fun updateState(update: (AlarmUiState) -> AlarmUiState) {
        _uiState.value = update(_uiState.value)
    }

    private fun sendEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }
}

// UI State and Events
data class AlarmUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val systemReady: Boolean = false,
    val selectedAlarmId: Long? = null
)

data class AlarmStats(
    val totalAlarms: Int = 0,
    val enabledAlarms: Int = 0,
    val nextAlarmTime: Long? = null,
    val overallSuccessRate: Float? = null
)

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class AlarmCreated(val alarmId: Long) : UiEvent()
    data class Navigate(val route: String) : UiEvent()
    object NavigateBack : UiEvent()
}
