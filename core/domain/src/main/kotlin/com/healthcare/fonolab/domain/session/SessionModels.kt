package com.healthcare.fonolab.domain.session

import com.healthcare.fonolab.domain.goal.ClinicalGoal
import java.time.Instant

enum class SessionStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

enum class ActivityStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    ABANDONED
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
