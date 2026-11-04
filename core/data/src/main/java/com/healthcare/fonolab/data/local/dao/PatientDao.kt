package com.healthcare.fonolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthcare.fonolab.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients WHERE (:status IS NULL OR status = :status) AND (:query = '' OR full_name LIKE '%' || :query || '%') ORDER BY full_name ASC")
    fun observeFiltered(query: String, status: String?): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    fun observeById(id: Long): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getById(id: Long): PatientEntity?

    @Query("SELECT COUNT(*) FROM patients WHERE status = 'ACTIVE' AND (:includeDemo = 1 OR is_demo = 0)")
    fun observeActiveCount(includeDemo: Boolean): Flow<Int>

    @Query("SELECT COUNT(*) FROM patients WHERE is_demo = 1")
    fun observeDemoCount(): Flow<Int>

    @Query("SELECT EXISTS(SELECT 1 FROM patients WHERE internal_code = :code)")
    suspend fun existsByInternalCode(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: PatientEntity): Long

    @Update
    suspend fun update(entity: PatientEntity)

    @Query("UPDATE patients SET status = :status, updated_at = :now WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, now: Instant)

    @Query("DELETE FROM patients WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM patients WHERE is_demo = 1")
    suspend fun deleteDemoPatients()

    @Query("SELECT COUNT(*) FROM clinical_goals WHERE patient_id = :patientId")
    suspend fun countGoalsForPatient(patientId: Long): Int

    @Query("SELECT COUNT(*) FROM therapy_sessions WHERE patient_id = :patientId")
    suspend fun countSessionsForPatient(patientId: Long): Int

    @Query("SELECT COUNT(*) FROM game_attempts WHERE activity_id IN (SELECT sa.id FROM session_activities sa INNER JOIN therapy_sessions s ON sa.session_id = s.id WHERE s.patient_id = :patientId)")
    suspend fun countAttemptsForPatient(patientId: Long): Int
}
