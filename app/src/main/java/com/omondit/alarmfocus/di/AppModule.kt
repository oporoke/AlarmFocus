package com.omondit.alarmfocus.di

import android.content.Context
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import com.omondit.alarmfocus.domain.repository.AlarmRepository
import com.omondit.alarmfocus.domain.usecase.CreateAlarmUseCase
import com.omondit.alarmfocus.domain.usecase.DeleteAlarmUseCase
import com.omondit.alarmfocus.domain.usecase.GetUpcomingAlarmsUseCase
import com.omondit.alarmfocus.domain.usecase.ToggleAlarmUseCase
import com.omondit.alarmfocus.presentation.viewmodel.AlarmViewModel
import com.omondit.alarmfocus.utils.AlarmScheduler
import com.omondit.alarmfocus.utils.AlarmValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Manual dependency injection for the alarm system
 * To use Hilt or similar DI framework
 */
class AppModule(private val context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Database
    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    // Repository
    val alarmRepository: AlarmRepository by lazy {
        AlarmRepositoryImpl(database.alarmDao())
    }

    // Utilities
    val alarmScheduler: AlarmScheduler by lazy {
        AlarmScheduler(context, applicationScope)
    }

    val alarmValidator: AlarmValidator by lazy {
        AlarmValidator(context)
    }

    // Use Cases
    val createAlarmUseCase: CreateAlarmUseCase by lazy {
        CreateAlarmUseCase(alarmRepository, alarmScheduler, alarmValidator)
    }

    val toggleAlarmUseCase: ToggleAlarmUseCase by lazy {
        ToggleAlarmUseCase(alarmRepository, alarmScheduler)
    }

    val deleteAlarmUseCase: DeleteAlarmUseCase by lazy {
        DeleteAlarmUseCase(alarmRepository, alarmScheduler)
    }

    val getUpcomingAlarmsUseCase: GetUpcomingAlarmsUseCase by lazy {
        GetUpcomingAlarmsUseCase(alarmRepository)
    }

    // ViewModel Factory
    fun createAlarmViewModel(): AlarmViewModel {
        return AlarmViewModel(
            repository = alarmRepository,
            createAlarmUseCase = createAlarmUseCase,
            toggleAlarmUseCase = toggleAlarmUseCase,
            deleteAlarmUseCase = deleteAlarmUseCase,
            getUpcomingAlarmsUseCase = getUpcomingAlarmsUseCase
        )
    }
}
