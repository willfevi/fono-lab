package com.healthcare.fonolab.feature.sessions.hub

import com.healthcare.fonolab.domain.datetime.Clock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.clinical.DocumentCategory
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.manager.DocumentManager
import com.healthcare.fonolab.domain.manager.SessionManager
import com.healthcare.fonolab.domain.session.FinishSessionUseCase
import com.healthcare.fonolab.domain.session.SessionWithDetails
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SessionHubUiState(
    val details: SessionWithDetails? = null,
    val isLoading: Boolean = true,
    val isNotFound: Boolean = false
)

sealed interface SessionHubEvent {
    data object SessionFinished : SessionHubEvent
    data class LaunchGame(val gameId: String, val activityId: Long) : SessionHubEvent
    data class Message(val text: String) : SessionHubEvent
}

class SessionHubViewModel(
    val sessionId: Long,
    private val sessionRepository: TherapySessionRepository,
    private val sessionManager: SessionManager,
    private val documentManager: DocumentManager,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val clock: Clock
) : ViewModel() {


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

    fun startPlannedOrPausedSession() {
        viewModelScope.launch {
            val res = sessionManager.startPlannedSession(sessionId)
            if (res is DomainResult.Success) {
                _event.emit(SessionHubEvent.Message("Sessão iniciada!"))
            } else {
                _event.emit(SessionHubEvent.Message("Não foi possível iniciar: verifique se há outra sessão aberta."))
            }
        }
    }

    fun pauseSession() {
        viewModelScope.launch {
            sessionManager.pauseSession(sessionId)
            _event.emit(SessionHubEvent.Message("Sessão pausada com segurança."))
        }
    }

    fun addGameActivity(gameId: String, launchImmediately: Boolean = true) {
        viewModelScope.launch {
            val now = clock.instant()
            val activityId = sessionRepository.addActivity(
                sessionId = sessionId,
                gameId = gameId,
                linkedGoalId = _uiState.value.details?.linkedGoals?.firstOrNull()?.id,
                configurationJson = "{}",
                orderIndex = (_uiState.value.details?.activities?.size ?: 0) + 1,
                now = now
            )
            if (launchImmediately) {
                _event.emit(SessionHubEvent.LaunchGame(gameId, activityId))
            }
        }
    }

    fun finishSession(notes: String, soapSubjective: String = "", soapPlan: String = "") {
        viewModelScope.launch {
            val details = _uiState.value.details
            val res = finishSessionUseCase(sessionId, notes, "Sessão concluída (${details?.activities?.size ?: 0} atividades).")
            if (res is DomainResult.Success) {
                if (details != null && (notes.isNotBlank() || soapSubjective.isNotBlank() || soapPlan.isNotBlank())) {
                    documentManager.createDocument(
                        patientId = details.session.patientId,
                        sessionId = sessionId,
                        category = DocumentCategory.SOAP_EVOLUTION,
                        title = "Evolução Clínica — Sessão #$sessionId",
                        professionalName = "Dra. Mariana Fonoaudióloga",
                        professionalCrfa = "CRFa 2-12345",
                        soapSubjective = soapSubjective,
                        soapObjective = "Realizadas ${details.activities.size} atividade(s) terapêutica(s).",
                        soapAssessment = notes,
                        soapPlan = soapPlan,
                        bodyContent = "",
                        lockAndSign = true
                    )
                }
                _event.emit(SessionHubEvent.SessionFinished)
            }
        }
    }
}
