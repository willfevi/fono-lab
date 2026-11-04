package com.healthcare.fonolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "anamnesis_records",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"], unique = true)]
)
data class AnamnesisEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "informant_name") val informantName: String,
    @ColumnInfo(name = "informant_kinship") val informantKinship: String,
    @ColumnInfo(name = "referred_by") val referredBy: String,
    @ColumnInfo(name = "anamnesis_date") val anamnesisDate: LocalDate,
    @ColumnInfo(name = "sections_payload") val sectionsPayload: String,
    @ColumnInfo(name = "general_summary") val generalSummary: String,
    @ColumnInfo(name = "version_number") val versionNumber: Int,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)

@Entity(
    tableName = "anamnesis_versions",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"]), Index(value = ["anamnesis_id"])]
)
data class AnamnesisVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "anamnesis_id") val anamnesisId: Long,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "version_number") val versionNumber: Int,
    @ColumnInfo(name = "snapshot_json") val snapshotJson: String,
    @ColumnInfo(name = "change_summary") val changeSummary: String,
    @ColumnInfo(name = "saved_at") val savedAt: Instant
)

@Entity(
    tableName = "clinical_assessments",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class ClinicalAssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "clinical_area") val clinicalArea: String,
    @ColumnInfo(name = "protocol_or_instrument") val protocolOrInstrument: String,
    @ColumnInfo(name = "assessment_date") val assessmentDate: LocalDate,
    @ColumnInfo(name = "baseline_score") val baselineScorePercentage: Int?,
    @ColumnInfo(name = "current_score") val currentScorePercentage: Int?,
    @ColumnInfo(name = "findings_summary") val findingsSummary: String,
    @ColumnInfo(name = "clinical_hypothesis") val clinicalHypothesis: String,
    @ColumnInfo(name = "recommendations") val recommendations: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)

@Entity(
    tableName = "phoneme_inventory_entries",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id", "phoneme", "position"], unique = true)]
)
data class PhonemeInventoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "phoneme") val phoneme: String,
    @ColumnInfo(name = "position") val position: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "error_type") val errorType: String,
    @ColumnInfo(name = "is_stimulable") val isStimulable: Boolean,
    @ColumnInfo(name = "best_cue_level") val bestCueLevel: String,
    @ColumnInfo(name = "sample_words") val sampleWords: String,
    @ColumnInfo(name = "clinician_notes") val clinicianNotes: String,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)

@Entity(
    tableName = "therapy_plans",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class TherapyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "clinical_area") val clinicalArea: String,
    @ColumnInfo(name = "general_objective") val generalObjective: String,
    @ColumnInfo(name = "specific_objectives") val specificObjectives: String,
    @ColumnInfo(name = "target_skills") val targetSkillsOrPhonemes: String,
    @ColumnInfo(name = "weekly_frequency") val weeklyFrequency: Int,
    @ColumnInfo(name = "session_duration_minutes") val sessionDurationMinutes: Int,
    @ColumnInfo(name = "start_date") val startDate: LocalDate,
    @ColumnInfo(name = "expected_end_date") val expectedEndDate: LocalDate?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "clinical_notes") val clinicalNotes: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)

@Entity(
    tableName = "generalization_probes",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class GeneralizationProbeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "goal_id") val goalId: Long?,
    @ColumnInfo(name = "target_skill") val targetSkillOrPhoneme: String,
    @ColumnInfo(name = "context") val context: String,
    @ColumnInfo(name = "interlocutor") val interlocutor: String,
    @ColumnInfo(name = "stage") val stage: String,
    @ColumnInfo(name = "accuracy_percentage") val accuracyPercentage: Int,
    @ColumnInfo(name = "is_spontaneous") val isSpontaneous: Boolean,
    @ColumnInfo(name = "probe_date") val probeDate: LocalDate,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Entity(
    tableName = "clinical_appointments",
    indices = [Index(value = ["date"]), Index(value = ["patient_id"])]
)
data class ClinicalAppointmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long?,
    @ColumnInfo(name = "patient_name") val patientName: String,
    @ColumnInfo(name = "linked_session_id") val linkedSessionId: Long?,
    @ColumnInfo(name = "date") val date: LocalDate,
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "end_time") val endTime: String,
    @ColumnInfo(name = "service_type") val serviceType: String,
    @ColumnInfo(name = "recurrence") val recurrence: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Entity(tableName = "waitlist_referrals")
data class WaitlistAndReferralEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "candidate_name") val candidateName: String,
    @ColumnInfo(name = "birth_date_text") val birthDateText: String,
    @ColumnInfo(name = "guardian_contact") val guardianContact: String,
    @ColumnInfo(name = "referred_by") val referredBy: String,
    @ColumnInfo(name = "main_complaint") val mainComplaint: String,
    @ColumnInfo(name = "preferred_schedule") val preferredSchedule: String,
    @ColumnInfo(name = "urgency") val urgency: String,
    @ColumnInfo(name = "is_converted") val isConvertedToPatient: Boolean,
    @ColumnInfo(name = "converted_patient_id") val convertedPatientId: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Entity(
    tableName = "home_practice_tasks",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class HomePracticeTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "linked_game_id") val linkedGameId: String?,
    @ColumnInfo(name = "target_skill") val targetSkill: String,
    @ColumnInfo(name = "caregiver_instructions") val caregiverInstructions: String,
    @ColumnInfo(name = "recommended_frequency") val recommendedFrequency: String,
    @ColumnInfo(name = "assigned_date") val assignedDate: LocalDate,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate?,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "difficulty_feedback") val difficultyFeedback: String,
    @ColumnInfo(name = "family_notes") val familyNotes: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Entity(
    tableName = "clinical_documents",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["patient_id"])]
)
data class ClinicalDocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "session_id") val sessionId: Long?,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "document_date") val documentDate: LocalDate,
    @ColumnInfo(name = "professional_name") val professionalName: String,
    @ColumnInfo(name = "professional_crfa") val professionalCrfa: String,
    @ColumnInfo(name = "soap_subjective") val soapSubjective: String,
    @ColumnInfo(name = "soap_objective") val soapObjective: String,
    @ColumnInfo(name = "soap_assessment") val soapAssessment: String,
    @ColumnInfo(name = "soap_plan") val soapPlan: String,
    @ColumnInfo(name = "body_content") val bodyContent: String,
    @ColumnInfo(name = "addendum_notes") val addendumNotes: String,
    @ColumnInfo(name = "is_signed_and_locked") val isSignedAndLocked: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)

@Entity(
    tableName = "billing_records",
    indices = [Index(value = ["patient_id"]), Index(value = ["status"])]
)
data class BillingRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long,
    @ColumnInfo(name = "patient_name") val patientName: String,
    @ColumnInfo(name = "session_id") val sessionId: Long?,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "amount_cents") val amountCents: Long,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate,
    @ColumnInfo(name = "payment_date") val paymentDate: LocalDate?,
    @ColumnInfo(name = "payment_method") val paymentMethod: String,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "receipt_issued") val receiptIssued: Boolean,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Entity(tableName = "clinical_tasks")
data class ClinicalTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "patient_id") val patientId: Long?,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "due_date") val dueDate: LocalDate?,
    @ColumnInfo(name = "priority") val priority: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "created_at") val createdAt: Instant
)

@Entity(tableName = "custom_stimuli")
data class CustomStimulusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "word") val word: String,
    @ColumnInfo(name = "target_phoneme") val targetPhoneme: String,
    @ColumnInfo(name = "phoneme_position") val phonemePosition: String,
    @ColumnInfo(name = "syllable_division") val syllableDivision: String,
    @ColumnInfo(name = "syllable_count") val syllableCount: Int,
    @ColumnInfo(name = "semantic_category") val semanticCategory: String,
    @ColumnInfo(name = "difficulty") val difficulty: String,
    @ColumnInfo(name = "icon_emoji") val iconEmoji: String,
    @ColumnInfo(name = "example_sentence") val exampleSentence: String,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean
)

@Entity(tableName = "audit_events")
data class AuditEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entity_type") val entityType: String,
    @ColumnInfo(name = "entity_id") val entityId: Long,
    @ColumnInfo(name = "action") val action: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "timestamp") val timestamp: Instant
)
