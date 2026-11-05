package com.healthcare.fonolab.domain.patient

import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.EntityType
import com.healthcare.fonolab.domain.common.storageCall
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

interface PatientRepository {
    fun observePatients(filter: PatientFilter): Flow<List<Patient>>
    fun observePatient(id: Long): Flow<Patient?>
    fun observeActivePatientCount(includeDemo: Boolean): Flow<Int>
    fun observeDemoPatientCount(): Flow<Int>
    suspend fun getPatient(id: Long): Patient?
    suspend fun insert(data: ValidPatientData, internalCode: String, isDemo: Boolean, now: Instant): Long
    suspend fun update(id: Long, data: ValidPatientData, now: Instant)
    suspend fun setStatus(id: Long, status: PatientStatus, now: Instant)
    suspend fun deletionImpact(id: Long): PatientDeletionImpact
    /** Deletes the patient and every related clinical record in a single transaction. */
    suspend fun delete(id: Long)
    suspend fun deleteDemoPatients()
    suspend fun isInternalCodeInUse(code: String): Boolean
}

class SavePatientUseCase @Inject constructor(
    private val repository: PatientRepository,
    private val validator: PatientValidator,
    private val codeGenerator: InternalCodeGenerator,
    private val clock: Clock,
) {
    /** Creates a patient when [patientId] is null, otherwise updates it. Returns the patient id. */
    suspend operator fun invoke(patientId: Long?, draft: PatientDraft, isDemo: Boolean = false): DomainResult<Long> {
        val validation = validator.validate(draft)
        val data = when (validation) {
            is DomainResult.Failure -> return validation
            is DomainResult.Success -> validation.value
        }
        val now = clock.instant()
        return storageCall {
            if (patientId == null) {
                repository.insert(data, uniqueCode(), isDemo, now)
            } else {
                if (repository.getPatient(patientId) == null) {
                    return DomainResult.Failure(DomainError.NotFound(EntityType.PATIENT))
                }
                repository.update(patientId, data, now)
                patientId
            }
        }
    }

    private suspend fun uniqueCode(): String {
        repeat(MAX_CODE_ATTEMPTS) {
            val code = codeGenerator.generate()
            if (!repository.isInternalCodeInUse(code)) return code
        }
        error("Não foi possível gerar um identificador interno único")
    }

    private companion object {
        const val MAX_CODE_ATTEMPTS = 20
    }
}

class ChangePatientStatusUseCase @Inject constructor(
    private val repository: PatientRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(patientId: Long, status: PatientStatus): DomainResult<Unit> = storageCall {
        if (repository.getPatient(patientId) == null) {
            return DomainResult.Failure(DomainError.NotFound(EntityType.PATIENT))
        }
        repository.setStatus(patientId, status, clock.instant())
    }
}

class DeletePatientUseCase @Inject constructor(private val repository: PatientRepository) {
    suspend fun impact(patientId: Long): DomainResult<PatientDeletionImpact> =
        storageCall { repository.deletionImpact(patientId) }

    suspend operator fun invoke(patientId: Long): DomainResult<Unit> = storageCall { repository.delete(patientId) }
}
