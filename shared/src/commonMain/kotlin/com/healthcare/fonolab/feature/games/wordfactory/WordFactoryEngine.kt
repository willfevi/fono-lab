package com.healthcare.fonolab.feature.games.wordfactory

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.game.GameAnswer
import com.healthcare.fonolab.domain.game.GameConfiguration
import com.healthcare.fonolab.domain.game.GameEngine
import com.healthcare.fonolab.domain.game.GameRound
import com.healthcare.fonolab.domain.game.RoundEvaluationResult
import kotlinx.serialization.Serializable

enum class FactoryMode(val displayName: String) {
    RHYMES("Rimas"),
    INITIAL_SOUND("Som Inicial"),
    SYLLABLE_SEGMENTATION("Contagem de Sílabas")
}

@Serializable
data class WordFactoryConfig(
    override val roundCount: Int = 4,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val mode: FactoryMode = FactoryMode.RHYMES
) : GameConfiguration

data class WordFactoryRound(
    val promptText: String,
    val baseEmoji: String,
    val baseWord: String,
    val expectedAnswer: String,
    val options: List<String>
) : GameRound

data class WordFactoryAnswer(
    val selectedOption: String
) : GameAnswer

class WordFactoryEngine : GameEngine<WordFactoryConfig, WordFactoryRound, WordFactoryAnswer> {

    private val rhymeRounds = listOf(
        WordFactoryRound("O que rima com PATO?", "🦆", "PATO", "GATO", listOf("GATO", "MESA", "BOLA")),
        WordFactoryRound("O que rima com MÃO?", "✋", "MÃO", "PÃO", listOf("PÃO", "CASA", "PEIXE")),
        WordFactoryRound("O que rima with COLA?", "🧴", "COLA", "BOLA", listOf("BOLA", "TREM", "SAPATO")),
        WordFactoryRound("O que rima com FLOR?", "🌸", "FLOR", "AMOR", listOf("AMOR", "DADO", "SUCO"))
    )

    private val initialSoundRounds = listOf(
        WordFactoryRound("Qual começa com o mesmo som de BOLA (som /b/)?", "⚽", "BOLA", "BONECA", listOf("BONECA", "GATO", "LÁPIS")),
        WordFactoryRound("Qual começa com o mesmo som de SAPO (som /s/)?", "🐸", "SAPO", "SOL", listOf("SOL", "VACA", "BOLO")),
        WordFactoryRound("Qual começa com o mesmo som de PATO (som /p/)?", "🦆", "PATO", "PIPOCA", listOf("PIPOCA", "DADO", "MACACO")),
        WordFactoryRound("Qual começa com o mesmo som de FACA (som /f/)?", "🔪", "FACA", "FOGO", listOf("FOGO", "RATO", "NAVIO"))
    )

    private val syllableRounds = listOf(
        WordFactoryRound("Quantas sílabas tem a palavra BOLA?", "⚽", "BOLA", "2", listOf("1", "2", "3")),
        WordFactoryRound("Quantas sílabas tem a palavra MACACO?", "🐵", "MACACO", "3", listOf("2", "3", "4")),
        WordFactoryRound("Quantas sílabas tem a palavra SOL?", "☀️", "SOL", "1", listOf("1", "2", "3")),
        WordFactoryRound("Quantas sílabas tem a palavra BORBOLETA?", "🦋", "BORBOLETA", "4", listOf("2", "3", "4"))
    )

    override suspend fun generateRounds(config: WordFactoryConfig): List<WordFactoryRound> {
        val list = when (config.mode) {
            FactoryMode.RHYMES -> rhymeRounds
            FactoryMode.INITIAL_SOUND -> initialSoundRounds
            FactoryMode.SYLLABLE_SEGMENTATION -> syllableRounds
        }
        return list.take(config.roundCount)
    }

    override fun evaluateAnswer(
        round: WordFactoryRound,
        answer: WordFactoryAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.selectedOption.equals(round.expectedAnswer, ignoreCase = true)
        return if (isCorrect) {
            RoundEvaluationResult.Correct("As engrenagens da fábrica giraram perfeitamente!")
        } else {
            val canRetry = currentAttempt < 2
            RoundEvaluationResult.Incorrect(
                canRetry = canRetry,
                feedbackMessage = if (canRetry) "A máquina deu um estalo. Tente mais uma vez!" else "A peça correta era: ${round.expectedAnswer}"
            )
        }
    }
}
