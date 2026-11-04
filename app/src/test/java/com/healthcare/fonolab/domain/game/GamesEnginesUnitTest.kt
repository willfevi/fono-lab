package com.healthcare.fonolab.domain.game

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
}
