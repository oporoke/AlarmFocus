package com.omondit.alarmfocus.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_apps")
data class BlockedAppEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val isBlocked: Boolean = true,
    val blockType: BlockType = BlockType.MANUAL,
    val blockUntil: Long? = null, // Timestamp when blocking ends, null = permanent
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class BlockType {
        POST_ALARM, // Blocked for 1 hour after alarm dismissal
        MANUAL, // User manually blocked
        FOCUS_SESSION // Blocked during focus session
    }

    companion object {
        val DEFAULT_SOCIAL_MEDIA_APPS = listOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.zhiliaoapp.musically", // TikTok
            "com.snapchat.android",
            "com.reddit.frontpage",
            "com.linkedin.android",
            "com.pinterest"
        )

        fun isDefaultSocialMedia(packageName: String): Boolean {
            return DEFAULT_SOCIAL_MEDIA_APPS.contains(packageName)
        }
    }
}
