package com.healthcare.fonolab.feature.patientdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.goal.ClinicalGoal
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.patient.ChangePatientStatusUseCase
import com.healthcare.fonolab.domain.patient.DeletePatientUseCase
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientDeletionImpact
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.StartSessionUseCase
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientDetailsUiState(
    val patient: Patient? = null,
    val goals: List<ClinicalGoal> = emptyList(),
    val sessions: List<TherapySession> = emptyList(),
    val deletionImpact: PatientDeletionImpact? = null,
    val isLoading: Boolean = true,
    val isStartingSession: Boolean = false
)

sealed interface PatientDetailsEvent {
    data object PatientDeleted : PatientDetailsEvent
    data class SessionStarted(val sessionId: Long) : PatientDetailsEvent
    data class Error(val message: String) : PatientDetailsEvent
}

@HiltViewModel
class PatientDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val patientRepository: PatientRepository,
    private val goalRepository: ClinicalGoalRepository,
    private val sessionRepository: TherapySessionRepository,
    private val startSessionUseCase: StartSessionUseCase,
    private val changeStatusUseCase: ChangePatientStatusUseCase,
    private val deletePatientUseCase: DeletePatientUseCase
) : ViewModel() {

    val patientId: Long = savedStateHandle.get<Long>("patientId") ?: 0L

    private val _event = MutableSharedFlow<PatientDetailsEvent>()
    val event: SharedFlow<PatientDetailsEvent> = _event.asSharedFlow()

    val uiState: StateFlow<PatientDetailsUiState> = combine(
        patientRepository.observePatient(patientId),
        goalRepository.observeGoalsForPatient(patientId),
        sessionRepository.observeSessionsForPatient(patientId)
    ) { patient, goals, sessions ->
        PatientDetailsUiState(
            patient = patient,
            goals = goals,
            sessions = sessions,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PatientDetailsUiState()
    )

    fun togglePatientStatus() {
        val current = uiState.value.patient ?: return
        val newStatus = if (current.status == PatientStatus.ACTIVE) PatientStatus.ARCHIVED else PatientStatus.ACTIVE
        viewModelScope.launch {
            changeStatusUseCase(patientId, newStatus)
        }
    }

    fun startSession() {
        val currentPatient = uiState.value.patient ?: return
        val goals = uiState.value.goals
        viewModelScope.launch {
            // First check if an active session already exists
            val active = sessionRepository.getActiveSession()
            if (active != null) {
                // If the active session is for this patient, resume it immediately
                if (active.session.patientId == patientId) {
                    _event.emit(PatientDetailsEvent.SessionStarted(active.session.id))
                    return@launch
                } else {
                    _event.emit(PatientDetailsEvent.Error("Já existe uma sessão ativa em andamento com outro paciente. Finalize-a antes de iniciar uma nova."))
                    return@launch
                }
            }

            // Otherwise, start a new session
            val res = startSessionUseCase(
                patientId = patientId,
                linkedGoalIds = goals.map { it.id },
                isDemo = currentPatient.isDemo
            )
            when (res) {
                is DomainResult.Success -> {
                    _event.emit(PatientDetailsEvent.SessionStarted(res.value))
                }
                is DomainResult.Failure -> {
                    _event.emit(PatientDetailsEvent.Error("Não foi possível iniciar a sessão."))
                }
            }
        }
    }

    fun deletePatient() {
        viewModelScope.launch {
            val result = deletePatientUseCase(patientId)
            if (result is DomainResult.Success) {
                _event.emit(PatientDetailsEvent.PatientDeleted)
            } else {
                _event.emit(PatientDetailsEvent.Error("Erro ao excluir paciente."))
            }
        }
    }
}
