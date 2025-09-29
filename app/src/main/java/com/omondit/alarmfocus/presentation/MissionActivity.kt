package com.omondit.alarmfocus.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.omondit.alarmfocus.data.database.AppDatabase
import com.omondit.alarmfocus.data.repository.AlarmRepositoryImpl
import com.omondit.alarmfocus.domain.model.MissionConfig
import com.omondit.alarmfocus.presentation.theme.AlarmFocusTheme
import com.omondit.alarmfocus.presentation.ui.screens.IntegratedMissionScreen
import com.omondit.alarmfocus.utils.MissionManager
import kotlinx.coroutines.launch

/**
 * Full-screen activity that hosts mission challenges
 * Designed to be impossible to dismiss accidentally
 */
class MissionActivity : ComponentActivity() {

    private lateinit var missionManager: MissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make this activity show over lock screen and keep screen on
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Initialize dependencies
        val database = AppDatabase.getDatabase(this)
        val repository = AlarmRepositoryImpl(database.alarmDao())
        missionManager = MissionManager(this, repository)

        val alarmId = intent.getLongExtra("alarm_id", -1L)
        val missionConfigJson = intent.getStringExtra("mission_config") ?: "{}"
        val missionConfig = MissionConfig.fromJson(missionConfigJson)

        if (alarmId == -1L) {
            finish()
            return
        }

        setContent {
            AlarmFocusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    IntegratedMissionScreen(
                        alarmId = alarmId,
                        missionConfig = missionConfig,
                        onMissionCompleted = { result ->
                            lifecycleScope.launch {
                                missionManager.completeMission(alarmId, result)
                                finish()
                            }
                        },
                        onMissionFailed = {
                            lifecycleScope.launch {
                                missionManager.failMission(alarmId, "User dismissed")
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent back button from dismissing mission
        // Users must complete the mission or use emergency dismiss
    }

    override fun onPause() {
        super.onPause()
        // Bring activity back to front if user tries to leave
        if (!isFinishing) {
            val intent = intent
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
    }
}
