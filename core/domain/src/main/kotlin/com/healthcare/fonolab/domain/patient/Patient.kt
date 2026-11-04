package com.healthcare.fonolab.domain.patient

import java.time.Instant
import java.time.LocalDate

enum class PatientStatus { ACTIVE, ARCHIVED }

data class Patient(
    val id: Long,
    val internalCode: String,
    val fullName: String,
    val birthDate: LocalDate,
    val guardianName: String?,
    val guardianContact: String?,
    val followUpStartDate: LocalDate,
    val clinicalNotes: String,
    val status: PatientStatus,
    val isDemo: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/** Raw form input; may be invalid. */
data class PatientDraft(
    val fullName: String = "",
    val birthDate: LocalDate? = null,
    val guardianName: String = "",
    val guardianContact: String = "",
    val followUpStartDate: LocalDate? = null,
    val clinicalNotes: String = "",
)

/** Normalized data produced only by [PatientValidator]. */
data class ValidPatientData(
    val fullName: String,
    val birthDate: LocalDate,
    val guardianName: String?,
    val guardianContact: String?,
    val followUpStartDate: LocalDate,
    val clinicalNotes: String,
)

fun Patient.toDraft(): PatientDraft = PatientDraft(
    fullName = fullName,
    birthDate = birthDate,
    guardianName = guardianName.orEmpty(),
    guardianContact = guardianContact.orEmpty(),
    followUpStartDate = followUpStartDate,
    clinicalNotes = clinicalNotes,
)

data class PatientFilter(
    val query: String = "",
    val status: PatientStatus? = PatientStatus.ACTIVE,
)

/** Records that will be removed together with a patient. */
data class PatientDeletionImpact(
    val goals: Int,
    val sessions: Int,
    val attempts: Int,
    val observations: Int,
)
