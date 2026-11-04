package com.healthcare.fonolab.data.repository

import com.healthcare.fonolab.data.local.dao.PatientDao
import com.healthcare.fonolab.data.local.entity.PatientEntity
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientDeletionImpact
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.patient.ValidPatientData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatientRepositoryImpl @Inject constructor(
    private val dao: PatientDao
) : PatientRepository {

    override fun observePatients(filter: PatientFilter): Flow<List<Patient>> {
        return dao.observeFiltered(
            query = filter.query.trim(),
            status = filter.status?.name
        ).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePatient(id: Long): Flow<Patient?> {
        return dao.observeById(id).map { it?.toDomain() }
    }

    override fun observeActivePatientCount(includeDemo: Boolean): Flow<Int> {
        return dao.observeActiveCount(includeDemo)
    }

    override fun observeDemoPatientCount(): Flow<Int> {
        return dao.observeDemoCount()
    }

    override suspend fun getPatient(id: Long): Patient? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun insert(
        data: ValidPatientData,
        internalCode: String,
        isDemo: Boolean,
        now: Instant
    ): Long {
        val entity = PatientEntity(
            internalCode = internalCode,
            fullName = data.fullName,
            birthDate = data.birthDate,
            guardianName = data.guardianName,
            guardianContact = data.guardianContact,
            followUpStartDate = data.followUpStartDate,
            clinicalNotes = data.clinicalNotes,
            status = PatientStatus.ACTIVE.name,
            isDemo = isDemo,
            createdAt = now,
            updatedAt = now
        )
        return dao.insert(entity)
    }

    override suspend fun update(id: Long, data: ValidPatientData, now: Instant) {
        val current = dao.getById(id) ?: return
        val updated = current.copy(
            fullName = data.fullName,
            birthDate = data.birthDate,
            guardianName = data.guardianName,
            guardianContact = data.guardianContact,
            followUpStartDate = data.followUpStartDate,
            clinicalNotes = data.clinicalNotes,
            updatedAt = now
        )
        dao.update(updated)
    }

    override suspend fun setStatus(id: Long, status: PatientStatus, now: Instant) {
        dao.updateStatus(id, status.name, now)
    }

    override suspend fun deletionImpact(id: Long): PatientDeletionImpact {
        val goals = dao.countGoalsForPatient(id)
        val sessions = dao.countSessionsForPatient(id)
        val attempts = dao.countAttemptsForPatient(id)
        return PatientDeletionImpact(
            goals = goals,
            sessions = sessions,
            attempts = attempts,
            observations = 0
        )
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun deleteDemoPatients() {
        dao.deleteDemoPatients()
    }

    override suspend fun isInternalCodeInUse(code: String): Boolean {
        return dao.existsByInternalCode(code)
    }

    private fun PatientEntity.toDomain(): Patient {
        return Patient(
            id = id,
            internalCode = internalCode,
            fullName = fullName,
            birthDate = birthDate,
            guardianName = guardianName,
            guardianContact = guardianContact,
            followUpStartDate = followUpStartDate,
            clinicalNotes = clinicalNotes,
            status = PatientStatus.valueOf(status),
            isDemo = isDemo,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
