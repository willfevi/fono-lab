package com.healthcare.fonolab.domain.goal

import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.EntityType
import com.healthcare.fonolab.domain.common.storageCall
import kotlinx.coroutines.flow.Flow
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

interface ClinicalGoalRepository {
    fun observeGoalsForPatient(patientId: Long): Flow<List<ClinicalGoal>>
    fun observeActiveGoalsForPatient(patientId: Long): Flow<List<ClinicalGoal>>
    fun observeGoal(id: Long): Flow<ClinicalGoal?>
    suspend fun getGoal(id: Long): ClinicalGoal?
    suspend fun insert(patientId: Long, data: ValidGoalData, now: Instant): Long
    suspend fun update(id: Long, data: ValidGoalData, now: Instant)
    suspend fun setStatus(id: Long, status: GoalStatus, now: Instant)
    suspend fun delete(id: Long)
}

class SaveGoalUseCase @Inject constructor(
    private val repository: ClinicalGoalRepository,
    private val validator: GoalValidator,
    private val clock: Clock
) {
    suspend operator fun invoke(goalId: Long?, patientId: Long, draft: ClinicalGoalDraft): DomainResult<Long> {
        val validation = validator.validate(draft)
        val data = when (validation) {
            is DomainResult.Failure -> return validation
            is DomainResult.Success -> validation.value
        }
        val now = clock.instant()
        return storageCall {
            if (goalId == null) {
                repository.insert(patientId, data, now)
            } else {
                if (repository.getGoal(goalId) == null) {
                    return DomainResult.Failure(DomainError.NotFound(EntityType.GOAL))
                }
                repository.update(goalId, data, now)
                goalId
            }
        }
    }
}

class ChangeGoalStatusUseCase @Inject constructor(
    private val repository: ClinicalGoalRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(goalId: Long, status: GoalStatus): DomainResult<Unit> = storageCall {
        if (repository.getGoal(goalId) == null) {
            return DomainResult.Failure(DomainError.NotFound(EntityType.GOAL))
        }
        repository.setStatus(goalId, status, clock.instant())
    }
}

class DeleteGoalUseCase @Inject constructor(
    private val repository: ClinicalGoalRepository
) {
    suspend operator fun invoke(goalId: Long): DomainResult<Unit> = storageCall {
        repository.delete(goalId)
    }
}
