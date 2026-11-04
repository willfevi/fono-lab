package com.healthcare.fonolab.data.repository

import com.healthcare.fonolab.data.local.dao.ClinicalPlatformDao
import com.healthcare.fonolab.data.local.entity.*
import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.goal.ClinicalArea
import com.healthcare.fonolab.domain.performance.CueLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClinicalPlatformRepositoryImpl @Inject constructor(
    private val dao: ClinicalPlatformDao,
    private val clock: Clock
) : ClinicalPlatformRepository {

    private val profileFlow = MutableStateFlow(ProfessionalProfile())

    // ========================================================================
    // 1. Anamnesis & Versioning
    // ========================================================================

    override fun observeAnamnesisForPatient(patientId: Long): Flow<AnamnesisRecord?> =
        dao.observeAnamnesisForPatient(patientId).map { it?.toDomain() }

    override fun observeAllAnamneses(): Flow<List<AnamnesisRecord>> =
        dao.observeAllAnamneses().map { list -> list.map { it.toDomain() } }

    override fun observeAnamnesisVersions(patientId: Long): Flow<List<AnamnesisVersion>> =
        dao.observeAnamnesisVersions(patientId).map { list -> list.map { it.toDomain() } }

    override suspend fun getAnamnesisForPatient(patientId: Long): AnamnesisRecord? =
        dao.getAnamnesisForPatient(patientId)?.toDomain()

    override suspend fun saveAnamnesis(record: AnamnesisRecord, changeSummary: String): Long {
        val existing = dao.getAnamnesisForPatient(record.patientId)
        val payload = encodeSections(record.sections)
        val now = clock.instant()
        val entity = AnamnesisEntity(
            id = existing?.id ?: record.id,
            patientId = record.patientId,
            status = record.status.name,
            informantName = record.informantName,
            informantKinship = record.informantKinship,
            referredBy = record.referredBy,
            anamnesisDate = record.anamnesisDate,
            sectionsPayload = payload,
            generalSummary = record.generalSummary,
            versionNumber = record.versionNumber,
            createdAt = existing?.createdAt ?: record.createdAt,
            updatedAt = now
        )
        val savedId = dao.upsertAnamnesis(entity)
        val finalId = if (entity.id != 0L) entity.id else savedId
        dao.insertAnamnesisVersion(
            AnamnesisVersionEntity(
                anamnesisId = finalId,
                patientId = record.patientId,
                versionNumber = record.versionNumber,
                snapshotJson = payload,
                changeSummary = changeSummary,
                savedAt = now
            )
        )
        return finalId
    }

    // ========================================================================
    // 2. Assessments & Phonology
    // ========================================================================

    override fun observeAssessmentsForPatient(patientId: Long): Flow<List<ClinicalAssessment>> =
        dao.observeAssessmentsForPatient(patientId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveAssessment(assessment: ClinicalAssessment): Long =
        dao.upsertAssessment(
            ClinicalAssessmentEntity(
                id = assessment.id,
                patientId = assessment.patientId,
                title = assessment.title,
                clinicalArea = assessment.clinicalArea.name,
                protocolOrInstrument = assessment.protocolOrInstrument,
                assessmentDate = assessment.assessmentDate,
                baselineScorePercentage = assessment.baselineScorePercentage,
                currentScorePercentage = assessment.currentScorePercentage,
                findingsSummary = assessment.findingsSummary,
                clinicalHypothesis = assessment.clinicalHypothesis,
                recommendations = assessment.recommendations,
                status = assessment.status.name,
                createdAt = assessment.createdAt,
                updatedAt = assessment.updatedAt
            )
        )

    override suspend fun deleteAssessment(id: Long) = dao.deleteAssessment(id)

    override fun observePhonemeInventory(patientId: Long): Flow<List<PhonemeInventoryEntry>> =
        dao.observePhonemeInventory(patientId).map { list -> list.map { it.toDomain() } }

    override suspend fun upsertPhonemeEntry(entry: PhonemeInventoryEntry): Long =
        dao.upsertPhonemeEntry(
            PhonemeInventoryEntity(
                id = entry.id,
                patientId = entry.patientId,
                phoneme = entry.phoneme,
                position = entry.position.name,
                status = entry.status.name,
                errorType = entry.errorType.name,
                isStimulable = entry.isStimulable,
                bestCueLevel = entry.bestCueLevel.name,
                sampleWords = entry.sampleWords,
                clinicianNotes = entry.clinicianNotes,
                updatedAt = entry.updatedAt
            )
        )

    // ========================================================================
    // 3. Therapies & Generalization Probes
    // ========================================================================

    override fun observeTherapyPlansForPatient(patientId: Long): Flow<List<TherapyPlan>> =
        dao.observeTherapyPlansForPatient(patientId).map { list -> list.map { it.toDomain() } }

    override fun observeAllTherapyPlans(): Flow<List<TherapyPlan>> =
        dao.observeAllTherapyPlans().map { list -> list.map { it.toDomain() } }

    override suspend fun saveTherapyPlan(plan: TherapyPlan): Long =
        dao.upsertTherapyPlan(
            TherapyPlanEntity(
                id = plan.id,
                patientId = plan.patientId,
                title = plan.title,
                clinicalArea = plan.clinicalArea.name,
                generalObjective = plan.generalObjective,
                specificObjectives = plan.specificObjectives,
                targetSkillsOrPhonemes = plan.targetSkillsOrPhonemes,
                weeklyFrequency = plan.weeklyFrequency,
                sessionDurationMinutes = plan.sessionDurationMinutes,
                startDate = plan.startDate,
                expectedEndDate = plan.expectedEndDate,
                status = plan.status.name,
                clinicalNotes = plan.clinicalNotes,
                createdAt = plan.createdAt,
                updatedAt = plan.updatedAt
            )
        )

    override suspend fun updateTherapyPlanStatus(id: Long, status: TherapyPlanStatus) =
        dao.updateTherapyPlanStatus(id, status.name)

    override fun observeGeneralizationProbes(patientId: Long): Flow<List<GeneralizationProbe>> =
        dao.observeGeneralizationProbes(patientId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertGeneralizationProbe(probe: GeneralizationProbe): Long =
        dao.insertGeneralizationProbe(
            GeneralizationProbeEntity(
                id = probe.id,
                patientId = probe.patientId,
                goalId = probe.goalId,
                targetSkillOrPhoneme = probe.targetSkillOrPhoneme,
                context = probe.context.name,
                interlocutor = probe.interlocutor.name,
                stage = probe.stage.name,
                accuracyPercentage = probe.accuracyPercentage,
                isSpontaneous = probe.isSpontaneous,
                probeDate = probe.probeDate,
                notes = probe.notes,
                createdAt = probe.createdAt
            )
        )

    // ========================================================================
    // 4. Schedule, Waitlist & Referrals
    // ========================================================================

    override fun observeAppointments(): Flow<List<ClinicalAppointment>> =
        dao.observeAppointments().map { list -> list.map { it.toDomain() } }

    override fun observeAppointmentsForPatient(patientId: Long): Flow<List<ClinicalAppointment>> =
        dao.observeAppointmentsForPatient(patientId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveAppointment(appointment: ClinicalAppointment): Long =
        dao.upsertAppointment(
            ClinicalAppointmentEntity(
                id = appointment.id,
                patientId = appointment.patientId,
                patientName = appointment.patientName,
                linkedSessionId = appointment.linkedSessionId,
                date = appointment.date,
                startTime = appointment.startTime,
                endTime = appointment.endTime,
                serviceType = appointment.serviceType,
                recurrence = appointment.recurrence.name,
                status = appointment.status.name,
                notes = appointment.notes,
                createdAt = appointment.createdAt
            )
        )

    override suspend fun updateAppointmentStatus(id: Long, status: AppointmentStatus) =
        dao.updateAppointmentStatus(id, status.name)

    override suspend fun deleteAppointment(id: Long) = dao.deleteAppointment(id)

    override fun observeWaitlistAndReferrals(): Flow<List<WaitlistAndReferralEntry>> =
        dao.observeWaitlistAndReferrals().map { list -> list.map { it.toDomain() } }

    override suspend fun saveWaitlistEntry(entry: WaitlistAndReferralEntry): Long =
        dao.upsertWaitlistEntry(
            WaitlistAndReferralEntity(
                id = entry.id,
                candidateName = entry.candidateName,
                birthDateText = entry.birthDateText,
                guardianContact = entry.guardianContact,
                referredBy = entry.referredBy,
                mainComplaint = entry.mainComplaint,
                preferredSchedule = entry.preferredSchedule,
                urgency = entry.urgency.name,
                isConvertedToPatient = entry.isConvertedToPatient,
                convertedPatientId = entry.convertedPatientId,
                createdAt = entry.createdAt
            )
        )

    override suspend fun markWaitlistConverted(id: Long, patientId: Long) =
        dao.markWaitlistConverted(id, patientId)

    // ========================================================================
    // 5. Home Practice
    // ========================================================================

    override fun observeHomePracticeForPatient(patientId: Long): Flow<List<HomePracticeTask>> =
        dao.observeHomePracticeForPatient(patientId).map { list -> list.map { it.toDomain() } }

    override fun observeAllHomePractice(): Flow<List<HomePracticeTask>> =
        dao.observeAllHomePractice().map { list -> list.map { it.toDomain() } }

    override suspend fun saveHomePracticeTask(task: HomePracticeTask): Long =
        dao.upsertHomePracticeTask(
            HomePracticeTaskEntity(
                id = task.id,
                patientId = task.patientId,
                title = task.title,
                linkedGameId = task.linkedGameId,
                targetSkill = task.targetSkill,
                caregiverInstructions = task.caregiverInstructions,
                recommendedFrequency = task.recommendedFrequency,
                assignedDate = task.assignedDate,
                dueDate = task.dueDate,
                status = task.status.name,
                difficultyFeedback = task.difficultyFeedback.name,
                familyNotes = task.familyNotes,
                createdAt = task.createdAt
            )
        )

    override suspend fun updateHomePracticeFeedback(
        id: Long,
        status: HomePracticeStatus,
        difficulty: HomeDifficultyFeedback,
        familyNotes: String
    ) = dao.updateHomePracticeFeedback(id, status.name, difficulty.name, familyNotes)

    // ========================================================================
    // 6. Clinical Documents & SOAP Notes
    // ========================================================================

    override fun observeDocumentsForPatient(patientId: Long): Flow<List<ClinicalDocument>> =
        dao.observeDocumentsForPatient(patientId).map { list -> list.map { it.toDomain() } }

    override fun observeAllDocuments(): Flow<List<ClinicalDocument>> =
        dao.observeAllDocuments().map { list -> list.map { it.toDomain() } }

    override suspend fun saveDocument(document: ClinicalDocument): Long =
        dao.upsertDocument(
            ClinicalDocumentEntity(
                id = document.id,
                patientId = document.patientId,
                sessionId = document.sessionId,
                category = document.category.name,
                title = document.title,
                documentDate = document.documentDate,
                professionalName = document.professionalName,
                professionalCrfa = document.professionalCrfa,
                soapSubjective = document.soapSubjective,
                soapObjective = document.soapObjective,
                soapAssessment = document.soapAssessment,
                soapPlan = document.soapPlan,
                bodyContent = document.bodyContent,
                addendumNotes = document.addendumNotes,
                isSignedAndLocked = document.isSignedAndLocked,
                createdAt = document.createdAt,
                updatedAt = document.updatedAt
            )
        )

    override suspend fun appendDocumentAddendum(id: Long, addendumText: String) {
        val current = dao.getDocumentById(id) ?: return
        val combined = if (current.addendumNotes.isBlank()) addendumText else "${current.addendumNotes}\n$addendumText"
        dao.upsertDocument(current.copy(addendumNotes = combined, updatedAt = clock.instant()))
    }

    // ========================================================================
    // 7. Financial Billing
    // ========================================================================

    override fun observeBillingRecords(): Flow<List<BillingRecord>> =
        dao.observeBillingRecords().map { list -> list.map { it.toDomain() } }

    override fun observeBillingForPatient(patientId: Long): Flow<List<BillingRecord>> =
        dao.observeBillingForPatient(patientId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveBillingRecord(record: BillingRecord): Long =
        dao.upsertBillingRecord(
            BillingRecordEntity(
                id = record.id,
                patientId = record.patientId,
                patientName = record.patientName,
                sessionId = record.sessionId,
                description = record.description,
                amountCents = record.amountCents,
                dueDate = record.dueDate,
                paymentDate = record.paymentDate,
                paymentMethod = record.paymentMethod.name,
                status = record.status.name,
                receiptIssued = record.receiptIssued,
                notes = record.notes,
                createdAt = record.createdAt
            )
        )

    override suspend fun updateBillingStatus(id: Long, status: BillingStatus, paymentMethod: PaymentMethod) {
        val paidDate = if (status == BillingStatus.PAID) LocalDate.now(clock) else null
        dao.updateBillingStatus(id, status.name, paymentMethod.name, paidDate, status == BillingStatus.PAID)
    }

    // ========================================================================
    // 8. Tasks, Stimuli, Audit & Profile
    // ========================================================================

    override fun observeClinicalTasks(): Flow<List<ClinicalTask>> =
        dao.observeClinicalTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun saveClinicalTask(task: ClinicalTask): Long =
        dao.upsertClinicalTask(
            ClinicalTaskEntity(
                id = task.id,
                patientId = task.patientId,
                title = task.title,
                description = task.description,
                dueDate = task.dueDate,
                priority = task.priority.name,
                isCompleted = task.isCompleted,
                category = task.category,
                createdAt = task.createdAt
            )
        )

    override suspend fun toggleTaskCompleted(id: Long, completed: Boolean) =
        dao.toggleTaskCompleted(id, completed)

    override fun observeCustomStimuli(): Flow<List<TherapeuticStimulus>> =
        dao.observeCustomStimuli().map { list -> list.map { it.toDomain() } }

    override suspend fun saveCustomStimulus(stimulus: TherapeuticStimulus): Long =
        dao.insertCustomStimulus(
            CustomStimulusEntity(
                id = stimulus.id,
                word = stimulus.word,
                targetPhoneme = stimulus.targetPhoneme,
                phonemePosition = stimulus.phonemePosition.name,
                syllableDivision = stimulus.syllableDivision,
                syllableCount = stimulus.syllableCount,
                semanticCategory = stimulus.semanticCategory,
                difficulty = stimulus.difficulty.name,
                iconEmoji = stimulus.iconEmoji,
                exampleSentence = stimulus.exampleSentence,
                isFavorite = stimulus.isFavorite
            )
        )

    override fun observeAuditEvents(limit: Int): Flow<List<AuditEvent>> =
        dao.observeAuditEvents(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun recordAudit(entityType: String, entityId: Long, action: String, description: String) {
        dao.insertAuditEvent(
            AuditEventEntity(
                entityType = entityType,
                entityId = entityId,
                action = action,
                description = description,
                timestamp = clock.instant()
            )
        )
    }

    override fun observeProfessionalProfile(): Flow<ProfessionalProfile> = profileFlow.asStateFlow()

    override suspend fun updateProfessionalProfile(profile: ProfessionalProfile) {
        profileFlow.value = profile
    }

    // ========================================================================
    // Mappers & Section Encoding
    // ========================================================================

    private fun encodeSections(sections: List<AnamnesisSectionContent>): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        return sections.joinToString(";") { s ->
            val f = encoder.encodeToString(s.familyReport.toByteArray(Charsets.UTF_8))
            val c = encoder.encodeToString(s.clinicianObservation.toByteArray(Charsets.UTF_8))
            val q = encoder.encodeToString(s.quickStatus.toByteArray(Charsets.UTF_8))
            "${s.sectionType.name}|$f|$c|$q"
        }
    }

    private fun decodeSections(payload: String): List<AnamnesisSectionContent> {
        val decoder = Base64.getUrlDecoder()
        val parsedByType = mutableMapOf<AnamnesisSectionType, AnamnesisSectionContent>()
        if (payload.isNotBlank()) {
            payload.split(";").forEach { entry ->
                val parts = entry.split("|")
                if (parts.size >= 4) {
                    val type = runCatching { AnamnesisSectionType.valueOf(parts[0]) }.getOrNull()
                    if (type != null) {
                        val f = runCatching { String(decoder.decode(parts[1]), Charsets.UTF_8) }.getOrDefault("")
                        val c = runCatching { String(decoder.decode(parts[2]), Charsets.UTF_8) }.getOrDefault("")
                        val q = runCatching { String(decoder.decode(parts[3]), Charsets.UTF_8) }.getOrDefault("")
                        parsedByType[type] = AnamnesisSectionContent(type, f, c, q)
                    }
                }
            }
        }
        return AnamnesisSectionType.entries.map { type ->
            parsedByType[type] ?: AnamnesisSectionContent(sectionType = type)
        }
    }

    private fun AnamnesisEntity.toDomain() = AnamnesisRecord(
        id = id,
        patientId = patientId,
        status = runCatching { AnamnesisStatus.valueOf(status) }.getOrDefault(AnamnesisStatus.IN_PROGRESS),
        informantName = informantName,
        informantKinship = informantKinship,
        referredBy = referredBy,
        anamnesisDate = anamnesisDate,
        sections = decodeSections(sectionsPayload),
        generalSummary = generalSummary,
        versionNumber = versionNumber,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun AnamnesisVersionEntity.toDomain() = AnamnesisVersion(
        id = id,
        anamnesisId = anamnesisId,
        patientId = patientId,
        versionNumber = versionNumber,
        snapshotJson = snapshotJson,
        changeSummary = changeSummary,
        savedAt = savedAt
    )

    private fun ClinicalAssessmentEntity.toDomain() = ClinicalAssessment(
        id = id,
        patientId = patientId,
        title = title,
        clinicalArea = runCatching { ClinicalArea.valueOf(clinicalArea) }.getOrDefault(ClinicalArea.SPEECH_AND_PHONOLOGY),
        protocolOrInstrument = protocolOrInstrument,
        assessmentDate = assessmentDate,
        baselineScorePercentage = baselineScorePercentage,
        currentScorePercentage = currentScorePercentage,
        findingsSummary = findingsSummary,
        clinicalHypothesis = clinicalHypothesis,
        recommendations = recommendations,
        status = runCatching { AssessmentStatus.valueOf(status) }.getOrDefault(AssessmentStatus.COMPLETED),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun PhonemeInventoryEntity.toDomain() = PhonemeInventoryEntry(
        id = id,
        patientId = patientId,
        phoneme = phoneme,
        position = runCatching { PhonemeWordPosition.valueOf(position) }.getOrDefault(PhonemeWordPosition.INITIAL),
        status = runCatching { PhonemeAcquisitionStatus.valueOf(status) }.getOrDefault(PhonemeAcquisitionStatus.NOT_EVALUATED),
        errorType = runCatching { PhonologicalErrorType.valueOf(errorType) }.getOrDefault(PhonologicalErrorType.NONE),
        isStimulable = isStimulable,
        bestCueLevel = runCatching { CueLevel.valueOf(bestCueLevel) }.getOrDefault(CueLevel.NONE),
        sampleWords = sampleWords,
        clinicianNotes = clinicianNotes,
        updatedAt = updatedAt
    )

    private fun TherapyPlanEntity.toDomain() = TherapyPlan(
        id = id,
        patientId = patientId,
        title = title,
        clinicalArea = runCatching { ClinicalArea.valueOf(clinicalArea) }.getOrDefault(ClinicalArea.SPEECH_AND_PHONOLOGY),
        generalObjective = generalObjective,
        specificObjectives = specificObjectives,
        targetSkillsOrPhonemes = targetSkillsOrPhonemes,
        weeklyFrequency = weeklyFrequency,
        sessionDurationMinutes = sessionDurationMinutes,
        startDate = startDate,
        expectedEndDate = expectedEndDate,
        status = runCatching { TherapyPlanStatus.valueOf(status) }.getOrDefault(TherapyPlanStatus.ACTIVE),
        clinicalNotes = clinicalNotes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun GeneralizationProbeEntity.toDomain() = GeneralizationProbe(
        id = id,
        patientId = patientId,
        goalId = goalId,
        targetSkillOrPhoneme = targetSkillOrPhoneme,
        context = runCatching { GeneralizationContext.valueOf(context) }.getOrDefault(GeneralizationContext.CLINIC),
        interlocutor = runCatching { InterlocutorType.valueOf(interlocutor) }.getOrDefault(InterlocutorType.THERAPIST),
        stage = runCatching { AcquisitionStage.valueOf(stage) }.getOrDefault(AcquisitionStage.IN_ACQUISITION),
        accuracyPercentage = accuracyPercentage,
        isSpontaneous = isSpontaneous,
        probeDate = probeDate,
        notes = notes,
        createdAt = createdAt
    )

    private fun ClinicalAppointmentEntity.toDomain() = ClinicalAppointment(
        id = id,
        patientId = patientId,
        patientName = patientName,
        linkedSessionId = linkedSessionId,
        date = date,
        startTime = startTime,
        endTime = endTime,
        serviceType = serviceType,
        recurrence = runCatching { AppointmentRecurrence.valueOf(recurrence) }.getOrDefault(AppointmentRecurrence.SINGLE),
        status = runCatching { AppointmentStatus.valueOf(status) }.getOrDefault(AppointmentStatus.SCHEDULED),
        notes = notes,
        createdAt = createdAt
    )

    private fun WaitlistAndReferralEntity.toDomain() = WaitlistAndReferralEntry(
        id = id,
        candidateName = candidateName,
        birthDateText = birthDateText,
        guardianContact = guardianContact,
        referredBy = referredBy,
        mainComplaint = mainComplaint,
        preferredSchedule = preferredSchedule,
        urgency = runCatching { ReferralUrgency.valueOf(urgency) }.getOrDefault(ReferralUrgency.MEDIUM),
        isConvertedToPatient = isConvertedToPatient,
        convertedPatientId = convertedPatientId,
        createdAt = createdAt
    )

    private fun HomePracticeTaskEntity.toDomain() = HomePracticeTask(
        id = id,
        patientId = patientId,
        title = title,
        linkedGameId = linkedGameId,
        targetSkill = targetSkill,
        caregiverInstructions = caregiverInstructions,
        recommendedFrequency = recommendedFrequency,
        assignedDate = assignedDate,
        dueDate = dueDate,
        status = runCatching { HomePracticeStatus.valueOf(status) }.getOrDefault(HomePracticeStatus.PENDING),
        difficultyFeedback = runCatching { HomeDifficultyFeedback.valueOf(difficultyFeedback) }.getOrDefault(HomeDifficultyFeedback.NOT_RATED),
        familyNotes = familyNotes,
        createdAt = createdAt
    )

    private fun ClinicalDocumentEntity.toDomain() = ClinicalDocument(
        id = id,
        patientId = patientId,
        sessionId = sessionId,
        category = runCatching { DocumentCategory.valueOf(category) }.getOrDefault(DocumentCategory.SOAP_EVOLUTION),
        title = title,
        documentDate = documentDate,
        professionalName = professionalName,
        professionalCrfa = professionalCrfa,
        soapSubjective = soapSubjective,
        soapObjective = soapObjective,
        soapAssessment = soapAssessment,
        soapPlan = soapPlan,
        bodyContent = bodyContent,
        addendumNotes = addendumNotes,
        isSignedAndLocked = isSignedAndLocked,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BillingRecordEntity.toDomain() = BillingRecord(
        id = id,
        patientId = patientId,
        patientName = patientName,
        sessionId = sessionId,
        description = description,
        amountCents = amountCents,
        dueDate = dueDate,
        paymentDate = paymentDate,
        paymentMethod = runCatching { PaymentMethod.valueOf(paymentMethod) }.getOrDefault(PaymentMethod.PIX),
        status = runCatching { BillingStatus.valueOf(status) }.getOrDefault(BillingStatus.PENDING),
        receiptIssued = receiptIssued,
        notes = notes,
        createdAt = createdAt
    )

    private fun ClinicalTaskEntity.toDomain() = ClinicalTask(
        id = id,
        patientId = patientId,
        title = title,
        description = description,
        dueDate = dueDate,
        priority = runCatching { TaskPriority.valueOf(priority) }.getOrDefault(TaskPriority.MEDIUM),
        isCompleted = isCompleted,
        category = category,
        createdAt = createdAt
    )

    private fun CustomStimulusEntity.toDomain() = TherapeuticStimulus(
        id = 1000L + id,
        word = word,
        targetPhoneme = targetPhoneme,
        phonemePosition = runCatching { PhonemePosition.valueOf(phonemePosition) }.getOrDefault(PhonemePosition.INITIAL),
        syllableDivision = syllableDivision,
        syllableCount = syllableCount,
        semanticCategory = semanticCategory,
        difficulty = runCatching { DifficultyLevel.valueOf(difficulty) }.getOrDefault(DifficultyLevel.EASY),
        iconEmoji = iconEmoji,
        exampleSentence = exampleSentence,
        isCustom = true,
        isFavorite = isFavorite
    )

    private fun AuditEventEntity.toDomain() = AuditEvent(
        id = id,
        entityType = entityType,
        entityId = entityId,
        action = action,
        description = description,
        timestamp = timestamp
    )
}
