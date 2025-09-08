package com.omondit.alarmfocus.presentation

import android.content.Context
import android.os.Build
import android.os.Bundle
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

class MainActivity : ComponentActivity() {

    private lateinit var appModule: AppModule
    private lateinit var permissionManager: PermissionManager

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize permission manager
        permissionManager = PermissionManager(this)
        appModule = AppModule(this)

        // Request permissions if needed
        checkAndRequestPermissions()


        setContent {
            AlarmFocusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ADHDAlarmApp(this)
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
        // Check permissions again in case user granted them in settings
        checkAndRequestPermissions()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun ADHDAlarmApp(context: Context) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "alarms"
    // Initialize dependencies
    val database = AppDatabase.getDatabase(context)
    val repository = AlarmRepositoryImpl(database.alarmDao())
    val scheduler = AlarmScheduler(context)
    val validator = AlarmValidator(context)
    val createAlarmUseCase = CreateAlarmUseCase(repository,scheduler,validator)
    val toggleAlarmUseCase = ToggleAlarmUseCase(repository,scheduler)
    val deleteAlarmUseCase = DeleteAlarmUseCase(repository,scheduler)
    val getUpcomingAlarmUseCase = GetUpcomingAlarmsUseCase(repository)
    // Create ViewModel with dependencies
    val viewModel = AlarmViewModel(
        repository, createAlarmUseCase,toggleAlarmUseCase,
        deleteAlarmUseCase,getUpcomingAlarmUseCase
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
                    onNavigateToCreate = {
                        // TODO: Navigate to detailed create screen
                        // For now, the dialog in AlarmsScreen handles this
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable("missions") { MissionsScreen() }
            composable("focus") { FocusScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
