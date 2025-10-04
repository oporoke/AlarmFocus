package com.omondit.alarmfocus.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val durationMinutes: Int,
    val intensity: FocusIntensity,
    val blockedCategories: String, // JSON array of AppCategory
    val startTime: String? = null, // HH:mm format for scheduled sessions
    val repeatDays: String = "[]", // JSON array of day numbers (1-7)
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class FocusIntensity {
        GENTLE, // Block social media only
        MODERATE, // Block social media + games
        STRICT // Block everything except productivity
    }

    companion object {
        fun getDefaultTemplate(type: TemplateType): FocusSessionEntity {
            return when (type) {
                TemplateType.WORK -> FocusSessionEntity(
                    name = "Work Session",
                    durationMinutes = 60,
                    intensity = FocusIntensity.MODERATE,
                    blockedCategories = "[\"SOCIAL_MEDIA\",\"GAMES\",\"ENTERTAINMENT\"]"
                )
                TemplateType.STUDY -> FocusSessionEntity(
                    name = "Study Session",
                    durationMinutes = 45,
                    intensity = FocusIntensity.STRICT,
                    blockedCategories = "[\"SOCIAL_MEDIA\",\"GAMES\",\"ENTERTAINMENT\",\"COMMUNICATION\"]"
                )
                TemplateType.EXERCISE -> FocusSessionEntity(
                    name = "Exercise",
                    durationMinutes = 30,
                    intensity = FocusIntensity.GENTLE,
                    blockedCategories = "[\"SOCIAL_MEDIA\"]"
                )
            }
        }
    }

    enum class TemplateType {
        WORK, STUDY, EXERCISE
    }
}
