package com.healthcare.fonolab.domain.session

import com.healthcare.fonolab.domain.goal.ClinicalGoal
import java.time.Instant

enum class SessionStatus(val displayName: String) {
    DRAFT("Rascunho"),
    PLANNED("Planejada"),
    IN_PROGRESS("Em andamento"),
    PAUSED("Pausada"),
    COMPLETED("Concluída"),
    CANCELLED("Cancelada")
}

enum class ActivityStatus(val displayName: String) {
    PENDING("Pendente"),
    IN_PROGRESS("Em execução"),
    COMPLETED("Concluída"),
    ABANDONED("Pulada")
}

data class TherapySession(
    val id: Long,
    val patientId: Long,
    val status: SessionStatus,
    val scheduledAt: Instant,
    val startedAt: Instant?,
    val endedAt: Instant?,
    val durationSeconds: Long,
    val clinicalNotes: String,
    val clinicalSummary: String,
    val isDemo: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class SessionActivity(
    val id: Long,
    val sessionId: Long,
    val gameId: String,
    val linkedGoalId: Long?,
    val configurationJson: String,
    val stateSnapshotJson: String?,
    val status: ActivityStatus,
    val orderIndex: Int,
    val startedAt: Instant?,
    val endedAt: Instant?
)

data class SessionWithDetails(
    val session: TherapySession,
    val linkedGoals: List<ClinicalGoal>,
    val activities: List<SessionActivity>
)
