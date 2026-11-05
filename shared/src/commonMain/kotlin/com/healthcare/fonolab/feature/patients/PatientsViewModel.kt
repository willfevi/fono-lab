package com.healthcare.fonolab.feature.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientsListUiState(
    val patients: List<Patient> = emptyList(),
    val query: String = "",
    val statusFilter: PatientStatus? = PatientStatus.ACTIVE,
    val isLoading: Boolean = false
)

class PatientsViewModel @Inject constructor(
    private val repository: PatientRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<PatientStatus?>(PatientStatus.ACTIVE)

    val uiState: StateFlow<PatientsListUiState> = combine(_query, _statusFilter) { q, status ->
        PatientFilter(query = q, status = status)
    }.flatMapLatest { filter ->
        repository.observePatients(filter).combine(_query) { list, q ->
            PatientsListUiState(
                patients = list,
                query = q,
                statusFilter = filter.status,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PatientsListUiState(isLoading = true)
    )

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }

    fun onStatusFilterChanged(status: PatientStatus?) {
        _statusFilter.value = status
    }
}
