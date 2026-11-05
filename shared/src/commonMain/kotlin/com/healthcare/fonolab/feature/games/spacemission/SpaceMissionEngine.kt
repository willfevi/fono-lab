package com.healthcare.fonolab.feature.games.spacemission

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.game.GameAnswer
import com.healthcare.fonolab.domain.game.GameConfiguration
import com.healthcare.fonolab.domain.game.GameEngine
import com.healthcare.fonolab.domain.game.GameRound
import com.healthcare.fonolab.domain.game.RoundEvaluationResult
import kotlinx.serialization.Serializable

@Serializable
data class SpaceMissionConfig(
    override val roundCount: Int = 4,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val categoryFilter: String = "ALL"
) : GameConfiguration

data class SpaceTarget(
    val id: String,
    val label: String,
    val emoji: String,
    val positionX: Float, // relative 0.0 - 1.0
    val positionY: Float
)

data class SpaceMissionRound(
    val instruction: String,
    val targetId: String,
    val targets: List<SpaceTarget>
) : GameRound

data class SpaceMissionAnswer(
    val tappedTargetId: String
) : GameAnswer

class SpaceMissionEngine : GameEngine<SpaceMissionConfig, SpaceMissionRound, SpaceMissionAnswer> {

    private val missions = listOf(
        SpaceMissionRound(
            instruction = "Astronauta, toque no Planeta Vermelho!",
            targetId = "mars",
            targets = listOf(
                SpaceTarget("mars", "Marte", "🪐", 0.3f, 0.4f),
                SpaceTarget("star", "Estrela", "⭐", 0.7f, 0.2f),
                SpaceTarget("satellite", "Satélite", "🛰️", 0.5f, 0.7f)
            )
        ),
        SpaceMissionRound(
            instruction = "Toque na Estrela Brilhante!",
            targetId = "star",
            targets = listOf(
                SpaceTarget("comet", "Cometa", "☄️", 0.2f, 0.3f),
                SpaceTarget("star", "Estrela", "⭐", 0.6f, 0.5f),
                SpaceTarget("ufo", "Nave Alien", "🛸", 0.4f, 0.8f)
            )
        ),
        SpaceMissionRound(
            instruction = "Identifique o Foguete Espacial!",
            targetId = "rocket",
            targets = listOf(
                SpaceTarget("moon", "Lua", "🌙", 0.3f, 0.6f),
                SpaceTarget("rocket", "Foguete", "🚀", 0.7f, 0.4f),
                SpaceTarget("sun", "Sol", "☀️", 0.5f, 0.2f)
            )
        ),
        SpaceMissionRound(
            instruction = "Toque no Satélite de Comunicação!",
            targetId = "satellite",
            targets = listOf(
                SpaceTarget("satellite", "Satélite", "🛰️", 0.4f, 0.3f),
                SpaceTarget("earth", "Terra", "🌍", 0.8f, 0.7f),
                SpaceTarget("telescope", "Telescópio", "🔭", 0.2f, 0.6f)
            )
        )
    )

    override suspend fun generateRounds(config: SpaceMissionConfig): List<SpaceMissionRound> {
        return missions.take(config.roundCount)
    }

    override fun evaluateAnswer(
        round: SpaceMissionRound,
        answer: SpaceMissionAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.tappedTargetId == round.targetId
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Missão cumprida com sucesso!")
        } else {
            val canRetry = currentAttempt < 2
            RoundEvaluationResult.Incorrect(
                canRetry = canRetry,
                feedbackMessage = if (canRetry) "Quase lá! Tente novamente." else "A nave ajustou os sensores para a próxima!"
            )
        }
    }
}
