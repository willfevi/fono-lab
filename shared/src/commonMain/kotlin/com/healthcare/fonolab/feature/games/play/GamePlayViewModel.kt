package com.healthcare.fonolab.feature.games.play

import com.healthcare.fonolab.domain.datetime.Clock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.platform.PlatformAudioService
import com.healthcare.fonolab.domain.performance.AttemptEvaluation
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.reward.RewardCalculator
import com.healthcare.fonolab.domain.session.ActivityStatus
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import com.healthcare.fonolab.feature.games.advanced.*
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterConfig
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterEngine
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterRound
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntAnswer
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntConfig
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntEngine
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntRound
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionConfig
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionEngine
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionRound
import com.healthcare.fonolab.feature.games.storysequence.StoryScene
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceConfig
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceEngine
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceRound
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryConfig
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryEngine
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryRound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GamePlayUiState(
    val gameId: String,
    val sessionId: Long? = null,
    val activityId: Long? = null,
    val currentRoundIndex: Int = 0,
    val totalRounds: Int = 0,
    val selectedCue: CueLevel = CueLevel.NONE,
    val soundHuntRound: SoundHuntRound? = null,
    val spaceRound: SpaceMissionRound? = null,
    val storyRound: StorySequenceRound? = null,
    val storyScenes: List<StoryScene> = emptyList(),
    val monsterRound: HungryMonsterRound? = null,
    val wordFactoryRound: WordFactoryRound? = null,
    val rhymeRound: RhymeDetectiveRound? = null,
    val magicMouthRound: MagicMouthRound? = null,
    val wordMarketRound: WordMarketRound? = null,
    val sentenceRound: SentenceDetectiveRound? = null,
    val soundMemoryRound: SoundMemoryRound? = null,
    val feedbackMessage: String? = null,
    val isGameOver: Boolean = false,
    val xpEarned: Int = 0,
    val correctCount: Int = 0,
    val cuedCount: Int = 0,
    val clinicalAccuracyPercent: Int = 0
)

class GamePlayViewModel(
    val gameId: String = "sound_hunt",
    val sessionId: Long? = null,
    val activityId: Long? = null,
    private val sessionRepository: TherapySessionRepository,
    private val audioService: PlatformAudioService,
    private val clock: Clock
) : ViewModel() {

    private val soundHuntEngine = SoundHuntEngine()
    private val spaceEngine = SpaceMissionEngine()
    private val storyEngine = StorySequenceEngine()
    private val monsterEngine = HungryMonsterEngine()
    private val factoryEngine = WordFactoryEngine()
    private val rhymeEngine = RhymeDetectiveEngine()
    private val magicMouthEngine = MagicMouthEngine()
    private val wordMarketEngine = WordMarketEngine()
    private val sentenceEngine = SentenceDetectiveEngine()
    private val soundMemoryEngine = SoundMemoryEngine()

    private var soundHuntRounds: List<SoundHuntRound> = emptyList()
    private var spaceRounds: List<SpaceMissionRound> = emptyList()
    private var storyRounds: List<StorySequenceRound> = emptyList()
    private var monsterRounds: List<HungryMonsterRound> = emptyList()
    private var factoryRounds: List<WordFactoryRound> = emptyList()
    private var rhymeRounds: List<RhymeDetectiveRound> = emptyList()
    private var magicMouthRounds: List<MagicMouthRound> = emptyList()
    private var wordMarketRounds: List<WordMarketRound> = emptyList()
    private var sentenceRounds: List<SentenceDetectiveRound> = emptyList()
    private var soundMemoryRounds: List<SoundMemoryRound> = emptyList()

    private val _uiState = MutableStateFlow(
        GamePlayUiState(
            gameId = gameId,
            sessionId = sessionId,
            activityId = activityId
        )
    )
    val uiState: StateFlow<GamePlayUiState> = _uiState.asStateFlow()

    init {
        loadRounds()
    }

    private fun loadRounds() {
        viewModelScope.launch {
            when (gameId) {
                "sound_hunt" -> {
                    soundHuntRounds = soundHuntEngine.generateRounds(SoundHuntConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = soundHuntRounds.size,
                        soundHuntRound = soundHuntRounds.firstOrNull()
                    )
                }
                "space_mission" -> {
                    spaceRounds = spaceEngine.generateRounds(SpaceMissionConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = spaceRounds.size,
                        spaceRound = spaceRounds.firstOrNull()
                    )
                }
                "story_sequence" -> {
                    storyRounds = storyEngine.generateRounds(StorySequenceConfig())
                    val first = storyRounds.firstOrNull()
                    _uiState.value = _uiState.value.copy(
                        totalRounds = storyRounds.size,
                        storyRound = first,
                        storyScenes = first?.initialShuffledScenes ?: emptyList()
                    )
                }
                "hungry_monster" -> {
                    monsterRounds = monsterEngine.generateRounds(HungryMonsterConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = monsterRounds.size,
                        monsterRound = monsterRounds.firstOrNull()
                    )
                }
                "word_factory" -> {
                    factoryRounds = factoryEngine.generateRounds(WordFactoryConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = factoryRounds.size,
                        wordFactoryRound = factoryRounds.firstOrNull()
                    )
                }
                "rhyme_detective" -> {
                    rhymeRounds = rhymeEngine.generateRounds(RhymeDetectiveConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = rhymeRounds.size,
                        rhymeRound = rhymeRounds.firstOrNull()
                    )
                }
                "magic_mouth" -> {
                    magicMouthRounds = magicMouthEngine.generateRounds(MagicMouthConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = magicMouthRounds.size,
                        magicMouthRound = magicMouthRounds.firstOrNull()
                    )
                }
                "word_market" -> {
                    wordMarketRounds = wordMarketEngine.generateRounds(WordMarketConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = wordMarketRounds.size,
                        wordMarketRound = wordMarketRounds.firstOrNull()
                    )
                }
                "sentence_detective" -> {
                    sentenceRounds = sentenceEngine.generateRounds(SentenceDetectiveConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = sentenceRounds.size,
                        sentenceRound = sentenceRounds.firstOrNull()
                    )
                }
                "sound_memory" -> {
                    soundMemoryRounds = soundMemoryEngine.generateRounds(SoundMemoryConfig())
                    _uiState.value = _uiState.value.copy(
                        totalRounds = soundMemoryRounds.size,
                        soundMemoryRound = soundMemoryRounds.firstOrNull()
                    )
                }
            }
        }
    }

    fun onCueSelected(cue: CueLevel) {
        _uiState.value = _uiState.value.copy(selectedCue = cue)
    }

    fun onSoundHuntAnswer(word: String) {
        val round = _uiState.value.soundHuntRound ?: return
        soundHuntEngine.evaluateAnswer(round, SoundHuntAnswer(word), 1)
        val isCorrect = word.equals(round.targetWord, ignoreCase = true)
        recordAttempt(
            stimulus = round.targetWord,
            response = word,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun onSpaceTargetTapped(targetId: String) {
        val round = _uiState.value.spaceRound ?: return
        val isCorrect = targetId == round.targetId
        recordAttempt(
            stimulus = round.targetId,
            response = targetId,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun moveStorySceneUp(index: Int) {
        if (index <= 0) return
        val current = _uiState.value.storyScenes.toMutableList()
        val item = current.removeAt(index)
        current.add(index - 1, item)
        _uiState.value = _uiState.value.copy(storyScenes = current)
    }

    fun moveStorySceneDown(index: Int) {
        val current = _uiState.value.storyScenes.toMutableList()
        if (index >= current.size - 1) return
        val item = current.removeAt(index)
        current.add(index + 1, item)
        _uiState.value = _uiState.value.copy(storyScenes = current)
    }

    fun onStoryConfirm(ids: List<Int>) {
        val round = _uiState.value.storyRound ?: return
        val isCorrect = round.acceptableOrders.any { it == ids }
        recordAttempt(
            stimulus = round.storyTitle,
            response = ids.joinToString(","),
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun onMonsterRate(rating: ClinicianRating, cue: CueLevel) {
        val round = _uiState.value.monsterRound ?: return
        val effectiveCue = if (cue != CueLevel.NONE) cue else _uiState.value.selectedCue
        recordAttempt(
            stimulus = round.stimulus.word,
            response = rating.name,
            eval = AttemptEvaluation.ClinicianRated(rating),
            cue = effectiveCue
        )
        val isCorrect = rating == ClinicianRating.CORRECT_INDEPENDENT || rating == ClinicianRating.CORRECT_WITH_CUE
        advanceRound(isCorrect, effectiveCue)
    }

    fun onFactoryOptionSelected(option: String) {
        val round = _uiState.value.wordFactoryRound ?: return
        val isCorrect = option.equals(round.expectedAnswer, ignoreCase = true)
        recordAttempt(
            stimulus = round.baseWord,
            response = option,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun onRhymeOptionSelected(word: String) {
        val round = _uiState.value.rhymeRound ?: return
        val isCorrect = word.equals(round.targetRhymeWord, ignoreCase = true)
        recordAttempt(
            stimulus = round.clueWord,
            response = word,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun onMagicMouthRate(rating: ClinicianRating, cue: CueLevel) {
        val round = _uiState.value.magicMouthRound ?: return
        recordAttempt(
            stimulus = "${round.targetWord} (${round.targetPhoneme})",
            response = rating.name,
            eval = AttemptEvaluation.ClinicianRated(rating),
            cue = cue
        )
        val isCorrect = rating == ClinicianRating.CORRECT_INDEPENDENT || rating == ClinicianRating.CORRECT_WITH_CUE
        advanceRound(isCorrect, cue)
    }

    fun onWordMarketOptionSelected(word: String) {
        val round = _uiState.value.wordMarketRound ?: return
        val isCorrect = word.equals(round.expectedWord, ignoreCase = true)
        recordAttempt(
            stimulus = round.targetCategory,
            response = word,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun onSentenceOptionSelected(part: String) {
        val round = _uiState.value.sentenceRound ?: return
        val isCorrect = part.equals(round.expectedMissingPart, ignoreCase = true)
        recordAttempt(
            stimulus = round.targetCompleteSentence,
            response = part,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun onSoundMemoryOptionSelected(optionId: String) {
        val round = _uiState.value.soundMemoryRound ?: return
        val isCorrect = optionId == round.expectedOptionId
        recordAttempt(
            stimulus = round.auditoryPrompt,
            response = optionId,
            eval = AttemptEvaluation.AutoScored(isCorrect),
            cue = _uiState.value.selectedCue
        )
        advanceRound(isCorrect)
    }

    fun speakInstruction(text: String) {
        audioService.speak(text)
    }

    private fun advanceRound(wasCorrect: Boolean, cueUsed: CueLevel = _uiState.value.selectedCue) {
        val currentState = _uiState.value
        val newCorrect = currentState.correctCount + (if (wasCorrect) 1 else 0)
        val newCued = currentState.cuedCount + (if (cueUsed != CueLevel.NONE) 1 else 0)
        val nextIdx = currentState.currentRoundIndex + 1
        val total = currentState.totalRounds.coerceAtLeast(1)
        val accuracy = ((newCorrect * 100) / total).coerceIn(0, 100)

        if (nextIdx >= currentState.totalRounds) {
            val xp = RewardCalculator.calculateXpGain(currentState.totalRounds, true)
            _uiState.value = currentState.copy(
                isGameOver = true,
                xpEarned = xp,
                correctCount = newCorrect,
                cuedCount = newCued,
                clinicalAccuracyPercent = accuracy
            )
            activityId?.let { actId ->
                viewModelScope.launch {
                    sessionRepository.updateActivityStatus(
                        actId,
                        ActivityStatus.COMPLETED,
                        null,
                        null,
                        clock.instant()
                    )
                }
            }
        } else {
            _uiState.value = currentState.copy(
                currentRoundIndex = nextIdx,
                selectedCue = CueLevel.NONE,
                correctCount = newCorrect,
                cuedCount = newCued,
                clinicalAccuracyPercent = accuracy,
                soundHuntRound = soundHuntRounds.getOrNull(nextIdx),
                spaceRound = spaceRounds.getOrNull(nextIdx),
                storyRound = storyRounds.getOrNull(nextIdx),
                storyScenes = storyRounds.getOrNull(nextIdx)?.initialShuffledScenes ?: emptyList(),
                monsterRound = monsterRounds.getOrNull(nextIdx),
                wordFactoryRound = factoryRounds.getOrNull(nextIdx),
                rhymeRound = rhymeRounds.getOrNull(nextIdx),
                magicMouthRound = magicMouthRounds.getOrNull(nextIdx),
                wordMarketRound = wordMarketRounds.getOrNull(nextIdx),
                sentenceRound = sentenceRounds.getOrNull(nextIdx),
                soundMemoryRound = soundMemoryRounds.getOrNull(nextIdx)
            )
        }
    }

    private fun recordAttempt(
        stimulus: String,
        response: String,
        eval: AttemptEvaluation,
        cue: CueLevel = CueLevel.NONE
    ) {
        val actId = activityId ?: return
        viewModelScope.launch {
            sessionRepository.recordAttempt(
                GameAttempt(
                    id = 0,
                    activityId = actId,
                    roundIndex = _uiState.value.currentRoundIndex,
                    attemptNumber = 1,
                    stimulusTarget = stimulus,
                    stimulusPresentedJson = "{}",
                    responseRecorded = response,
                    evaluation = eval,
                    cueLevel = cue,
                    responseTimeMs = null,
                    clinicianNotes = "",
                    timestamp = clock.instant()
                )
            )
        }
    }
}
