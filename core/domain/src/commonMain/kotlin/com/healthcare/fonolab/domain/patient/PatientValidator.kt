package com.healthcare.fonolab.domain.patient

import com.healthcare.fonolab.domain.datetime.Clock
import com.healthcare.fonolab.domain.datetime.currentLocalDate
import com.healthcare.fonolab.domain.datetime.isAfter
import com.healthcare.fonolab.domain.datetime.isBefore
import com.healthcare.fonolab.domain.datetime.LocalDate
import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.ValidationIssue
import com.healthcare.fonolab.domain.datetime.DateErrorReason
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.datetime.DateValidationResult

enum class PatientField {
    FULL_NAME,
    BIRTH_DATE,
    GUARDIAN_NAME,
    GUARDIAN_CONTACT,
    FOLLOW_UP_START,
    CLINICAL_NOTES
}

enum class PatientValidationError(val field: PatientField) : ValidationIssue {
    FULL_NAME_REQUIRED(PatientField.FULL_NAME),
    FULL_NAME_TOO_SHORT(PatientField.FULL_NAME),
    FULL_NAME_TOO_LONG(PatientField.FULL_NAME),
    BIRTH_DATE_REQUIRED(PatientField.BIRTH_DATE),
    BIRTH_DATE_INVALID_FORMAT(PatientField.BIRTH_DATE),
    BIRTH_DATE_IN_FUTURE(PatientField.BIRTH_DATE),
    BIRTH_DATE_TOO_OLD(PatientField.BIRTH_DATE),
    GUARDIAN_NAME_TOO_LONG(PatientField.GUARDIAN_NAME),
    GUARDIAN_CONTACT_INVALID(PatientField.GUARDIAN_CONTACT),
    FOLLOW_UP_START_REQUIRED(PatientField.FOLLOW_UP_START),
    FOLLOW_UP_START_BEFORE_BIRTH(PatientField.FOLLOW_UP_START),
    FOLLOW_UP_START_IN_FUTURE(PatientField.FOLLOW_UP_START),
    CLINICAL_NOTES_TOO_LONG(PatientField.CLINICAL_NOTES),
}

/** Validates and normalizes patient form input. Optional fields never block registration. */
class PatientValidator(private val clock: Clock) {

    private val dateTimeManager = DateTimeManager(clock)

    fun validate(draft: PatientDraft): DomainResult<ValidPatientData> {
        val today = currentLocalDate(clock)
        val errors = mutableListOf<PatientValidationError>()
        val name = draft.fullName.trim().replace(WHITESPACE, " ")

        when {
            name.isEmpty() -> errors += PatientValidationError.FULL_NAME_REQUIRED
            name.length < MIN_NAME_LENGTH -> errors += PatientValidationError.FULL_NAME_TOO_SHORT
            name.length > MAX_NAME_LENGTH -> errors += PatientValidationError.FULL_NAME_TOO_LONG
        }

        // Resolve birthDate from either typed LocalDate or Brazilian DD/MM/AAAA text
        val resolvedBirthDate: LocalDate? = when {
            draft.birthDateText.isNotBlank() -> {
                when (val res = dateTimeManager.validateBirthDate(draft.birthDateText, today, MAX_AGE_YEARS)) {
                    is DateValidationResult.Valid -> res.date
                    is DateValidationResult.Invalid -> {
                        errors += when (res.reason) {
                            DateErrorReason.EMPTY -> PatientValidationError.BIRTH_DATE_REQUIRED
                            DateErrorReason.FUTURE_DATE_NOT_ALLOWED -> PatientValidationError.BIRTH_DATE_IN_FUTURE
                            DateErrorReason.PAST_DATE_TOO_OLD -> PatientValidationError.BIRTH_DATE_TOO_OLD
                            else -> PatientValidationError.BIRTH_DATE_INVALID_FORMAT
                        }
                        null
                    }
                }
            }
            draft.birthDate != null -> {
                val bd = draft.birthDate
                when {
                    bd.isAfter(today) -> {
                        errors += PatientValidationError.BIRTH_DATE_IN_FUTURE
                        null
                    }
                    bd.isBefore(today.minusYears(MAX_AGE_YEARS)) -> {
                        errors += PatientValidationError.BIRTH_DATE_TOO_OLD
                        null
                    }
                    else -> bd
                }
            }
            else -> {
                errors += PatientValidationError.BIRTH_DATE_REQUIRED
                null
            }
        }

        // Resolve followUpStartDate from either text, LocalDate, or default to today
        val resolvedFollowUp: LocalDate? = when {
            draft.followUpStartDateText.isNotBlank() -> {
                dateTimeManager.parseDate(draft.followUpStartDateText) ?: draft.followUpStartDate ?: today
            }
            draft.followUpStartDate != null -> draft.followUpStartDate
            else -> null
        }

        when {
            resolvedFollowUp == null -> errors += PatientValidationError.FOLLOW_UP_START_REQUIRED
            resolvedFollowUp.isAfter(today) -> errors += PatientValidationError.FOLLOW_UP_START_IN_FUTURE
            resolvedBirthDate != null && resolvedFollowUp.isBefore(resolvedBirthDate) ->
                errors += PatientValidationError.FOLLOW_UP_START_BEFORE_BIRTH
        }

        val guardianName = draft.guardianName.trim().takeIf { it.isNotEmpty() }
        if (guardianName != null && guardianName.length > MAX_NAME_LENGTH) {
            errors += PatientValidationError.GUARDIAN_NAME_TOO_LONG
        }

        val contact = draft.guardianContact.trim().takeIf { it.isNotEmpty() }
        if (contact != null && !isValidContact(contact)) {
            errors += PatientValidationError.GUARDIAN_CONTACT_INVALID
        }

        val notes = draft.clinicalNotes.trim()
        if (notes.length > MAX_NOTES_LENGTH) errors += PatientValidationError.CLINICAL_NOTES_TOO_LONG

        if (errors.isNotEmpty() || resolvedBirthDate == null || resolvedFollowUp == null) {
            return DomainResult.Failure(DomainError.Validation(errors))
        }
        return DomainResult.Success(
            ValidPatientData(
                fullName = name,
                birthDate = resolvedBirthDate,
                guardianName = guardianName,
                guardianContact = contact,
                followUpStartDate = resolvedFollowUp,
                clinicalNotes = notes,
                socialName = draft.socialName.trim().takeIf { it.isNotEmpty() },
                phone = draft.phone.trim().takeIf { it.isNotEmpty() },
                email = draft.email.trim().takeIf { it.isNotEmpty() },
                address = draft.address.trim().takeIf { it.isNotEmpty() },
                guardianKinship = draft.guardianKinship.trim().takeIf { it.isNotEmpty() },
                emergencyContact = draft.emergencyContact.trim().takeIf { it.isNotEmpty() },
                school = draft.school.trim().takeIf { it.isNotEmpty() },
                educationLevel = draft.educationLevel.trim().takeIf { it.isNotEmpty() },
                communicationModalities = draft.communicationModalities.trim().ifEmpty { "Fala oral" },
                sensoryPreferences = draft.sensoryPreferences.trim(),
                effectiveReinforcers = draft.effectiveReinforcers.trim(),
                homeLanguages = draft.homeLanguages.trim().ifEmpty { "Português Brasileiro" },
                schoolLanguage = draft.schoolLanguage.trim().ifEmpty { "Português Brasileiro" },
                therapyLanguage = draft.therapyLanguage.trim().ifEmpty { "Português Brasileiro" },
                lowStimulationMode = draft.lowStimulationMode
            ),
        )
    }

    private fun isValidContact(contact: String): Boolean {
        if (EMAIL.matches(contact)) return true
        if (!PHONE_ALLOWED.matches(contact)) return false
        val digits = contact.count(Char::isDigit)
        return digits in MIN_PHONE_DIGITS..MAX_PHONE_DIGITS
    }

    companion object {
        const val MIN_NAME_LENGTH = 3
        const val MAX_NAME_LENGTH = 120
        const val MAX_NOTES_LENGTH = 4000
        private const val MAX_AGE_YEARS = 120L
        private const val MIN_PHONE_DIGITS = 8
        private const val MAX_PHONE_DIGITS = 15
        private val WHITESPACE = Regex("\\s+")
        private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        private val PHONE_ALLOWED = Regex("^[0-9+()\\-\\s.]+$")
    }
}
