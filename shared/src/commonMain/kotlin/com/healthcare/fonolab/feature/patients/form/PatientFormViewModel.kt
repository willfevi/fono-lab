package com.healthcare.fonolab.feature.patients.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.datetime.DateValidationResult
import com.healthcare.fonolab.domain.manager.PatientManager
import com.healthcare.fonolab.domain.patient.PatientDraft
import com.healthcare.fonolab.domain.patient.PatientValidationError
import com.healthcare.fonolab.domain.patient.toDraft
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientFormUiState(
    val patientId: Long? = null,
    val draft: PatientDraft = PatientDraft(),
    val calculatedAgeText: String? = null,
    val birthDateHelperError: String? = null,
    val isSaving: Boolean = false,
    val errors: Set<PatientValidationError> = emptySet(),
    val isEditMode: Boolean = false
)

sealed interface PatientFormEvent {
    data class Saved(val patientId: Long, val openAnamnesisImmediately: Boolean) : PatientFormEvent
    data class Error(val message: String) : PatientFormEvent
}

class PatientFormViewModel(
    val patientId: Long? = null,
    private val patientManager: PatientManager,
    private val dateTimeManager: DateTimeManager
) : ViewModel() {

    private val todayBr = dateTimeManager.formatDateBr(dateTimeManager.today())

    private val _uiState = MutableStateFlow(
        PatientFormUiState(
            patientId = patientId,
            draft = PatientDraft(
                followUpStartDate = dateTimeManager.today(),
                followUpStartDateText = todayBr
            ),
            isEditMode = patientId != null
        )
    )
    val uiState: StateFlow<PatientFormUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PatientFormEvent>()
    val event: SharedFlow<PatientFormEvent> = _event.asSharedFlow()

    init {
        if (patientId != null) {
            viewModelScope.launch {
                val existing = patientManager.getPatient(patientId)
                if (existing != null) {
                    val draft = existing.toDraft()
                    val age = dateTimeManager.calculateAge(existing.birthDate).toFormattedBr()
                    _uiState.value = _uiState.value.copy(
                        draft = draft,
                        calculatedAgeText = age
                    )
                }
            }
        }
    }

    fun onFullNameChange(name: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(fullName = name))
    }

    fun onSocialNameChange(socialName: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(socialName = socialName))
    }

    /**
     * Handles Brazilian date input (DD/MM/AAAA) smoothly while typing, auto-formatting slashes
     * and computing the patient's age as soon as a complete valid date is entered.
     */
    fun onBirthDateTextChange(rawInput: String) {
        val normalized = dateTimeManager.normalizeBrDateInput(rawInput)
        val parsed = dateTimeManager.parseDate(normalized)
        val ageText = if (parsed != null && !parsed.isAfter(dateTimeManager.today())) {
            dateTimeManager.calculateAge(parsed).toFormattedBr()
        } else {
            null
        }
        val helperError = if (normalized.length == 10) {
            when (val res = dateTimeManager.validateBirthDate(normalized)) {
                is DateValidationResult.Invalid -> res.messagePtBr
                is DateValidationResult.Valid -> null
            }
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(
                birthDateText = normalized,
                birthDate = parsed
            ),
            calculatedAgeText = ageText,
            birthDateHelperError = helperError,
            errors = _uiState.value.errors - setOf(
                PatientValidationError.BIRTH_DATE_REQUIRED,
                PatientValidationError.BIRTH_DATE_INVALID_FORMAT,
                PatientValidationError.BIRTH_DATE_IN_FUTURE,
                PatientValidationError.BIRTH_DATE_TOO_OLD
            )
        )
    }

    fun onFollowUpDateTextChange(rawInput: String) {
        val normalized = dateTimeManager.normalizeBrDateInput(rawInput)
        val parsed = dateTimeManager.parseDate(normalized)
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(
                followUpStartDateText = normalized,
                followUpStartDate = parsed ?: _uiState.value.draft.followUpStartDate
            )
        )
    }

    fun onGuardianNameChange(name: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(guardianName = name))
    }

    fun onGuardianKinshipChange(kinship: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(guardianKinship = kinship))
    }

    fun onGuardianContactChange(contact: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(guardianContact = contact))
    }

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(email = email))
    }

    fun onEmergencyContactChange(emergency: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(emergencyContact = emergency))
    }

    fun onAddressChange(address: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(address = address))
    }

    fun onSchoolChange(school: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(school = school))
    }

    fun onEducationLevelChange(level: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(educationLevel = level))
    }

    fun onCommunicationModalitiesChange(modalities: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(communicationModalities = modalities))
    }

    fun onHomeLanguagesChange(languages: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(homeLanguages = languages))
    }

    fun onEffectiveReinforcersChange(reinforcers: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(effectiveReinforcers = reinforcers))
    }

    fun onSensoryPreferencesChange(sensory: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(sensoryPreferences = sensory))
    }

    fun onLowStimulationToggle(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(lowStimulationMode = enabled))
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(draft = _uiState.value.draft.copy(clinicalNotes = notes))
    }

    fun save(openAnamnesisImmediately: Boolean = false) {
        if (_uiState.value.isSaving) return
        _uiState.value = _uiState.value.copy(isSaving = true, errors = emptySet())

        viewModelScope.launch {
            val result = patientManager.savePatient(patientId, _uiState.value.draft)
            when (result) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _event.emit(PatientFormEvent.Saved(result.value, openAnamnesisImmediately))
                }
                is DomainResult.Failure -> {
                    val issues = (result.error as? DomainError.Validation)
                        ?.issues
                        ?.filterIsInstance<PatientValidationError>()
                        ?.toSet() ?: emptySet()
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errors = issues
                    )
                    if (issues.isEmpty()) {
                        _event.emit(PatientFormEvent.Error("Erro ao salvar paciente."))
                    }
                }
            }
        }
    }
}
