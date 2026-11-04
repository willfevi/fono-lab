package com.healthcare.fonolab.feature.games.play

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.audio.NativeAudioService
import com.healthcare.fonolab.domain.performance.AttemptEvaluation
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.reward.RewardCalculator
import com.healthcare.fonolab.domain.session.ActivityStatus
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterAnswer
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterConfig
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterEngine
import com.healthcare.fonolab.feature.games.hungrymonster.HungryMonsterRound
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntAnswer
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntConfig
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntEngine
import com.healthcare.fonolab.feature.games.soundhunt.SoundHuntRound
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionAnswer
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionConfig
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionEngine
import com.healthcare.fonolab.feature.games.spacemission.SpaceMissionRound
import com.healthcare.fonolab.feature.games.storysequence.StoryScene
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceAnswer
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceConfig
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceEngine
import com.healthcare.fonolab.feature.games.storysequence.StorySequenceRound
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryAnswer
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryConfig
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryEngine
import com.healthcare.fonolab.feature.games.wordfactory.WordFactoryRound
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class GamePlayUiState(
    val gameId: String,
    val sessionId: Long? = null,
    val activityId: Long? = null,
    val currentRoundIndex: Int = 0,
    val totalRounds: Int = 0,
    val soundHuntRound: SoundHuntRound? = null,
    val spaceRound: SpaceMissionRound? = null,
    val storyRound: StorySequenceRound? = null,
    val storyScenes: List<StoryScene> = emptyList(),
    val monsterRound: HungryMonsterRound? = null,
    val wordFactoryRound: WordFactoryRound? = null,
    val feedbackMessage: String? = null,
    val isGameOver: Boolean = false,
    val xpEarned: Int = 0
)

@HiltViewModel
class GamePlayViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: TherapySessionRepository,
    private val audioService: NativeAudioService,
    private val clock: Clock
) : ViewModel() {

    val gameId: String = savedStateHandle.get<String>("gameId") ?: "sound_hunt"
    val sessionId: Long? = savedStateHandle.get<Long>("sessionId")?.takeIf { it > 0 }
    val activityId: Long? = savedStateHandle.get<Long>("activityId")?.takeIf { it > 0 }

    private val soundHuntEngine = SoundHuntEngine()
    private val spaceEngine = SpaceMissionEngine()
    private val storyEngine = StorySequenceEngine()
    private val monsterEngine = HungryMonsterEngine()
    private val factoryEngine = WordFactoryEngine()

    private var soundHuntRounds: List<SoundHuntRound> = emptyList()
    private var spaceRounds: List<SpaceMissionRound> = emptyList()
    private var storyRounds: List<StorySequenceRound> = emptyList()
    private var monsterRounds: List<HungryMonsterRound> = emptyList()
    private var factoryRounds: List<WordFactoryRound> = emptyList()

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
            }
        }
    }

    fun onSoundHuntAnswer(word: String) {
        val round = _uiState.value.soundHuntRound ?: return
        val res = soundHuntEngine.evaluateAnswer(round, SoundHuntAnswer(word), 1)
        recordAttempt(
            stimulus = round.targetWord,
            response = word,
            eval = AttemptEvaluation.AutoScored(word.equals(round.targetWord, ignoreCase = true))
        )
        advanceRound()
    }

    fun onSpaceTargetTapped(targetId: String) {
        val round = _uiState.value.spaceRound ?: return
        recordAttempt(
            stimulus = round.targetId,
            response = targetId,
            eval = AttemptEvaluation.AutoScored(targetId == round.targetId)
        )
        advanceRound()
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
            eval = AttemptEvaluation.AutoScored(isCorrect)
        )
        advanceRound()
    }

    fun onMonsterRate(rating: ClinicianRating, cue: CueLevel) {
        val round = _uiState.value.monsterRound ?: return
        recordAttempt(
            stimulus = round.stimulus.word,
            response = rating.name,
            eval = AttemptEvaluation.ClinicianRated(rating),
            cue = cue
        )
        advanceRound()
    }

    fun onFactoryOptionSelected(option: String) {
        val round = _uiState.value.wordFactoryRound ?: return
        val isCorrect = option.equals(round.expectedAnswer, ignoreCase = true)
        recordAttempt(
            stimulus = round.baseWord,
            response = option,
            eval = AttemptEvaluation.AutoScored(isCorrect)
        )
        advanceRound()
    }

    fun speakInstruction(text: String) {
        audioService.speak(text)
    }

    private fun advanceRound() {
        val nextIdx = _uiState.value.currentRoundIndex + 1
        if (nextIdx >= _uiState.value.totalRounds) {
            val xp = RewardCalculator.calculateXpGain(_uiState.value.totalRounds, true)
            _uiState.value = _uiState.value.copy(
                isGameOver = true,
                xpEarned = xp
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
            _uiState.value = _uiState.value.copy(
                currentRoundIndex = nextIdx,
                soundHuntRound = soundHuntRounds.getOrNull(nextIdx),
                spaceRound = spaceRounds.getOrNull(nextIdx),
                storyRound = storyRounds.getOrNull(nextIdx),
                storyScenes = storyRounds.getOrNull(nextIdx)?.initialShuffledScenes ?: emptyList(),
                monsterRound = monsterRounds.getOrNull(nextIdx),
                wordFactoryRound = factoryRounds.getOrNull(nextIdx)
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
