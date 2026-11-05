package com.healthcare.fonolab.feature.games.advanced

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.game.GameAnswer
import com.healthcare.fonolab.domain.game.GameConfiguration
import com.healthcare.fonolab.domain.game.GameEngine
import com.healthcare.fonolab.domain.game.GameRound
import com.healthcare.fonolab.domain.game.RoundEvaluationResult

// ============================================================================
// GAME 6: DETETIVE DAS RIMAS (rhyme_detective)
// Phonological awareness: rhyme recognition & ending sound correspondence
// ============================================================================

data class RhymeDetectiveConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY
) : GameConfiguration

data class RhymeOption(
    val word: String,
    val emoji: String,
    val endingSound: String
)

data class RhymeDetectiveRound(
    val roundNumber: Int,
    val clueWord: String,
    val clueEmoji: String,
    val targetRhymeWord: String,
    val promptText: String,
    val options: List<RhymeOption>
) : GameRound

data class RhymeDetectiveAnswer(val selectedWord: String) : GameAnswer

class RhymeDetectiveEngine : GameEngine<RhymeDetectiveConfig, RhymeDetectiveRound, RhymeDetectiveAnswer> {
    override suspend fun generateRounds(config: RhymeDetectiveConfig): List<RhymeDetectiveRound> {
        val bank = listOf(
            RhymeDetectiveRound(
                1, "Gato", "🐱", "Pato",
                "Qual palavra termina com o mesmo som de GATO (-ATO)?",
                listOf(
                    RhymeOption("Pato", "🦆", "-ATO"),
                    RhymeOption("Bola", "⚽", "-OLA"),
                    RhymeOption("Casa", "🏠", "-ASA")
                )
            ),
            RhymeDetectiveRound(
                2, "Mão", "✋", "Pão",
                "Qual palavra rima com MÃO (-ÃO)?",
                listOf(
                    RhymeOption("Sol", "☀️", "-OL"),
                    RhymeOption("Pão", "🍞", "-ÃO"),
                    RhymeOption("Trem", "🚂", "-EM")
                )
            ),
            RhymeDetectiveRound(
                3, "Janela", "🪟", "Panela",
                "Ajude o detetive! O que rima com JANELA (-ELA)?",
                listOf(
                    RhymeOption("Panela", "🍲", "-ELA"),
                    RhymeOption("Cadeira", "🪑", "-EIRA"),
                    RhymeOption("Sapato", "👞", "-ATO")
                )
            ),
            RhymeDetectiveRound(
                4, "Balão", "🎈", "Leão",
                "Descubra a pista que rima com BALÃO (-ÃO):",
                listOf(
                    RhymeOption("Estrela", "⭐", "-ELA"),
                    RhymeOption("Leão", "🦁", "-ÃO"),
                    RhymeOption("Nuvem", "☁️", "-UVEM")
                )
            ),
            RhymeDetectiveRound(
                5, "Foca", "🦭", "Pipoca",
                "Última pista! Qual palavra rima com FOCA (-OCA)?",
                listOf(
                    RhymeOption("Pipoca", "🍿", "-OCA"),
                    RhymeOption("Banana", "🍌", "-ANA"),
                    RhymeOption("Coruja", "🦉", "-UJA")
                )
            )
        )
        return bank.take(config.roundCount.coerceIn(1, bank.size))
    }

    override fun evaluateAnswer(
        round: RhymeDetectiveRound,
        answer: RhymeDetectiveAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.selectedWord.equals(round.targetRhymeWord, ignoreCase = true)
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Excelente! ${round.clueWord} rima com ${round.targetRhymeWord}!")
        } else {
            RoundEvaluationResult.Incorrect(
                canRetry = currentAttempt < 2,
                feedbackMessage = "Escute o final da palavra ${round.clueWord} e tente novamente."
            )
        }
    }
}

// ============================================================================
// GAME 7: BOCA MÁGICA (magic_mouth)
// Articulation point/manner cueing & phonetic production rated by clinician
// ============================================================================

data class MagicMouthConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY
) : GameConfiguration

data class MagicMouthRound(
    val roundNumber: Int,
    val targetPhoneme: String,
    val position: PhonemePosition,
    val targetWord: String,
    val emoji: String,
    val articulatoryCueTitle: String,
    val articulatoryTip: String
) : GameRound

data class MagicMouthAnswer(val producedCorrectly: Boolean) : GameAnswer

class MagicMouthEngine : GameEngine<MagicMouthConfig, MagicMouthRound, MagicMouthAnswer> {
    override suspend fun generateRounds(config: MagicMouthConfig): List<MagicMouthRound> {
        val bank = listOf(
            MagicMouthRound(
                1, "/p/", PhonemePosition.INITIAL, "Pipa", "🪁",
                "Som Bilabial Explosivo (/p/)",
                "Junte os lábios suavemente e solte o ar de uma vez sem vibrar a garganta: P-P-PIPA!"
            ),
            MagicMouthRound(
                2, "/f/", PhonemePosition.INITIAL, "Foca", "🦭",
                "Som Labiodental Fricativo (/f/)",
                "Encoste os dentes de cima no lábio de baixo e sopre um ventinho contínuo: FFFF-FOCA!"
            ),
            MagicMouthRound(
                3, "/s/", PhonemePosition.INITIAL, "Sapo", "🐸",
                "Som Alveolar Fricativo (/s/)",
                "Sorria com os dentes quase fechados e faça o som da cobrinha: SSSS-SAPO!"
            ),
            MagicMouthRound(
                4, "/l/", PhonemePosition.INITIAL, "Lua", "🌙",
                "Som Alveolar Lateral (/l/)",
                "Eleve a ponta da língua atrás dos dentes superiores: LLL-LUA!"
            ),
            MagicMouthRound(
                5, "/ɾ/", PhonemePosition.MEDIAL, "Arara", "🦜",
                "Vibração Simples da Ponta da Língua (/ɾ/)",
                "Toque rapidamente a ponta da língua no céu da boca (atrás dos dentes): A-RA-RA!"
            )
        )
        return bank.take(config.roundCount.coerceIn(1, bank.size))
    }

    override fun evaluateAnswer(
        round: MagicMouthRound,
        answer: MagicMouthAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        return if (answer.producedCorrectly) {
            RoundEvaluationResult.Correct("Ótima produção articulatória de ${round.targetWord}!")
        } else {
            RoundEvaluationResult.Incorrect(
                canRetry = true,
                feedbackMessage = "Observe a dica visual da Boca Mágica: ${round.articulatoryTip}"
            )
        }
    }
}

// ============================================================================
// GAME 8: MERCADO DAS PALAVRAS (word_market)
// Vocabulary & semantic categorization
// ============================================================================

data class WordMarketConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY
) : GameConfiguration

data class MarketItemOption(
    val word: String,
    val emoji: String,
    val category: String
)

data class WordMarketRound(
    val roundNumber: Int,
    val targetCategory: String,
    val instruction: String,
    val expectedWord: String,
    val options: List<MarketItemOption>
) : GameRound

data class WordMarketAnswer(val selectedWord: String) : GameAnswer

class WordMarketEngine : GameEngine<WordMarketConfig, WordMarketRound, WordMarketAnswer> {
    override suspend fun generateRounds(config: WordMarketConfig): List<WordMarketRound> {
        val bank = listOf(
            WordMarketRound(
                1, "Frutas",
                "Coloque no carrinho apenas o item da categoria FRUTAS:",
                "Maçã",
                listOf(
                    MarketItemOption("Maçã", "🍎", "Frutas"),
                    MarketItemOption("Sapato", "👟", "Vestuário"),
                    MarketItemOption("Martelo", "🔨", "Ferramentas")
                )
            ),
            WordMarketRound(
                2, "Meios de Transporte",
                "Qual destes itens pertence à categoria MEIOS DE TRANSPORTE?",
                "Avião",
                listOf(
                    MarketItemOption("Cenoura", "🥕", "Alimentos"),
                    MarketItemOption("Avião", "✈️", "Meios de Transporte"),
                    MarketItemOption("Camiseta", "👕", "Vestuário")
                )
            ),
            WordMarketRound(
                3, "Animais Aquáticos",
                "Encontre o item que faz parte dos ANIMAIS AQUÁTICOS:",
                "Baleia",
                listOf(
                    MarketItemOption("Baleia", "🐋", "Animais Aquáticos"),
                    MarketItemOption("Violão", "🎸", "Instrumentos Musicais"),
                    MarketItemOption("Relógio", "⌚", "Objetos")
                )
            ),
            WordMarketRound(
                4, "Material Escolar",
                "Selecione o item usado como MATERIAL ESCOLAR:",
                "Caderno",
                listOf(
                    MarketItemOption("Sorvete", "🍦", "Alimentos"),
                    MarketItemOption("Caderno", "📓", "Material Escolar"),
                    MarketItemOption("Travesseiro", "🛌", "Quarto")
                )
            ),
            WordMarketRound(
                5, "Instrumentos Musicais",
                "Última compra! Escolha o item da categoria INSTRUMENTOS MUSICAIS:",
                "Tambor",
                listOf(
                    MarketItemOption("Tambor", "🥁", "Instrumentos Musicais"),
                    MarketItemOption("Guarda-chuva", "☂️", "Acessórios"),
                    MarketItemOption("Abacaxi", "🍍", "Frutas")
                )
            )
        )
        return bank.take(config.roundCount.coerceIn(1, bank.size))
    }

    override fun evaluateAnswer(
        round: WordMarketRound,
        answer: WordMarketAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.selectedWord.equals(round.expectedWord, ignoreCase = true)
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Muito bem! ${round.expectedWord} pertence a ${round.targetCategory}!")
        } else {
            RoundEvaluationResult.Incorrect(
                canRetry = currentAttempt < 2,
                feedbackMessage = "Pense na categoria ${round.targetCategory} e tente novamente."
            )
        }
    }
}

// ============================================================================
// GAME 9: DETETIVE DAS FRASES (sentence_detective)
// Expressive language & morphosyntax (Subject + Verb + Object)
// ============================================================================

data class SentenceDetectiveConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY
) : GameConfiguration

data class SentenceDetectiveRound(
    val roundNumber: Int,
    val sceneEmoji: String,
    val sentenceFrame: String, // e.g. "O menino ___ a bola."
    val expectedMissingPart: String,
    val targetCompleteSentence: String,
    val options: List<String>
) : GameRound

data class SentenceDetectiveAnswer(val selectedPart: String) : GameAnswer

class SentenceDetectiveEngine : GameEngine<SentenceDetectiveConfig, SentenceDetectiveRound, SentenceDetectiveAnswer> {
    override suspend fun generateRounds(config: SentenceDetectiveConfig): List<SentenceDetectiveRound> {
        val bank = listOf(
            SentenceDetectiveRound(
                1, "👦⚽",
                "O menino ______ a bola no gol.",
                "chutou",
                "O menino chutou a bola no gol.",
                listOf("chutou", "dormiu", "bebeu")
            ),
            SentenceDetectiveRound(
                2, "👧🍎",
                "A menina está comendo uma ______ vermelha.",
                "maçã",
                "A menina está comendo uma maçã vermelha.",
                listOf("maçã", "cadeira", "nuvem")
            ),
            SentenceDetectiveRound(
                3, "🐶🦴",
                "______ escondeu o osso no jardim.",
                "O cachorro",
                "O cachorro escondeu o osso no jardim.",
                listOf("O cachorro", "A geladeira", "O lápis")
            ),
            SentenceDetectiveRound(
                4, "👩📖",
                "A professora ______ uma história divertida.",
                "leu",
                "A professora leu uma história divertida.",
                listOf("leu", "nadou", "choveu")
            ),
            SentenceDetectiveRound(
                5, "🐱🥛",
                "O gatinho bebeu o ______ na tigela.",
                "leite",
                "O gatinho bebeu o leite na tigela.",
                listOf("leite", "sapato", "caminhão")
            )
        )
        return bank.take(config.roundCount.coerceIn(1, bank.size))
    }

    override fun evaluateAnswer(
        round: SentenceDetectiveRound,
        answer: SentenceDetectiveAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.selectedPart.equals(round.expectedMissingPart, ignoreCase = true)
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Frase completa: ${round.targetCompleteSentence}")
        } else {
            RoundEvaluationResult.Incorrect(
                canRetry = currentAttempt < 2,
                feedbackMessage = "Veja a cena ${round.sceneEmoji} e escolha a palavra que faz sentido na frase."
            )
        }
    }
}

// ============================================================================
// GAME 10: MEMÓRIA SONORA (sound_memory)
// Auditory perception, sound-image association & working memory
// ============================================================================

data class SoundMemoryConfig(
    override val roundCount: Int = 5,
    override val difficulty: DifficultyLevel = DifficultyLevel.EASY
) : GameConfiguration

data class SoundMemoryOption(
    val id: String,
    val label: String,
    val emoji: String,
    val onomatopoeia: String
)

data class SoundMemoryRound(
    val roundNumber: Int,
    val auditoryPrompt: String,
    val spokenCue: String,
    val expectedOptionId: String,
    val options: List<SoundMemoryOption>
) : GameRound

data class SoundMemoryAnswer(val selectedOptionId: String) : GameAnswer

class SoundMemoryEngine : GameEngine<SoundMemoryConfig, SoundMemoryRound, SoundMemoryAnswer> {
    override suspend fun generateRounds(config: SoundMemoryConfig): List<SoundMemoryRound> {
        val bank = listOf(
            SoundMemoryRound(
                1,
                "Escute a sequência sonora: 'Muuu! Muuu!' Qual figura faz esse som?",
                "Muuu! Muuu! Quem faz esse som no sítio?",
                "cow",
                listOf(
                    SoundMemoryOption("cow", "Vaca", "🐄", "Muuu!"),
                    SoundMemoryOption("duck", "Pato", "🦆", "Quack!"),
                    SoundMemoryOption("train", "Trem", "🚂", "Piuí!")
                )
            ),
            SoundMemoryRound(
                2,
                "Memória Auditiva: 'Tic-Tac, Tic-Tac!' Qual objeto produz esse som rítmico?",
                "Tic-Tac, Tic-Tac! Quem marca as horas com esse som?",
                "clock",
                listOf(
                    SoundMemoryOption("bell", "Sino", "🔔", "Blém-Blom!"),
                    SoundMemoryOption("clock", "Relógio", "⏰", "Tic-Tac!"),
                    SoundMemoryOption("drum", "Tambor", "🥁", "Tum-Tum!")
                )
            ),
            SoundMemoryRound(
                3,
                "Lembre a ordem dos 2 sons: Primeiro AU-AU, depois MIAU!",
                "Primeiro o cachorro fez Au-Au, depois o gato fez Miau!",
                "dog_cat",
                listOf(
                    SoundMemoryOption("dog_cat", "Cachorro 🐶 ➔ Gato 🐱", "🐶🐱", "Au-Au ➔ Miau"),
                    SoundMemoryOption("cat_bird", "Gato 🐱 ➔ Passarinho 🐦", "🐱🐦", "Miau ➔ Piu-Piu"),
                    SoundMemoryOption("lion_frog", "Leão 🦁 ➔ Sapo 🐸", "🦁🐸", "Roar ➔ Webit")
                )
            ),
            SoundMemoryRound(
                4,
                "Lembre a sequência de palavras faladas: SOL — LUA",
                "Escute e memorize na ordem: Sol e depois Lua!",
                "sun_moon",
                listOf(
                    SoundMemoryOption("sun_moon", "Sol ☀️ ➔ Lua 🌙", "☀️🌙", "Sol ➔ Lua"),
                    SoundMemoryOption("moon_star", "Lua 🌙 ➔ Estrela ⭐", "🌙⭐", "Lua ➔ Estrela"),
                    SoundMemoryOption("rain_sun", "Chuva 🌧️ ➔ Sol ☀️", "🌧️☀️", "Chuva ➔ Sol")
                )
            ),
            SoundMemoryRound(
                5,
                "Desafio Triplo! Memorize a sequência: BOLA — PATO — CASA",
                "Escute as três palavras na ordem: Bola, Pato, Casa!",
                "ball_duck_house",
                listOf(
                    SoundMemoryOption("ball_duck_house", "Bola ⚽ ➔ Pato 🦆 ➔ Casa 🏠", "⚽🦆🏠", "Bola, Pato, Casa"),
                    SoundMemoryOption("duck_ball_house", "Pato 🦆 ➔ Bola ⚽ ➔ Casa 🏠", "🦆⚽🏠", "Pato, Bola, Casa"),
                    SoundMemoryOption("house_ball_duck", "Casa 🏠 ➔ Bola ⚽ ➔ Pato 🦆", "🏠⚽🦆", "Casa, Bola, Pato")
                )
            )
        )
        return bank.take(config.roundCount.coerceIn(1, bank.size))
    }

    override fun evaluateAnswer(
        round: SoundMemoryRound,
        answer: SoundMemoryAnswer,
        currentAttempt: Int
    ): RoundEvaluationResult {
        val isCorrect = answer.selectedOptionId == round.expectedOptionId
        return if (isCorrect) {
            RoundEvaluationResult.Correct("Memória auditiva afiada! Você acertou a associação sonora!")
        } else {
            RoundEvaluationResult.Incorrect(
                canRetry = currentAttempt < 2,
                feedbackMessage = "Ouça novamente a pista sonora e preste atenção na ordem."
            )
        }
    }
}
