package com.omondit.alarmfocus.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import com.omondit.alarmfocus.di.AppModule
import com.omondit.alarmfocus.domain.usecase.CreateAlarmUseCase
import com.omondit.alarmfocus.domain.usecase.DeleteAlarmUseCase
import com.omondit.alarmfocus.domain.usecase.GetUpcomingAlarmsUseCase
import com.omondit.alarmfocus.domain.usecase.ToggleAlarmUseCase
import com.omondit.alarmfocus.presentation.theme.AlarmFocusTheme
import com.omondit.alarmfocus.presentation.ui.navigation.ADHDBottomNavigation
import com.omondit.alarmfocus.presentation.ui.screens.AlarmsScreen
import com.omondit.alarmfocus.presentation.ui.screens.FocusScreen
import com.omondit.alarmfocus.presentation.ui.screens.MissionsScreen
import com.omondit.alarmfocus.presentation.ui.screens.SettingsScreen
import com.omondit.alarmfocus.presentation.viewmodel.AlarmViewModel
import com.omondit.alarmfocus.utils.AlarmScheduler
import com.omondit.alarmfocus.utils.AlarmValidator
import com.omondit.alarmfocus.utils.PermissionManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appModule: AppModule
    private lateinit var permissionManager: PermissionManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        Log.d("MainActivity", "onCreate called")

        // Initialize permission manager
        permissionManager = PermissionManager(this)
        appModule = AppModule(this)

        // Request permissions if needed
        checkAndRequestPermissions()

        // Check if we should redirect to mission
        // ONLY if this is NOT a fresh launch from notification
        val shouldCheckMission = intent?.action != Intent.ACTION_MAIN ||
            intent?.categories?.contains(Intent.CATEGORY_LAUNCHER) == true

        setContent {
            AlarmFocusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ADHDAlarmApp(this)
                }
            }
        }

        // Only check for active alarm if this is a normal app launch
        // NOT when coming from notification click
        if (shouldCheckMission) {
            checkAndRestoreActiveAlarm()
        }
    }

    private fun checkAndRestoreActiveAlarm() {
        val prefs = getSharedPreferences("alarm_service_state", Context.MODE_PRIVATE)
        val activeAlarmId = prefs.getLong("active_alarm_id", -1L)
        val missionActive = prefs.getBoolean("mission_active", false)

        Log.d("MainActivity", "Checking active alarm: id=$activeAlarmId, missionActive=$missionActive")

        // ONLY redirect if there's an active mission and this is a fresh app launch
        if (activeAlarmId != -1L && missionActive) {
            Log.d("MainActivity", "Active mission detected, launching MissionActivity")

            lifecycleScope.launch {
                try {
                    val alarm = appModule.alarmRepository.getAlarmById(activeAlarmId)
                    val missionConfig = alarm?.missionConfig ?: prefs.getString("mission_config", "{}")

                    val missionIntent = Intent(this@MainActivity, MissionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra("alarm_id", activeAlarmId)
                        putExtra("mission_config", missionConfig)
                    }
                    startActivity(missionIntent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error launching mission", e)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkAndRequestPermissions() {
        if (!permissionManager.areAllPermissionsGranted()) {
            permissionManager.requestAllPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        checkAndRequestPermissions()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ADHDAlarmApp(context: Context) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "alarms"

    val database = AppDatabase.getDatabase(context)
    val repository = AlarmRepositoryImpl(database.alarmDao())
    val scheduler = AlarmScheduler(context)
    val validator = AlarmValidator(context)
    val createAlarmUseCase = CreateAlarmUseCase(repository, scheduler, validator)
    val toggleAlarmUseCase = ToggleAlarmUseCase(repository, scheduler)
    val deleteAlarmUseCase = DeleteAlarmUseCase(repository, scheduler)
    val getUpcomingAlarmUseCase = GetUpcomingAlarmsUseCase(repository)

    val viewModel = AlarmViewModel(
        repository, createAlarmUseCase, toggleAlarmUseCase,
        deleteAlarmUseCase, getUpcomingAlarmUseCase
    )

    Scaffold(
        bottomBar = {
            ADHDBottomNavigation(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "alarms",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("alarms") {
                AlarmsScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = {},
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("missions") { MissionsScreen() }
            composable("focus") { FocusScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
