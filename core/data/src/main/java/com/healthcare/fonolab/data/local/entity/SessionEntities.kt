package com.healthcare.fonolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "therapy_sessions",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["patient_id"]),
        Index(value = ["status"]),
        Index(value = ["scheduled_at"])
    ]
)
data class TherapySessionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Instant,

    @ColumnInfo(name = "started_at")
    val startedAt: Instant?,

    @ColumnInfo(name = "ended_at")
    val endedAt: Instant?,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Long,

    @ColumnInfo(name = "clinical_notes")
    val clinicalNotes: String,

    @ColumnInfo(name = "clinical_summary")
    val clinicalSummary: String,

    @ColumnInfo(name = "is_demo")
    val isDemo: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)

@Entity(
    tableName = "session_goal_cross_ref",
    primaryKeys = ["session_id", "goal_id"],
    foreignKeys = [
        ForeignKey(
            entity = TherapySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClinicalGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goal_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["goal_id"])
    ]
)
data class SessionGoalCrossRef(
    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "goal_id")
    val goalId: Long
)

@Entity(
    tableName = "session_activities",
    foreignKeys = [
        ForeignKey(
            entity = TherapySessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ClinicalGoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["linked_goal_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["linked_goal_id"])
    ]
)
data class SessionActivityEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "session_id")
    val sessionId: Long,

    @ColumnInfo(name = "game_id")
    val gameId: String,

    @ColumnInfo(name = "linked_goal_id")
    val linkedGoalId: Long?,

    @ColumnInfo(name = "configuration_json")
    val configurationJson: String,

    @ColumnInfo(name = "state_snapshot_json")
    val stateSnapshotJson: String?,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int,

    @ColumnInfo(name = "started_at")
    val startedAt: Instant?,

    @ColumnInfo(name = "ended_at")
    val endedAt: Instant?
)

@Entity(
    tableName = "game_attempts",
    foreignKeys = [
        ForeignKey(
            entity = SessionActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["activity_id"]),
        Index(value = ["evaluation_type"])
    ]
)
data class GameAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "activity_id")
    val activityId: Long,

    @ColumnInfo(name = "round_index")
    val roundIndex: Int,

    @ColumnInfo(name = "attempt_number")
    val attemptNumber: Int,

    @ColumnInfo(name = "stimulus_target")
    val stimulusTarget: String,

    @ColumnInfo(name = "stimulus_presented_json")
    val stimulusPresentedJson: String,

    @ColumnInfo(name = "response_recorded")
    val responseRecorded: String,

    @ColumnInfo(name = "evaluation_type")
    val evaluationType: String, // AUTO, CLINICIAN, NONE

    @ColumnInfo(name = "auto_is_correct")
    val autoIsCorrect: Boolean?,

    @ColumnInfo(name = "clinician_rating")
    val clinicianRating: String?,

    @ColumnInfo(name = "cue_level")
    val cueLevel: String,

    @ColumnInfo(name = "response_time_ms")
    val responseTimeMs: Long?,

    @ColumnInfo(name = "clinician_notes")
    val clinicianNotes: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Instant
)
