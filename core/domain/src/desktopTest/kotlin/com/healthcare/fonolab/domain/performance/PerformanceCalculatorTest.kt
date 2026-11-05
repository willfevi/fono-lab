package com.healthcare.fonolab.domain.performance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class PerformanceCalculatorTest {

    @Test
    fun `empty attempts produces null accuracy`() {
        val summary = PerformanceCalculator.calculate(emptyList())
        assertEquals(0, summary.totalAttempts)
        assertEquals(0, summary.autoScoredCount)
        assertNull(summary.autoAccuracyPercentage)
        assertEquals(0, summary.clinicianRatedCount)
    }

    @Test
    fun `auto scored attempts calculate exact percentage and preserves clinician metrics separation`() {
        val attempts = listOf(
            GameAttempt(
                id = 1,
                activityId = 1,
                roundIndex = 0,
                attemptNumber = 1,
                stimulusTarget = "Sapo",
                stimulusPresentedJson = "{}",
                responseRecorded = "Sapo",
                evaluation = AttemptEvaluation.AutoScored(true),
                cueLevel = CueLevel.NONE,
                responseTimeMs = 1200,
                clinicianNotes = "",
                timestamp = Instant.now()
            ),
            GameAttempt(
                id = 2,
                activityId = 1,
                roundIndex = 1,
                attemptNumber = 1,
                stimulusTarget = "Sol",
                stimulusPresentedJson = "{}",
                responseRecorded = "Lua",
                evaluation = AttemptEvaluation.AutoScored(false),
                cueLevel = CueLevel.NONE,
                responseTimeMs = 2100,
                clinicianNotes = "",
                timestamp = Instant.now()
            ),
            GameAttempt(
                id = 3,
                activityId = 1,
                roundIndex = 2,
                attemptNumber = 1,
                stimulusTarget = "Sino",
                stimulusPresentedJson = "{}",
                responseRecorded = "CORRECT_INDEPENDENT",
                evaluation = AttemptEvaluation.ClinicianRated(ClinicianRating.CORRECT_INDEPENDENT),
                cueLevel = CueLevel.NONE,
                responseTimeMs = null,
                clinicianNotes = "Boa articulação",
                timestamp = Instant.now()
            )
        )

        val summary = PerformanceCalculator.calculate(attempts)
        assertEquals(3, summary.totalAttempts)
        assertEquals(2, summary.autoScoredCount)
        assertEquals(1, summary.autoScoredCorrect)
        assertEquals(50.0f, summary.autoAccuracyPercentage)

        assertEquals(1, summary.clinicianRatedCount)
        assertEquals(1, summary.independentCorrectCount)
        assertEquals(0, summary.cuedCorrectCount)
    }
}
