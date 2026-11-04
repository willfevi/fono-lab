package com.healthcare.fonolab.domain.session

import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.EntityType
import com.healthcare.fonolab.domain.common.InvalidStateReason
import com.healthcare.fonolab.domain.common.storageCall
import com.healthcare.fonolab.domain.performance.GameAttempt
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

interface TherapySessionRepository {
    fun observeSessionsForPatient(patientId: Long): Flow<List<TherapySession>>
    fun observeRecentSessions(limit: Int): Flow<List<TherapySession>>
    fun observeActiveSession(): Flow<SessionWithDetails?>
    fun observeSession(sessionId: Long): Flow<SessionWithDetails?>
    fun observeCompletedSessionCount(since: Instant?): Flow<Int>
    fun observePlannedSessionCount(): Flow<Int>

    suspend fun getSessionWithDetails(sessionId: Long): SessionWithDetails?
    suspend fun getActiveSession(): SessionWithDetails?

    suspend fun createSession(
        patientId: Long,
        scheduledAt: Instant,
        status: SessionStatus,
        linkedGoalIds: List<Long>,
        isDemo: Boolean,
        now: Instant
    ): Long

    suspend fun updateSessionStatus(
        sessionId: Long,
        status: SessionStatus,
        startedAt: Instant?,
        endedAt: Instant?,
        durationSeconds: Long,
        now: Instant
    )

    suspend fun updateSessionNotes(
        sessionId: Long,
        notes: String,
        summary: String,
        now: Instant
    )

    suspend fun addActivity(
        sessionId: Long,
        gameId: String,
        linkedGoalId: Long?,
        configurationJson: String,
        orderIndex: Int,
        now: Instant
    ): Long

    suspend fun updateActivityStatus(
        activityId: Long,
        status: ActivityStatus,
        snapshotJson: String?,
        startedAt: Instant?,
        endedAt: Instant?
    )

    suspend fun recordAttempt(attempt: GameAttempt): Long
    suspend fun getAttemptsForActivity(activityId: Long): List<GameAttempt>
    suspend fun getAttemptsForPatient(patientId: Long): List<GameAttempt>
}

/**
 * Creates a planned or draft session in advance.
 * CRITICAL RULE: A planned session NEVER starts automatically upon creation and NEVER counts
 * as a completed clinical session.
 */
class PlanSessionUseCase @Inject constructor(
    private val repository: TherapySessionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        patientId: Long,
        scheduledAt: Instant,
        linkedGoalIds: List<Long> = emptyList(),
        plannedGameIds: List<String> = emptyList(),
        notes: String = "",
        summary: String = "",
        isDraft: Boolean = false,
        isDemo: Boolean = false
    ): DomainResult<Long> {
        val now = clock.instant()
        val initialStatus = if (isDraft) SessionStatus.DRAFT else SessionStatus.PLANNED
        return storageCall {
            val sessionId = repository.createSession(
                patientId = patientId,
                scheduledAt = scheduledAt,
                status = initialStatus,
                linkedGoalIds = linkedGoalIds,
                isDemo = isDemo,
                now = now
            )
            if (notes.isNotBlank() || summary.isNotBlank()) {
                repository.updateSessionNotes(sessionId, notes, summary, now)
            }
            plannedGameIds.forEachIndexed { index, gameId ->
                repository.addActivity(
                    sessionId = sessionId,
                    gameId = gameId,
                    linkedGoalId = linkedGoalIds.firstOrNull(),
                    configurationJson = "{}",
                    orderIndex = index + 1,
                    now = now
                )
            }
            sessionId
        }
    }
}

/**
 * Transitions a PLANNED, DRAFT, or PAUSED session to IN_PROGRESS only when the clinician explicitly starts it.
 */
class StartPlannedSessionUseCase @Inject constructor(
    private val repository: TherapySessionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(sessionId: Long): DomainResult<Long> {
        val active = repository.getActiveSession()
        if (active != null && active.session.id != sessionId) {
            return DomainResult.Failure(DomainError.InvalidState(InvalidStateReason.SESSION_ALREADY_IN_PROGRESS))
        }
        val details = repository.getSessionWithDetails(sessionId)
            ?: return DomainResult.Failure(DomainError.NotFound(EntityType.SESSION))

        if (details.session.status == SessionStatus.COMPLETED || details.session.status == SessionStatus.CANCELLED) {
            return DomainResult.Failure(DomainError.InvalidState(InvalidStateReason.SESSION_ALREADY_CLOSED))
        }

        val now = clock.instant()
        return storageCall {
            repository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.IN_PROGRESS,
                startedAt = details.session.startedAt ?: now,
                endedAt = null,
                durationSeconds = details.session.durationSeconds,
                now = now
            )
            sessionId
        }
    }
}

class PauseSessionUseCase @Inject constructor(
    private val repository: TherapySessionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(sessionId: Long): DomainResult<Unit> {
        val details = repository.getSessionWithDetails(sessionId)
            ?: return DomainResult.Failure(DomainError.NotFound(EntityType.SESSION))

        if (details.session.status != SessionStatus.IN_PROGRESS) {
            return DomainResult.Failure(DomainError.InvalidState(InvalidStateReason.SESSION_NOT_IN_PROGRESS))
        }

        val now = clock.instant()
        val started = details.session.startedAt ?: now
        val elapsed = maxOf(0L, java.time.Duration.between(started, now).seconds)

        return storageCall {
            repository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.PAUSED,
                startedAt = started,
                endedAt = null,
                durationSeconds = maxOf(details.session.durationSeconds, elapsed),
                now = now
            )
        }
    }
}

class StartSessionUseCase @Inject constructor(
    private val repository: TherapySessionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        patientId: Long,
        linkedGoalIds: List<Long>,
        isDemo: Boolean = false
    ): DomainResult<Long> {
        val active = repository.getActiveSession()
        if (active != null) {
            return DomainResult.Failure(DomainError.InvalidState(InvalidStateReason.SESSION_ALREADY_IN_PROGRESS))
        }

        val now = clock.instant()
        return storageCall {
            val sessionId = repository.createSession(
                patientId = patientId,
                scheduledAt = now,
                status = SessionStatus.IN_PROGRESS,
                linkedGoalIds = linkedGoalIds,
                isDemo = isDemo,
                now = now
            )
            repository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.IN_PROGRESS,
                startedAt = now,
                endedAt = null,
                durationSeconds = 0,
                now = now
            )
            sessionId
        }
    }
}

class FinishSessionUseCase @Inject constructor(
    private val repository: TherapySessionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        sessionId: Long,
        clinicalNotes: String,
        clinicalSummary: String
    ): DomainResult<Unit> {
        val details = repository.getSessionWithDetails(sessionId)
            ?: return DomainResult.Failure(DomainError.NotFound(EntityType.SESSION))

        if (details.session.status != SessionStatus.IN_PROGRESS && details.session.status != SessionStatus.PAUSED) {
            return DomainResult.Failure(DomainError.InvalidState(InvalidStateReason.SESSION_NOT_IN_PROGRESS))
        }

        val now = clock.instant()
        val started = details.session.startedAt ?: now
        val duration = maxOf(details.session.durationSeconds, java.time.Duration.between(started, now).seconds)

        return storageCall {
            repository.updateSessionNotes(sessionId, clinicalNotes, clinicalSummary, now)
            repository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.COMPLETED,
                startedAt = started,
                endedAt = now,
                durationSeconds = duration,
                now = now
            )
        }
    }
}

class CancelSessionUseCase @Inject constructor(
    private val repository: TherapySessionRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(sessionId: Long): DomainResult<Unit> {
        val details = repository.getSessionWithDetails(sessionId)
            ?: return DomainResult.Failure(DomainError.NotFound(EntityType.SESSION))

        if (details.session.status == SessionStatus.COMPLETED || details.session.status == SessionStatus.CANCELLED) {
            return DomainResult.Failure(DomainError.InvalidState(InvalidStateReason.SESSION_ALREADY_CLOSED))
        }

        val now = clock.instant()
        return storageCall {
            repository.updateSessionStatus(
                sessionId = sessionId,
                status = SessionStatus.CANCELLED,
                startedAt = details.session.startedAt,
                endedAt = now,
                durationSeconds = details.session.durationSeconds,
                now = now
            )
        }
    }
}
