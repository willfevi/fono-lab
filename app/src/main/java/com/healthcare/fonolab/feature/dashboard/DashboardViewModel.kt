package com.healthcare.fonolab.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.SessionWithDetails
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val totalActivePatients: Int = 0,
    val completedSessionsCount: Int = 0,
    val plannedSessionsCount: Int = 0,
    val activeSession: SessionWithDetails? = null,
    val recentSessions: List<TherapySession> = emptyList(),
    val recentPatients: List<Patient> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val sessionRepository: TherapySessionRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            patientRepository.observeActivePatientCount(includeDemo = true),
            sessionRepository.observeCompletedSessionCount(null),
            sessionRepository.observePlannedSessionCount()
        ) { active, completed, planned -> Triple(active, completed, planned) },
        sessionRepository.observeActiveSession(),
        sessionRepository.observeRecentSessions(5),
        patientRepository.observePatients(PatientFilter(status = PatientStatus.ACTIVE))
    ) { (activeCount, completedCount, plannedCount), activeSession, recentSessions, activePatients ->
        DashboardUiState(
            totalActivePatients = activeCount,
            completedSessionsCount = completedCount,
            plannedSessionsCount = plannedCount,
            activeSession = activeSession,
            recentSessions = recentSessions,
            recentPatients = activePatients.take(5),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
