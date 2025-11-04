package com.omondit.alarmfocus.domain.model

import org.junit.Assert.*
import org.junit.Test

class MathMissionTest {

    @Test
    fun `easy difficulty should generate simple problems`() {
        val mission = MathMission(Mission.Difficulty.EASY)
        val challenge = mission.generateChallenge(0)

        assertNotNull(challenge)
        assertTrue(challenge.question.contains("+") || challenge.question.contains("-"))
        assertFalse(challenge.question.contains("*") || challenge.question.contains("/"))
    }

    @Test
    fun `medium difficulty should include multiplication`() {
        val mission = MathMission(Mission.Difficulty.MEDIUM)
        val challenge = mission.generateChallenge(0)

        assertNotNull(challenge)
        // Medium difficulty allows +, -, *
        val hasValidOperations = challenge.question.contains("+") ||
                challenge.question.contains("-") ||
                challenge.question.contains("*")
        assertTrue(hasValidOperations)
    }

    @Test
    fun `hard difficulty should include division`() {
        val mission = MathMission(Mission.Difficulty.HARD)
        val challenges = (1..10).map { mission.generateChallenge(0) }

        // At least one challenge should have division (statistically)
        val hasAnyDivision = challenges.any { it.question.contains("/") }
        // Note: This might occasionally fail due to randomness, but very unlikely
        assertTrue("Hard difficulty should eventually generate division problems", hasAnyDivision)
    }

    @Test
    fun `correct answer should be validated`() {
        val mission = MathMission(Mission.Difficulty.EASY)
        val challenge = mission.generateChallenge(0)

        val result = mission.validateAnswer(challenge, challenge.correctAnswer)

        assertTrue(result.isCorrect)
        assertFalse(result.shouldEscalate)
    }

    @Test
    fun `wrong answer should trigger escalation`() {
        val mission = MathMission(Mission.Difficulty.EASY)
        val challenge = mission.generateChallenge(0)

        val result = mission.validateAnswer(challenge, "999999")

        assertFalse(result.isCorrect)
        assertTrue(result.shouldEscalate)
    }

    @Test
    fun `escalation should increase difficulty`() {
        val mission = MathMission(Mission.Difficulty.EASY)

        val challenge0 = mission.generateChallenge(0)
        assertEquals(60, challenge0.timeoutSeconds) // Easy timeout

        val challenge1 = mission.generateChallenge(1)
        assertEquals(90, challenge1.timeoutSeconds) // Medium timeout

        val challenge3 = mission.generateChallenge(3)
        assertEquals(120, challenge3.timeoutSeconds) // Hard timeout
    }

    @Test
    fun `challenge should have valid metadata`() {
        val mission = MathMission(Mission.Difficulty.MEDIUM)
        val challenge = mission.generateChallenge(0)

        assertTrue(challenge.id.startsWith("math_"))
        assertEquals(3, challenge.allowedAttempts)
        assertTrue(challenge.data.containsKey("num1"))
        assertTrue(challenge.data.containsKey("num2"))
        assertTrue(challenge.data.containsKey("operation"))
    }

    @Test
    fun `answer validation should trim whitespace`() {
        val mission = MathMission(Mission.Difficulty.EASY)
        val challenge = mission.generateChallenge(0)

        val resultWithSpaces = mission.validateAnswer(challenge, "  ${challenge.correctAnswer}  ")

        assertTrue(resultWithSpaces.isCorrect)
    }

    @Test
    fun `escalation level should be stored in challenge data`() {
        val mission = MathMission(Mission.Difficulty.EASY)
        val escalationLevel = 2

        val challenge = mission.generateChallenge(escalationLevel)

        assertEquals(escalationLevel, challenge.data["escalation_level"])
        assertEquals("MEDIUM", challenge.data["effective_difficulty"])
    }
}
