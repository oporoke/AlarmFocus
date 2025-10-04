package com.omondit.alarmfocus.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val usageTimeMillis: Long,
    val launchCount: Int,
    val lastUsedTimestamp: Long,
    val date: String, // YYYY-MM-DD format for daily aggregation
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class AppCategory {
        SOCIAL_MEDIA,
        PRODUCTIVITY,
        GAMES,
        COMMUNICATION,
        ENTERTAINMENT,
        UTILITIES,
        EDUCATION,
        OTHER
    }

    companion object {
        fun categorizeApp(packageName: String): AppCategory {
            return when {
                packageName.contains("instagram") ||
                    packageName.contains("facebook") ||
                    packageName.contains("twitter") ||
                    packageName.contains("tiktok") ||
                    packageName.contains("snapchat") ||
                    packageName.contains("reddit") -> AppCategory.SOCIAL_MEDIA

                packageName.contains("game") ||
                    packageName.contains("play.") && packageName.contains("game") -> AppCategory.GAMES

                packageName.contains("whatsapp") ||
                    packageName.contains("telegram") ||
                    packageName.contains("messenger") ||
                    packageName.contains("signal") -> AppCategory.COMMUNICATION

                packageName.contains("gmail") ||
                    packageName.contains("calendar") ||
                    packageName.contains("docs") ||
                    packageName.contains("office") ||
                    packageName.contains("notion") ||
                    packageName.contains("todoist") -> AppCategory.PRODUCTIVITY

                packageName.contains("youtube") ||
                    packageName.contains("netflix") ||
                    packageName.contains("spotify") ||
                    packageName.contains("prime") -> AppCategory.ENTERTAINMENT

                packageName.contains("duolingo") ||
                    packageName.contains("khan") ||
                    packageName.contains("coursera") -> AppCategory.EDUCATION

                else -> AppCategory.OTHER
            }
        }
    }
}
