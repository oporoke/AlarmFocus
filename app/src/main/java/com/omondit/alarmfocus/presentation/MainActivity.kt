package com.omondit.alarmfocus.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.omondit.alarmfocus.presentation.ui.navigation.ADHDBottomNavigation
import com.omondit.alarmfocus.presentation.ui.screens.AlarmsScreen
import com.omondit.alarmfocus.presentation.ui.screens.FocusScreen
import com.omondit.alarmfocus.presentation.ui.screens.MissionsScreen
import com.omondit.alarmfocus.presentation.ui.screens.SettingsScreen
import com.omondit.alarmfocus.presentation.theme.AlarmFocusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlarmFocusTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ADHDAlarmApp()
                }
            }
        }
    }
}



@Composable
fun ADHDAlarmApp() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: "alarms"

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
            composable("alarms") { AlarmsScreen() }
            composable("missions") { MissionsScreen() }
            composable("focus") { FocusScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}