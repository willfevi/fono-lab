package com.healthcare.fonolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "patients",
    indices = [
        Index(value = ["full_name"]),
        Index(value = ["status"]),
        Index(value = ["internal_code"], unique = true)
    ]
)
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "internal_code")
    val internalCode: String,

    @ColumnInfo(name = "full_name")
    val fullName: String,

    @ColumnInfo(name = "birth_date")
    val birthDate: LocalDate,

    @ColumnInfo(name = "guardian_name")
    val guardianName: String?,

    @ColumnInfo(name = "guardian_contact")
    val guardianContact: String?,

    @ColumnInfo(name = "follow_up_start_date")
    val followUpStartDate: LocalDate,

    @ColumnInfo(name = "clinical_notes")
    val clinicalNotes: String,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "is_demo")
    val isDemo: Boolean,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
