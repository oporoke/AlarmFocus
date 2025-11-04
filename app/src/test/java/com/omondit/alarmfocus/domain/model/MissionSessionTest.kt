package com.omondit.alarmfocus.domain.model

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MissionSessionTest {

    private lateinit var mathMission: MathMission
    private lateinit var missionSession: MissionSession

    @Before
    fun setup() {
        mathMission = MathMission(Mission.Difficulty.EASY)
        missionSession = MissionSession(
            alarmId = 1L,
            mission = mathMission
        )
    }

    @Test
    fun `generateNewChallenge should create a challenge`() {
        val challenge = missionSession.generateNewChallenge()

        assertNotNull(challenge)
        assertTrue(challenge.question.isNotEmpty())
        assertTrue(challenge.correctAnswer.isNotEmpty())
        assertEquals(60, challenge.timeoutSeconds)
    }

    @Test
    fun `escalation level should increase on wrong answer`() {
        val challenge = missionSession.generateNewChallenge()
        val initialEscalation = missionSession.escalationLevel

        assertEquals(0, initialEscalation)

        // Submit wrong answer
        val result = missionSession.submitAnswer("wrong")

        assertFalse(result.isCorrect)
        assertTrue(result.shouldEscalate)
        assertEquals(1, missionSession.escalationLevel)
    }

    @Test
    fun `difficulty should escalate with wrong answers`() {
        // Start with Easy difficulty
        val easyMission = MathMission(Mission.Difficulty.EASY)
        val session = MissionSession(1L, easyMission)

        // Generate challenge at level 0 (Easy)
        val challenge1 = session.generateNewChallenge()
        assertEquals(60, challenge1.timeoutSeconds)

        // Escalate to level 1
        session.escalationLevel = 1
        val challenge2 = session.generateNewChallenge()
        assertEquals(90, challenge2.timeoutSeconds) // Medium difficulty timeout

        // Escalate to level 3
        session.escalationLevel = 3
        val challenge3 = session.generateNewChallenge()
        assertEquals(120, challenge3.timeoutSeconds) // Hard difficulty timeout
    }

    @Test
    fun `attempts should be tracked correctly`() {
        val challenge = missionSession.generateNewChallenge()

        assertEquals(0, missionSession.attempts)

        missionSession.submitAnswer("wrong")
        assertEquals(1, missionSession.attempts)

        // Generate new challenge resets attempts
        missionSession.generateNewChallenge()
        assertEquals(0, missionSession.attempts)
    }

    @Test
    fun `correct answer should not escalate difficulty`() {
        val challenge = missionSession.generateNewChallenge()
        val correctAnswer = challenge.correctAnswer

        val result = missionSession.submitAnswer(correctAnswer)

        assertTrue(result.isCorrect)
        assertFalse(result.shouldEscalate)
        assertEquals(0, missionSession.escalationLevel)
    }

    @Test
    fun `isMaxAttemptsReached should return true when limit reached`() {
        val challenge = missionSession.generateNewChallenge()

        assertFalse(missionSession.isMaxAttemptsReached())

        // Submit wrong answers up to max attempts
        for (i in 1..challenge.allowedAttempts) {
            missionSession.submitAnswer("wrong")
        }

        assertTrue(missionSession.isMaxAttemptsReached())
    }

    @Test
    fun `getCompletionResult should calculate score correctly`() {
        missionSession.generateNewChallenge()
        missionSession.startTime = System.currentTimeMillis() - 5000 // 5 seconds ago

        val result = missionSession.getCompletionResult(success = true)

        assertTrue(result.success)
        assertTrue(result.score > 0f)
        assertTrue(result.score <= 1f)
        assertTrue(result.completionTime >= 5000)
    }

    @Test
    fun `failed mission should have zero score`() {
        missionSession.generateNewChallenge()

        val result = missionSession.getCompletionResult(success = false)

        assertFalse(result.success)
        assertEquals(0f, result.score, 0.01f)
    }
}
