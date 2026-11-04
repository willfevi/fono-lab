package com.healthcare.fonolab.domain.game

import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.feature.games.advanced.MagicMouthAnswer
import com.healthcare.fonolab.feature.games.advanced.MagicMouthConfig
import com.healthcare.fonolab.feature.games.advanced.MagicMouthEngine
import com.healthcare.fonolab.feature.games.advanced.RhymeDetectiveAnswer
import com.healthcare.fonolab.feature.games.advanced.RhymeDetectiveConfig
import com.healthcare.fonolab.feature.games.advanced.RhymeDetectiveEngine
import com.healthcare.fonolab.feature.games.advanced.SentenceDetectiveAnswer
import com.healthcare.fonolab.feature.games.advanced.SentenceDetectiveConfig
import com.healthcare.fonolab.feature.games.advanced.SentenceDetectiveEngine
import com.healthcare.fonolab.feature.games.advanced.SoundMemoryAnswer
import com.healthcare.fonolab.feature.games.advanced.SoundMemoryConfig
import com.healthcare.fonolab.feature.games.advanced.SoundMemoryEngine
import com.healthcare.fonolab.feature.games.advanced.WordMarketAnswer
import com.healthcare.fonolab.feature.games.advanced.WordMarketConfig
import com.healthcare.fonolab.feature.games.advanced.WordMarketEngine
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterAnswer
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterConfig
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterEngine
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntAnswer
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntConfig
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntEngine
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionAnswer
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionConfig
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionEngine
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceAnswer
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceConfig
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceEngine
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryAnswer
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryConfig
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesEnginesUnitTest {

    @Test
    fun `sound hunt engine generates rounds and evaluates correctly`() = runBlocking {
        val engine = SoundHuntEngine()
        val rounds = engine.generateRounds(SoundHuntConfig(roundCount = 3))
        assertEquals(3, rounds.size)

        val first = rounds.first()
        val resCorrect = engine.evaluateAnswer(first, SoundHuntAnswer(first.targetWord), 1)
        assertTrue(resCorrect is RoundEvaluationResult.Correct)

        val resIncorrect = engine.evaluateAnswer(first, SoundHuntAnswer("INCORRETO"), 1)
        assertTrue(resIncorrect is RoundEvaluationResult.Incorrect)
    }

    @Test
    fun `space mission engine validates tapped targets`() = runBlocking {
        val engine = SpaceMissionEngine()
        val rounds = engine.generateRounds(SpaceMissionConfig(roundCount = 2))
        assertEquals(2, rounds.size)

        val first = rounds.first()
        val res = engine.evaluateAnswer(first, SpaceMissionAnswer(first.targetId), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `story sequence engine accepts valid order`() = runBlocking {
        val engine = StorySequenceEngine()
        val rounds = engine.generateRounds(StorySequenceConfig(roundCount = 1))
        val first = rounds.first()

        val expectedOrder = first.acceptableOrders.first()
        val res = engine.evaluateAnswer(first, StorySequenceAnswer(expectedOrder), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `word factory engine evaluates phonological choices`() = runBlocking {
        val engine = WordFactoryEngine()
        val rounds = engine.generateRounds(WordFactoryConfig(roundCount = 2))
        val first = rounds.first()

        val res = engine.evaluateAnswer(first, WordFactoryAnswer(first.expectedAnswer), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `hungry monster engine evaluates voice and naming attempts`() = runBlocking {
        val engine = HungryMonsterEngine()
        val rounds = engine.generateRounds(HungryMonsterConfig(roundCount = 2))
        assertEquals(2, rounds.size)
        val first = rounds.first()
        val res = engine.evaluateAnswer(
            first,
            HungryMonsterAnswer(
                clinicianRating = ClinicianRating.CORRECT_INDEPENDENT,
                cueLevel = CueLevel.NONE
            ),
            1
        )
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `rhyme detective engine validates rhyming words`() = runBlocking {
        val engine = RhymeDetectiveEngine()
        val rounds = engine.generateRounds(RhymeDetectiveConfig(roundCount = 3))
        assertEquals(3, rounds.size)
        val first = rounds.first()
        val res = engine.evaluateAnswer(first, RhymeDetectiveAnswer(first.targetRhymeWord), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `magic mouth engine validates articulatory production`() = runBlocking {
        val engine = MagicMouthEngine()
        val rounds = engine.generateRounds(MagicMouthConfig(roundCount = 3))
        assertEquals(3, rounds.size)
        val first = rounds.first()
        val res = engine.evaluateAnswer(first, MagicMouthAnswer(producedCorrectly = true), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `word market engine validates semantic categorization`() = runBlocking {
        val engine = WordMarketEngine()
        val rounds = engine.generateRounds(WordMarketConfig(roundCount = 3))
        assertEquals(3, rounds.size)
        val first = rounds.first()
        val res = engine.evaluateAnswer(first, WordMarketAnswer(first.expectedWord), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `sentence detective engine validates morphosyntactic completion`() = runBlocking {
        val engine = SentenceDetectiveEngine()
        val rounds = engine.generateRounds(SentenceDetectiveConfig(roundCount = 3))
        assertEquals(3, rounds.size)
        val first = rounds.first()
        val res = engine.evaluateAnswer(first, SentenceDetectiveAnswer(first.expectedMissingPart), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }

    @Test
    fun `sound memory engine validates working memory sequences`() = runBlocking {
        val engine = SoundMemoryEngine()
        val rounds = engine.generateRounds(SoundMemoryConfig(roundCount = 3))
        assertEquals(3, rounds.size)
        val first = rounds.first()
        val res = engine.evaluateAnswer(first, SoundMemoryAnswer(first.expectedOptionId), 1)
        assertTrue(res is RoundEvaluationResult.Correct)
    }
}
