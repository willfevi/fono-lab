package com.healthcare.fonolab.feature.patients.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.patient.PatientDraft
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientValidationError
import com.healthcare.fonolab.domain.patient.SavePatientUseCase
import com.healthcare.fonolab.domain.patient.toDraft
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PatientFormUiState(
    val patientId: Long? = null,
    val draft: PatientDraft = PatientDraft(followUpStartDate = LocalDate.now()),
    val isSaving: Boolean = false,
    val errors: Set<PatientValidationError> = emptySet(),
    val isEditMode: Boolean = false
)

sealed interface PatientFormEvent {
    data class Saved(val patientId: Long) : PatientFormEvent
    data class Error(val message: String) : PatientFormEvent
}

@HiltViewModel
class PatientFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savePatientUseCase: SavePatientUseCase,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val patientId: Long? = savedStateHandle.get<Long>("patientId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(PatientFormUiState(patientId = patientId, isEditMode = patientId != null))
    val uiState: StateFlow<PatientFormUiState> = _uiState.asStateFlow()

    private val _event = MutableSharedFlow<PatientFormEvent>()
    val event: SharedFlow<PatientFormEvent> = _event.asSharedFlow()

    init {
        if (patientId != null) {
            viewModelScope.launch {
                val existing = patientRepository.getPatient(patientId)
                if (existing != null) {
                    _uiState.value = _uiState.value.copy(draft = existing.toDraft())
                }
            }
        }
    }

    fun onFullNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(fullName = name)
        )
    }

    fun onBirthDateChange(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(birthDate = date)
        )
    }

    fun onGuardianNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(guardianName = name)
        )
    }

    fun onGuardianContactChange(contact: String) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(guardianContact = contact)
        )
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(
            draft = _uiState.value.draft.copy(clinicalNotes = notes)
        )
    }

    fun save() {
        if (_uiState.value.isSaving) return
        _uiState.value = _uiState.value.copy(isSaving = true, errors = emptySet())

        viewModelScope.launch {
            val result = savePatientUseCase(patientId, _uiState.value.draft)
            when (result) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _event.emit(PatientFormEvent.Saved(result.value))
                }
                is DomainResult.Failure -> {
                    val issues = (result.error as? DomainError.Validation)?.issues?.filterIsInstance<PatientValidationError>()?.toSet() ?: emptySet()
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
