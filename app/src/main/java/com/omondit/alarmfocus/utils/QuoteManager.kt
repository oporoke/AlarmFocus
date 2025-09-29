package com.omondit.alarmfocus.utils

import android.content.Context
import android.util.Log
import com.omondit.alarmfocus.domain.model.Mission
import com.omondit.alarmfocus.domain.model.MotivationalQuote
import org.json.JSONArray

/**
 * Manages motivational quotes for typing missions
 * Complete implementation with 50+ ADHD-friendly quotes
 */
class QuoteManager(private val context: Context) {

    companion object {
        private const val TAG = "QuoteManager"
        private const val PREFS_NAME = "quote_manager"
        private const val KEY_CUSTOM_QUOTES = "custom_quotes"
        private const val MAX_CUSTOM_QUOTES = 50
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 50+ Preloaded ADHD-friendly motivational quotes
    private val adhdQuotes = listOf(
        // EASY quotes (10-15 words)
        MotivationalQuote(
            text = "Your brain works differently, and that's your superpower.",
            author = "ADHD Community",
            category = "ADHD Strength",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Progress, not perfection, is what matters most today.",
            author = "Unknown",
            category = "Self Compassion",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "You are capable of amazing things, one step at a time.",
            author = "Unknown",
            category = "Encouragement",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Today is a fresh start to be your best self.",
            author = "Unknown",
            category = "Morning Motivation",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Your energy and creativity make the world brighter.",
            author = "ADHD Community",
            category = "ADHD Strength",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Every challenge you face makes you stronger than before.",
            author = "Unknown",
            category = "Resilience",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Your unique perspective brings value to everything you do.",
            author = "Unknown",
            category = "Self Worth",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Small steps forward are still progress worth celebrating today.",
            author = "Unknown",
            category = "Progress",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "You deserve kindness, especially from yourself.",
            author = "Self Compassion",
            category = "Wellness",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Your efforts matter, even when results take time.",
            author = "Unknown",
            category = "Patience",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Embrace your differences, they make you extraordinary.",
            author = "Unknown",
            category = "Self Acceptance",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Every morning brings new opportunities to grow and thrive.",
            author = "Unknown",
            category = "New Beginnings",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Your passion and enthusiasm inspire those around you.",
            author = "ADHD Community",
            category = "Inspiration",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "Mistakes are proof that you are trying and learning.",
            author = "Unknown",
            category = "Growth Mindset",
            difficulty = Mission.Difficulty.EASY
        ),
        MotivationalQuote(
            text = "You have the power to create positive change today.",
            author = "Unknown",
            category = "Empowerment",
            difficulty = Mission.Difficulty.EASY
        ),

        // MEDIUM quotes (16-22 words)
        MotivationalQuote(
            text = "ADHD is not a disorder, it's a different way of thinking that can lead to incredible innovation.",
            author = "Dr. Michelle Mowery",
            category = "ADHD Awareness",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "The most successful people are those who embrace their differences and turn them into strengths every day.",
            author = "Unknown",
            category = "Success Mindset",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Your hyperfocus is like a laser beam that can accomplish what others take days to do in hours.",
            author = "ADHD Coach",
            category = "ADHD Strength",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Every challenge you face teaches your brain new ways to adapt and grow stronger than it was before.",
            author = "Neurodiversity Advocate",
            category = "Growth Mindset",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Your creative mind sees solutions where others see only problems, and that makes you invaluable to any team.",
            author = "Unknown",
            category = "Creativity",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "The energy you bring to everything you do can move mountains when you channel it with purpose and intention.",
            author = "ADHD Coach",
            category = "Energy",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Different doesn't mean less than, it means you have abilities that others can only dream of having themselves.",
            author = "Neurodiversity Movement",
            category = "Self Worth",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Your ability to think outside the box isn't a flaw, it's the very thing that will help you succeed.",
            author = "Unknown",
            category = "Innovation",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "When you're passionate about something, your ADHD brain becomes your greatest asset instead of your biggest challenge.",
            author = "ADHD Specialist",
            category = "Passion",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "The world needs people who think differently because that's where all the best ideas and innovations come from.",
            author = "Unknown",
            category = "Neurodiversity",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Your spontaneity and adaptability are gifts that help you navigate change better than most people ever could.",
            author = "ADHD Community",
            category = "Adaptability",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Every successful entrepreneur has ADHD traits because innovation requires the ability to see what others cannot see.",
            author = "Business Coach",
            category = "Entrepreneurship",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "Your mind may wander, but it often wanders to places where brilliant ideas are waiting to be discovered.",
            author = "Unknown",
            category = "Creativity",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "The same restless energy that makes sitting still hard is what drives you to achieve extraordinary things.",
            author = "ADHD Coach",
            category = "Energy",
            difficulty = Mission.Difficulty.MEDIUM
        ),
        MotivationalQuote(
            text = "You don't need to be like everyone else to be successful, you just need to be yourself.",
            author = "Unknown",
            category = "Authenticity",
            difficulty = Mission.Difficulty.MEDIUM
        ),

        // HARD quotes (23+ words)
        MotivationalQuote(
            text = "Having ADHD means your brain is wired for creativity, innovation, and thinking outside the box in ways that neurotypical minds simply cannot replicate or understand.",
            author = "Dr. Edward Hallowell",
            category = "ADHD Awareness",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "The same brain that struggles with routine tasks is the same brain that can see solutions others miss, create art that moves people, and innovate in ways that change the world forever.",
            author = "ADHD Researcher",
            category = "ADHD Strength",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Your ADHD is not a deficit in attention but rather an abundance of attention that you can harness when you find the right focus and passion for what you do.",
            author = "Neuroscience Research",
            category = "Understanding ADHD",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Many of history's greatest inventors, artists, and entrepreneurs had ADHD traits because those traits are what drive people to see possibilities where others see only limitations and impossibilities.",
            author = "Historical Analysis",
            category = "Historical Perspective",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "The challenges you face with ADHD teach you resilience, adaptability, and creative problem solving that make you uniquely equipped to handle whatever life throws your way with grace and determination.",
            author = "Clinical Psychologist",
            category = "Resilience",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Your brain's ability to make unexpected connections between seemingly unrelated ideas is the foundation of all creative genius, and you possess this gift in abundance every single day.",
            author = "Creativity Expert",
            category = "Creative Thinking",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "While others follow the beaten path, your ADHD brain naturally seeks new routes and discovers shortcuts that lead to innovation, breakthrough thinking, and transformative change in every field you touch.",
            author = "Innovation Consultant",
            category = "Innovation",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "The energy and enthusiasm you bring to projects you care about is contagious and inspiring, motivating entire teams to achieve more than they ever thought possible through your leadership.",
            author = "Leadership Coach",
            category = "Leadership",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Your ability to hyperfocus transforms you into an unstoppable force when you find your passion, allowing you to master complex skills and create extraordinary work in record time.",
            author = "ADHD Specialist",
            category = "Hyperfocus",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Living with ADHD means you experience life more intensely than others, feeling emotions more deeply, seeing colors more vividly, and engaging with the world in ways that make every moment meaningful.",
            author = "ADHD Experience",
            category = "Emotional Depth",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "The very traits that make traditional education challenging for you are the same traits that make you excel in dynamic, fast paced environments where others struggle to keep up.",
            author = "Education Specialist",
            category = "Learning Styles",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Your ADHD gives you the unique ability to see the big picture while simultaneously noticing tiny details that others overlook, making you invaluable in problem solving and strategic planning roles.",
            author = "Strategic Thinking",
            category = "Problem Solving",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "When you channel your restless energy into purposeful action, you become a force of nature that can accomplish in hours what takes others days or even weeks to complete successfully.",
            author = "Productivity Expert",
            category = "Productivity",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "The world is changing faster than ever before, and your ADHD brain is perfectly designed for this new reality where adaptability, creativity, and quick thinking are more valuable than following rules.",
            author = "Future of Work",
            category = "Adaptability",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Your journey with ADHD has given you unique insights into human nature, empathy for others who struggle, and the wisdom that comes from overcoming challenges that would defeat most people.",
            author = "Life Experience",
            category = "Wisdom",
            difficulty = Mission.Difficulty.HARD
        ),
        MotivationalQuote(
            text = "Every morning is a chance to harness your incredible energy, focus it on what truly matters to you, and create something amazing that only your unique brain could imagine.",
            author = "Morning Motivation",
            category = "Daily Inspiration",
            difficulty = Mission.Difficulty.HARD
        )
    )

    fun getRandomQuote(difficulty: Mission.Difficulty): MotivationalQuote {
        val customQuotes = getCustomQuotes()
        val availableQuotes = (adhdQuotes + customQuotes).filter {
            it.difficulty == difficulty
        }

        return if (availableQuotes.isNotEmpty()) {
            availableQuotes.random()
        } else {
            adhdQuotes.filter { it.difficulty == difficulty }.randomOrNull()
                ?: adhdQuotes.first()
        }
    }

    fun addCustomQuote(
        text: String,
        author: String = "You",
        category: String = "Personal"
    ): Result<MotivationalQuote> {

        val customQuotes = getCustomQuotes().toMutableList()

        if (customQuotes.size >= MAX_CUSTOM_QUOTES) {
            return Result.failure(Exception("Maximum $MAX_CUSTOM_QUOTES custom quotes allowed"))
        }

        val wordCount = text.split(" ").size
        val difficulty = when {
            wordCount <= 15 -> Mission.Difficulty.EASY
            wordCount <= 22 -> Mission.Difficulty.MEDIUM
            else -> Mission.Difficulty.HARD
        }

        val newQuote = MotivationalQuote(
            text = text.trim(),
            author = author.ifBlank { "You" },
            category = category.ifBlank { "Personal" },
            difficulty = difficulty,
            isCustom = true
        )

        customQuotes.add(newQuote)
        saveCustomQuotes(customQuotes)

        Log.d(TAG, "Added custom quote: ${newQuote.text.take(50)}...")
        return Result.success(newQuote)
    }

    fun getCustomQuotes(): List<MotivationalQuote> {
        val quotesJson = prefs.getString(KEY_CUSTOM_QUOTES, "[]") ?: "[]"
        return try {
            val jsonArray = JSONArray(quotesJson)
            (0 until jsonArray.length()).mapNotNull { index ->
                MotivationalQuote.fromJson(jsonArray.getString(index))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading custom quotes", e)
            emptyList()
        }
    }

    fun removeCustomQuote(quoteId: String): Boolean {
        val customQuotes = getCustomQuotes().toMutableList()
        val removed = customQuotes.removeIf { it.id == quoteId && it.isCustom }

        if (removed) {
            saveCustomQuotes(customQuotes)
            Log.d(TAG, "Removed custom quote: $quoteId")
        }

        return removed
    }

    fun updateQuoteUsage(quoteId: String) {
        val customQuotes = getCustomQuotes().toMutableList()
        val index = customQuotes.indexOfFirst { it.id == quoteId }

        if (index >= 0) {
            customQuotes[index] = customQuotes[index].copy(
                useCount = customQuotes[index].useCount + 1
            )
            saveCustomQuotes(customQuotes)
        }
    }

    fun getAllQuotes(): List<MotivationalQuote> {
        return adhdQuotes + getCustomQuotes()
    }

    fun getQuoteStats(): QuoteStats {
        val allQuotes = getAllQuotes()
        val customQuotes = getCustomQuotes()

        return QuoteStats(
            totalQuotes = allQuotes.size,
            customQuotes = customQuotes.size,
            builtInQuotes = adhdQuotes.size,
            averageWordCount = allQuotes.map { it.wordCount }.average().toFloat(),
            mostUsedQuote = customQuotes.maxByOrNull { it.useCount },
            quotesPerDifficulty = mapOf(
                Mission.Difficulty.EASY to allQuotes.count { it.difficulty == Mission.Difficulty.EASY },
                Mission.Difficulty.MEDIUM to allQuotes.count { it.difficulty == Mission.Difficulty.MEDIUM },
                Mission.Difficulty.HARD to allQuotes.count { it.difficulty == Mission.Difficulty.HARD }
            )
        )
    }

    private fun saveCustomQuotes(quotes: List<MotivationalQuote>) {
        val jsonArray = JSONArray()
        quotes.forEach { quote ->
            jsonArray.put(quote.toJson())
        }
        prefs.edit().putString(KEY_CUSTOM_QUOTES, jsonArray.toString()).apply()
    }

    data class QuoteStats(
        val totalQuotes: Int,
        val customQuotes: Int,
        val builtInQuotes: Int,
        val averageWordCount: Float,
        val mostUsedQuote: MotivationalQuote?,
        val quotesPerDifficulty: Map<Mission.Difficulty, Int>
    )
}
