package com.healthcare.fonolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.healthcare.fonolab.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ClinicalPlatformDao {

    // 1. Anamnesis
    @Query("SELECT * FROM anamnesis_records WHERE patient_id = :patientId LIMIT 1")
    fun observeAnamnesisForPatient(patientId: Long): Flow<AnamnesisEntity?>

    @Query("SELECT * FROM anamnesis_records ORDER BY updated_at DESC")
    fun observeAllAnamneses(): Flow<List<AnamnesisEntity>>

    @Query("SELECT * FROM anamnesis_records WHERE patient_id = :patientId LIMIT 1")
    suspend fun getAnamnesisForPatient(patientId: Long): AnamnesisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnamnesis(entity: AnamnesisEntity): Long

    @Query("SELECT * FROM anamnesis_versions WHERE patient_id = :patientId ORDER BY version_number DESC")
    fun observeAnamnesisVersions(patientId: Long): Flow<List<AnamnesisVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnamnesisVersion(entity: AnamnesisVersionEntity): Long

    // 2. Assessments & Phonology
    @Query("SELECT * FROM clinical_assessments WHERE patient_id = :patientId ORDER BY assessment_date DESC")
    fun observeAssessmentsForPatient(patientId: Long): Flow<List<ClinicalAssessmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAssessment(entity: ClinicalAssessmentEntity): Long

    @Query("DELETE FROM clinical_assessments WHERE id = :id")
    suspend fun deleteAssessment(id: Long)

    @Query("SELECT * FROM phoneme_inventory_entries WHERE patient_id = :patientId ORDER BY phoneme ASC")
    fun observePhonemeInventory(patientId: Long): Flow<List<PhonemeInventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPhonemeEntry(entity: PhonemeInventoryEntity): Long

    // 3. Therapies & Generalization
    @Query("SELECT * FROM therapy_plans WHERE patient_id = :patientId ORDER BY start_date DESC")
    fun observeTherapyPlansForPatient(patientId: Long): Flow<List<TherapyPlanEntity>>

    @Query("SELECT * FROM therapy_plans ORDER BY start_date DESC")
    fun observeAllTherapyPlans(): Flow<List<TherapyPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTherapyPlan(entity: TherapyPlanEntity): Long

    @Query("UPDATE therapy_plans SET status = :status WHERE id = :id")
    suspend fun updateTherapyPlanStatus(id: Long, status: String)

    @Query("SELECT * FROM generalization_probes WHERE patient_id = :patientId ORDER BY probe_date DESC, created_at DESC")
    fun observeGeneralizationProbes(patientId: Long): Flow<List<GeneralizationProbeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneralizationProbe(entity: GeneralizationProbeEntity): Long

    // 4. Appointments, Waitlist & Referrals
    @Query("SELECT * FROM clinical_appointments ORDER BY date ASC, start_time ASC")
    fun observeAppointments(): Flow<List<ClinicalAppointmentEntity>>

    @Query("SELECT * FROM clinical_appointments WHERE patient_id = :patientId ORDER BY date DESC, start_time ASC")
    fun observeAppointmentsForPatient(patientId: Long): Flow<List<ClinicalAppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppointment(entity: ClinicalAppointmentEntity): Long

    @Query("UPDATE clinical_appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Query("DELETE FROM clinical_appointments WHERE id = :id")
    suspend fun deleteAppointment(id: Long)

    @Query("SELECT * FROM waitlist_referrals ORDER BY is_converted ASC, created_at DESC")
    fun observeWaitlistAndReferrals(): Flow<List<WaitlistAndReferralEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWaitlistEntry(entity: WaitlistAndReferralEntity): Long

    @Query("UPDATE waitlist_referrals SET is_converted = 1, converted_patient_id = :patientId WHERE id = :id")
    suspend fun markWaitlistConverted(id: Long, patientId: Long)

    // 5. Home Practice
    @Query("SELECT * FROM home_practice_tasks WHERE patient_id = :patientId ORDER BY assigned_date DESC")
    fun observeHomePracticeForPatient(patientId: Long): Flow<List<HomePracticeTaskEntity>>

    @Query("SELECT * FROM home_practice_tasks ORDER BY assigned_date DESC")
    fun observeAllHomePractice(): Flow<List<HomePracticeTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertHomePracticeTask(entity: HomePracticeTaskEntity): Long

    @Query("UPDATE home_practice_tasks SET status = :status, difficulty_feedback = :difficulty, family_notes = :notes WHERE id = :id")
    suspend fun updateHomePracticeFeedback(id: Long, status: String, difficulty: String, notes: String)

    // 6. Clinical Documents & SOAP Notes
    @Query("SELECT * FROM clinical_documents WHERE patient_id = :patientId ORDER BY document_date DESC, created_at DESC")
    fun observeDocumentsForPatient(patientId: Long): Flow<List<ClinicalDocumentEntity>>

    @Query("SELECT * FROM clinical_documents ORDER BY document_date DESC, created_at DESC")
    fun observeAllDocuments(): Flow<List<ClinicalDocumentEntity>>

    @Query("SELECT * FROM clinical_documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: Long): ClinicalDocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocument(entity: ClinicalDocumentEntity): Long

    // 7. Financial Billing
    @Query("SELECT * FROM billing_records ORDER BY due_date DESC, created_at DESC")
    fun observeBillingRecords(): Flow<List<BillingRecordEntity>>

    @Query("SELECT * FROM billing_records WHERE patient_id = :patientId ORDER BY due_date DESC")
    fun observeBillingForPatient(patientId: Long): Flow<List<BillingRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBillingRecord(entity: BillingRecordEntity): Long

    @Query("UPDATE billing_records SET status = :status, payment_method = :method, payment_date = :paymentDate, receipt_issued = :receiptIssued WHERE id = :id")
    suspend fun updateBillingStatus(
        id: Long,
        status: String,
        method: String,
        paymentDate: LocalDate?,
        receiptIssued: Boolean
    )

    // 8. Tasks, Stimuli & Audit
    @Query("SELECT * FROM clinical_tasks ORDER BY is_completed ASC, created_at DESC")
    fun observeClinicalTasks(): Flow<List<ClinicalTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClinicalTask(entity: ClinicalTaskEntity): Long

    @Query("UPDATE clinical_tasks SET is_completed = :completed WHERE id = :id")
    suspend fun toggleTaskCompleted(id: Long, completed: Boolean)

    @Query("SELECT * FROM custom_stimuli ORDER BY id DESC")
    fun observeCustomStimuli(): Flow<List<CustomStimulusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomStimulus(entity: CustomStimulusEntity): Long

    @Query("SELECT * FROM audit_events ORDER BY timestamp DESC LIMIT :limit")
    fun observeAuditEvents(limit: Int): Flow<List<AuditEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEvent(entity: AuditEventEntity): Long
}
