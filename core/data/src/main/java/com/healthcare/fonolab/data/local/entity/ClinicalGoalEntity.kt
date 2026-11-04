package com.healthcare.fonolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "clinical_goals",
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
        Index(value = ["status"])
    ]
)
data class ClinicalGoalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "clinical_area")
    val clinicalArea: String,

    @ColumnInfo(name = "custom_area_name")
    val customAreaName: String?,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "target_skill")
    val targetSkill: String,

    @ColumnInfo(name = "target_stimulus")
    val targetStimulus: String?,

    @ColumnInfo(name = "phoneme_position")
    val phonemePosition: String?,

    @ColumnInfo(name = "initial_difficulty")
    val initialDifficulty: String,

    @ColumnInfo(name = "target_accuracy_percentage")
    val targetAccuracyPercentage: Int?,

    @ColumnInfo(name = "mastery_criterion_description")
    val masteryCriterionDescription: String?,

    @ColumnInfo(name = "clinical_notes")
    val clinicalNotes: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
