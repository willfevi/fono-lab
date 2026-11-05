package com.healthcare.fonolab.domain.performance

import java.time.Instant

/** How the attempt was evaluated. Distinct types never mixed. */
sealed interface AttemptEvaluation {
    data class AutoScored(val isCorrect: Boolean) : AttemptEvaluation
    data class ClinicianRated(val rating: ClinicianRating) : AttemptEvaluation
    data object NotEvaluated : AttemptEvaluation
}

enum class ClinicianRating {
    CORRECT_INDEPENDENT,
    CORRECT_WITH_CUE,
    INCORRECT,
    NO_RESPONSE,
    NEEDS_REVIEW
}

enum class CueLevel {
    NONE,
    VERBAL,
    VISUAL,
    GESTURAL,
    TACTILE,
    MODELING
}

data class GameAttempt(
    val id: Long,
    val activityId: Long,
    val roundIndex: Int,
    val attemptNumber: Int,
    val stimulusTarget: String,
    val stimulusPresentedJson: String,
    val responseRecorded: String,
    val evaluation: AttemptEvaluation,
    val cueLevel: CueLevel,
    val responseTimeMs: Long?,
    val clinicianNotes: String,
    val timestamp: Instant
)

data class ObjectivePerformanceSummary(
    val totalAttempts: Int,
    val autoScoredCount: Int,
    val autoScoredCorrect: Int,
    val autoAccuracyPercentage: Float?,
    val clinicianRatedCount: Int,
    val independentCorrectCount: Int,
    val cuedCorrectCount: Int,
    val incorrectCount: Int,
    val noResponseCount: Int
)

object PerformanceCalculator {
    fun calculate(attempts: List<GameAttempt>): ObjectivePerformanceSummary {
        var autoTotal = 0
        var autoCorrect = 0
        var clinicianTotal = 0
        var indCorrect = 0
        var cuedCorrect = 0
        var incorrect = 0
        var noResp = 0

        for (a in attempts) {
            when (val ev = a.evaluation) {
                is AttemptEvaluation.AutoScored -> {
                    autoTotal++
                    if (ev.isCorrect) autoCorrect++
                }
                is AttemptEvaluation.ClinicianRated -> {
                    clinicianTotal++
                    when (ev.rating) {
                        ClinicianRating.CORRECT_INDEPENDENT -> indCorrect++
                        ClinicianRating.CORRECT_WITH_CUE -> cuedCorrect++
                        ClinicianRating.INCORRECT -> incorrect++
                        ClinicianRating.NO_RESPONSE -> noResp++
                        ClinicianRating.NEEDS_REVIEW -> {}
                    }
                }
                AttemptEvaluation.NotEvaluated -> {}
            }
        }

        val accuracy = if (autoTotal > 0) (autoCorrect.toFloat() / autoTotal) * 100f else null

        return ObjectivePerformanceSummary(
            totalAttempts = attempts.size,
            autoScoredCount = autoTotal,
            autoScoredCorrect = autoCorrect,
            autoAccuracyPercentage = accuracy,
            clinicianRatedCount = clinicianTotal,
            independentCorrectCount = indCorrect,
            cuedCorrectCount = cuedCorrect,
            incorrectCount = incorrect,
            noResponseCount = noResp
        )
    }
}
