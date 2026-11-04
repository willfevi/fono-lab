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
    val updatedAt: Instant,

    // Extended fields
    @ColumnInfo(name = "social_name", defaultValue = "")
    val socialName: String? = null,

    @ColumnInfo(name = "phone", defaultValue = "")
    val phone: String? = null,

    @ColumnInfo(name = "email", defaultValue = "")
    val email: String? = null,

    @ColumnInfo(name = "address", defaultValue = "")
    val address: String? = null,

    @ColumnInfo(name = "guardian_kinship", defaultValue = "")
    val guardianKinship: String? = null,

    @ColumnInfo(name = "emergency_contact", defaultValue = "")
    val emergencyContact: String? = null,

    @ColumnInfo(name = "school", defaultValue = "")
    val school: String? = null,

    @ColumnInfo(name = "education_level", defaultValue = "")
    val educationLevel: String? = null,

    @ColumnInfo(name = "communication_modalities", defaultValue = "Fala oral")
    val communicationModalities: String = "Fala oral",

    @ColumnInfo(name = "sensory_preferences", defaultValue = "")
    val sensoryPreferences: String = "",

    @ColumnInfo(name = "effective_reinforcers", defaultValue = "")
    val effectiveReinforcers: String = "",

    @ColumnInfo(name = "home_languages", defaultValue = "Português Brasileiro")
    val homeLanguages: String = "Português Brasileiro",

    @ColumnInfo(name = "school_language", defaultValue = "Português Brasileiro")
    val schoolLanguage: String = "Português Brasileiro",

    @ColumnInfo(name = "therapy_language", defaultValue = "Português Brasileiro")
    val therapyLanguage: String = "Português Brasileiro",

    @ColumnInfo(name = "low_stimulation_mode", defaultValue = "0")
    val lowStimulationMode: Boolean = false
)
