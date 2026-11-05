package com.healthcare.fonolab.domain.patient

import com.healthcare.fonolab.domain.datetime.dateFormatter
import com.healthcare.fonolab.domain.datetime.DateTimeFormatter
import com.healthcare.fonolab.domain.datetime.Instant
import com.healthcare.fonolab.domain.datetime.LocalDate


enum class PatientStatus(val displayName: String) {
    ACTIVE("Ativo"),
    PAUSED("Pausado"),
    DISCHARGED("Alta Clínica"),
    INACTIVE("Inativo"),
    ARCHIVED("Arquivado")
}

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
    // Extended fields from Master Prompt (Phase 3 + Advanced Clinical Modules)
    val socialName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val guardianKinship: String? = null,
    val emergencyContact: String? = null,
    val school: String? = null,
    val educationLevel: String? = null,
    val communicationModalities: String = "Fala oral",
    val sensoryPreferences: String = "",
    val effectiveReinforcers: String = "",
    val homeLanguages: String = "Português Brasileiro",
    val schoolLanguage: String = "Português Brasileiro",
    val therapyLanguage: String = "Português Brasileiro",
    val lowStimulationMode: Boolean = false
)

/** Raw form input; may be invalid. Supports both typed LocalDate and Brazilian DD/MM/AAAA string input. */
data class PatientDraft(
    val fullName: String = "",
    val socialName: String = "",
    val birthDate: LocalDate? = null,
    val birthDateText: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val guardianName: String = "",
    val guardianKinship: String = "",
    val guardianContact: String = "",
    val emergencyContact: String = "",
    val school: String = "",
    val educationLevel: String = "",
    val followUpStartDate: LocalDate? = null,
    val followUpStartDateText: String = "",
    val clinicalNotes: String = "",
    val communicationModalities: String = "Fala oral",
    val sensoryPreferences: String = "",
    val effectiveReinforcers: String = "",
    val homeLanguages: String = "Português Brasileiro",
    val schoolLanguage: String = "Português Brasileiro",
    val therapyLanguage: String = "Português Brasileiro",
    val lowStimulationMode: Boolean = false
)

/** Normalized data produced only by [PatientValidator]. */
data class ValidPatientData(
    val fullName: String,
    val birthDate: LocalDate,
    val guardianName: String?,
    val guardianContact: String?,
    val followUpStartDate: LocalDate,
    val clinicalNotes: String,
    val socialName: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null,
    val guardianKinship: String? = null,
    val emergencyContact: String? = null,
    val school: String? = null,
    val educationLevel: String? = null,
    val communicationModalities: String = "Fala oral",
    val sensoryPreferences: String = "",
    val effectiveReinforcers: String = "",
    val homeLanguages: String = "Português Brasileiro",
    val schoolLanguage: String = "Português Brasileiro",
    val therapyLanguage: String = "Português Brasileiro",
    val lowStimulationMode: Boolean = false
)

private val brFormatter: DateTimeFormatter = dateFormatter("dd/MM/yyyy")

fun Patient.toDraft(): PatientDraft = PatientDraft(
    fullName = fullName,
    socialName = socialName.orEmpty(),
    birthDate = birthDate,
    birthDateText = birthDate.format(brFormatter),
    phone = phone.orEmpty(),
    email = email.orEmpty(),
    address = address.orEmpty(),
    guardianName = guardianName.orEmpty(),
    guardianKinship = guardianKinship.orEmpty(),
    guardianContact = guardianContact.orEmpty(),
    emergencyContact = emergencyContact.orEmpty(),
    school = school.orEmpty(),
    educationLevel = educationLevel.orEmpty(),
    followUpStartDate = followUpStartDate,
    followUpStartDateText = followUpStartDate.format(brFormatter),
    clinicalNotes = clinicalNotes,
    communicationModalities = communicationModalities,
    sensoryPreferences = sensoryPreferences,
    effectiveReinforcers = effectiveReinforcers,
    homeLanguages = homeLanguages,
    schoolLanguage = schoolLanguage,
    therapyLanguage = therapyLanguage,
    lowStimulationMode = lowStimulationMode
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
