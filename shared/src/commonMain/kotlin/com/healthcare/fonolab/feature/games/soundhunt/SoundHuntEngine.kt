package com.healthcare.fonolab.feature.games.soundhunt

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.common.PresentationMode
import com.healthcare.fonolab.domain.game.GameAnswer
import com.healthcare.fonolab.domain.game.GameConfiguration
import com.healthcare.fonolab.domain.game.GameEngine
import com.healthcare.fonolab.domain.game.GameRound
import com.healthcare.fonolab.domain.game.RoundEvaluationResult
import kotlinx.serialization.Serializable

@Serializable
data class SoundHuntConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val targetPhoneme: String = "/s/",
    val phonemePosition: PhonemePosition = PhonemePosition.INITIAL,
    val presentationMode: PresentationMode = PresentationMode.TEXT_AND_AUDIO,
    val optionCount: Int = 3,
    val allowRetry: Boolean = true
) : GameConfiguration

data class SoundHuntOption(
    val word: String,
    val emoji: String,
    val isCorrect: Boolean
)

data class SoundHuntRound(
    val targetWord: String,
    val targetEmoji: String,
    val instructionText: String,
    val options: List<SoundHuntOption>
) : GameRound

data class SoundHuntAnswer(
    val selectedWord: String
) : GameAnswer

class SoundHuntEngine : GameEngine<SoundHuntConfig, SoundHuntRound, SoundHuntAnswer> {

    // Valid offline stimuli dataset
    private val stimulusBank = listOf(
        SoundHuntOption("Sapo", "🐸", true) to listOf(SoundHuntOption("Gato", "🐱", false), SoundHuntOption("Pato", "🦆", false), SoundHuntOption("Vaca", "🐮", false)),
        SoundHuntOption("Sol", "☀️", true) to listOf(SoundHuntOption("Lua", "🌙", false), SoundHuntOption("Estrela", "⭐", false), SoundHuntOption("Nuvem", "☁️", false)),
        SoundHuntOption("Selo", "✉️", true) to listOf(SoundHuntOption("Livro", "📖", false), SoundHuntOption("Lápis", "✏️", false), SoundHuntOption("Caneta", "🖊️", false)),
        SoundHuntOption("Sopa", "🍲", true) to listOf(SoundHuntOption("Pizza", "🍕", false), SoundHuntOption("Bolo", "🍰", false), SoundHuntOption("Fruta", "🍎", false)),
        SoundHuntOption("Sino", "🔔", true) to listOf(SoundHuntOption("Bola", "⚽", false), SoundHuntOption("Carro", "🚗", false), SoundHuntOption("Trem", "🚂", false))
    )

    override suspend fun generateRounds(config: SoundHuntConfig): List<SoundHuntRound> {
        return stimulusBank.take(config.roundCount).map { (correct, foils) ->
            val totalOptions = (listOf(correct) + foils.take(config.optionCount - 1)).shuffled()
            SoundHuntRound(
                targetWord = correct.word,
                targetEmoji = correct.emoji,
                instructionText = "Encontre a figura que começa com o som ${config.targetPhoneme}:",
                options = totalOptions
            )
        }
    }

    override fun evaluateAnswer(
        round: SoundHuntRound,
        answer: SoundHuntAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.selectedWord.equals(round.targetWord, ignoreCase = true)
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Parabéns! Você encontrou: ${round.targetWord}")
        } else {
            val canRetry = currentAttempt < 2
            RoundEvaluationResult.Incorrect(
                canRetry = canRetry,
                feedbackMessage = if (canRetry) "Tente novamente!" else "O correto era: ${round.targetWord}"
            )
        }
    }
}
