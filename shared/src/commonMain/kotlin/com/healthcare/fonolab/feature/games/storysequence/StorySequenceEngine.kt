package com.healthcare.fonolab.feature.games.storysequence

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.game.GameAnswer
import com.healthcare.fonolab.domain.game.GameConfiguration
import com.healthcare.fonolab.domain.game.GameEngine
import com.healthcare.fonolab.domain.game.GameRound
import com.healthcare.fonolab.domain.game.RoundEvaluationResult
import kotlinx.serialization.Serializable

@Serializable
data class StorySequenceConfig(
    override val roundCount: Int = 3,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY
) : GameConfiguration

data class StoryScene(
    val id: Int,
    val title: String,
    val description: String,
    val emoji: String
)

data class StorySequenceRound(
    val storyTitle: String,
    val initialShuffledScenes: List<StoryScene>,
    val acceptableOrders: List<List<Int>> // IDs in acceptable temporal sequences
) : GameRound

data class StorySequenceAnswer(
    val orderedSceneIds: List<Int>
) : GameAnswer

class StorySequenceEngine : GameEngine<StorySequenceConfig, StorySequenceRound, StorySequenceAnswer> {

    private val stories = listOf(
        StorySequenceRound(
            storyTitle = "O Banho do Cachorrinho",
            initialShuffledScenes = listOf(
                StoryScene(2, "Lavando com sabão", "Passou xampu no pelo", "🧼"),
                StoryScene(1, "Cachorro sujo", "Brincou na lama", "🐕"),
                StoryScene(3, "Cachorro limpo e seco", "Ficou cheiroso e feliz", "✨")
            ),
            acceptableOrders = listOf(listOf(1, 2, 3))
        ),
        StorySequenceRound(
            storyTitle = "Plantando a Flor",
            initialShuffledScenes = listOf(
                StoryScene(3, "Flor desabrochando", "A flor nasceu colorida", "🌻"),
                StoryScene(1, "Plantando a semente", "Colocou a semente na terra", "🌱"),
                StoryScene(2, "Regando com água", "Regou a terrinha todo dia", "🚿")
            ),
            acceptableOrders = listOf(listOf(1, 2, 3))
        ),
        StorySequenceRound(
            storyTitle = "Fazendo um Bolo",
            initialShuffledScenes = listOf(
                StoryScene(3, "Bolo quentinho", "Saiu do forno pronto para comer", "🎂"),
                StoryScene(1, "Misturando ingredientes", "Ovos, leite e farinha na tigela", "🥣"),
                StoryScene(2, "Bolo no forno", "Assando na temperatura certa", "🔥")
            ),
            acceptableOrders = listOf(listOf(1, 2, 3))
        )
    )

    override suspend fun generateRounds(config: StorySequenceConfig): List<StorySequenceRound> {
        return stories.take(config.roundCount)
    }

    override fun evaluateAnswer(
        round: StorySequenceRound,
        answer: StorySequenceAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = round.acceptableOrders.any { it == answer.orderedSceneIds }
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Sequência temporal perfeita! Ótima história.")
        } else {
            val canRetry = currentAttempt < 2
            RoundEvaluationResult.Incorrect(
                canRetry = canRetry,
                feedbackMessage = if (canRetry) "Observe bem o que acontece antes e depois." else "Essa foi a ordem comum da história."
            )
        }
    }
}
