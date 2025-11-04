package com.omondit.alarmfocus.utils

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class EncryptionManagerExtTest {

    private lateinit var context: Context
    private lateinit var encryptionManager: EncryptionManager

    @Before
    fun setup() {
        context = mock(Context::class.java)
        encryptionManager = EncryptionManager(context)
    }

    @Test
    fun `saveAlarmServiceState should store all fields`() {
        val alarmId = 123L
        val soundUri = "content://media/1"
        val missionConfig = "{\"type\":\"MATH\"}"
        val missionActive = true

        encryptionManager.saveAlarmServiceState(alarmId, soundUri, missionConfig, missionActive)

        val state = encryptionManager.getAlarmServiceState()

        assertEquals(alarmId, state.activeAlarmId)
        assertEquals(soundUri, state.activeAlarmSound)
        assertEquals(missionConfig, state.missionConfig)
        assertEquals(missionActive, state.missionActive)
        assertTrue(state.alarmStartTime > 0)
    }

    @Test
    fun `getAlarmServiceState should return default values when empty`() {
        encryptionManager.clearAlarmServiceState()

        val state = encryptionManager.getAlarmServiceState()

        assertEquals(-1L, state.activeAlarmId)
        assertNull(state.activeAlarmSound)
        assertEquals("{}", state.missionConfig)
        assertFalse(state.missionActive)
    }

    @Test
    fun `updateMissionActiveStatus should update only mission active flag`() {
        encryptionManager.saveAlarmServiceState(123L, "uri", "{}", false)

        encryptionManager.updateMissionActiveStatus(true)

        val state = encryptionManager.getAlarmServiceState()
        assertEquals(123L, state.activeAlarmId)
        assertTrue(state.missionActive)
    }

    @Test
    fun `clearAlarmServiceState should remove all data`() {
        encryptionManager.saveAlarmServiceState(123L, "uri", "{}", true)

        encryptionManager.clearAlarmServiceState()

        val state = encryptionManager.getAlarmServiceState()
        assertEquals(-1L, state.activeAlarmId)
    }
}
