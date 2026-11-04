package com.healthcare.fonolab.feature.sessions.hub

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.session.FinishSessionUseCase
import com.healthcare.fonolab.domain.session.SessionWithDetails
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import javax.inject.Inject

data class SessionHubUiState(
    val details: SessionWithDetails? = null,
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false
)

sealed interface SessionHubEvent {
    data object SessionFinished : SessionHubEvent
    data class LaunchGame(val gameId: String, val activityId: Long) : SessionHubEvent
}

@HiltViewModel
class SessionHubViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sessionRepository: TherapySessionRepository,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val clock: Clock
) : ViewModel() {

    val sessionId: Long = savedStateHandle.get<Long>("sessionId") ?: 0L

    private val _uiState = MutableStateFlow(SessionHubUiState(isLoading = true))
    val uiState: StateFlow<SessionHubUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.observeSession(sessionId).collect { details ->
                _uiState.value = SessionHubUiState(
                    details = details,
                    isLoading = false,
                    isNotFound = (details == null)
                )
            }
        }
    }

    private val _event = MutableSharedFlow<SessionHubEvent>()
    val event: SharedFlow<SessionHubEvent> = _event.asSharedFlow()

    fun addGameActivity(gameId: String) {
        viewModelScope.launch {
            val now = clock.instant()
            val activityId = sessionRepository.addActivity(
                sessionId = sessionId,
                gameId = gameId,
                linkedGoalId = null,
                configurationJson = "{}",
                orderIndex = (uiState.value.details?.activities?.size ?: 0) + 1,
                now = now
            )
            _event.emit(SessionHubEvent.LaunchGame(gameId, activityId))
        }
    }

    fun finishSession(notes: String) {
        viewModelScope.launch {
            val res = finishSessionUseCase(sessionId, notes, "Atividades terapêuticas concluídas.")
            if (res is DomainResult.Success) {
                _event.emit(SessionHubEvent.SessionFinished)
            }
        }
    }
}
