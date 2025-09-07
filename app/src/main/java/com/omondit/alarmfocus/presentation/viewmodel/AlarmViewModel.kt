package com.omondit.alarmfocus.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omondit.alarmfocus.data.database.entities.AlarmEntity
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.utils.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState = _uiState.asStateFlow()

    // Observe all alarms from database
    val alarms: StateFlow<List<AlarmEntity>> = alarmRepository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun createAlarm(
        hour: Int,
        minute: Int,
        label: String,
        soundUri: String? = null
    ) {
        viewModelScope.launch {
            try {
                val alarm = AlarmEntity(
                    hour = hour,
                    minute = minute,
                    label = label,
                    soundUri = soundUri,
                    isEnabled = true
                )

                val alarmId = alarmRepository.insertAlarm(alarm)
                val createdAlarm = alarm.copy(id = alarmId)

                // Schedule with system AlarmManager
                alarmScheduler.scheduleAlarm(createdAlarm)

                _uiState.value = _uiState.value.copy(
                    message = "Alarm created successfully",
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create alarm: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            try {
                val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
                alarmRepository.updateAlarm(updatedAlarm)

                if (updatedAlarm.isEnabled) {
                    alarmScheduler.scheduleAlarm(updatedAlarm)
                } else {
                    alarmScheduler.cancelAlarm(updatedAlarm.id)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to toggle alarm: ${e.message}"
                )
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            try {
                alarmScheduler.cancelAlarm(alarm.id)
                alarmRepository.deleteAlarm(alarm)

                _uiState.value = _uiState.value.copy(
                    message = "Alarm deleted"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete alarm: ${e.message}"
                )
            }
        }
    }

    fun dismissMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }
}

data class AlarmUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)
