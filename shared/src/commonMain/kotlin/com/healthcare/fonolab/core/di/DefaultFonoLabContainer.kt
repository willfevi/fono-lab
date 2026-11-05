package com.healthcare.fonolab.core.di

import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.datetime.PlatformClock
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.goal.*
import com.healthcare.fonolab.domain.manager.*
import com.healthcare.fonolab.domain.patient.*
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.session.*
import kotlinx.coroutines.flow.*
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Universal in-memory reactive repository provider with Brazilian clinical seeds,
 * powering Desktop JVM and test environments offline with zero dependencies.
 */
class DefaultFonoLabContainer(
    val jvmClock: Clock = Clock.system(ZoneId.of("America/Sao_Paulo")),
    val clock: PlatformClock = com.healthcare.fonolab.domain.datetime.SystemPlatformClock(jvmClock)
) : FonoLabContainer {

    override val dateTimeManager = DateTimeManager(jvmClock)

    // ========================================================================
    // 1. REPOSITORIES
    // ========================================================================
    override val patientRepository: PatientRepository = InMemoryPatientRepository(clock)
    override val sessionRepository: TherapySessionRepository = InMemoryTherapySessionRepository(clock)
    override val goalRepository: ClinicalGoalRepository = InMemoryClinicalGoalRepository(clock)
    override val platformRepository: ClinicalPlatformRepository = InMemoryClinicalPlatformRepository(clock)

    // ========================================================================
    // 2. CLINICAL MANAGERS
    // ========================================================================
    override val auditManager = AuditManager(clinicalRepository = platformRepository)
    override val formManager = FormManager(
        patientValidator = PatientValidator(jvmClock),
        dateTimeManager = dateTimeManager
    )
    override val patientManager = PatientManager(
        repository = patientRepository,
        savePatientUseCase = SavePatientUseCase(
            repository = patientRepository,
            validator = PatientValidator(jvmClock),
            codeGenerator = InternalCodeGenerator(jvmClock, kotlin.random.Random.Default),
            clock = jvmClock
        ),
        changeStatusUseCase = ChangePatientStatusUseCase(
            repository = patientRepository,
            clock = jvmClock
        ),
        deletePatientUseCase = DeletePatientUseCase(patientRepository),
        auditManager = auditManager,
        dateTimeManager = dateTimeManager
    )
    override val sessionManager = SessionManager(
        sessionRepository = sessionRepository,
        planSessionUseCase = PlanSessionUseCase(sessionRepository, jvmClock),
        startPlannedSessionUseCase = StartPlannedSessionUseCase(sessionRepository, jvmClock),
        pauseSessionUseCase = PauseSessionUseCase(sessionRepository, jvmClock),
        startSessionUseCase = StartSessionUseCase(sessionRepository, jvmClock),
        finishSessionUseCase = FinishSessionUseCase(sessionRepository, jvmClock),
        cancelSessionUseCase = CancelSessionUseCase(sessionRepository, jvmClock),
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val anamnesisManager = AnamnesisManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val gameManager = GameManager(sessionRepository = sessionRepository)
    override val clinicalDataManager = ClinicalDataManager()
    override val reportManager = ReportManager(clinicalDataManager = clinicalDataManager)
    override val billingManager = BillingManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val scheduleManager = ScheduleManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val referralManager = ReferralManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager
    )
    override val contentLibraryManager = ContentLibraryManager(clinicalRepository = platformRepository)
    override val assessmentManager = AssessmentManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val phonologyManager = PhonologyManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager
    )
    override val therapyManager = TherapyManager(
        clinicalRepository = platformRepository,
        goalRepository = goalRepository,
        saveGoalUseCase = SaveGoalUseCase(
            repository = goalRepository,
            validator = GoalValidator(),
            clock = jvmClock
        ),
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val generalizationManager = GeneralizationManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager
    )
    override val planningAssistant = SessionPlanningAssistantManager()
    override val homePracticeManager = HomePracticeManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager
    )
    override val caregiverManager = CaregiverManager()
    override val documentManager = DocumentManager(
        clinicalRepository = platformRepository,
        dateTimeManager = dateTimeManager,
        auditManager = auditManager
    )
    override val templateManager = TemplateManager()
    override val docAssistant = DocumentationAssistantManager(clinicalDataManager = clinicalDataManager)
    override val historyManager = HistoryManager(
        clinicalRepository = platformRepository,
        sessionRepository = sessionRepository,
        dateTimeManager = dateTimeManager
    )
    override val qualityControlManager = QualityControlManager(
        patientRepository = patientRepository,
        clinicalRepository = platformRepository,
        sessionRepository = sessionRepository
    )
    override val accountManager = AccountManager(clinicalRepository = platformRepository)
}

/**
 * In-memory reactive implementation of PatientRepository with realistic clinical seeds.
 */
class InMemoryPatientRepository(private val clock: PlatformClock) : PatientRepository {
    private val now = clock.nowInstant()
    private val patients = MutableStateFlow<List<Patient>>(
        listOf(
            Patient(
                id = 1L,
                internalCode = "PAC-001",
                fullName = "Lucas Gabriel Silva",
                birthDate = LocalDate.now().minusYears(6).minusMonths(3),
                guardianName = "Mariana Silva",
                guardianContact = "(11) 98765-4321",
                followUpStartDate = LocalDate.now().minusMonths(4),
                clinicalNotes = "Desvio fonológico com processos de plosivação e ensurdecimento.",
                status = PatientStatus.ACTIVE,
                isDemo = false,
                createdAt = now.minusSeconds(86400 * 120),
                updatedAt = now.minusSeconds(86400 * 5)
            ),
            Patient(
                id = 2L,
                internalCode = "PAC-002",
                fullName = "Beatriz Helena Santos",
                birthDate = LocalDate.now().minusYears(4).minusMonths(8),
                guardianName = "Carlos Santos",
                guardianContact = "(11) 97654-3210",
                followUpStartDate = LocalDate.now().minusMonths(2),
                clinicalNotes = "Atraso no desenvolvimento da linguagem expressiva e queixa de troca de fonemas.",
                status = PatientStatus.ACTIVE,
                isDemo = false,
                createdAt = now.minusSeconds(86400 * 60),
                updatedAt = now.minusSeconds(86400 * 2)
            ),
            Patient(
                id = 3L,
                internalCode = "PAC-003",
                fullName = "Gabriel Oliveira Rocha",
                birthDate = LocalDate.now().minusYears(8).minusMonths(1),
                guardianName = "Patrícia Rocha",
                guardianContact = "(21) 99887-6655",
                followUpStartDate = LocalDate.now().minusMonths(6),
                clinicalNotes = "Dificuldade em consciência fonológica e segmentação silábica em contexto escolar.",
                status = PatientStatus.ACTIVE,
                isDemo = false,
                createdAt = now.minusSeconds(86400 * 180),
                updatedAt = now.minusSeconds(86400 * 1)
            ),
            Patient(
                id = 4L,
                internalCode = "PAC-004",
                fullName = "Sofia Mendes Carvalho",
                birthDate = LocalDate.now().minusYears(5).minusMonths(6),
                guardianName = "Renata Carvalho",
                guardianContact = "(31) 98877-2233",
                followUpStartDate = LocalDate.now().minusMonths(1),
                clinicalNotes = "Ceceio anterior e interdentalização durante fala espontânea.",
                status = PatientStatus.ACTIVE,
                isDemo = false,
                createdAt = now.minusSeconds(86400 * 30),
                updatedAt = now.minusSeconds(86400 * 3)
            )
        )
    )

    override fun observePatients(filter: PatientFilter): Flow<List<Patient>> = patients.map { list ->
        list.filter { p ->
            (filter.status == null || p.status == filter.status) &&
                    (filter.query.isBlank() || p.fullName.contains(filter.query, ignoreCase = true) || p.internalCode.contains(filter.query, ignoreCase = true))
        }
    }

    override fun observePatient(id: Long): Flow<Patient?> = patients.map { list -> list.find { it.id == id } }
    override fun observeActivePatientCount(includeDemo: Boolean): Flow<Int> = patients.map { list -> list.count { it.status == PatientStatus.ACTIVE } }
    override fun observeDemoPatientCount(): Flow<Int> = patients.map { list -> list.count { it.isDemo } }
    override suspend fun getPatient(id: Long): Patient? = patients.value.find { it.id == id }

    override suspend fun insert(data: ValidPatientData, internalCode: String, isDemo: Boolean, now: Instant): Long {
        val newId = (patients.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newPatient = Patient(
            id = newId,
            internalCode = internalCode,
            fullName = data.fullName,
            birthDate = data.birthDate,
            guardianName = data.guardianName,
            guardianContact = data.guardianContact,
            followUpStartDate = data.followUpStartDate,
            clinicalNotes = data.clinicalNotes,
            status = PatientStatus.ACTIVE,
            isDemo = isDemo,
            createdAt = now,
            updatedAt = now
        )
        patients.update { it + newPatient }
        return newId
    }

    override suspend fun update(id: Long, data: ValidPatientData, now: Instant) {
        patients.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        fullName = data.fullName,
                        birthDate = data.birthDate,
                        guardianName = data.guardianName,
                        guardianContact = data.guardianContact,
                        followUpStartDate = data.followUpStartDate,
                        clinicalNotes = data.clinicalNotes,
                        updatedAt = now
                    )
                } else it
            }
        }
    }

    override suspend fun setStatus(id: Long, status: PatientStatus, now: Instant) {
        patients.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(status = status, updatedAt = now)
                } else it
            }
        }
    }

    override suspend fun deletionImpact(id: Long): PatientDeletionImpact {
        return PatientDeletionImpact(goals = 0, sessions = 0, attempts = 0, observations = 0)
    }

    override suspend fun delete(id: Long) {
        patients.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun deleteDemoPatients() {
        patients.update { list -> list.filterNot { it.isDemo } }
    }

    override suspend fun isInternalCodeInUse(code: String): Boolean =
        patients.value.any { it.internalCode.equals(code, ignoreCase = true) }
}

/**
 * In-memory reactive implementation of TherapySessionRepository matching all domain signatures.
 */
class InMemoryTherapySessionRepository(private val clock: PlatformClock) : TherapySessionRepository {
    private val now = clock.nowInstant()
    private val sessions = MutableStateFlow<List<TherapySession>>(
        listOf(
            TherapySession(
                id = 1L,
                patientId = 1L,
                status = SessionStatus.PLANNED,
                scheduledAt = now.plusSeconds(3600),
                startedAt = null,
                endedAt = null,
                durationSeconds = 0,
                clinicalNotes = "Sessão planejada com foco em discriminação de fonemas fricativos /s/ e /z/.",
                clinicalSummary = "Planejado",
                isDemo = false,
                createdAt = now.minusSeconds(86400),
                updatedAt = now.minusSeconds(86400)
            ),
            TherapySession(
                id = 2L,
                patientId = 2L,
                status = SessionStatus.COMPLETED,
                scheduledAt = now.minusSeconds(86400 * 3),
                startedAt = now.minusSeconds(86400 * 3),
                endedAt = now.minusSeconds(86400 * 3 - 2700),
                durationSeconds = 2700,
                clinicalNotes = "Ótimo engajamento no Monstro Comilão. Sustentação de fonação acima de 4s.",
                clinicalSummary = "Sessão concluída com 85% de acurácia.",
                isDemo = false,
                createdAt = now.minusSeconds(86400 * 3),
                updatedAt = now.minusSeconds(86400 * 3)
            )
        )
    )

    private val attempts = MutableStateFlow<List<GameAttempt>>(emptyList())
    private val activities = MutableStateFlow<List<SessionActivity>>(emptyList())

    override fun observeSessionsForPatient(patientId: Long): Flow<List<TherapySession>> =
        sessions.map { list -> list.filter { it.patientId == patientId } }

    override fun observeRecentSessions(limit: Int): Flow<List<TherapySession>> =
        sessions.map { it.take(limit) }

    override fun observeActiveSession(): Flow<SessionWithDetails?> =
        sessions.map { list ->
            list.find { it.status == SessionStatus.IN_PROGRESS }?.let { session ->
                val linkedActivities = activities.value.filter { it.sessionId == session.id }
                SessionWithDetails(
                    session = session,
                    linkedGoals = emptyList(),
                    activities = linkedActivities
                )
            }
        }

    override fun observeSession(sessionId: Long): Flow<SessionWithDetails?> =
        sessions.map { list ->
            list.find { it.id == sessionId }?.let { session ->
                val linkedActivities = activities.value.filter { it.sessionId == session.id }
                SessionWithDetails(
                    session = session,
                    linkedGoals = emptyList(),
                    activities = linkedActivities
                )
            }
        }

    override fun observeCompletedSessionCount(since: Instant?): Flow<Int> =
        sessions.map { list -> list.count { it.status == SessionStatus.COMPLETED && (since == null || it.scheduledAt.isAfter(since)) } }

    override fun observePlannedSessionCount(): Flow<Int> =
        sessions.map { list -> list.count { it.status == SessionStatus.PLANNED } }

    override suspend fun getSessionWithDetails(sessionId: Long): SessionWithDetails? {
        val s = sessions.value.find { it.id == sessionId } ?: return null
        val linkedActivities = activities.value.filter { it.sessionId == s.id }
        return SessionWithDetails(
            session = s,
            linkedGoals = emptyList(),
            activities = linkedActivities
        )
    }

    override suspend fun getActiveSession(): SessionWithDetails? {
        val s = sessions.value.find { it.status == SessionStatus.IN_PROGRESS } ?: return null
        val linkedActivities = activities.value.filter { it.sessionId == s.id }
        return SessionWithDetails(
            session = s,
            linkedGoals = emptyList(),
            activities = linkedActivities
        )
    }

    override suspend fun createSession(
        patientId: Long,
        scheduledAt: Instant,
        status: SessionStatus,
        linkedGoalIds: List<Long>,
        isDemo: Boolean,
        now: Instant
    ): Long {
        val newId = (sessions.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newSession = TherapySession(
            id = newId,
            patientId = patientId,
            status = status,
            scheduledAt = scheduledAt,
            startedAt = null,
            endedAt = null,
            durationSeconds = 0,
            clinicalNotes = "",
            clinicalSummary = "",
            isDemo = isDemo,
            createdAt = now,
            updatedAt = now
        )
        sessions.update { it + newSession }
        return newId
    }

    override suspend fun updateSessionStatus(
        sessionId: Long,
        status: SessionStatus,
        startedAt: Instant?,
        endedAt: Instant?,
        durationSeconds: Long,
        now: Instant
    ) {
        sessions.update { list ->
            list.map {
                if (it.id == sessionId) {
                    it.copy(
                        status = status,
                        startedAt = startedAt ?: it.startedAt,
                        endedAt = endedAt ?: it.endedAt,
                        durationSeconds = durationSeconds,
                        updatedAt = now
                    )
                } else it
            }
        }
    }

    override suspend fun updateSessionNotes(sessionId: Long, notes: String, summary: String, now: Instant) {
        sessions.update { list ->
            list.map {
                if (it.id == sessionId) {
                    it.copy(clinicalNotes = notes, clinicalSummary = summary, updatedAt = now)
                } else it
            }
        }
    }

    override suspend fun addActivity(
        sessionId: Long,
        gameId: String,
        linkedGoalId: Long?,
        configurationJson: String,
        orderIndex: Int,
        now: Instant
    ): Long {
        val newId = (activities.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val act = SessionActivity(
            id = newId,
            sessionId = sessionId,
            gameId = gameId,
            linkedGoalId = linkedGoalId,
            configurationJson = configurationJson,
            orderIndex = orderIndex,
            status = ActivityStatus.PENDING,
            stateSnapshotJson = null,
            startedAt = null,
            endedAt = null
        )
        activities.update { it + act }
        return newId
    }

    override suspend fun updateActivityStatus(
        activityId: Long,
        status: ActivityStatus,
        snapshotJson: String?,
        startedAt: Instant?,
        endedAt: Instant?
    ) {
        activities.update { list ->
            list.map {
                if (it.id == activityId) {
                    it.copy(
                        status = status,
                        stateSnapshotJson = snapshotJson ?: it.stateSnapshotJson,
                        startedAt = startedAt ?: it.startedAt,
                        endedAt = endedAt ?: it.endedAt
                    )
                } else it
            }
        }
    }

    override suspend fun recordAttempt(attempt: GameAttempt): Long {
        val newId = (attempts.value.maxOfOrNull { it.id } ?: 0L) + 1L
        attempts.update { it + attempt.copy(id = newId) }
        return newId
    }

    override suspend fun getAttemptsForActivity(activityId: Long): List<GameAttempt> =
        attempts.value.filter { it.activityId == activityId }

    override suspend fun getAttemptsForPatient(patientId: Long): List<GameAttempt> {
        val patientSessionIds = sessions.value.filter { it.patientId == patientId }.map { it.id }.toSet()
        val patientActivityIds = activities.value.filter { it.sessionId in patientSessionIds }.map { it.id }.toSet()
        return attempts.value.filter { it.activityId in patientActivityIds }
    }
}

/**
 * In-memory reactive implementation of ClinicalGoalRepository.
 */
class InMemoryClinicalGoalRepository(private val clock: PlatformClock) : ClinicalGoalRepository {
    private val now = clock.nowInstant()
    private val goals = MutableStateFlow<List<ClinicalGoal>>(
        listOf(
            ClinicalGoal(
                id = 1L,
                patientId = 1L,
                clinicalArea = ClinicalArea.SPEECH_AND_PHONOLOGY,
                customAreaName = null,
                description = "Produção correta do arquifonema /S/ em final de sílaba",
                targetSkill = "Arquifonema {S}",
                targetStimulus = "/s/",
                phonemePosition = null,
                initialDifficulty = com.healthcare.fonolab.domain.common.DifficultyLevel.MEDIUM,
                targetAccuracyPercentage = 80,
                masteryCriterionDescription = "80% de acerto em 3 sessões consecutivas",
                clinicalNotes = "Paciente em evolução favorável",
                status = GoalStatus.ACTIVE,
                createdAt = now.minusSeconds(86400 * 30),
                updatedAt = now.minusSeconds(86400 * 2)
            ),
            ClinicalGoal(
                id = 2L,
                patientId = 2L,
                clinicalArea = ClinicalArea.RECEPTIVE_LANGUAGE,
                customAreaName = null,
                description = "Discriminação auditiva entre os pares mínimos /p/ e /b/",
                targetSkill = "Pares Mínimos P/B",
                targetStimulus = "p, b",
                phonemePosition = null,
                initialDifficulty = com.healthcare.fonolab.domain.common.DifficultyLevel.EASY,
                targetAccuracyPercentage = 85,
                masteryCriterionDescription = "85% de discriminação independente",
                clinicalNotes = "Uso de pistas visuais em redução gradual",
                status = GoalStatus.ACTIVE,
                createdAt = now.minusSeconds(86400 * 20),
                updatedAt = now.minusSeconds(86400 * 5)
            )
        )
    )

    override fun observeGoalsForPatient(patientId: Long): Flow<List<ClinicalGoal>> =
        goals.map { list -> list.filter { it.patientId == patientId } }

    override fun observeActiveGoalsForPatient(patientId: Long): Flow<List<ClinicalGoal>> =
        goals.map { list -> list.filter { it.patientId == patientId && it.status == GoalStatus.ACTIVE } }

    override fun observeGoal(id: Long): Flow<ClinicalGoal?> =
        goals.map { list -> list.find { it.id == id } }

    override suspend fun getGoal(id: Long): ClinicalGoal? = goals.value.find { it.id == id }

    override suspend fun insert(patientId: Long, data: ValidGoalData, now: Instant): Long {
        val newId = (goals.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val newGoal = ClinicalGoal(
            id = newId,
            patientId = patientId,
            clinicalArea = data.clinicalArea,
            customAreaName = data.customAreaName,
            description = data.description,
            targetSkill = data.targetSkill,
            targetStimulus = data.targetStimulus,
            phonemePosition = data.phonemePosition,
            initialDifficulty = data.initialDifficulty,
            targetAccuracyPercentage = data.targetAccuracyPercentage,
            masteryCriterionDescription = data.masteryCriterionDescription,
            clinicalNotes = data.clinicalNotes,
            status = GoalStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )
        goals.update { it + newGoal }
        return newId
    }

    override suspend fun update(id: Long, data: ValidGoalData, now: Instant) {
        goals.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        clinicalArea = data.clinicalArea,
                        customAreaName = data.customAreaName,
                        description = data.description,
                        targetSkill = data.targetSkill,
                        targetStimulus = data.targetStimulus,
                        phonemePosition = data.phonemePosition,
                        initialDifficulty = data.initialDifficulty,
                        targetAccuracyPercentage = data.targetAccuracyPercentage,
                        masteryCriterionDescription = data.masteryCriterionDescription,
                        clinicalNotes = data.clinicalNotes,
                        updatedAt = now
                    )
                } else it
            }
        }
    }

    override suspend fun setStatus(id: Long, status: GoalStatus, now: Instant) {
        goals.update { list ->
            list.map {
                if (it.id == id) it.copy(status = status, updatedAt = now) else it
            }
        }
    }

    override suspend fun delete(id: Long) {
        goals.update { list -> list.filterNot { it.id == id } }
    }
}

/**
 * In-memory reactive implementation of ClinicalPlatformRepository for anamnesis, appointments, documents, and billing.
 */
class InMemoryClinicalPlatformRepository(private val clock: PlatformClock) : ClinicalPlatformRepository {
    private val now = clock.nowInstant()
    private val anamneses = MutableStateFlow<Map<Long, AnamnesisRecord>>(
        mapOf(
            1L to AnamnesisRecord(
                id = 1L,
                patientId = 1L,
                status = AnamnesisStatus.COMPLETED,
                informantName = "Mariana Silva",
                informantKinship = "Mãe",
                referredBy = "Escola Municipal",
                anamnesisDate = java.time.LocalDate.now().minusMonths(4),
                sections = emptyList(),
                generalSummary = "Anamnese completa sem intercorrências no parto. Queixa fonológica comunicada pela escola.",
                versionNumber = 1,
                createdAt = now.minusSeconds(86400 * 120),
                updatedAt = now.minusSeconds(86400 * 120)
            )
        )
    )

    private val versions = MutableStateFlow<List<AnamnesisVersion>>(emptyList())
    private val assessments = MutableStateFlow<List<ClinicalAssessment>>(emptyList())
    private val phonemes = MutableStateFlow<List<PhonemeInventoryEntry>>(emptyList())
    private val therapyPlans = MutableStateFlow<List<TherapyPlan>>(emptyList())
    private val probes = MutableStateFlow<List<GeneralizationProbe>>(emptyList())
    private val appointments = MutableStateFlow<List<ClinicalAppointment>>(emptyList())
    private val waitlist = MutableStateFlow<List<WaitlistAndReferralEntry>>(emptyList())
    private val homeTasks = MutableStateFlow<List<HomePracticeTask>>(emptyList())
    private val documents = MutableStateFlow<List<ClinicalDocument>>(emptyList())
    private val billing = MutableStateFlow<List<BillingRecord>>(emptyList())
    private val tasks = MutableStateFlow<List<ClinicalTask>>(emptyList())
    private val stimuli = MutableStateFlow<List<TherapeuticStimulus>>(emptyList())
    private val audit = MutableStateFlow<List<AuditEvent>>(emptyList())
    private val profile = MutableStateFlow(ProfessionalProfile())

    override fun observeAnamnesisForPatient(patientId: Long): Flow<AnamnesisRecord?> = anamneses.map { it[patientId] }
    override fun observeAllAnamneses(): Flow<List<AnamnesisRecord>> = anamneses.map { it.values.toList() }
    override fun observeAnamnesisVersions(patientId: Long): Flow<List<AnamnesisVersion>> = versions.map { list -> list.filter { it.patientId == patientId } }
    override suspend fun getAnamnesisForPatient(patientId: Long): AnamnesisRecord? = anamneses.value[patientId]
    override suspend fun saveAnamnesis(record: AnamnesisRecord, changeSummary: String): Long {
        anamneses.update { it + (record.patientId to record) }
        return record.id
    }

    override fun observeAssessmentsForPatient(patientId: Long): Flow<List<ClinicalAssessment>> = assessments.map { list -> list.filter { it.patientId == patientId } }
    override suspend fun saveAssessment(assessment: ClinicalAssessment): Long {
        val newId = (assessments.value.maxOfOrNull { it.id } ?: 0L) + 1L
        assessments.update { it + assessment.copy(id = newId) }
        return newId
    }
    override suspend fun deleteAssessment(id: Long) {
        assessments.update { list -> list.filterNot { it.id == id } }
    }

    override fun observePhonemeInventory(patientId: Long): Flow<List<PhonemeInventoryEntry>> = phonemes.map { list -> list.filter { it.patientId == patientId } }
    override suspend fun upsertPhonemeEntry(entry: PhonemeInventoryEntry): Long {
        val newId = if (entry.id <= 0L) (phonemes.value.maxOfOrNull { it.id } ?: 0L) + 1L else entry.id
        phonemes.update { list -> list.filterNot { it.id == newId } + entry.copy(id = newId) }
        return newId
    }

    override fun observeTherapyPlansForPatient(patientId: Long): Flow<List<TherapyPlan>> = therapyPlans.map { list -> list.filter { it.patientId == patientId } }
    override fun observeAllTherapyPlans(): Flow<List<TherapyPlan>> = therapyPlans
    override suspend fun saveTherapyPlan(plan: TherapyPlan): Long {
        val newId = (therapyPlans.value.maxOfOrNull { it.id } ?: 0L) + 1L
        therapyPlans.update { it + plan.copy(id = newId) }
        return newId
    }
    override suspend fun updateTherapyPlanStatus(id: Long, status: TherapyPlanStatus) {
        therapyPlans.update { list -> list.map { if (it.id == id) it.copy(status = status) else it } }
    }

    override fun observeGeneralizationProbes(patientId: Long): Flow<List<GeneralizationProbe>> = probes.map { list -> list.filter { it.patientId == patientId } }
    override suspend fun insertGeneralizationProbe(probe: GeneralizationProbe): Long {
        val newId = (probes.value.maxOfOrNull { it.id } ?: 0L) + 1L
        probes.update { it + probe.copy(id = newId) }
        return newId
    }

    override fun observeAppointments(): Flow<List<ClinicalAppointment>> = appointments
    override fun observeAppointmentsForPatient(patientId: Long): Flow<List<ClinicalAppointment>> = appointments.map { list -> list.filter { it.patientId == patientId } }
    override suspend fun saveAppointment(appointment: ClinicalAppointment): Long {
        val newId = (appointments.value.maxOfOrNull { it.id } ?: 0L) + 1L
        appointments.update { it + appointment.copy(id = newId) }
        return newId
    }
    override suspend fun updateAppointmentStatus(id: Long, status: AppointmentStatus) {
        appointments.update { list -> list.map { if (it.id == id) it.copy(status = status) else it } }
    }
    override suspend fun deleteAppointment(id: Long) {
        appointments.update { list -> list.filterNot { it.id == id } }
    }

    override fun observeWaitlistAndReferrals(): Flow<List<WaitlistAndReferralEntry>> = waitlist
    override suspend fun saveWaitlistEntry(entry: WaitlistAndReferralEntry): Long {
        val newId = (waitlist.value.maxOfOrNull { it.id } ?: 0L) + 1L
        waitlist.update { it + entry.copy(id = newId) }
        return newId
    }
    override suspend fun markWaitlistConverted(id: Long, patientId: Long) {
        waitlist.update { list -> list.map { if (it.id == id) it.copy(isConvertedToPatient = true, convertedPatientId = patientId) else it } }
    }

    override fun observeHomePracticeForPatient(patientId: Long): Flow<List<HomePracticeTask>> = homeTasks.map { list -> list.filter { it.patientId == patientId } }
    override fun observeAllHomePractice(): Flow<List<HomePracticeTask>> = homeTasks
    override suspend fun saveHomePracticeTask(task: HomePracticeTask): Long {
        val newId = (homeTasks.value.maxOfOrNull { it.id } ?: 0L) + 1L
        homeTasks.update { it + task.copy(id = newId) }
        return newId
    }
    override suspend fun updateHomePracticeFeedback(id: Long, status: HomePracticeStatus, difficulty: HomeDifficultyFeedback, familyNotes: String) {
        homeTasks.update { list -> list.map { if (it.id == id) it.copy(status = status, difficultyFeedback = difficulty, familyNotes = familyNotes) else it } }
    }

    override fun observeDocumentsForPatient(patientId: Long): Flow<List<ClinicalDocument>> = documents.map { list -> list.filter { it.patientId == patientId } }
    override fun observeAllDocuments(): Flow<List<ClinicalDocument>> = documents
    override suspend fun saveDocument(document: ClinicalDocument): Long {
        val newId = (documents.value.maxOfOrNull { it.id } ?: 0L) + 1L
        documents.update { it + document.copy(id = newId) }
        return newId
    }
    override suspend fun appendDocumentAddendum(id: Long, addendumText: String) {
        documents.update { list -> list.map { if (it.id == id) it.copy(bodyContent = it.bodyContent + "\n[Adendo]: " + addendumText) else it } }
    }

    override fun observeBillingRecords(): Flow<List<BillingRecord>> = billing
    override fun observeBillingForPatient(patientId: Long): Flow<List<BillingRecord>> = billing.map { list -> list.filter { it.patientId == patientId } }
    override suspend fun saveBillingRecord(record: BillingRecord): Long {
        val newId = (billing.value.maxOfOrNull { it.id } ?: 0L) + 1L
        billing.update { it + record.copy(id = newId) }
        return newId
    }
    override suspend fun updateBillingStatus(id: Long, status: BillingStatus, paymentMethod: PaymentMethod) {
        billing.update { list -> list.map { if (it.id == id) it.copy(status = status, paymentMethod = paymentMethod) else it } }
    }

    override fun observeClinicalTasks(): Flow<List<ClinicalTask>> = tasks
    override suspend fun saveClinicalTask(task: ClinicalTask): Long {
        val newId = (tasks.value.maxOfOrNull { it.id } ?: 0L) + 1L
        tasks.update { it + task.copy(id = newId) }
        return newId
    }
    override suspend fun toggleTaskCompleted(id: Long, completed: Boolean) {
        tasks.update { list -> list.map { if (it.id == id) it.copy(isCompleted = completed) else it } }
    }

    override fun observeCustomStimuli(): Flow<List<TherapeuticStimulus>> = stimuli
    override suspend fun saveCustomStimulus(stimulus: TherapeuticStimulus): Long {
        val newId = (stimuli.value.maxOfOrNull { it.id } ?: 0L) + 1L
        stimuli.update { it + stimulus.copy(id = newId) }
        return newId
    }

    override fun observeAuditEvents(limit: Int): Flow<List<AuditEvent>> = audit.map { it.take(limit) }
    override suspend fun recordAudit(entityType: String, entityId: Long, action: String, description: String) {
        val newId = (audit.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val ev = AuditEvent(id = newId, entityType = entityType, entityId = entityId, action = action, description = description, timestamp = clock.nowInstant())
        audit.update { listOf(ev) + it }
    }

    override fun observeProfessionalProfile(): Flow<ProfessionalProfile> = profile
    override suspend fun updateProfessionalProfile(profile: ProfessionalProfile) {
        this.profile.value = profile
    }
}
