package com.omondit.alarmfocus

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.omondit.alarmfocus.presentation.ADHDAlarmApp
import com.omondit.alarmfocus.presentation.theme.AlarmFocusTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.omondit.alarmfocus", appContext.packageName)
    }
}

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bottomNavigationWorksCorrectly() {
        composeTestRule.setContent {
            AlarmFocusTheme {
                ADHDAlarmApp()
            }
        }

        // Test navigation between tabs
        composeTestRule.onNodeWithContentDescription("Wake-up missions tab").performClick()
        composeTestRule.onNodeWithText("Missions").assertExists()

        composeTestRule.onNodeWithContentDescription("Settings tab").performClick()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }
}
