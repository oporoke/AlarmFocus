package com.omondit.alarmfocus.domain.model

import android.content.Context
import android.util.Log
import com.omondit.alarmfocus.utils.QuoteManager
import org.json.JSONArray
import org.json.JSONObject

/**
 * Motivational Quote Typing Mission Implementation
 * Requires user to type a motivational quote with high accuracy
 */
class TypingMission(
    override val difficulty: Mission.Difficulty = Mission.Difficulty.EASY,
    override val config: MissionConfig = MissionConfig(),
    private val quoteManager: QuoteManager? = null
) : Mission() {

    override val type = MissionType.TYPING

    companion object {
        private const val TAG = "TypingMission"
        private const val REQUIRED_ACCURACY = 0.95f // 95%
        private const val MAX_ATTEMPTS = 5
    }

    override fun generateChallenge(): Challenge {
        val selectedQuote = quoteManager?.getRandomQuote(difficulty) ?: getDefaultQuote()

        return Challenge(
            id = generateChallengeId(),
            question = "Type this quote with 95% accuracy:",
            correctAnswer = selectedQuote.text,
            data = mapOf(
                "quoteText" to selectedQuote.text,
                "author" to selectedQuote.author,
                "category" to selectedQuote.category,
                "requiredAccuracy" to REQUIRED_ACCURACY.toString(),
                "wordCount" to selectedQuote.text.split(" ").size.toString()
            ),
            timeoutSeconds = calculateTimeLimit(selectedQuote.text, difficulty),
            allowedAttempts = MAX_ATTEMPTS
        )
    }

    override fun validateAnswer(challenge: Challenge, answer: String): ValidationResult {
        val originalText = challenge.correctAnswer
        val typedText = answer.trim()

        if (typedText.isEmpty()) {
            return ValidationResult(
                isCorrect = false,
                message = "Please type the quote to continue.",
                shouldEscalate = false
            )
        }

        val accuracy = calculateTypingAccuracy(originalText, typedText)
        val requiredAccuracy = (challenge.data["requiredAccuracy"] as? String)?.toFloatOrNull() ?: REQUIRED_ACCURACY

        return if (accuracy >= requiredAccuracy) {
            ValidationResult(
                isCorrect = true,
                message = "Perfect! ${(accuracy * 100).toInt()}% accuracy. You're motivated and ready!",
                shouldEscalate = false
            )
        } else {
            ValidationResult(
                isCorrect = false,
                message = "Only ${(accuracy * 100).toInt()}% accuracy. Need ${(requiredAccuracy * 100).toInt()}%. Try again!",
                shouldEscalate = true
            )
        }
    }

    private fun calculateTypingAccuracy(original: String, typed: String): Float {
        val originalWords = original.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val typedWords = typed.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }

        val maxLength = maxOf(originalWords.size, typedWords.size)
        if (maxLength == 0) return 0f

        var correctWords = 0
        for (i in 0 until maxLength) {
            val originalWord = originalWords.getOrNull(i) ?: ""
            val typedWord = typedWords.getOrNull(i) ?: ""

            if (originalWord == typedWord) {
                correctWords++
            }
        }

        return correctWords.toFloat() / maxLength
    }

    private fun calculateTimeLimit(text: String, difficulty: Difficulty): Int {
        val wordCount = text.split(" ").size
        val baseSecondsPerWord = when (difficulty) {
            Difficulty.EASY -> 4
            Difficulty.MEDIUM -> 3
            Difficulty.HARD -> 2
        }
        return (wordCount * baseSecondsPerWord).coerceAtLeast(30)
    }

    private fun getDefaultQuote(): MotivationalQuote {
        return MotivationalQuote(
            text = "Every morning is a new beginning, a chance to make today better than yesterday.",
            author = "Unknown",
            category = "Morning Motivation"
        )
    }

    private fun generateChallengeId(): String {
        return "typing_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}

/**
 * Represents a motivational quote
 */
data class MotivationalQuote(
    val id: String = generateQuoteId(),
    val text: String,
    val author: String = "Unknown",
    val category: String = "General",
    val difficulty: Mission.Difficulty = Mission.Difficulty.EASY,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val useCount: Int = 0
) {

    val wordCount: Int get() = text.split(" ").size

    fun toJson(): String {
        return JSONObject().apply {
            put("id", id)
            put("text", text)
            put("author", author)
            put("category", category)
            put("difficulty", difficulty.name)
            put("isCustom", isCustom)
            put("createdAt", createdAt)
            put("useCount", useCount)
        }.toString()
    }

    companion object {
        fun fromJson(json: String): MotivationalQuote? {
            return try {
                val obj = JSONObject(json)
                MotivationalQuote(
                    id = obj.getString("id"),
                    text = obj.getString("text"),
                    author = obj.optString("author", "Unknown"),
                    category = obj.optString("category", "General"),
                    difficulty = Mission.Difficulty.valueOf(obj.optString("difficulty", "EASY")),
                    isCustom = obj.optBoolean("isCustom", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    useCount = obj.optInt("useCount", 0)
                )
            } catch (e: Exception) {
                Log.e("MotivationalQuote", "Error parsing quote", e)
                null
            }
        }

        fun generateQuoteId(): String {
            return "quote_${System.currentTimeMillis()}_${(100..999).random()}"
        }
    }
}
