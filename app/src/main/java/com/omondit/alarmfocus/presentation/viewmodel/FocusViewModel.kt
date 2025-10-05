package com.omondit.alarmfocus.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omondit.alarmfocus.data.database.entities.FocusSessionEntity
import com.omondit.alarmfocus.utils.FocusModeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for Focus Mode screen
 */
class FocusViewModel(
    private val focusModeManager: FocusModeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState: StateFlow<FocusUiState> = _uiState.asStateFlow()

    val allSessions: StateFlow<List<FocusSessionEntity>> = focusModeManager.getAllSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadActiveSession()
    }

    private fun loadActiveSession() {
        viewModelScope.launch {
            val activeSession = focusModeManager.getActiveSession()
            _uiState.value = _uiState.value.copy(
                activeSession = activeSession,
                isSessionActive = activeSession != null
            )
        }
    }

    fun startQuickSession(durationMinutes: Int) {
        viewModelScope.launch {
            try {
                val session = FocusSessionEntity(
                    name = "Quick Focus",
                    durationMinutes = durationMinutes,
                    intensity = when (durationMinutes) {
                        15 -> FocusSessionEntity.FocusIntensity.GENTLE
                        30 -> FocusSessionEntity.FocusIntensity.MODERATE
                        else -> FocusSessionEntity.FocusIntensity.STRICT
                    },
                    blockedCategories = when (durationMinutes) {
                        15 -> "[\"SOCIAL_MEDIA\"]"
                        30 -> "[\"SOCIAL_MEDIA\",\"GAMES\"]"
                        else -> "[\"SOCIAL_MEDIA\",\"GAMES\",\"ENTERTAINMENT\"]"
                    }
                )
                val sessionId = focusModeManager.createSession(session)
                focusModeManager.startFocusSession(sessionId)
                loadActiveSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start focus session: ${e.message}"
                )
            }
        }
    }

    fun stopFocusSession() {
        viewModelScope.launch {
            try {
                focusModeManager.stopFocusSession()
                loadActiveSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to stop focus session: ${e.message}"
                )
            }
        }
    }

    fun startTemplate(templateType: FocusSessionEntity.TemplateType) {
        viewModelScope.launch {
            try {
                val template = FocusSessionEntity.getDefaultTemplate(templateType)
                val sessionId = focusModeManager.createSession(template)
                focusModeManager.startFocusSession(sessionId)
                loadActiveSession()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start template: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class FocusUiState(
    val isSessionActive: Boolean = false,
    val activeSession: FocusSessionEntity? = null,
    val error: String? = null
)
