package com.healthcare.fonolab.feature.patientdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.goal.ClinicalArea
import com.healthcare.fonolab.domain.goal.ClinicalGoal
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.manager.*
import com.healthcare.fonolab.domain.patient.*
import com.healthcare.fonolab.domain.performance.CueLevel
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.session.StartSessionUseCase
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientDetailsUiState(
    val patient: Patient? = null,
    val formattedBirthDateBr: String = "",
    val formattedFollowUpDateBr: String = "",
    val calculatedAgeBr: String = "",
    val anamnesis: AnamnesisRecord? = null,
    val goals: List<ClinicalGoal> = emptyList(),
    val therapyPlans: List<TherapyPlan> = emptyList(),
    val assessments: List<ClinicalAssessment> = emptyList(),
    val phonemeEntries: List<PhonemeInventoryEntry> = emptyList(),
    val generalizationProbes: List<GeneralizationProbe> = emptyList(),
    val sessions: List<TherapySession> = emptyList(),
    val homePracticeTasks: List<HomePracticeTask> = emptyList(),
    val documents: List<ClinicalDocument> = emptyList(),
    val timelineEvents: List<TimelineEvent> = emptyList(),
    val selectedTimelineFilter: TimelineFilterCategory = TimelineFilterCategory.ALL,
    val suggestedActivities: List<SuggestedSessionActivity> = emptyList(),
    val overallClinicalAccuracy: Int = 0,
    val isLoading: Boolean = true,
    val isStartingSession: Boolean = false
)

sealed interface PatientDetailsEvent {
    data object PatientDeleted : PatientDetailsEvent
    data class SessionStarted(val sessionId: Long) : PatientDetailsEvent
    data class Message(val message: String) : PatientDetailsEvent
    data class Error(val message: String) : PatientDetailsEvent
}

@HiltViewModel
class PatientDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val patientRepository: PatientRepository,
    private val goalRepository: ClinicalGoalRepository,
    private val sessionRepository: TherapySessionRepository,
    private val startSessionUseCase: StartSessionUseCase,
    private val changeStatusUseCase: ChangePatientStatusUseCase,
    private val deletePatientUseCase: DeletePatientUseCase,
    val dateTimeManager: DateTimeManager,
    val anamnesisManager: AnamnesisManager,
    val assessmentManager: AssessmentManager,
    val phonologyManager: PhonologyManager,
    val therapyManager: TherapyManager,
    val generalizationManager: GeneralizationManager,
    val sessionManager: SessionManager,
    val planningAssistant: SessionPlanningAssistantManager,
    val homePracticeManager: HomePracticeManager,
    val caregiverManager: CaregiverManager,
    val documentManager: DocumentManager,
    val templateManager: TemplateManager,
    val docAssistant: DocumentationAssistantManager,
    val historyManager: HistoryManager,
    val clinicalDataManager: ClinicalDataManager
) : ViewModel() {

    val patientId: Long = savedStateHandle.get<Long>("patientId") ?: 0L

    private val _event = MutableSharedFlow<PatientDetailsEvent>()
    val event: SharedFlow<PatientDetailsEvent> = _event.asSharedFlow()

    private val selectedFilterFlow = MutableStateFlow(TimelineFilterCategory.ALL)
    private val attemptsFlow = MutableStateFlow<List<GameAttempt>>(emptyList())

    init {
        refreshAttempts()
    }

    private fun refreshAttempts() {
        viewModelScope.launch {
            attemptsFlow.value = sessionRepository.getAttemptsForPatient(patientId)
        }
    }

    private val coreFlow = combine(
        patientRepository.observePatient(patientId),
        anamnesisManager.observeAnamnesis(patientId),
        goalRepository.observeGoalsForPatient(patientId),
        therapyManager.observeTherapyPlans(patientId),
        sessionRepository.observeSessionsForPatient(patientId)
    ) { patient, anamnesis, goals, plans, sessions ->
         CoreBundle(patient, anamnesis, goals, plans, sessions)
    }

    private val clinicalModulesFlow = combine(
        assessmentManager.observeAssessments(patientId),
        phonologyManager.observeInventory(patientId),
        generalizationManager.observeProbes(patientId),
        homePracticeManager.observeForPatient(patientId),
        documentManager.observeDocumentsForPatient(patientId)
    ) { assessments, phonemes, probes, homeTasks, docs ->
        ClinicalModulesBundle(assessments, phonemes, probes, homeTasks, docs)
    }

    val uiState: StateFlow<PatientDetailsUiState> = combine(
        coreFlow,
        clinicalModulesFlow,
        historyManager.observePatientTimeline(patientId),
        selectedFilterFlow,
        attemptsFlow
    ) { core, modules, timeline, filter, attempts ->
        val p = core.patient
        val filteredTimeline = if (filter == TimelineFilterCategory.ALL) {
            timeline
        } else {
            timeline.filter { it.category == filter }
        }
        val accuracy = clinicalDataManager.calculateOverallClinicalAccuracy(attempts)
        PatientDetailsUiState(
            patient = p,
            formattedBirthDateBr = p?.let { dateTimeManager.formatDateBr(it.birthDate) }.orEmpty(),
            formattedFollowUpDateBr = p?.let { dateTimeManager.formatDateBr(it.followUpStartDate) }.orEmpty(),
            calculatedAgeBr = p?.let { dateTimeManager.calculateAge(it.birthDate).toFormattedBr() }.orEmpty(),
            anamnesis = core.anamnesis,
            goals = core.goals,
            therapyPlans = core.plans,
            assessments = modules.assessments,
            phonemeEntries = modules.phonemes,
            generalizationProbes = modules.probes,
            sessions = core.sessions,
            homePracticeTasks = modules.homeTasks,
            documents = modules.docs,
            timelineEvents = filteredTimeline,
            selectedTimelineFilter = filter,
            suggestedActivities = planningAssistant.suggestActivitiesForGoals(core.goals),
            overallClinicalAccuracy = accuracy,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PatientDetailsUiState()
    )

    fun setTimelineFilter(category: TimelineFilterCategory) {
        selectedFilterFlow.value = category
    }

    fun togglePatientStatus() {
        val current = uiState.value.patient ?: return
        val newStatus = if (current.status == PatientStatus.ACTIVE) PatientStatus.ARCHIVED else PatientStatus.ACTIVE
        viewModelScope.launch {
            changeStatusUseCase(patientId, newStatus)
        }
    }

    fun startSession() {
        val currentPatient = uiState.value.patient ?: return
        val goals = uiState.value.goals
        viewModelScope.launch {
            val active = sessionRepository.getActiveSession()
            if (active != null) {
                if (active.session.patientId == patientId) {
                    _event.emit(PatientDetailsEvent.SessionStarted(active.session.id))
                    return@launch
                } else {
                    _event.emit(PatientDetailsEvent.Error("Já existe uma sessão ativa com outro paciente. Finalize-a ou pause-a primeiro."))
                    return@launch
                }
            }
            val res = startSessionUseCase(
                patientId = patientId,
                linkedGoalIds = goals.map { it.id },
                isDemo = currentPatient.isDemo
            )
            when (res) {
                is DomainResult.Success -> _event.emit(PatientDetailsEvent.SessionStarted(res.value))
                is DomainResult.Failure -> _event.emit(PatientDetailsEvent.Error("Não foi possível iniciar a sessão."))
            }
        }
    }

    /**
     * Plans a session in advance (PLANNED status). NEVER starts automatically.
     */
    fun createPlannedSession(
        scheduledDateBr: String,
        selectedGameIds: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            val goalIds = uiState.value.goals.map { it.id }
            val res = sessionManager.createPlannedSession(
                patientId = patientId,
                scheduledDateTextBr = scheduledDateBr,
                linkedGoalIds = goalIds,
                plannedGameIds = selectedGameIds,
                notes = notes,
                summary = "Sessão planejada com antecedência (${selectedGameIds.size} atividades)",
                isDraft = false
            )
            if (res is DomainResult.Success) {
                _event.emit(PatientDetailsEvent.Message("Sessão planejada salva com sucesso! Ela não foi iniciada automaticamente."))
            } else {
                _event.emit(PatientDetailsEvent.Error("Erro ao salvar sessão planejada."))
            }
        }
    }

    fun startPlannedSessionNow(sessionId: Long) {
        viewModelScope.launch {
            val res = sessionManager.startPlannedSession(sessionId)
            when (res) {
                is DomainResult.Success -> _event.emit(PatientDetailsEvent.SessionStarted(res.value))
                is DomainResult.Failure -> _event.emit(PatientDetailsEvent.Error("Já existe outra sessão em andamento."))
            }
        }
    }

    fun duplicateSession(sessionId: Long) {
        viewModelScope.launch {
            sessionManager.duplicateSessionAsPlanned(sessionId)
            _event.emit(PatientDetailsEvent.Message("Sessão duplicada como nova sessão planejada!"))
        }
    }

    fun addTherapyPlan(
        title: String,
        area: ClinicalArea,
        generalObjective: String,
        specificObjectives: String,
        targetSkills: String,
        frequency: Int,
        durationMinutes: Int,
        startDateBr: String
    ) {
        viewModelScope.launch {
            therapyManager.createTherapyPlan(
                patientId = patientId,
                title = title,
                area = area,
                generalObjective = generalObjective,
                specificObjectives = specificObjectives,
                targetSkills = targetSkills,
                weeklyFrequency = frequency,
                durationMinutes = durationMinutes,
                startDateBr = startDateBr
            )
            _event.emit(PatientDetailsEvent.Message("Terapia e objetivos registrados com sucesso!"))
        }
    }

    fun addAssessment(
        title: String,
        area: ClinicalArea,
        protocol: String,
        dateBr: String,
        baselineScore: Int?,
        findings: String,
        hypothesis: String
    ) {
        viewModelScope.launch {
            assessmentManager.saveAssessment(
                patientId = patientId,
                title = title,
                area = area,
                protocol = protocol,
                dateTextBr = dateBr,
                baselineScore = baselineScore,
                currentScore = baselineScore,
                findings = findings,
                hypothesis = hypothesis,
                recommendations = "Intervenção fonoaudiológica estruturada."
            )
            _event.emit(PatientDetailsEvent.Message("Avaliação clínica salva!"))
        }
    }

    fun savePhonemeStatus(
        phoneme: String,
        position: PhonemeWordPosition,
        status: PhonemeAcquisitionStatus,
        errorType: PhonologicalErrorType,
        isStimulable: Boolean,
        sampleWords: String
    ) {
        viewModelScope.launch {
            val existing = uiState.value.phonemeEntries.firstOrNull {
                it.phoneme == phoneme && it.position == position
            }
            phonologyManager.recordPhonemeStatus(
                patientId = patientId,
                phoneme = phoneme,
                position = position,
                status = status,
                errorType = errorType,
                isStimulable = isStimulable,
                bestCue = CueLevel.VISUAL,
                sampleWords = sampleWords,
                existingId = existing?.id ?: 0L
            )
            _event.emit(PatientDetailsEvent.Message("Mapa fonológico atualizado para $phoneme!"))
        }
    }

    fun recordGeneralizationProbe(
        targetSkill: String,
        context: GeneralizationContext,
        interlocutor: InterlocutorType,
        stage: AcquisitionStage,
        accuracy: Int,
        isSpontaneous: Boolean
    ) {
        viewModelScope.launch {
            generalizationManager.recordProbe(
                patientId = patientId,
                goalId = uiState.value.goals.firstOrNull()?.id,
                targetSkillOrPhoneme = targetSkill,
                context = context,
                interlocutor = interlocutor,
                stage = stage,
                accuracyPercentage = accuracy,
                isSpontaneous = isSpontaneous
            )
            _event.emit(PatientDetailsEvent.Message("Sondagem de generalização registrada!"))
        }
    }

    fun assignHomePractice(
        title: String,
        gameId: String?,
        targetSkill: String,
        instructions: String,
        frequency: String
    ) {
        viewModelScope.launch {
            homePracticeManager.assignHomeActivity(
                patientId = patientId,
                title = title,
                linkedGameId = gameId,
                targetSkill = targetSkill,
                instructions = instructions,
                frequency = frequency
            )
            _event.emit(PatientDetailsEvent.Message("Atividade domiciliar atribuída à família!"))
        }
    }

    fun updateHomePracticeFeedback(
        taskId: Long,
        status: HomePracticeStatus,
        difficulty: HomeDifficultyFeedback,
        notes: String
    ) {
        viewModelScope.launch {
            homePracticeManager.recordFamilyFeedback(taskId, status, difficulty, notes)
            _event.emit(PatientDetailsEvent.Message("Feedback familiar atualizado!"))
        }
    }

    fun createClinicalDocument(
        category: DocumentCategory,
        title: String,
        soapS: String,
        soapO: String,
        soapA: String,
        soapP: String,
        bodyContent: String,
        lockAndSign: Boolean
    ) {
        viewModelScope.launch {
            documentManager.createDocument(
                patientId = patientId,
                sessionId = uiState.value.sessions.firstOrNull()?.id,
                category = category,
                title = title,
                professionalName = "Dra. Mariana Fonoaudióloga",
                professionalCrfa = "CRFa 2-12345",
                soapSubjective = soapS,
                soapObjective = soapO,
                soapAssessment = soapA,
                soapPlan = soapP,
                bodyContent = bodyContent,
                lockAndSign = lockAndSign
            )
            _event.emit(PatientDetailsEvent.Message("Documento clínico registrado no prontuário!"))
        }
    }

    fun deletePatient() {
        viewModelScope.launch {
            val result = deletePatientUseCase(patientId)
            if (result is DomainResult.Success) {
                _event.emit(PatientDetailsEvent.PatientDeleted)
            } else {
                _event.emit(PatientDetailsEvent.Error("Erro ao excluir paciente."))
            }
        }
    }

    private data class CoreBundle(
        val patient: Patient?,
        val anamnesis: AnamnesisRecord?,
        val goals: List<ClinicalGoal>,
        val plans: List<TherapyPlan>,
        val sessions: List<TherapySession>
    )

    private data class ClinicalModulesBundle(
        val assessments: List<ClinicalAssessment>,
        val phonemes: List<PhonemeInventoryEntry>,
        val probes: List<GeneralizationProbe>,
        val homeTasks: List<HomePracticeTask>,
        val docs: List<ClinicalDocument>
    )
}
