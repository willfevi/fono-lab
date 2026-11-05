package com.healthcare.fonolab.domain.game

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.goal.ClinicalArea

enum class GameMechanic {
    AUDITORY_DISCRIMINATION,
    RECEPTIVE_IDENTIFICATION,
    TEMPORAL_SEQUENCING,
    SPEECH_PRODUCTION_NAMING,
    PHONOLOGICAL_MANIPULATION
}

data class GameDefinition(
    val id: String,
    val title: String,
    val description: String,
    val clinicalArea: ClinicalArea,
    val targetSkills: List<String>,
    val indicativeAgeRange: String,
    val estimatedDurationMinutes: Int,
    val mechanic: GameMechanic,
    val isConfigurable: Boolean,
    val iconEmoji: String
)

interface GameConfiguration {
    val roundCount: Int
    val difficulty: DifficultyLevel
}

interface GameRound

interface GameAnswer

sealed interface RoundEvaluationResult {
    data class Correct(val feedbackMessage: String) : RoundEvaluationResult
    data class Incorrect(val canRetry: Boolean, val feedbackMessage: String) : RoundEvaluationResult
}

interface GameEngine<C : GameConfiguration, R : GameRound, A : GameAnswer> {
    suspend fun generateRounds(config: C): List<R>
    fun evaluateAnswer(round: R, answer: A, currentAttempt: Int): RoundEvaluationResult
}
