package com.healthcare.fonolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthcare.fonolab.data.local.entity.ClinicalGoalEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface ClinicalGoalDao {
    @Query("SELECT * FROM clinical_goals WHERE patient_id = :patientId ORDER BY created_at DESC")
    fun observeByPatient(patientId: Long): Flow<List<ClinicalGoalEntity>>

    @Query("SELECT * FROM clinical_goals WHERE patient_id = :patientId AND status = 'ACTIVE' ORDER BY created_at DESC")
    fun observeActiveByPatient(patientId: Long): Flow<List<ClinicalGoalEntity>>

    @Query("SELECT * FROM clinical_goals WHERE id = :id")
    fun observeById(id: Long): Flow<ClinicalGoalEntity?>

    @Query("SELECT * FROM clinical_goals WHERE id = :id")
    suspend fun getById(id: Long): ClinicalGoalEntity?

    @Query("SELECT * FROM clinical_goals WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<ClinicalGoalEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: ClinicalGoalEntity): Long

    @Update
    suspend fun update(entity: ClinicalGoalEntity)

    @Query("UPDATE clinical_goals SET status = :status, updated_at = :now WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, now: Instant)

    @Query("DELETE FROM clinical_goals WHERE id = :id")
    suspend fun deleteById(id: Long)
}
