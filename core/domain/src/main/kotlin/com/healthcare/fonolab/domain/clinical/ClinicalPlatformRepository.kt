package com.healthcare.fonolab.domain.clinical

import kotlinx.coroutines.flow.Flow

/**
 * Persistence contract for the complete clinical and administrative modules of FonoLab.
 * Implemented by Room in `:core:data` and designed for multiplatform KMP/SQLDelight/Room KMP compatibility.
 */
interface ClinicalPlatformRepository {

    // 1. Anamnesis & Versioning
    fun observeAnamnesisForPatient(patientId: Long): Flow<AnamnesisRecord?>
    fun observeAllAnamneses(): Flow<List<AnamnesisRecord>>
    fun observeAnamnesisVersions(patientId: Long): Flow<List<AnamnesisVersion>>
    suspend fun getAnamnesisForPatient(patientId: Long): AnamnesisRecord?
    suspend fun saveAnamnesis(record: AnamnesisRecord, changeSummary: String): Long

    // 2. Assessments & Phonetic-Phonological Map
    fun observeAssessmentsForPatient(patientId: Long): Flow<List<ClinicalAssessment>>
    suspend fun saveAssessment(assessment: ClinicalAssessment): Long
    suspend fun deleteAssessment(id: Long)

    fun observePhonemeInventory(patientId: Long): Flow<List<PhonemeInventoryEntry>>
    suspend fun upsertPhonemeEntry(entry: PhonemeInventoryEntry): Long

    // 3. Therapies & Generalization Probes
    fun observeTherapyPlansForPatient(patientId: Long): Flow<List<TherapyPlan>>
    fun observeAllTherapyPlans(): Flow<List<TherapyPlan>>
    suspend fun saveTherapyPlan(plan: TherapyPlan): Long
    suspend fun updateTherapyPlanStatus(id: Long, status: TherapyPlanStatus)

    fun observeGeneralizationProbes(patientId: Long): Flow<List<GeneralizationProbe>>
    suspend fun insertGeneralizationProbe(probe: GeneralizationProbe): Long

    // 4. Schedule, Waitlist & Referrals
    fun observeAppointments(): Flow<List<ClinicalAppointment>>
    fun observeAppointmentsForPatient(patientId: Long): Flow<List<ClinicalAppointment>>
    suspend fun saveAppointment(appointment: ClinicalAppointment): Long
    suspend fun updateAppointmentStatus(id: Long, status: AppointmentStatus)
    suspend fun deleteAppointment(id: Long)

    fun observeWaitlistAndReferrals(): Flow<List<WaitlistAndReferralEntry>>
    suspend fun saveWaitlistEntry(entry: WaitlistAndReferralEntry): Long
    suspend fun markWaitlistConverted(id: Long, patientId: Long)

    // 5. Home Practice & Caregiver Orientation
    fun observeHomePracticeForPatient(patientId: Long): Flow<List<HomePracticeTask>>
    fun observeAllHomePractice(): Flow<List<HomePracticeTask>>
    suspend fun saveHomePracticeTask(task: HomePracticeTask): Long
    suspend fun updateHomePracticeFeedback(
        id: Long,
        status: HomePracticeStatus,
        difficulty: HomeDifficultyFeedback,
        familyNotes: String
    )

    // 6. Clinical Documents & SOAP Notes
    fun observeDocumentsForPatient(patientId: Long): Flow<List<ClinicalDocument>>
    fun observeAllDocuments(): Flow<List<ClinicalDocument>>
    suspend fun saveDocument(document: ClinicalDocument): Long
    suspend fun appendDocumentAddendum(id: Long, addendumText: String)

    // 7. Financial Management (BRL)
    fun observeBillingRecords(): Flow<List<BillingRecord>>
    fun observeBillingForPatient(patientId: Long): Flow<List<BillingRecord>>
    suspend fun saveBillingRecord(record: BillingRecord): Long
    suspend fun updateBillingStatus(id: Long, status: BillingStatus, paymentMethod: PaymentMethod)

    // 8. Tasks, Audit, Stimuli & Profile
    fun observeClinicalTasks(): Flow<List<ClinicalTask>>
    suspend fun saveClinicalTask(task: ClinicalTask): Long
    suspend fun toggleTaskCompleted(id: Long, completed: Boolean)

    fun observeCustomStimuli(): Flow<List<TherapeuticStimulus>>
    suspend fun saveCustomStimulus(stimulus: TherapeuticStimulus): Long

    fun observeAuditEvents(limit: Int = 50): Flow<List<AuditEvent>>
    suspend fun recordAudit(entityType: String, entityId: Long, action: String, description: String)

    fun observeProfessionalProfile(): Flow<ProfessionalProfile>
    suspend fun updateProfessionalProfile(profile: ProfessionalProfile)
}
