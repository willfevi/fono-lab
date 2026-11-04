package com.healthcare.fonolab.domain.patient

import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.ValidationIssue
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

enum class PatientField { FULL_NAME, BIRTH_DATE, GUARDIAN_NAME, GUARDIAN_CONTACT, FOLLOW_UP_START, CLINICAL_NOTES }

enum class PatientValidationError(val field: PatientField) : ValidationIssue {
    FULL_NAME_REQUIRED(PatientField.FULL_NAME),
    FULL_NAME_TOO_SHORT(PatientField.FULL_NAME),
    FULL_NAME_TOO_LONG(PatientField.FULL_NAME),
    BIRTH_DATE_REQUIRED(PatientField.BIRTH_DATE),
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
class PatientValidator @Inject constructor(private val clock: Clock) {

    fun validate(draft: PatientDraft): DomainResult<ValidPatientData> {
        val today = LocalDate.now(clock)
        val errors = mutableListOf<PatientValidationError>()
        val name = draft.fullName.trim().replace(WHITESPACE, " ")

        when {
            name.isEmpty() -> errors += PatientValidationError.FULL_NAME_REQUIRED
            name.length < MIN_NAME_LENGTH -> errors += PatientValidationError.FULL_NAME_TOO_SHORT
            name.length > MAX_NAME_LENGTH -> errors += PatientValidationError.FULL_NAME_TOO_LONG
        }

        val birthDate = draft.birthDate
        when {
            birthDate == null -> errors += PatientValidationError.BIRTH_DATE_REQUIRED
            birthDate.isAfter(today) -> errors += PatientValidationError.BIRTH_DATE_IN_FUTURE
            birthDate.isBefore(today.minusYears(MAX_AGE_YEARS)) -> errors += PatientValidationError.BIRTH_DATE_TOO_OLD
        }

        val followUp = draft.followUpStartDate
        when {
            followUp == null -> errors += PatientValidationError.FOLLOW_UP_START_REQUIRED
            followUp.isAfter(today) -> errors += PatientValidationError.FOLLOW_UP_START_IN_FUTURE
            birthDate != null && followUp.isBefore(birthDate) ->
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

        if (errors.isNotEmpty() || birthDate == null || followUp == null) {
            return DomainResult.Failure(DomainError.Validation(errors))
        }
        return DomainResult.Success(
            ValidPatientData(
                fullName = name,
                birthDate = birthDate,
                guardianName = guardianName,
                guardianContact = contact,
                followUpStartDate = followUp,
                clinicalNotes = notes,
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
