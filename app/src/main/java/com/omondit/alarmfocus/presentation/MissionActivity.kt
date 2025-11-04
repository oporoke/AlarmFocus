package com.omondit.alarmfocus.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.omondit.alarmfocus.AlarmFocusApplication
import com.omondit.alarmfocus.domain.model.MissionConfig
import com.omondit.alarmfocus.presentation.theme.AlarmFocusTheme
import com.omondit.alarmfocus.presentation.ui.screens.IntegratedMissionScreen
import com.omondit.alarmfocus.services.AlarmService
import com.omondit.alarmfocus.utils.MissionManager
import kotlinx.coroutines.launch

class MissionActivity : ComponentActivity() {

    private lateinit var missionManager: MissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val appModule = (application as AlarmFocusApplication).appModule
        missionManager = appModule.missionManager

        val alarmId = intent.getLongExtra("alarm_id", -1L)
        val missionConfigJson = intent.getStringExtra("mission_config") ?: "{}"

        if (alarmId == -1L) {
            finish()
            return
        }

        val missionConfig = MissionConfig.fromJson(missionConfigJson)

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
                                val completeIntent = Intent(this@MissionActivity, AlarmService::class.java).apply {
                                    action = AlarmService.ACTION_MISSION_COMPLETED
                                    putExtra(AlarmService.EXTRA_ALARM_ID, alarmId)
                                }
                                startService(completeIntent)
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
        super.onBackPressed()
        Toast.makeText(this, "Complete the challenge to dismiss alarm", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        val appModule = (application as AlarmFocusApplication).appModule
        val savedState = appModule.encryptionManager.getAlarmServiceState()

        if (savedState.missionActive && !isFinishing) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing) {
                    val intent = Intent(this, MissionActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("alarm_id", intent.getLongExtra("alarm_id", -1L))
                        putExtra("mission_config", intent.getStringExtra("mission_config"))
                    }
                    startActivity(intent)
                }
            }, 100)
        }
    }
}
