package com.healthcare.fonolab.domain.manager

import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.goal.ClinicalArea
import com.healthcare.fonolab.domain.goal.ClinicalGoal
import com.healthcare.fonolab.domain.goal.ClinicalGoalDraft
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.goal.GoalStatus
import com.healthcare.fonolab.domain.goal.SaveGoalUseCase
import com.healthcare.fonolab.domain.patient.ChangePatientStatusUseCase
import com.healthcare.fonolab.domain.patient.DeletePatientUseCase
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientDraft
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.patient.PatientValidator
import com.healthcare.fonolab.domain.patient.SavePatientUseCase
import com.healthcare.fonolab.domain.performance.AttemptEvaluation
import com.healthcare.fonolab.domain.performance.ClinicianRating
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.performance.ObjectivePerformanceSummary
import com.healthcare.fonolab.domain.performance.PerformanceCalculator
import com.healthcare.fonolab.domain.session.CancelSessionUseCase
import com.healthcare.fonolab.domain.session.FinishSessionUseCase
import com.healthcare.fonolab.domain.session.PauseSessionUseCase
import com.healthcare.fonolab.domain.session.PlanSessionUseCase
import com.healthcare.fonolab.domain.session.SessionStatus
import com.healthcare.fonolab.domain.session.SessionWithDetails
import com.healthcare.fonolab.domain.session.StartPlannedSessionUseCase
import com.healthcare.fonolab.domain.session.StartSessionUseCase
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

// ============================================================================
// 1. PATIENT MANAGER & FORM MANAGER
// ============================================================================

class PatientManager(
    private val repository: PatientRepository,
    private val savePatientUseCase: SavePatientUseCase,
    private val changeStatusUseCase: ChangePatientStatusUseCase,
    private val deletePatientUseCase: DeletePatientUseCase,
    private val auditManager: AuditManager,
    private val dateTimeManager: DateTimeManager
) {
    fun observePatients(filter: PatientFilter = PatientFilter()): Flow<List<Patient>> =
        repository.observePatients(filter)

    fun observePatient(id: Long): Flow<Patient?> = repository.observePatient(id)

    suspend fun getPatient(id: Long): Patient? = repository.getPatient(id)

    suspend fun savePatient(patientId: Long?, draft: PatientDraft, isDemo: Boolean = false): DomainResult<Long> {
        val res = savePatientUseCase(patientId, draft, isDemo)
        if (res is DomainResult.Success) {
            val action = if (patientId == null) "CREATE" else "UPDATE"
            auditManager.log("PATIENT", res.value, action, "Paciente '${draft.fullName.trim()}' salvo com sucesso.")
        }
        return res
    }

    suspend fun setStatus(patientId: Long, status: PatientStatus): DomainResult<Unit> {
        val res = changeStatusUseCase(patientId, status)
        if (res is DomainResult.Success) {
            auditManager.log("PATIENT", patientId, "STATUS_${status.name}", "Status do paciente alterado para ${status.displayName}.")
        }
        return res
    }

    suspend fun deletePatient(patientId: Long): DomainResult<Unit> {
        auditManager.log("PATIENT", patientId, "DELETE", "Prontuário #$patientId excluído permanentemente.")
        return deletePatientUseCase(patientId)
    }

    fun formatPatientAge(patient: Patient): String =
        dateTimeManager.calculateAge(patient.birthDate).toFormattedBr()
}

class FormManager(
    private val patientValidator: PatientValidator,
    private val dateTimeManager: DateTimeManager
) {
    val quickAnswerOptions: List<String> = listOf(
        "Dentro do esperado",
        "Alterado / Em observação",
        "Não informado",
        "Não se aplica",
        "Não sabe informar"
    )

    fun normalizeDateInput(raw: String): String = dateTimeManager.normalizeBrDateInput(raw)

    fun validatePatientDraft(draft: PatientDraft) = patientValidator.validate(draft)
}

// ============================================================================
// 2. ANAMNESIS MANAGER (12 SECTIONS, PARTIAL SAVE & VERSIONING)
// ============================================================================

class AnamnesisManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeAnamnesis(patientId: Long): Flow<AnamnesisRecord?> =
        clinicalRepository.observeAnamnesisForPatient(patientId)

    fun observeVersions(patientId: Long): Flow<List<AnamnesisVersion>> =
        clinicalRepository.observeAnamnesisVersions(patientId)

    fun createEmptyTemplate(patientId: Long, defaultInformant: String = ""): AnamnesisRecord {
        val now = dateTimeManager.nowInstant()
        val sections = AnamnesisSectionType.entries.map { type ->
            AnamnesisSectionContent(sectionType = type)
        }
        return AnamnesisRecord(
            id = 0L,
            patientId = patientId,
            status = AnamnesisStatus.NOT_STARTED,
            informantName = defaultInformant,
            informantKinship = "Mãe / Pai / Responsável",
            referredBy = "Demanda espontânea",
            anamnesisDate = dateTimeManager.today(),
            sections = sections,
            generalSummary = "",
            versionNumber = 1,
            createdAt = now,
            updatedAt = now
        )
    }

    suspend fun saveAnamnesis(
        record: AnamnesisRecord,
        markCompleted: Boolean = false,
        changeSummary: String = "Atualização clínica da anamnese"
    ): Long {
        val filledCount = record.sections.count { it.isFilled }
        val resolvedStatus = when {
            markCompleted -> AnamnesisStatus.COMPLETED
            filledCount > 0 || record.generalSummary.isNotBlank() -> AnamnesisStatus.IN_PROGRESS
            else -> AnamnesisStatus.NOT_STARTED
        }
        val nextVersion = if (record.id == 0L) 1 else record.versionNumber + 1
        val updated = record.copy(
            status = resolvedStatus,
            versionNumber = nextVersion,
            updatedAt = dateTimeManager.nowInstant()
        )
        val id = clinicalRepository.saveAnamnesis(updated, changeSummary)
        auditManager.log(
            "ANAMNESIS",
            id,
            if (markCompleted) "COMPLETE" else "SAVE_DRAFT",
            "Anamnese (v$nextVersion) salva com status ${resolvedStatus.displayName}."
        )
        return id
    }
}

// ============================================================================
// 3. ASSESSMENT MANAGER & PHONOLOGY MANAGER
// ============================================================================

class AssessmentManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeAssessments(patientId: Long): Flow<List<ClinicalAssessment>> =
        clinicalRepository.observeAssessmentsForPatient(patientId)

    suspend fun saveAssessment(
        patientId: Long,
        title: String,
        area: ClinicalArea,
        protocol: String,
        dateTextBr: String,
        baselineScore: Int?,
        currentScore: Int?,
        findings: String,
        hypothesis: String,
        recommendations: String,
        status: AssessmentStatus = AssessmentStatus.COMPLETED
    ): Long {
        val date = dateTimeManager.parseDate(dateTextBr) ?: dateTimeManager.today()
        val now = dateTimeManager.nowInstant()
        val assessment = ClinicalAssessment(
            id = 0L,
            patientId = patientId,
            title = title.trim().ifEmpty { "Avaliação Fonoaudiológica" },
            clinicalArea = area,
            protocolOrInstrument = protocol.trim().ifEmpty { "Avaliação Clínica Estruturada / ABFW" },
            assessmentDate = date,
            baselineScorePercentage = baselineScore,
            currentScorePercentage = currentScore,
            findingsSummary = findings.trim(),
            clinicalHypothesis = hypothesis.trim(),
            recommendations = recommendations.trim(),
            status = status,
            createdAt = now,
            updatedAt = now
        )
        val id = clinicalRepository.saveAssessment(assessment)
        auditManager.log("ASSESSMENT", id, "CREATE", "Avaliação '${assessment.title}' registrada.")
        return id
    }
}

class PhonologyManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager
) {
    /** Standard Brazilian Portuguese consonantal phoneme inventory for clinical mapping. */
    val standardBrazilianPhonemes: List<Pair<String, String>> = listOf(
        "/p/" to "Pato / Sapo",
        "/b/" to "Bola / Lobo",
        "/t/" to "Tatu / Bota",
        "/d/" to "Dado / Fada",
        "/k/" to "Casa / Bico",
        "/g/" to "Gato / Fogo",
        "/f/" to "Faca / Café",
        "/v/" to "Vaca / Uva",
        "/s/" to "Sapo / Massa",
        "/z/" to "Zebra / Casa",
        "/ʃ/" to "Chave / Caixa",
        "/ʒ/" to "Jaca / Relógio",
        "/m/" to "Mala / Cama",
        "/n/" to "Navio / Banana",
        "/ɲ/" to "Ninho / Galinha",
        "/l/" to "Lua / Bola",
        "/ʎ/" to "Palhaço / Folha",
        "/ɾ/" to "Arara / Barata (Tepe)",
        "/R/" to "Rato / Carro (Vibrante)",
        "CCV /ɾ/" to "Prato / Braço / Trem",
        "CCV /l/" to "Planta / Blusa / Flor"
    )

    fun observeInventory(patientId: Long): Flow<List<PhonemeInventoryEntry>> =
        clinicalRepository.observePhonemeInventory(patientId)

    suspend fun recordPhonemeStatus(
        patientId: Long,
        phoneme: String,
        position: PhonemeWordPosition,
        status: PhonemeAcquisitionStatus,
        errorType: PhonologicalErrorType,
        isStimulable: Boolean,
        bestCue: CueLevel,
        sampleWords: String,
        notes: String = "",
        existingId: Long = 0L
    ): Long {
        return clinicalRepository.upsertPhonemeEntry(
            PhonemeInventoryEntry(
                id = existingId,
                patientId = patientId,
                phoneme = phoneme,
                position = position,
                status = status,
                errorType = errorType,
                isStimulable = isStimulable,
                bestCueLevel = bestCue,
                sampleWords = sampleWords,
                clinicianNotes = notes,
                updatedAt = dateTimeManager.nowInstant()
            )
        )
    }
}

// ============================================================================
// 4. THERAPY MANAGER & GENERALIZATION MANAGER
// ============================================================================

class TherapyManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val goalRepository: ClinicalGoalRepository,
    private val saveGoalUseCase: SaveGoalUseCase,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeTherapyPlans(patientId: Long): Flow<List<TherapyPlan>> =
        clinicalRepository.observeTherapyPlansForPatient(patientId)

    fun observeGoals(patientId: Long): Flow<List<ClinicalGoal>> =
        goalRepository.observeGoalsForPatient(patientId)

    suspend fun createTherapyPlan(
        patientId: Long,
        title: String,
        area: ClinicalArea,
        generalObjective: String,
        specificObjectives: String,
        targetSkills: String,
        weeklyFrequency: Int,
        durationMinutes: Int,
        startDateBr: String,
        expectedEndDateBr: String = "",
        notes: String = ""
    ): Long {
        val now = dateTimeManager.nowInstant()
        val start = dateTimeManager.parseDate(startDateBr) ?: dateTimeManager.today()
        val end = dateTimeManager.parseDate(expectedEndDateBr)
        val plan = TherapyPlan(
            id = 0L,
            patientId = patientId,
            title = title.trim().ifEmpty { "Terapia de ${area.displayName}" },
            clinicalArea = area,
            generalObjective = generalObjective.trim(),
            specificObjectives = specificObjectives.trim(),
            targetSkillsOrPhonemes = targetSkills.trim(),
            weeklyFrequency = weeklyFrequency.coerceIn(1, 7),
            sessionDurationMinutes = durationMinutes.coerceIn(15, 120),
            startDate = start,
            expectedEndDate = end,
            status = TherapyPlanStatus.ACTIVE,
            clinicalNotes = notes.trim(),
            createdAt = now,
            updatedAt = now
        )
        val planId = clinicalRepository.saveTherapyPlan(plan)
        // Also create a corresponding ClinicalGoal if targetSkills/generalObjective are provided
        if (generalObjective.isNotBlank() || targetSkills.isNotBlank()) {
            saveGoalUseCase(
                patientId = patientId,
                goalId = null,
                draft = ClinicalGoalDraft(
                    clinicalArea = area,
                    description = generalObjective.ifBlank { title },
                    targetSkill = targetSkills.ifBlank { area.displayName },
                    targetAccuracyPercentage = 80,
                    masteryCriterionDescription = "80% de acerto em 3 sessões consecutivas",
                    clinicalNotes = specificObjectives
                )
            )
        }
        auditManager.log("THERAPY", planId, "CREATE", "Plano terapêutico '${plan.title}' criado.")
        return planId
    }

    suspend fun updateStatus(planId: Long, status: TherapyPlanStatus) {
        clinicalRepository.updateTherapyPlanStatus(planId, status)
    }
}

class GeneralizationManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager
) {
    fun observeProbes(patientId: Long): Flow<List<GeneralizationProbe>> =
        clinicalRepository.observeGeneralizationProbes(patientId)

    suspend fun recordProbe(
        patientId: Long,
        goalId: Long?,
        targetSkillOrPhoneme: String,
        context: GeneralizationContext,
        interlocutor: InterlocutorType,
        stage: AcquisitionStage,
        accuracyPercentage: Int,
        isSpontaneous: Boolean,
        notes: String = ""
    ): Long {
        return clinicalRepository.insertGeneralizationProbe(
            GeneralizationProbe(
                id = 0L,
                patientId = patientId,
                goalId = goalId,
                targetSkillOrPhoneme = targetSkillOrPhoneme.trim(),
                context = context,
                interlocutor = interlocutor,
                stage = stage,
                accuracyPercentage = accuracyPercentage.coerceIn(0, 100),
                isSpontaneous = isSpontaneous,
                probeDate = dateTimeManager.today(),
                notes = notes.trim(),
                createdAt = dateTimeManager.nowInstant()
            )
        )
    }
}

// ============================================================================
// 5. SESSION MANAGER & SESSION PLANNING ASSISTANT MANAGER
// ============================================================================

data class SuggestedSessionActivity(
    val gameId: String,
    val gameTitle: String,
    val rationale: String,
    val estimatedMinutes: Int
)

class SessionManager(
    private val sessionRepository: TherapySessionRepository,
    private val planSessionUseCase: PlanSessionUseCase,
    private val startPlannedSessionUseCase: StartPlannedSessionUseCase,
    private val pauseSessionUseCase: PauseSessionUseCase,
    private val startSessionUseCase: StartSessionUseCase,
    private val finishSessionUseCase: FinishSessionUseCase,
    private val cancelSessionUseCase: CancelSessionUseCase,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeSessionsForPatient(patientId: Long): Flow<List<TherapySession>> =
        sessionRepository.observeSessionsForPatient(patientId)

    fun observeRecentSessions(limit: Int = 50): Flow<List<TherapySession>> =
        sessionRepository.observeRecentSessions(limit)

    fun observeActiveSession(): Flow<SessionWithDetails?> =
        sessionRepository.observeActiveSession()

    fun observeSessionDetails(sessionId: Long): Flow<SessionWithDetails?> =
        sessionRepository.observeSession(sessionId)

    /**
     * Saves a PLANNED or DRAFT session in advance WITHOUT starting it.
     */
    suspend fun createPlannedSession(
        patientId: Long,
        scheduledDateTextBr: String,
        linkedGoalIds: List<Long>,
        plannedGameIds: List<String>,
        notes: String,
        summary: String,
        isDraft: Boolean = false
    ): DomainResult<Long> {
        val date = dateTimeManager.parseDate(scheduledDateTextBr) ?: dateTimeManager.today()
        val scheduledInstant = dateTimeManager.toStartOfDayInstant(date)
        val res = planSessionUseCase(
            patientId = patientId,
            scheduledAt = scheduledInstant,
            linkedGoalIds = linkedGoalIds,
            plannedGameIds = plannedGameIds,
            notes = notes,
            summary = summary,
            isDraft = isDraft
        )
        if (res is DomainResult.Success) {
            auditManager.log(
                "SESSION",
                res.value,
                if (isDraft) "SAVE_DRAFT" else "PLAN",
                "Sessão planejada para ${dateTimeManager.formatDateBr(date)} (${plannedGameIds.size} atividades)."
            )
        }
        return res
    }

    suspend fun startPlannedSession(sessionId: Long): DomainResult<Long> {
        val res = startPlannedSessionUseCase(sessionId)
        if (res is DomainResult.Success) {
            auditManager.log("SESSION", sessionId, "START", "Sessão #$sessionId iniciada pelo profissional.")
        }
        return res
    }

    suspend fun startImmediateSession(patientId: Long, linkedGoalIds: List<Long>, isDemo: Boolean = false): DomainResult<Long> =
        startSessionUseCase(patientId, linkedGoalIds, isDemo)

    suspend fun pauseSession(sessionId: Long): DomainResult<Unit> =
        pauseSessionUseCase(sessionId)

    suspend fun finishSession(sessionId: Long, notes: String, summary: String): DomainResult<Unit> {
        val res = finishSessionUseCase(sessionId, notes, summary)
        if (res is DomainResult.Success) {
            auditManager.log("SESSION", sessionId, "COMPLETE", "Sessão #$sessionId concluída.")
        }
        return res
    }

    suspend fun cancelSession(sessionId: Long): DomainResult<Unit> =
        cancelSessionUseCase(sessionId)

    suspend fun duplicateSessionAsPlanned(sourceSessionId: Long): DomainResult<Long> {
        val details = sessionRepository.getSessionWithDetails(sourceSessionId)
            ?: return planSessionUseCase(0L, dateTimeManager.nowInstant())
        val gameIds = details.activities.sortedBy { it.orderIndex }.map { it.gameId }
        val goalIds = details.linkedGoals.map { it.id }
        return planSessionUseCase(
            patientId = details.session.patientId,
            scheduledAt = dateTimeManager.nowInstant(),
            linkedGoalIds = goalIds,
            plannedGameIds = gameIds,
            notes = details.session.clinicalNotes,
            summary = "Cópia planejada da Sessão #${details.session.id}"
        )
    }
}

class SessionPlanningAssistantManager() {
    /**
     * Suggests therapeutic games and activities aligned with the patient's active clinical goals,
     * keeping final decision authority with the speech-language pathologist.
     */
    fun suggestActivitiesForGoals(goals: List<ClinicalGoal>, targetMinutes: Int = 30): List<SuggestedSessionActivity> {
        val suggestions = mutableListOf<SuggestedSessionActivity>()
        val areas = goals.map { it.clinicalArea }.distinct().ifEmpty {
            listOf(ClinicalArea.SPEECH_AND_PHONOLOGY, ClinicalArea.PHONOLOGICAL_AWARENESS, ClinicalArea.EXPRESSIVE_LANGUAGE)
        }

        for (area in areas) {
            when (area) {
                ClinicalArea.SPEECH_AND_PHONOLOGY, ClinicalArea.OROFACIAL_MYOFUNCTIONAL -> {
                    suggestions += SuggestedSessionActivity(
                        "sound_hunt",
                        "Caça ao Som",
                        "Bombardeio auditivo e discriminação de fonemas-alvo em posição inicial/medial.",
                        6
                    )
                    suggestions += SuggestedSessionActivity(
                        "magic_mouth",
                        "Boca Mágica",
                        "Treino articulatório com apoio visual de ponto e modo articulatório.",
                        8
                    )
                    suggestions += SuggestedSessionActivity(
                        "hungry_monster",
                        "Monstrinho Faminto",
                        "Produção rápida de palavras-alvo com registro de hierarquia de pistas.",
                        7
                    )
                }
                ClinicalArea.PHONOLOGICAL_AWARENESS -> {
                    suggestions += SuggestedSessionActivity(
                        "rhyme_detective",
                        "Detetive das Rimas",
                        "Identificação de rimas e aliterações para fortalecimento metafonológico.",
                        7
                    )
                    suggestions += SuggestedSessionActivity(
                        "word_factory",
                        "Fábrica de Palavras",
                        "Síntese e segmentação silábica lúdica.",
                        8
                    )
                }
                ClinicalArea.RECEPTIVE_LANGUAGE, ClinicalArea.MEMORY_AND_ATTENTION -> {
                    suggestions += SuggestedSessionActivity(
                        "space_mission",
                        "Missão Espacial",
                        "Compreensão de instruções simples e compostas com relações espaciais.",
                        7
                    )
                    suggestions += SuggestedSessionActivity(
                        "sound_memory",
                        "Memória Sonora",
                        "Memória de trabalho auditiva e associação som-imagem.",
                        7
                    )
                }
                ClinicalArea.VOCABULARY_AND_SEMANTICS -> {
                    suggestions += SuggestedSessionActivity(
                        "word_market",
                        "Mercado das Palavras",
                        "Categorização semântica e expansão lexical estruturada.",
                        8
                    )
                }
                ClinicalArea.EXPRESSIVE_LANGUAGE, ClinicalArea.MORPHOSYNTAX, ClinicalArea.NARRATIVE_AND_SEQUENCING, ClinicalArea.PRAGMATICS, ClinicalArea.VOICE, ClinicalArea.OTHER -> {
                    suggestions += SuggestedSessionActivity(
                        "sentence_detective",
                        "Detetive das Frases",
                        "Estruturação morfossintática (sujeito + verbo + complemento).",
                        8
                    )
                    suggestions += SuggestedSessionActivity(
                        "story_sequence",
                        "História Embaralhada",
                        "Ordenação temporal de cenas e reconto narrativo.",
                        8
                    )
                }
            }
        }
        return suggestions.distinctBy { it.gameId }.take(maxOf(2, targetMinutes / 7))
    }
}

// ============================================================================
// 6. GAME MANAGER, CLINICAL DATA MANAGER & CONTENT LIBRARY MANAGER
// ============================================================================

class GameManager(
    private val sessionRepository: TherapySessionRepository
) {
    val supportedGameIds: List<String> = listOf(
        "sound_hunt",
        "hungry_monster",
        "word_factory",
        "story_sequence",
        "space_mission",
        "rhyme_detective",
        "magic_mouth",
        "word_market",
        "sentence_detective",
        "sound_memory"
    )

    suspend fun getAttemptsForPatient(patientId: Long): List<GameAttempt> =
        sessionRepository.getAttemptsForPatient(patientId)
}

class ClinicalDataManager() {
    /**
     * Separates ludic game points from real clinical trial-by-trial accuracy and cue hierarchy dependency.
     */
    fun summarizePerformance(attempts: List<GameAttempt>): ObjectivePerformanceSummary =
        PerformanceCalculator.calculate(attempts)

    fun calculateOverallClinicalAccuracy(attempts: List<GameAttempt>): Int {
        if (attempts.isEmpty()) return 0
        var totalEvaluated = 0
        var weightedCorrect = 0f
        for (a in attempts) {
            when (val ev = a.evaluation) {
                is AttemptEvaluation.AutoScored -> {
                    totalEvaluated++
                    if (ev.isCorrect) {
                        weightedCorrect += if (a.cueLevel == CueLevel.NONE) 1.0f else 0.75f
                    }
                }
                is AttemptEvaluation.ClinicianRated -> {
                    totalEvaluated++
                    when (ev.rating) {
                        ClinicianRating.CORRECT_INDEPENDENT -> weightedCorrect += 1.0f
                        ClinicianRating.CORRECT_WITH_CUE -> weightedCorrect += 0.7f
                        else -> {}
                    }
                }
                AttemptEvaluation.NotEvaluated -> {}
            }
        }
        return if (totalEvaluated == 0) 0 else ((weightedCorrect / totalEvaluated) * 100f).toInt().coerceIn(0, 100)
    }

    fun calculateCueDistribution(attempts: List<GameAttempt>): Map<CueLevel, Int> =
        attempts.groupingBy { it.cueLevel }.eachCount()
}

class ContentLibraryManager(
    private val clinicalRepository: ClinicalPlatformRepository
) {
    val builtInStimuli: List<TherapeuticStimulus> = listOf(
        TherapeuticStimulus(1, "Sapo", "/s/", PhonemePosition.INITIAL, "SA-PO", 2, "Animais", DifficultyLevel.EASY, "🐸", "O sapo pulou na lagoa.", false, true),
        TherapeuticStimulus(2, "Bola", "/b/", PhonemePosition.INITIAL, "BO-LA", 2, "Brinquedos", DifficultyLevel.EASY, "⚽", "A criança chutou a bola azul.", false, true),
        TherapeuticStimulus(3, "Pato", "/p/", PhonemePosition.INITIAL, "PA-TO", 2, "Animais", DifficultyLevel.EASY, "🦆", "O pato nada no lago.", false, false),
        TherapeuticStimulus(4, "Faca", "/f/", PhonemePosition.INITIAL, "FA-CA", 2, "Objetos da Casa", DifficultyLevel.EASY, "🔪", "A faca está na mesa.", false, false),
        TherapeuticStimulus(5, "Vaca", "/v/", PhonemePosition.INITIAL, "VA-CA", 2, "Animais", DifficultyLevel.EASY, "🐄", "A vaca come capim no sítio.", false, true),
        TherapeuticStimulus(6, "Chave", "/ʃ/", PhonemePosition.INITIAL, "CHA-VE", 2, "Objetos da Casa", DifficultyLevel.MEDIUM, "🔑", "Mamãe abriu a porta com a chave.", false, true),
        TherapeuticStimulus(7, "Jaca", "/ʒ/", PhonemePosition.INITIAL, "JA-CA", 2, "Alimentos e Frutas", DifficultyLevel.MEDIUM, "🍈", "A jaca madura caiu do pé.", false, false),
        TherapeuticStimulus(8, "Arara", "/ɾ/", PhonemePosition.MEDIAL, "A-RA-RA", 3, "Animais", DifficultyLevel.HARD, "🦜", "A arara colorida voou alto.", false, true),
        TherapeuticStimulus(9, "Rato", "/R/", PhonemePosition.INITIAL, "RA-TO", 2, "Animais", DifficultyLevel.MEDIUM, "🐭", "O rato comeu o queijo.", false, false),
        TherapeuticStimulus(10, "Prato", "/pɾ/", PhonemePosition.INITIAL, "PRA-TO", 2, "Objetos da Casa", DifficultyLevel.HARD, "🍽️", "A sopa está quentinha no prato.", false, true),
        TherapeuticStimulus(11, "Morango", "/ɾ/", PhonemePosition.MEDIAL, "MO-RAN-GO", 3, "Alimentos e Frutas", DifficultyLevel.MEDIUM, "🍓", "Eu gosto de comer morango doce.", false, false),
        TherapeuticStimulus(12, "Palhaço", "/ʎ/", PhonemePosition.MEDIAL, "PA-LHA-ÇO", 3, "Profissões e Pessoas", DifficultyLevel.HARD, "🤡", "O palhaço fez todo mundo sorrir.", false, true)
    )

    fun observeAllStimuli(): Flow<List<TherapeuticStimulus>> =
        clinicalRepository.observeCustomStimuli().map { custom -> builtInStimuli + custom }

    suspend fun addCustomStimulus(
        word: String,
        phoneme: String,
        position: PhonemePosition,
        syllableDivision: String,
        category: String,
        difficulty: DifficultyLevel,
        emoji: String = "🗣️",
        sentence: String = ""
    ): Long {
        val syllables = syllableDivision.split("-").filter { it.isNotBlank() }.size.coerceAtLeast(1)
        return clinicalRepository.saveCustomStimulus(
            TherapeuticStimulus(
                id = 0L,
                word = word.trim(),
                targetPhoneme = phoneme.trim(),
                phonemePosition = position,
                syllableDivision = syllableDivision.trim().uppercase(),
                syllableCount = syllables,
                semanticCategory = category.trim().ifEmpty { "Personalizado" },
                difficulty = difficulty,
                iconEmoji = emoji,
                exampleSentence = sentence.trim(),
                isCustom = true,
                isFavorite = true
            )
        )
    }
}

// ============================================================================
// 7. HISTORY MANAGER (LONGITUDINAL TIMELINE WITH FILTERS)
// ============================================================================

class HistoryManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val sessionRepository: TherapySessionRepository,
    private val dateTimeManager: DateTimeManager
) {
    fun observePatientTimeline(patientId: Long): Flow<List<TimelineEvent>> {
        return combine(
            clinicalRepository.observeAnamnesisForPatient(patientId),
            clinicalRepository.observeAssessmentsForPatient(patientId),
            clinicalRepository.observeTherapyPlansForPatient(patientId),
            sessionRepository.observeSessionsForPatient(patientId),
            clinicalRepository.observeDocumentsForPatient(patientId)
        ) { anamnesis, assessments, therapies, sessions, documents ->
            val events = mutableListOf<TimelineEvent>()

            if (anamnesis != null && anamnesis.status != AnamnesisStatus.NOT_STARTED) {
                events += TimelineEvent(
                    id = "anamnesis_${anamnesis.id}",
                    patientId = patientId,
                    category = TimelineFilterCategory.ANAMNESIS,
                    title = "Anamnese Fonoaudiológica (v${anamnesis.versionNumber})",
                    subtitle = "${anamnesis.filledSectionsCount}/12 seções preenchidas • Informante: ${anamnesis.informantName.ifBlank { "Responsável" }}",
                    details = anamnesis.generalSummary.ifBlank { "Registro estruturado de histórico clínico e desenvolvimento." },
                    date = anamnesis.anamnesisDate,
                    timestamp = anamnesis.updatedAt,
                    badgeText = anamnesis.status.displayName
                )
            }

            assessments.forEach { a ->
                events += TimelineEvent(
                    id = "assessment_${a.id}",
                    patientId = patientId,
                    category = TimelineFilterCategory.ASSESSMENTS,
                    title = a.title,
                    subtitle = "${a.clinicalArea.displayName} • Protocolo: ${a.protocolOrInstrument}",
                    details = a.findingsSummary.ifBlank { a.clinicalHypothesis },
                    date = a.assessmentDate,
                    timestamp = a.updatedAt,
                    badgeText = a.status.displayName
                )
            }

            therapies.forEach { t ->
                events += TimelineEvent(
                    id = "therapy_${t.id}",
                    patientId = patientId,
                    category = TimelineFilterCategory.THERAPIES,
                    title = t.title,
                    subtitle = "${t.weeklyFrequency}x/semana • ${t.sessionDurationMinutes} min • Alvos: ${t.targetSkillsOrPhonemes}",
                    details = t.generalObjective,
                    date = t.startDate,
                    timestamp = t.updatedAt,
                    badgeText = t.status.displayName
                )
            }

            sessions.forEach { s ->
                val date = dateTimeManager.toLocalDate(s.scheduledAt)
                events += TimelineEvent(
                    id = "session_${s.id}",
                    patientId = patientId,
                    category = TimelineFilterCategory.SESSIONS,
                    title = "Sessão Clínica #${s.id}",
                    subtitle = "Status: ${s.status.displayName} • Duração: ${s.durationSeconds / 60} min",
                    details = s.clinicalNotes.ifBlank { s.clinicalSummary.ifBlank { "Sessão registrada no prontuário." } },
                    date = date,
                    timestamp = s.updatedAt,
                    badgeText = s.status.displayName
                )
            }

            documents.forEach { d ->
                events += TimelineEvent(
                    id = "doc_${d.id}",
                    patientId = patientId,
                    category = TimelineFilterCategory.DOCUMENTS,
                    title = d.title,
                    subtitle = "${d.category.displayName} • ${d.professionalName}",
                    details = d.bodyContent.take(160),
                    date = d.documentDate,
                    timestamp = d.updatedAt,
                    badgeText = if (d.isSignedAndLocked) "Finalizado" else "Rascunho"
                )
            }

            events.sortedByDescending { it.timestamp }
        }
    }
}

// ============================================================================
// 8. SCHEDULE MANAGER & REFERRAL MANAGER
// ============================================================================

class ScheduleManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeAppointments(): Flow<List<ClinicalAppointment>> =
        clinicalRepository.observeAppointments()

    suspend fun scheduleAppointment(
        patientId: Long?,
        patientName: String,
        dateTextBr: String,
        startTime: String,
        endTime: String,
        serviceType: String,
        recurrence: AppointmentRecurrence,
        notes: String
    ): Long {
        val date = dateTimeManager.parseDate(dateTextBr) ?: dateTimeManager.today()
        val id = clinicalRepository.saveAppointment(
            ClinicalAppointment(
                id = 0L,
                patientId = patientId,
                patientName = patientName.trim(),
                linkedSessionId = null,
                date = date,
                startTime = startTime.trim().ifEmpty { "14:00" },
                endTime = endTime.trim().ifEmpty { "14:50" },
                serviceType = serviceType.trim().ifEmpty { "Terapia Fonoaudiológica" },
                recurrence = recurrence,
                status = AppointmentStatus.SCHEDULED,
                notes = notes.trim(),
                createdAt = dateTimeManager.nowInstant()
            )
        )
        auditManager.log("APPOINTMENT", id, "SCHEDULE", "Agendamento para $patientName em ${dateTimeManager.formatDateBr(date)} às $startTime.")
        return id
    }

    suspend fun updateStatus(appointmentId: Long, status: AppointmentStatus) {
        clinicalRepository.updateAppointmentStatus(appointmentId, status)
    }
}

class ReferralManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager
) {
    fun observeWaitlistAndReferrals(): Flow<List<WaitlistAndReferralEntry>> =
        clinicalRepository.observeWaitlistAndReferrals()

    suspend fun registerReferralOrWaitlist(
        candidateName: String,
        birthDateText: String,
        guardianContact: String,
        referredBy: String,
        mainComplaint: String,
        preferredSchedule: String,
        urgency: ReferralUrgency
    ): Long {
        return clinicalRepository.saveWaitlistEntry(
            WaitlistAndReferralEntry(
                id = 0L,
                candidateName = candidateName.trim(),
                birthDateText = dateTimeManager.normalizeBrDateInput(birthDateText),
                guardianContact = guardianContact.trim(),
                referredBy = referredBy.trim().ifEmpty { "Família / Pediatra" },
                mainComplaint = mainComplaint.trim(),
                preferredSchedule = preferredSchedule.trim().ifEmpty { "Flexível" },
                urgency = urgency,
                isConvertedToPatient = false,
                convertedPatientId = null,
                createdAt = dateTimeManager.nowInstant()
            )
        )
    }

    suspend fun markConverted(entryId: Long, newPatientId: Long) {
        clinicalRepository.markWaitlistConverted(entryId, newPatientId)
    }
}

// ============================================================================
// 9. HOME PRACTICE MANAGER & CAREGIVER MANAGER
// ============================================================================

class HomePracticeManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager
) {
    fun observeForPatient(patientId: Long): Flow<List<HomePracticeTask>> =
        clinicalRepository.observeHomePracticeForPatient(patientId)

    fun observeAll(): Flow<List<HomePracticeTask>> =
        clinicalRepository.observeAllHomePractice()

    suspend fun assignHomeActivity(
        patientId: Long,
        title: String,
        linkedGameId: String?,
        targetSkill: String,
        instructions: String,
        frequency: String,
        dueDateTextBr: String = ""
    ): Long {
        val due = dateTimeManager.parseDate(dueDateTextBr) ?: dateTimeManager.today().plusDays(7)
        return clinicalRepository.saveHomePracticeTask(
            HomePracticeTask(
                id = 0L,
                patientId = patientId,
                title = title.trim(),
                linkedGameId = linkedGameId,
                targetSkill = targetSkill.trim(),
                caregiverInstructions = instructions.trim(),
                recommendedFrequency = frequency.trim().ifEmpty { "10 minutos por dia • 3x na semana" },
                assignedDate = dateTimeManager.today(),
                dueDate = due,
                status = HomePracticeStatus.PENDING,
                difficultyFeedback = HomeDifficultyFeedback.NOT_RATED,
                familyNotes = "",
                createdAt = dateTimeManager.nowInstant()
            )
        )
    }

    suspend fun recordFamilyFeedback(
        taskId: Long,
        status: HomePracticeStatus,
        difficulty: HomeDifficultyFeedback,
        familyNotes: String
    ) {
        clinicalRepository.updateHomePracticeFeedback(taskId, status, difficulty, familyNotes)
    }
}

class CaregiverManager() {
    fun generateCaregiverOrientationSheet(patientName: String, tasks: List<HomePracticeTask>): String {
        return buildString {
            appendLine("GUIA DE PRÁTICA DOMICILIAR — FONOLAB")
            appendLine("Paciente: $patientName")
            appendLine("--------------------------------------------------")
            if (tasks.isEmpty()) {
                appendLine("Realizar leitura compartilhada e estimulação natural em rotina diária (10 min/dia).")
            } else {
                tasks.forEachIndexed { idx, t ->
                    appendLine("${idx + 1}. ${t.title} (Alvo: ${t.targetSkill})")
                    appendLine("   Frequência sugerida: ${t.recommendedFrequency}")
                    appendLine("   Como fazer em casa: ${t.caregiverInstructions}")
                    appendLine()
                }
            }
            appendLine("Dica clínica: Mantenha o momento leve, elogie as tentativas e não exija perfeição imediata.")
        }
    }
}

// ============================================================================
// 10. DOCUMENT MANAGER, TEMPLATE MANAGER & DOCUMENTATION ASSISTANT MANAGER
// ============================================================================

class DocumentManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeDocumentsForPatient(patientId: Long): Flow<List<ClinicalDocument>> =
        clinicalRepository.observeDocumentsForPatient(patientId)

    fun observeAllDocuments(): Flow<List<ClinicalDocument>> =
        clinicalRepository.observeAllDocuments()

    suspend fun createDocument(
        patientId: Long,
        sessionId: Long?,
        category: DocumentCategory,
        title: String,
        professionalName: String,
        professionalCrfa: String,
        soapSubjective: String = "",
        soapObjective: String = "",
        soapAssessment: String = "",
        soapPlan: String = "",
        bodyContent: String,
        lockAndSign: Boolean = false
    ): Long {
        val now = dateTimeManager.nowInstant()
        val resolvedBody = if (category == DocumentCategory.SOAP_EVOLUTION && bodyContent.isBlank()) {
            buildString {
                if (soapSubjective.isNotBlank()) appendLine("S (Subjetivo): $soapSubjective")
                if (soapObjective.isNotBlank()) appendLine("O (Objetivo): $soapObjective")
                if (soapAssessment.isNotBlank()) appendLine("A (Avaliação): $soapAssessment")
                if (soapPlan.isNotBlank()) appendLine("P (Plano): $soapPlan")
            }.trim()
        } else {
            bodyContent.trim()
        }

        val id = clinicalRepository.saveDocument(
            ClinicalDocument(
                id = 0L,
                patientId = patientId,
                sessionId = sessionId,
                category = category,
                title = title.trim().ifEmpty { category.displayName },
                documentDate = dateTimeManager.today(),
                professionalName = professionalName,
                professionalCrfa = professionalCrfa,
                soapSubjective = soapSubjective.trim(),
                soapObjective = soapObjective.trim(),
                soapAssessment = soapAssessment.trim(),
                soapPlan = soapPlan.trim(),
                bodyContent = resolvedBody,
                addendumNotes = "",
                isSignedAndLocked = lockAndSign,
                createdAt = now,
                updatedAt = now
            )
        )
        auditManager.log("DOCUMENT", id, "CREATE", "Documento '${category.displayName}' emitido.")
        return id
    }

    suspend fun addAddendum(documentId: Long, addendumText: String) {
        val stamped = "[Adendo em ${dateTimeManager.formatDateTimeBr(dateTimeManager.nowInstant())}]: ${addendumText.trim()}"
        clinicalRepository.appendDocumentAddendum(documentId, stamped)
        auditManager.log("DOCUMENT", documentId, "ADDENDUM", "Adendo clínico anexado ao documento #$documentId.")
    }
}

class TemplateManager() {
    fun getDocumentTemplateBody(category: DocumentCategory, patientName: String): String = when (category) {
        DocumentCategory.SOAP_EVOLUTION ->
            "Paciente $patientName compareceu à sessão acompanhado(a) pelo responsável, apresentando boa disposição e engajamento lúdico."
        DocumentCategory.SCHOOL_REPORT ->
            "Declaro para fins de articulação pedagógica que o(a) aluno(a) $patientName encontra-se em acompanhamento fonoaudiológico regular, com foco em estimulação da linguagem, consciência fonológica e comunicação escolar. Recomenda-se apoio visual para instruções multi-etapas e tempo adicional de resposta oral."
        DocumentCategory.REFERRAL_LETTER ->
            "Encaminho o(a) paciente $patientName para avaliação especializada, tendo em vista achados clínicos observados durante triagem e acompanhamento fonoaudiológico."
        DocumentCategory.ATTENDANCE_DECLARATION ->
            "Declaro para os devidos fins que o(a) paciente $patientName esteve em atendimento fonoaudiológico nesta data."
        DocumentCategory.DISCHARGE_SUMMARY ->
            "O(A) paciente $patientName atingiu os critérios de domínio estabelecidos para os objetivos fonoaudiológicos, demonstrando generalização consistente em fala espontânea e diferentes contextos comunicativos, recebendo alta fonoaudiológica."
        DocumentCategory.CONSENT_TERM ->
            "Autorizo a realização de avaliação, acompanhamento fonoaudiológico e registro seguro de dados clínicos no prontuário eletrônico para fins exclusivamente terapêuticos de $patientName."
        else ->
            "Relatório clínico referente ao acompanhamento fonoaudiológico de $patientName."
    }
}

class DocumentationAssistantManager(
    private val clinicalDataManager: ClinicalDataManager
) {
    /**
     * Generates an editable clinical SOAP draft from real session attempts without making automated diagnoses.
     */
    fun generateSoapDraft(
        patientName: String,
        activitiesCount: Int,
        attempts: List<GameAttempt>
    ): Map<String, String> {
        val summary = clinicalDataManager.summarizePerformance(attempts)
        val overallAcc = clinicalDataManager.calculateOverallClinicalAccuracy(attempts)
        return mapOf(
            "S" to "$patientName compareceu ao atendimento apresentando estado de alerta adequado e boa receptividade às propostas lúdicas.",
            "O" to "Realizadas $activitiesCount atividade(s) terapêutica(s), totalizando ${summary.totalAttempts} tentativas registradas. Acurácia clínica global de $overallAcc% (Independentes: ${summary.independentCorrectCount + summary.autoScoredCorrect}, Com pista: ${summary.cuedCorrectCount}).",
            "A" to "Observa-se evolução na estabilidade dos alvos trabalhados com redução gradual da necessidade de pistas diretas.",
            "P" to "Manter estimulação dos objetivos ativos na próxima sessão e enviar treino domiciliar para generalização."
        )
    }
}

class ReportManager(
    private val clinicalDataManager: ClinicalDataManager
) {
    fun buildExecutiveMetrics(
        sessions: List<TherapySession>,
        attempts: List<GameAttempt>,
        goals: List<ClinicalGoal>
    ): Map<String, String> {
        val completedSessions = sessions.count { it.status == SessionStatus.COMPLETED }
        val plannedSessions = sessions.count { it.status == SessionStatus.PLANNED || it.status == SessionStatus.DRAFT }
        val activeGoals = goals.count { it.status == GoalStatus.ACTIVE }
        val accuracy = clinicalDataManager.calculateOverallClinicalAccuracy(attempts)
        return mapOf(
            "Sessões Concluídas" to completedSessions.toString(),
            "Sessões Planejadas" to plannedSessions.toString(),
            "Metas Ativas" to activeGoals.toString(),
            "Acurácia Clínica" to "$accuracy%"
        )
    }
}

// ============================================================================
// 11. BILLING MANAGER (BRL, SEPARATED FROM CLINICAL RECORDS)
// ============================================================================

class BillingManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager,
    private val auditManager: AuditManager
) {
    fun observeAllBilling(): Flow<List<BillingRecord>> =
        clinicalRepository.observeBillingRecords()

    fun observePatientBilling(patientId: Long): Flow<List<BillingRecord>> =
        clinicalRepository.observeBillingForPatient(patientId)

    suspend fun createCharge(
        patientId: Long,
        patientName: String,
        description: String,
        amountBrlReais: Double,
        dueDateTextBr: String,
        paymentMethod: PaymentMethod,
        markPaidImmediately: Boolean = false,
        notes: String = ""
    ): Long {
        val cents = kotlin.math.round(amountBrlReais * 100.0).toLong().coerceAtLeast(0L)
        val due = dateTimeManager.parseDate(dueDateTextBr) ?: dateTimeManager.today()
        val status = if (markPaidImmediately) BillingStatus.PAID else BillingStatus.PENDING
        val id = clinicalRepository.saveBillingRecord(
            BillingRecord(
                id = 0L,
                patientId = patientId,
                patientName = patientName.trim(),
                sessionId = null,
                description = description.trim().ifEmpty { "Sessão de Terapia Fonoaudiológica" },
                amountCents = cents,
                dueDate = due,
                paymentDate = if (markPaidImmediately) dateTimeManager.today() else null,
                paymentMethod = paymentMethod,
                status = status,
                receiptIssued = markPaidImmediately,
                notes = notes.trim(),
                createdAt = dateTimeManager.nowInstant()
            )
        )
        val totalCents = (amountBrlReais * 100).toInt()
        val r = totalCents / 100
        val c = kotlin.math.abs(totalCents % 100).toString().padStart(2, '0')
        auditManager.log("BILLING", id, "CREATE", "Lançamento financeiro de R$ $r,$c para $patientName.")
        return id
    }

    suspend fun markPaid(recordId: Long, method: PaymentMethod) {
        clinicalRepository.updateBillingStatus(recordId, BillingStatus.PAID, method)
        auditManager.log("BILLING", recordId, "PAID", "Pagamento confirmado via ${method.displayName}.")
    }
}

// ============================================================================
// 12. QUALITY CONTROL, ACCOUNT, AUTH, ACCESSIBILITY & REMAINING MANAGERS
// ============================================================================

data class ClinicalQualityAlert(
    val id: String,
    val title: String,
    val description: String,
    val patientId: Long?,
    val severity: TaskPriority
)

class QualityControlManager(
    private val patientRepository: PatientRepository,
    private val clinicalRepository: ClinicalPlatformRepository,
    private val sessionRepository: TherapySessionRepository
) {
    fun observeClinicalAlerts(): Flow<List<ClinicalQualityAlert>> {
        return combine(
            patientRepository.observePatients(PatientFilter(status = PatientStatus.ACTIVE)),
            clinicalRepository.observeAllAnamneses(),
            clinicalRepository.observeAllTherapyPlans(),
            sessionRepository.observeRecentSessions(30)
        ) { patients, anamneses, therapies, sessions ->
            val alerts = mutableListOf<ClinicalQualityAlert>()
            val anamnesisByPatient = anamneses.associateBy { it.patientId }
            val therapiesByPatient = therapies.groupBy { it.patientId }

            for (patient in patients) {
                val anamnesis = anamnesisByPatient[patient.id]
                if (anamnesis == null || anamnesis.status == AnamnesisStatus.NOT_STARTED) {
                    alerts += ClinicalQualityAlert(
                        id = "missing_anamnesis_${patient.id}",
                        title = "Anamnese pendente: ${patient.fullName}",
                        description = "Inicie ou preencha a anamnese estruturada de 12 seções do paciente.",
                        patientId = patient.id,
                        severity = TaskPriority.HIGH
                    )
                } else if (anamnesis.status == AnamnesisStatus.IN_PROGRESS) {
                    alerts += ClinicalQualityAlert(
                        id = "incomplete_anamnesis_${patient.id}",
                        title = "Anamnese em andamento (${anamnesis.completionPercentage}%): ${patient.fullName}",
                        description = "Faltam ${12 - anamnesis.filledSectionsCount} seções para concluir a anamnese.",
                        patientId = patient.id,
                        severity = TaskPriority.MEDIUM
                    )
                }

                if (therapiesByPatient[patient.id].isNullOrEmpty()) {
                    alerts += ClinicalQualityAlert(
                        id = "missing_therapy_${patient.id}",
                        title = "Sem plano terapêutico ativo: ${patient.fullName}",
                        description = "Cadastre ao menos uma terapia e objetivos clínicos mensuráveis.",
                        patientId = patient.id,
                        severity = TaskPriority.MEDIUM
                    )
                }
            }

            sessions.filter { it.status == SessionStatus.COMPLETED && it.clinicalNotes.isBlank() }.take(3).forEach { s ->
                alerts += ClinicalQualityAlert(
                    id = "session_missing_notes_${s.id}",
                    title = "Sessão #${s.id} sem evolução detalhada",
                    description = "Registre a evolução SOAP ou observações clínicas da sessão concluída.",
                    patientId = s.patientId,
                    severity = TaskPriority.LOW
                )
            }

            alerts
        }
    }
}

class AccountManager(
    private val clinicalRepository: ClinicalPlatformRepository
) {
    fun observeProfile(): Flow<ProfessionalProfile> = clinicalRepository.observeProfessionalProfile()
    suspend fun saveProfile(profile: ProfessionalProfile) = clinicalRepository.updateProfessionalProfile(profile)
}

class AuthManager() {
    /**
     * Future-ready authentication contract. Never blocks offline local usage in the current version,
     * while supporting the visual Login / Account preview screen.
     */
    fun isOfflineModeEnabled(): Boolean = true
}

class AccessibilityManager() {
    val supportedCommunicationModalities = listOf(
        "Fala oral",
        "Comunicação Alternativa e Aumentativa (CAA)",
        "Gestos e Apontamento",
        "Libras / Bilinguismo",
        "Escrita / Prancha Visual"
    )
}

class LanguageProfileManager() {
    val commonLanguageContexts = listOf(
        "Português Brasileiro (Monolíngue)",
        "Português + Inglês (Bilíngue)",
        "Português + Espanhol (Bilíngue)",
        "Português + Libras (Bilíngue Bimodal)",
        "Outro contexto multilíngue"
    )
}

class CollaborationManager() {
    val externalSpecialties = listOf(
        "Pediatria",
        "Neuropediatria",
        "Otorrinolaringologia",
        "Psicologia Infantil / ABA",
        "Terapia Ocupacional (Integração Sensorial)",
        "Psicopedagogia",
        "Coordenação Escolar / Professor(a)"
    )
}

class SupervisionManager() {
    fun anonymizePatientName(fullName: String, internalCode: String): String {
        val initials = fullName.split(" ").filter { it.isNotBlank() }.joinToString(".") { it.take(1).uppercase() }
        return "Caso Clínico $initials ($internalCode)"
    }
}

class TeamManager() {
    val availableRoles = listOf(
        "Administrador Clínico",
        "Fonoaudiólogo(a) Titular",
        "Fonoaudiólogo(a) Supervisor(a)",
        "Estagiário(a) / Residente",
        "Recepção / Secretaria"
    )
}

class MediaManager() {
    val supportedAttachmentCategories = listOf(
        "Amostra de Fala / Áudio",
        "Foto de Motricidade Orofacial",
        "Exame Externo (Audiometria / BERA / TAN)",
        "Relatório Escolar / Laudo Médico"
    )
}

class EngagementManager() {
    fun calculateStarsForAccuracy(accuracyPercentage: Int): Int = when {
        accuracyPercentage >= 80 -> 3
        accuracyPercentage >= 50 -> 2
        else -> 1
    }
}

class NotificationManager(
    private val clinicalRepository: ClinicalPlatformRepository,
    private val dateTimeManager: DateTimeManager
) {
    fun observeTasks(): Flow<List<ClinicalTask>> = clinicalRepository.observeClinicalTasks()

    suspend fun createTask(
        patientId: Long?,
        title: String,
        description: String,
        dueDateBr: String,
        priority: TaskPriority,
        category: String
    ): Long {
        val due = dateTimeManager.parseDate(dueDateBr)
        return clinicalRepository.saveClinicalTask(
            ClinicalTask(
                id = 0L,
                patientId = patientId,
                title = title.trim(),
                description = description.trim(),
                dueDate = due,
                priority = priority,
                isCompleted = false,
                category = category.trim().ifEmpty { "Lembrete" },
                createdAt = dateTimeManager.nowInstant()
            )
        )
    }

    suspend fun toggleTask(taskId: Long, completed: Boolean) =
        clinicalRepository.toggleTaskCompleted(taskId, completed)
}

class SyncManager() {
    fun getSyncStatusSummary(): String =
        "Persistência local Room SQLite ativa (100% offline). Arquitetura preparada para sincronização remota futura."
}

class SettingsManager() {
    val defaultSessionDurationMinutes: Int = 50
    val defaultLocaleTag: String = "pt-BR"
}

class AuditManager(
    private val clinicalRepository: ClinicalPlatformRepository
) {
    fun observeRecentLogs(limit: Int = 50): Flow<List<AuditEvent>> =
        clinicalRepository.observeAuditEvents(limit)

    suspend fun log(entityType: String, entityId: Long, action: String, description: String) {
        runCatching {
            clinicalRepository.recordAudit(entityType, entityId, action, description)
        }
    }
}
