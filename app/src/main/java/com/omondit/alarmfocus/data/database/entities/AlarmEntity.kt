package com.omondit.alarmfocus.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "Alarm",
    val isEnabled: Boolean = true,
    val repeatDays: String = "[]", // JSON array: ["MON","TUE","WED"]
    val soundUri: String? = null, // null = default ultra-loud sound
    val missionType: String = "NONE", // For future missions
    val missionConfig: String = "{}", // JSON config for missions
    val createdAt: Long = System.currentTimeMillis(),
    val lastTriggered: Long? = null,
    val isActive: Boolean = false // Currently ringing
)