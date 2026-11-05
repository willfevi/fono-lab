package com.healthcare.fonolab.data.repository

import com.healthcare.fonolab.data.local.dao.ClinicalGoalDao
import com.healthcare.fonolab.data.local.dao.TherapySessionDao
import com.healthcare.fonolab.data.local.entity.GameAttemptEntity
import com.healthcare.fonolab.data.local.entity.SessionActivityEntity
import com.healthcare.fonolab.data.local.entity.SessionGoalCrossRef
import com.healthcare.fonolab.data.local.entity.TherapySessionEntity
import com.healthcare.fonolab.data.repository.ClinicalGoalRepositoryImpl.Companion.toDomain
import com.healthcare.fonolab.domain.performance.AttemptEvaluation
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.session.ActivityStatus
import com.healthcare.fonolab.domain.session.SessionActivity
import com.healthcare.fonolab.domain.session.SessionStatus
import com.healthcare.fonolab.domain.session.SessionWithDetails
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Instant

class TherapySessionRepositoryImpl(
    private val sessionDao: TherapySessionDao,
    private val goalDao: ClinicalGoalDao
) : TherapySessionRepository {

    override fun observeSessionsForPatient(patientId: Long): Flow<List<TherapySession>> {
        return sessionDao.observeByPatient(patientId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeRecentSessions(limit: Int): Flow<List<TherapySession>> {
        return sessionDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeActiveSession(): Flow<SessionWithDetails?> {
        return sessionDao.observeActiveSession().flatMapLatest { entity ->
            if (entity == null) flowOf(null)
            else observeSession(entity.id)
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun observeSession(sessionId: Long): Flow<SessionWithDetails?> {
        return sessionDao.observeById(sessionId).flatMapLatest { sessionEntity ->
            if (sessionEntity == null) return@flatMapLatest flowOf(null)
            sessionDao.observeActivitiesForSession(sessionId).map { activities ->
                val linkedGoalIds = sessionDao.getLinkedGoalIds(sessionId)
                val goals = if (linkedGoalIds.isNotEmpty()) goalDao.getByIds(linkedGoalIds) else emptyList()
                SessionWithDetails(
                    session = sessionEntity.toDomain(),
                    linkedGoals = goals.map { it.toDomain() },
                    activities = activities.map { it.toDomain() }
                )
            }
        }
    }

    override fun observeCompletedSessionCount(since: Instant?): Flow<Int> {
        return sessionDao.observeCompletedCount(since)
    }

    override fun observePlannedSessionCount(): Flow<Int> {
        return sessionDao.observePlannedCount()
    }

    override suspend fun getSessionWithDetails(sessionId: Long): SessionWithDetails? {
        val entity = sessionDao.getById(sessionId) ?: return null
        val linkedGoalIds = sessionDao.getLinkedGoalIds(sessionId)
        val goals = if (linkedGoalIds.isNotEmpty()) goalDao.getByIds(linkedGoalIds) else emptyList()
        val activities = sessionDao.getActivitiesForSession(sessionId)
        return SessionWithDetails(
            session = entity.toDomain(),
            linkedGoals = goals.map { it.toDomain() },
            activities = activities.map { it.toDomain() }
        )
    }

    override suspend fun getActiveSession(): SessionWithDetails? {
        val entity = sessionDao.getActiveSession() ?: return null
        return getSessionWithDetails(entity.id)
    }

    override suspend fun createSession(
        patientId: Long,
        scheduledAt: Instant,
        status: SessionStatus,
        linkedGoalIds: List<Long>,
        isDemo: Boolean,
        now: Instant
    ): Long {
        val entity = TherapySessionEntity(
            patientId = patientId,
            status = status.name,
            scheduledAt = scheduledAt,
            startedAt = if (status == SessionStatus.IN_PROGRESS) now else null,
            endedAt = null,
            durationSeconds = 0,
            clinicalNotes = "",
            clinicalSummary = "",
            isDemo = isDemo,
            createdAt = now,
            updatedAt = now
        )
        val sessionId = sessionDao.insertSession(entity)
        if (linkedGoalIds.isNotEmpty()) {
            val refs = linkedGoalIds.map { SessionGoalCrossRef(sessionId, it) }
            sessionDao.insertGoalCrossRefs(refs)
        }
        return sessionId
    }

    override suspend fun updateSessionStatus(
        sessionId: Long,
        status: SessionStatus,
        startedAt: Instant?,
        endedAt: Instant?,
        durationSeconds: Long,
        now: Instant
    ) {
        sessionDao.updateSessionStatus(sessionId, status.name, startedAt, endedAt, durationSeconds, now)
    }

    override suspend fun updateSessionNotes(
        sessionId: Long,
        notes: String,
        summary: String,
        now: Instant
    ) {
        sessionDao.updateSessionNotes(sessionId, notes, summary, now)
    }

    override suspend fun addActivity(
        sessionId: Long,
        gameId: String,
        linkedGoalId: Long?,
        configurationJson: String,
        orderIndex: Int,
        now: Instant
    ): Long {
        val entity = SessionActivityEntity(
            sessionId = sessionId,
            gameId = gameId,
            linkedGoalId = linkedGoalId,
            configurationJson = configurationJson,
            stateSnapshotJson = null,
            status = ActivityStatus.PENDING.name,
            orderIndex = orderIndex,
            startedAt = null,
            endedAt = null
        )
        return sessionDao.insertActivity(entity)
    }

    override suspend fun updateActivityStatus(
        activityId: Long,
        status: ActivityStatus,
        snapshotJson: String?,
        startedAt: Instant?,
        endedAt: Instant?
    ) {
        sessionDao.updateActivityStatus(activityId, status.name, snapshotJson, startedAt, endedAt)
    }

    override suspend fun recordAttempt(attempt: GameAttempt): Long {
        val evalType = when (attempt.evaluation) {
            is AttemptEvaluation.AutoScored -> "AUTO"
            is AttemptEvaluation.ClinicianRated -> "CLINICIAN"
            AttemptEvaluation.NotEvaluated -> "NONE"
        }
        val autoCorrect = (attempt.evaluation as? AttemptEvaluation.AutoScored)?.isCorrect
        val clinicianRating = (attempt.evaluation as? AttemptEvaluation.ClinicianRated)?.rating?.name

        val entity = GameAttemptEntity(
            activityId = attempt.activityId,
            roundIndex = attempt.roundIndex,
            attemptNumber = attempt.attemptNumber,
            stimulusTarget = attempt.stimulusTarget,
            stimulusPresentedJson = attempt.stimulusPresentedJson,
            responseRecorded = attempt.responseRecorded,
            evaluationType = evalType,
            autoIsCorrect = autoCorrect,
            clinicianRating = clinicianRating,
            cueLevel = attempt.cueLevel.name,
            responseTimeMs = attempt.responseTimeMs,
            clinicianNotes = attempt.clinicianNotes,
            timestamp = attempt.timestamp
        )
        return sessionDao.insertAttempt(entity)
    }

    override suspend fun getAttemptsForActivity(activityId: Long): List<GameAttempt> {
        return sessionDao.getAttemptsForActivity(activityId).map { it.toDomain() }
    }

    override suspend fun getAttemptsForPatient(patientId: Long): List<GameAttempt> {
        return sessionDao.getAttemptsForPatient(patientId).map { it.toDomain() }
    }

    private fun TherapySessionEntity.toDomain(): TherapySession {
        return TherapySession(
            id = id,
            patientId = patientId,
            status = SessionStatus.valueOf(status),
            scheduledAt = scheduledAt,
            startedAt = startedAt,
            endedAt = endedAt,
            durationSeconds = durationSeconds,
            clinicalNotes = clinicalNotes,
            clinicalSummary = clinicalSummary,
            isDemo = isDemo,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun SessionActivityEntity.toDomain(): SessionActivity {
        return SessionActivity(
            id = id,
            sessionId = sessionId,
            gameId = gameId,
            linkedGoalId = linkedGoalId,
            configurationJson = configurationJson,
            stateSnapshotJson = stateSnapshotJson,
            status = ActivityStatus.valueOf(status),
            orderIndex = orderIndex,
            startedAt = startedAt,
            endedAt = endedAt
        )
    }

    private fun GameAttemptEntity.toDomain(): GameAttempt {
        val evaluation = when (evaluationType) {
            "AUTO" -> AttemptEvaluation.AutoScored(autoIsCorrect ?: false)
            "CLINICIAN" -> AttemptEvaluation.ClinicianRated(
                clinicianRating?.let { ClinicianRating.valueOf(it) } ?: ClinicianRating.NEEDS_REVIEW
            )
            else -> AttemptEvaluation.NotEvaluated
        }
        return GameAttempt(
            id = id,
            activityId = activityId,
            roundIndex = roundIndex,
            attemptNumber = attemptNumber,
            stimulusTarget = stimulusTarget,
            stimulusPresentedJson = stimulusPresentedJson,
            responseRecorded = responseRecorded,
            evaluation = evaluation,
            cueLevel = CueLevel.valueOf(cueLevel),
            responseTimeMs = responseTimeMs,
            clinicianNotes = clinicianNotes,
            timestamp = timestamp
        )
    }
}
