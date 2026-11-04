package com.healthcare.fonolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.healthcare.fonolab.data.local.entity.GameAttemptEntity
import com.healthcare.fonolab.data.local.entity.SessionActivityEntity
import com.healthcare.fonolab.data.local.entity.SessionGoalCrossRef
import com.healthcare.fonolab.data.local.entity.TherapySessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TherapySessionDao {

    @Query("SELECT * FROM therapy_sessions WHERE patient_id = :patientId ORDER BY scheduled_at DESC")
    fun observeByPatient(patientId: Long): Flow<List<TherapySessionEntity>>

    @Query("SELECT * FROM therapy_sessions ORDER BY scheduled_at DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<TherapySessionEntity>>

    @Query("SELECT * FROM therapy_sessions WHERE status = 'IN_PROGRESS' ORDER BY scheduled_at DESC LIMIT 1")
    fun observeActiveSession(): Flow<TherapySessionEntity?>

    @Query("SELECT * FROM therapy_sessions WHERE status = 'IN_PROGRESS' ORDER BY scheduled_at DESC LIMIT 1")
    suspend fun getActiveSession(): TherapySessionEntity?

    @Query("SELECT * FROM therapy_sessions WHERE id = :id")
    fun observeById(id: Long): Flow<TherapySessionEntity?>

    @Query("SELECT * FROM therapy_sessions WHERE id = :id")
    suspend fun getById(id: Long): TherapySessionEntity?

    @Query("SELECT COUNT(*) FROM therapy_sessions WHERE status = 'COMPLETED' AND (:since IS NULL OR ended_at >= :since)")
    fun observeCompletedCount(since: Instant?): Flow<Int>

    @Query("SELECT COUNT(*) FROM therapy_sessions WHERE status = 'PLANNED'")
    fun observePlannedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(entity: TherapySessionEntity): Long

    @Query("UPDATE therapy_sessions SET status = :status, started_at = :startedAt, ended_at = :endedAt, duration_seconds = :durationSeconds, updated_at = :now WHERE id = :sessionId")
    suspend fun updateSessionStatus(
        sessionId: Long,
        status: String,
        startedAt: Instant?,
        endedAt: Instant?,
        durationSeconds: Long,
        now: Instant
    )

    @Query("UPDATE therapy_sessions SET clinical_notes = :notes, clinical_summary = :summary, updated_at = :now WHERE id = :sessionId")
    suspend fun updateSessionNotes(sessionId: Long, notes: String, summary: String, now: Instant)

    // Goal Cross Refs
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalCrossRefs(refs: List<SessionGoalCrossRef>)

    @Query("SELECT goal_id FROM session_goal_cross_ref WHERE session_id = :sessionId")
    suspend fun getLinkedGoalIds(sessionId: Long): List<Long>

    // Activities
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActivity(entity: SessionActivityEntity): Long

    @Query("SELECT * FROM session_activities WHERE session_id = :sessionId ORDER BY order_index ASC")
    fun observeActivitiesForSession(sessionId: Long): Flow<List<SessionActivityEntity>>

    @Query("SELECT * FROM session_activities WHERE session_id = :sessionId ORDER BY order_index ASC")
    suspend fun getActivitiesForSession(sessionId: Long): List<SessionActivityEntity>

    @Query("SELECT * FROM session_activities WHERE id = :id")
    suspend fun getActivityById(id: Long): SessionActivityEntity?

    @Query("UPDATE session_activities SET status = :status, state_snapshot_json = :snapshotJson, started_at = :startedAt, ended_at = :endedAt WHERE id = :activityId")
    suspend fun updateActivityStatus(
        activityId: Long,
        status: String,
        snapshotJson: String?,
        startedAt: Instant?,
        endedAt: Instant?
    )

    // Attempts
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAttempt(entity: GameAttemptEntity): Long

    @Query("SELECT * FROM game_attempts WHERE activity_id = :activityId ORDER BY round_index ASC, attempt_number ASC")
    suspend fun getAttemptsForActivity(activityId: Long): List<GameAttemptEntity>

    @Query("SELECT a.* FROM game_attempts a INNER JOIN session_activities sa ON a.activity_id = sa.id INNER JOIN therapy_sessions s ON sa.session_id = s.id WHERE s.patient_id = :patientId ORDER BY a.timestamp ASC")
    suspend fun getAttemptsForPatient(patientId: Long): List<GameAttemptEntity>
}
