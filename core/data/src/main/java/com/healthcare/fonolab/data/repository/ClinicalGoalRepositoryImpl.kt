package com.healthcare.fonolab.data.repository

import com.healthcare.fonolab.data.local.dao.ClinicalGoalDao
import com.healthcare.fonolab.data.local.entity.ClinicalGoalEntity
import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.goal.ClinicalArea
import com.healthcare.fonolab.domain.goal.ClinicalGoal
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.goal.GoalStatus
import com.healthcare.fonolab.domain.goal.ValidGoalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class ClinicalGoalRepositoryImpl(
    private val dao: ClinicalGoalDao
) : ClinicalGoalRepository {

    override fun observeGoalsForPatient(patientId: Long): Flow<List<ClinicalGoal>> {
        return dao.observeByPatient(patientId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeActiveGoalsForPatient(patientId: Long): Flow<List<ClinicalGoal>> {
        return dao.observeActiveByPatient(patientId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeGoal(id: Long): Flow<ClinicalGoal?> {
        return dao.observeById(id).map { it?.toDomain() }
    }

    override suspend fun getGoal(id: Long): ClinicalGoal? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun insert(patientId: Long, data: ValidGoalData, now: Instant): Long {
        val entity = ClinicalGoalEntity(
            patientId = patientId,
            clinicalArea = data.clinicalArea.name,
            customAreaName = data.customAreaName,
            description = data.description,
            targetSkill = data.targetSkill,
            targetStimulus = data.targetStimulus,
            phonemePosition = data.phonemePosition?.name,
            initialDifficulty = data.initialDifficulty.name,
            targetAccuracyPercentage = data.targetAccuracyPercentage,
            masteryCriterionDescription = data.masteryCriterionDescription,
            clinicalNotes = data.clinicalNotes,
            status = GoalStatus.ACTIVE.name,
            createdAt = now,
            updatedAt = now
        )
        return dao.insert(entity)
    }

    override suspend fun update(id: Long, data: ValidGoalData, now: Instant) {
        val current = dao.getById(id) ?: return
        val updated = current.copy(
            clinicalArea = data.clinicalArea.name,
            customAreaName = data.customAreaName,
            description = data.description,
            targetSkill = data.targetSkill,
            targetStimulus = data.targetStimulus,
            phonemePosition = data.phonemePosition?.name,
            initialDifficulty = data.initialDifficulty.name,
            targetAccuracyPercentage = data.targetAccuracyPercentage,
            masteryCriterionDescription = data.masteryCriterionDescription,
            clinicalNotes = data.clinicalNotes,
            updatedAt = now
        )
        dao.update(updated)
    }

    override suspend fun setStatus(id: Long, status: GoalStatus, now: Instant) {
        dao.updateStatus(id, status.name, now)
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    companion object {
        fun ClinicalGoalEntity.toDomain(): ClinicalGoal {
            return ClinicalGoal(
                id = id,
                patientId = patientId,
                clinicalArea = ClinicalArea.valueOf(clinicalArea),
                customAreaName = customAreaName,
                description = description,
                targetSkill = targetSkill,
                targetStimulus = targetStimulus,
                phonemePosition = phonemePosition?.let { PhonemePosition.valueOf(it) },
                initialDifficulty = DifficultyLevel.valueOf(initialDifficulty),
                targetAccuracyPercentage = targetAccuracyPercentage,
                masteryCriterionDescription = masteryCriterionDescription,
                clinicalNotes = clinicalNotes,
                status = GoalStatus.valueOf(status),
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }
    }
}
