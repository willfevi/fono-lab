package com.healthcare.fonolab.feature.games.hungrymonster

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.game.GameAnswer
import com.healthcare.fonolab.domain.game.GameConfiguration
import com.healthcare.fonolab.domain.game.GameEngine
import com.healthcare.fonolab.domain.game.GameRound
import com.healthcare.fonolab.domain.game.RoundEvaluationResult
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import kotlinx.serialization.Serializable

@Serializable
data class HungryMonsterConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val taskType: String = "SPONTANEOUS_NAMING" // or REPETITION
) : GameConfiguration

data class MonsterStimulus(
    val word: String,
    val emoji: String,
    val prompt: String
)

data class HungryMonsterRound(
    val stimulus: MonsterStimulus,
    val monsterMood: String = "FAMINTO"
) : GameRound

data class HungryMonsterAnswer(
    val clinicianRating: ClinicianRating,
    val cueLevel: CueLevel,
    val observation: String = ""
) : GameAnswer

class HungryMonsterEngine : GameEngine<HungryMonsterConfig, HungryMonsterRound, HungryMonsterAnswer> {

    private val foodsAndItems = listOf(
        MonsterStimulus("Maçã", "🍎", "O Monstrinho quer comer uma..."),
        MonsterStimulus("Banana", "🍌", "Agora ele quer uma..."),
        MonsterStimulus("Cenoura", "🥕", "Que delícia de..."),
        MonsterStimulus("Morango", "🍓", "Ele adora um..."),
        MonsterStimulus("Pão", "🍞", "Para terminar, um pedaço de...")
    )

    override suspend fun generateRounds(config: HungryMonsterConfig): List<HungryMonsterRound> {
        return foodsAndItems.take(config.roundCount).map {
            HungryMonsterRound(stimulus = it)
        }
    }

    override fun evaluateAnswer(
        round: HungryMonsterRound,
        answer: HungryMonsterAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        return when (answer.clinicianRating) {
            ClinicianRating.CORRECT_INDEPENDENT -> RoundEvaluationResult.Correct("O monstrinho adorou a resposta!")
            ClinicianRating.CORRECT_WITH_CUE -> RoundEvaluationResult.Correct("Com uma ajudinha, ele comeu tudo!")
            ClinicianRating.INCORRECT -> RoundEvaluationResult.Incorrect(false, "O monstrinho ficou curioso e quer ouvir de novo!")
            ClinicianRating.NO_RESPONSE -> RoundEvaluationResult.Incorrect(false, "O monstrinho esperou com calma.")
            ClinicianRating.NEEDS_REVIEW -> RoundEvaluationResult.Correct("Resposta registrada para revisão.")
        }
    }
}
