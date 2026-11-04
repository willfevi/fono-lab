package com.healthcare.fonolab.feature.patientdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.*
import com.healthcare.fonolab.core.designsystem.theme.*
import com.healthcare.fonolab.core.pdf.AnamnesisPdfExporter
import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.goal.ClinicalArea
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.SessionStatus
import com.healthcare.fonolab.feature.games.GameCatalogRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    viewModel: PatientDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToStartSession: (Long) -> Unit,
    onNavigateToSessionHub: (Long) -> Unit,
    onNavigateToAnamnesis: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPlanSessionDialog by remember { mutableStateOf(false) }
    var showNewTherapyDialog by remember { mutableStateOf(false) }
    var showNewAssessmentDialog by remember { mutableStateOf(false) }
    var showNewGeneralizationDialog by remember { mutableStateOf(false) }
    var showNewHomeTaskDialog by remember { mutableStateOf(false) }
    var showNewDocumentDialog by remember { mutableStateOf(false) }
    var selectedPhonemeForEdit by remember { mutableStateOf<Pair<String, String>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is PatientDetailsEvent.PatientDeleted -> onNavigateBack()
                is PatientDetailsEvent.SessionStarted -> onNavigateToSessionHub(event.sessionId)
                is PatientDetailsEvent.Message -> snackbarHostState.showSnackbar(event.message)
                is PatientDetailsEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (state.isLoading || state.patient == null) {
        FonoLoadingScreen(message = "Carregando prontuário eletrônico...")
        return
    }

    val patient = state.patient!!
    val completedSessionsCount = state.sessions.count { it.status == SessionStatus.COMPLETED }
    val plannedSessionsCount = state.sessions.count { it.status == SessionStatus.PLANNED || it.status == SessionStatus.DRAFT }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MockupBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Prontuário Clínico",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextDark
                        )
                        Text(
                            text = "#${patient.internalCode} • ${patient.status.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MockupSurface)
                                .border(1.dp, MockupBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MockupTextDark, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToAnamnesis(patient.id) }) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MockupYellowCard)
                                .border(1.dp, MockupBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = "Anamnese", tint = MockupDarkPill, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = { onNavigateToEdit(patient.id) }) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MockupSurface)
                                .border(1.dp, MockupBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MockupTextDark, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(onClick = viewModel::togglePatientStatus) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MockupSurface)
                                .border(1.dp, MockupBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (patient.status == PatientStatus.ACTIVE) Icons.Default.Archive else Icons.Default.Unarchive,
                                contentDescription = "Arquivar",
                                tint = MockupTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MockupSurface)
                                .border(1.dp, MockupBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MockupBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Patient Compact Hero Card
            FonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(MockupPurpleHero.copy(alpha = 0.45f))
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = patient.fullName.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MockupDarkPill
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = patient.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MockupTextDark
                            )
                            Text(
                                text = "Nasc.: ${state.formattedBirthDateBr} (${state.calculatedAgeBr})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MockupTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PastelMetricCard(
                            label = "Acurácia",
                            value = "${state.overallClinicalAccuracy}%",
                            unit = "clínica",
                            containerColor = MockupGreenCard,
                            modifier = Modifier.weight(1f)
                        )
                        PastelMetricCard(
                            label = "Planejadas",
                            value = plannedSessionsCount.toString(),
                            unit = "sessões",
                            containerColor = MockupCyanCard,
                            modifier = Modifier.weight(1f)
                        )
                        PastelMetricCard(
                            label = "Concluídas",
                            value = completedSessionsCount.toString(),
                            unit = "sessões",
                            containerColor = MockupPeachCard,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FonoSecondaryButton(
                            text = "Planejar Sessão",
                            onClick = { showPlanSessionDialog = true },
                            icon = Icons.Default.EventNote,
                            modifier = Modifier.weight(1f)
                        )
                        FonoPrimaryButton(
                            text = "Iniciar Agora",
                            onClick = viewModel::startSession,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Scrollable Clinical Hub Tabs
            val tabs = listOf(
                "Resumo & Anamnese",
                "Fonologia & Avaliações",
                "Terapias & Generalização",
                "Sessões (${state.sessions.size})",
                "Casa & SOAP",
                "Linha do Tempo"
            )
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MockupDarkPill,
                edgePadding = 16.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTab == index) MockupDarkPill else MockupTextMuted,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> OverviewAndAnamnesisTab(
                    state = state,
                    onOpenAnamnesis = { onNavigateToAnamnesis(patient.id) }
                )
                1 -> PhonologyAndAssessmentsTab(
                    state = state,
                    standardPhonemes = viewModel.phonologyManager.standardBrazilianPhonemes,
                    onNewAssessment = { showNewAssessmentDialog = true },
                    onPhonemeClick = { selectedPhonemeForEdit = it }
                )
                2 -> TherapiesAndGeneralizationTab(
                    state = state,
                    onNewTherapy = { showNewTherapyDialog = true },
                    onNewProbe = { showNewGeneralizationDialog = true }
                )
                3 -> AdvanceSessionsTab(
                    state = state,
                    onPlanSession = { showPlanSessionDialog = true },
                    onStartPlannedSession = viewModel::startPlannedSessionNow,
                    onDuplicateSession = viewModel::duplicateSession,
                    onOpenSessionHub = onNavigateToSessionHub
                )
                4 -> HomePracticeAndDocumentsTab(
                    state = state,
                    onNewHomeTask = { showNewHomeTaskDialog = true },
                    onUpdateHomeFeedback = viewModel::updateHomePracticeFeedback,
                    onNewDocument = { showNewDocumentDialog = true },
                    onExportDocumentPdf = { doc ->
                        val file = AnamnesisPdfExporter.exportClinicalDocumentToFile(context, patient, doc)
                        viewModel.createClinicalDocument(
                            doc.category, doc.title, doc.soapSubjective, doc.soapObjective, doc.soapAssessment, doc.soapPlan,
                            doc.bodyContent, true
                        )
                    }
                )
                5 -> LongitudinalTimelineTab(
                    state = state,
                    onSelectFilter = viewModel::setTimelineFilter
                )
            }
        }
    }

    // DIALOG 1: ADVANCE SESSION PLANNER (PLANNED SESSION - NEVER AUTO-STARTS)
    if (showPlanSessionDialog) {
        var dateBr by remember { mutableStateOf(viewModel.dateTimeManager.formatDateBr(viewModel.dateTimeManager.today())) }
        val suggestedIds = remember(state.suggestedActivities) {
            state.suggestedActivities.map { it.gameId }.toMutableStateList()
        }
        var planNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPlanSessionDialog = false },
            title = { Text("Montar Sessão com Antecedência") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "A sessão será salva com status PLANEJADA e só iniciará quando você tocar em 'Iniciar Sessão'.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MockupTextMuted
                    )
                    OutlinedTextField(
                        value = dateBr,
                        onValueChange = { dateBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Data prevista (DD/MM/AAAA)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Sugestões do Assistente Clínico e Catálogo (10 Jogos):",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    GameCatalogRegistry.games.forEach { game ->
                        val checked = suggestedIds.contains(game.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (checked) suggestedIds.remove(game.id) else suggestedIds.add(game.id)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    if (it) suggestedIds.add(game.id) else suggestedIds.remove(game.id)
                                }
                            )
                            Text("${game.iconEmoji} ${game.title} (${game.estimatedDurationMinutes} min)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedTextField(
                        value = planNotes,
                        onValueChange = { planNotes = it },
                        label = { Text("Roteiro / materiais e preparação") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPlanSessionDialog = false
                        viewModel.createPlannedSession(dateBr, suggestedIds.toList(), planNotes)
                    }
                ) {
                    Text("Salvar Sessão Planejada")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanSessionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // DIALOG 2: NEW THERAPY PLAN
    if (showNewTherapyDialog) {
        var title by remember { mutableStateOf("Terapia Fonológica e Linguagem") }
        var generalObj by remember { mutableStateOf("Adequação do inventário fonológico e expansão morfossintática") }
        var specificObj by remember { mutableStateOf("Aquisição de fricativas e líquidas com 80% de precisão sem pista") }
        var targetSkills by remember { mutableStateOf("/s/, /ʃ/, /ɾ/, Consciência Fonológica") }
        var startDateBr by remember { mutableStateOf(viewModel.dateTimeManager.formatDateBr(viewModel.dateTimeManager.today())) }

        AlertDialog(
            onDismissRequest = { showNewTherapyDialog = false },
            title = { Text("Nova Terapia e Objetivos") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título da Terapia") }, singleLine = true)
                    OutlinedTextField(value = generalObj, onValueChange = { generalObj = it }, label = { Text("Objetivo Geral") })
                    OutlinedTextField(value = specificObj, onValueChange = { specificObj = it }, label = { Text("Critério de Domínio / Específico") })
                    OutlinedTextField(value = targetSkills, onValueChange = { targetSkills = it }, label = { Text("Fonemas / Habilidades-alvo") })
                    OutlinedTextField(
                        value = startDateBr,
                        onValueChange = { startDateBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Data de início (DD/MM/AAAA)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewTherapyDialog = false
                        viewModel.addTherapyPlan(
                            title = title,
                            area = ClinicalArea.SPEECH_AND_PHONOLOGY,
                            generalObjective = generalObj,
                            specificObjectives = specificObj,
                            targetSkills = targetSkills,
                            frequency = 2,
                            durationMinutes = 50,
                            startDateBr = startDateBr
                        )
                    }
                ) {
                    Text("Salvar Terapia")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTherapyDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIALOG 3: NEW CLINICAL ASSESSMENT
    if (showNewAssessmentDialog) {
        var title by remember { mutableStateOf("Avaliação Fonológica e Linguagem (ABFW)") }
        var protocol by remember { mutableStateOf("ABFW - Fonologia e Vocabulário") }
        var dateBr by remember { mutableStateOf(viewModel.dateTimeManager.formatDateBr(viewModel.dateTimeManager.today())) }
        var findings by remember { mutableStateOf("Observados processos de simplificação de líquidas e encontro consonantal.") }
        var hypothesis by remember { mutableStateOf("Transtorno Fonológico de grau leve-moderado.") }

        AlertDialog(
            onDismissRequest = { showNewAssessmentDialog = false },
            title = { Text("Registrar Avaliação Clínica") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Instrumento / Avaliação") })
                    OutlinedTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("Protocolo utilizado") })
                    OutlinedTextField(
                        value = dateBr,
                        onValueChange = { dateBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Data (DD/MM/AAAA)") }
                    )
                    OutlinedTextField(value = findings, onValueChange = { findings = it }, label = { Text("Achados clínicos e linha de base") })
                    OutlinedTextField(value = hypothesis, onValueChange = { hypothesis = it }, label = { Text("Hipótese diagnóstica fonoaudiológica") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewAssessmentDialog = false
                        viewModel.addAssessment(title, ClinicalArea.SPEECH_AND_PHONOLOGY, protocol, dateBr, 65, findings, hypothesis)
                    }
                ) {
                    Text("Salvar Avaliação")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewAssessmentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIALOG 4: PHONEME INVENTORY UPDATE
    selectedPhonemeForEdit?.let { (phoneme, sampleWords) ->
        var selectedStatus by remember { mutableStateOf(PhonemeAcquisitionStatus.IN_ACQUISITION) }
        var selectedError by remember { mutableStateOf(PhonologicalErrorType.NONE) }
        var stimulable by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { selectedPhonemeForEdit = null },
            title = { Text("Fonema $phoneme ($sampleWords)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Status de Aquisição:", fontWeight = FontWeight.Bold)
                    PhonemeAcquisitionStatus.entries.forEach { st ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedStatus = st }
                        ) {
                            RadioButton(selected = selectedStatus == st, onClick = { selectedStatus = st })
                            Text(st.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = stimulable, onCheckedChange = { stimulable = it })
                        Text("Estimulável com pista visual/tátil")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.savePhonemeStatus(
                            phoneme = phoneme,
                            position = PhonemeWordPosition.INITIAL,
                            status = selectedStatus,
                            errorType = selectedError,
                            isStimulable = stimulable,
                            sampleWords = sampleWords
                        )
                        selectedPhonemeForEdit = null
                    }
                ) {
                    Text("Salvar no Mapa Fonológico")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedPhonemeForEdit = null }) { Text("Cancelar") }
            }
        )
    }

    // DIALOG 5: GENERALIZATION PROBE
    if (showNewGeneralizationDialog) {
        var targetSkill by remember { mutableStateOf("/ɾ/ em Onset Medial e Fala Espontânea") }
        var selectedContext by remember { mutableStateOf(GeneralizationContext.HOME) }
        var selectedInterlocutor by remember { mutableStateOf(InterlocutorType.CAREGIVER) }
        var selectedStage by remember { mutableStateOf(AcquisitionStage.GENERALIZED) }

        AlertDialog(
            onDismissRequest = { showNewGeneralizationDialog = false },
            title = { Text("Registrar Sondagem de Generalização") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = targetSkill, onValueChange = { targetSkill = it }, label = { Text("Fonema ou Habilidade") })
                    Text("Ambiente / Contexto:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GeneralizationContext.entries.forEach { ctx ->
                            FilterChip(
                                selected = selectedContext == ctx,
                                onClick = { selectedContext = ctx },
                                label = { Text(ctx.displayName) }
                            )
                        }
                    }
                    Text("Interlocutor:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        InterlocutorType.entries.forEach { inter ->
                            FilterChip(
                                selected = selectedInterlocutor == inter,
                                onClick = { selectedInterlocutor = inter },
                                label = { Text(inter.displayName) }
                            )
                        }
                    }
                    Text("Estágio Clínico:", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AcquisitionStage.entries.forEach { st ->
                            FilterChip(
                                selected = selectedStage == st,
                                onClick = { selectedStage = st },
                                label = { Text(st.displayName) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewGeneralizationDialog = false
                        viewModel.recordGeneralizationProbe(targetSkill, selectedContext, selectedInterlocutor, selectedStage, 85, true)
                    }
                ) {
                    Text("Registrar Sondagem")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGeneralizationDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIALOG 6: HOME PRACTICE ASSIGNMENT
    if (showNewHomeTaskDialog) {
        var title by remember { mutableStateOf("Treino Lúdico: Boca Mágica e Rimas em Casa") }
        var targetSkill by remember { mutableStateOf("Produção de /s/ e /ɾ/ + Rimas") }
        var instructions by remember { mutableStateOf("Brincar diante do espelho por 10 minutos nomeando 5 palavras com o som trabalhado, sem corrigir de forma punitiva.") }
        var frequency by remember { mutableStateOf("10 min/dia • 3x na semana") }

        AlertDialog(
            onDismissRequest = { showNewHomeTaskDialog = false },
            title = { Text("Prescrever Atividade para Casa") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título da atividade") })
                    OutlinedTextField(value = targetSkill, onValueChange = { targetSkill = it }, label = { Text("Habilidade-alvo") })
                    OutlinedTextField(value = frequency, onValueChange = { frequency = it }, label = { Text("Frequência recomendada") })
                    OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Orientação para os pais/cuidadores") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewHomeTaskDialog = false
                        viewModel.assignHomePractice(title, "magic_mouth", targetSkill, instructions, frequency)
                    }
                ) {
                    Text("Enviar para Família")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewHomeTaskDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // DIALOG 7: SOAP EVOLUTION / CLINICAL DOCUMENT
    if (showNewDocumentDialog) {
        val draftMap = remember {
            viewModel.docAssistant.generateSoapDraft(patient.fullName, state.sessions.size.coerceAtLeast(1), emptyList())
        }
        var selectedCategory by remember { mutableStateOf(DocumentCategory.SOAP_EVOLUTION) }
        var soapS by remember { mutableStateOf(draftMap["S"].orEmpty()) }
        var soapO by remember { mutableStateOf(draftMap["O"].orEmpty()) }
        var soapA by remember { mutableStateOf(draftMap["A"].orEmpty()) }
        var soapP by remember { mutableStateOf(draftMap["P"].orEmpty()) }
        var freeBody by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNewDocumentDialog = false },
            title = { Text("Novo Documento / Evolução SOAP") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(
                            DocumentCategory.SOAP_EVOLUTION,
                            DocumentCategory.SCHOOL_REPORT,
                            DocumentCategory.REFERRAL_LETTER,
                            DocumentCategory.ATTENDANCE_DECLARATION,
                            DocumentCategory.DISCHARGE_SUMMARY
                        ).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = cat
                                    if (cat != DocumentCategory.SOAP_EVOLUTION) {
                                        freeBody = viewModel.templateManager.getDocumentTemplateBody(cat, patient.fullName)
                                    }
                                },
                                label = { Text(cat.displayName) }
                            )
                        }
                    }

                    if (selectedCategory == DocumentCategory.SOAP_EVOLUTION) {
                        OutlinedTextField(value = soapS, onValueChange = { soapS = it }, label = { Text("S — Subjetivo (Estado da criança / relato familiar)") })
                        OutlinedTextField(value = soapO, onValueChange = { soapO = it }, label = { Text("O — Objetivo (Desempenho, tentativas e pistas)") })
                        OutlinedTextField(value = soapA, onValueChange = { soapA = it }, label = { Text("A — Avaliação (Análise fonoaudiológica)") })
                        OutlinedTextField(value = soapP, onValueChange = { soapP = it }, label = { Text("P — Plano (Conduta para próxima sessão e casa)") })
                    } else {
                        OutlinedTextField(
                            value = freeBody,
                            onValueChange = { freeBody = it },
                            label = { Text("Conteúdo do documento") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewDocumentDialog = false
                        viewModel.createClinicalDocument(
                            category = selectedCategory,
                            title = selectedCategory.displayName,
                            soapS = soapS,
                            soapO = soapO,
                            soapA = soapA,
                            soapP = soapP,
                            bodyContent = freeBody,
                            lockAndSign = true
                        )
                    }
                ) {
                    Text("Emitir e Assinar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewDocumentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Prontuário?") },
            text = {
                Text("Esta ação é permanente e removerá todas as metas, anamnese, avaliações e sessões associadas a ${patient.fullName}.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePatient()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir Definitivamente")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun OverviewAndAnamnesisTab(
    state: PatientDetailsUiState,
    onOpenAnamnesis: () -> Unit
) {
    val patient = state.patient ?: return
    val anamnesis = state.anamnesis
    val anamnesisStatusText = anamnesis?.status?.displayName ?: AnamnesisStatus.NOT_STARTED.displayName
    val anamnesisProgress = anamnesis?.let { "${it.filledSectionsCount}/12 seções preenchidas (v${it.versionNumber})" }
        ?: "12 seções clínicas prontas para preenchimento"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Integrated Anamnesis Card (Phase 3)
        item {
            FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupYellowCard) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Anamnese Fonoaudiológica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MockupDarkPill
                        )
                        FonoStatusBadge(
                            text = anamnesisStatusText,
                            containerColor = MockupSurface,
                            contentColor = MockupDarkPill
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = anamnesisProgress,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MockupDarkPill
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FonoPrimaryButton(
                        text = if (anamnesis == null) "Preencher Anamnese (12 Seções + PDF)" else "Editar / Exportar Anamnese em PDF",
                        onClick = onOpenAnamnesis,
                        icon = Icons.Default.Assignment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Complete Patient Identification & Profiles
        item {
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Dados Cadastrais e Familiares", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupTextDark)
                    InfoPair("Data de Nascimento (BR)", "${state.formattedBirthDateBr} (${state.calculatedAgeBr})")
                    InfoPair("Responsável Legal", "${patient.guardianName ?: "Não informado"} ${patient.guardianKinship?.let { "($it)" } ?: ""}")
                    InfoPair("Contato / Telefone", patient.guardianContact ?: patient.phone ?: "Não informado")
                    InfoPair("Contato de Emergência", patient.emergencyContact ?: "Não informado")
                    InfoPair("E-mail / Endereço", listOfNotNull(patient.email, patient.address).joinToString(" • ").ifBlank { "Não informado" })
                    InfoPair("Escola / Escolaridade", listOfNotNull(patient.school, patient.educationLevel).joinToString(" • ").ifBlank { "Não informada" })
                    InfoPair("Início do Acompanhamento", state.formattedFollowUpDateBr)
                }
            }
        }

        item {
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Perfil Comunicativo, Bilíngue e Sensorial", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupTextDark)
                    InfoPair("Modalidade Comunicativa", patient.communicationModalities)
                    InfoPair("Contexto Linguístico / Idiomas", patient.homeLanguages)
                    InfoPair("Reforçadores e Interesses", patient.effectiveReinforcers.ifBlank { "Jogos interativos e elogios verbais" })
                    InfoPair("Modo de Baixo Estímulo", if (patient.lowStimulationMode) "Ativado (Acessibilidade sensorial)" else "Padrão")
                    if (patient.clinicalNotes.isNotBlank()) {
                        InfoPair("Observações Clínicas", patient.clinicalNotes)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPair(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MockupTextMuted, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
    }
}

@Composable
private fun PhonologyAndAssessmentsTab(
    state: PatientDetailsUiState,
    standardPhonemes: List<Pair<String, String>>,
    onNewAssessment: () -> Unit,
    onPhonemeClick: (Pair<String, String>) -> Unit
) {
    val entryByPhoneme = state.phonemeEntries.associateBy { it.phoneme }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mapa Fonético-Fonológico (PT-BR)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MockupTextDark)
                FonoSecondaryButton(
                    text = "Nova Avaliação",
                    onClick = onNewAssessment,
                    icon = Icons.Default.Add,
                    modifier = Modifier.height(38.dp)
                )
            }
            Text(
                text = "Toque em qualquer fonema para registrar aquisição, omissão/substituição e estimulabilidade.",
                style = MaterialTheme.typography.bodySmall,
                color = MockupTextMuted
            )
        }

        items(standardPhonemes) { (phoneme, example) ->
            val recorded = entryByPhoneme[phoneme]
            val status = recorded?.status ?: PhonemeAcquisitionStatus.NOT_EVALUATED
            val badgeBg = when (status) {
                PhonemeAcquisitionStatus.GENERALIZED, PhonemeAcquisitionStatus.IN_MAINTENANCE -> MockupGreenCard
                PhonemeAcquisitionStatus.IN_ACQUISITION, PhonemeAcquisitionStatus.IN_INTERVENTION -> MockupYellowCard
                PhonemeAcquisitionStatus.EVALUATED_ABSENT -> MockupPeachCard
                PhonemeAcquisitionStatus.NOT_EVALUATED -> MockupSurface
            }
            FonoCard(
                onClick = { onPhonemeClick(phoneme to example) },
                modifier = Modifier.fillMaxWidth(),
                containerColor = badgeBg
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$phoneme — $example", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupDarkPill)
                        Text(
                            text = recorded?.let { "Posição: ${it.position.displayName} • Estimulável: ${if (it.isStimulable) "Sim" else "Não"}" }
                                ?: "Toque para avaliar produção fonêmica",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupTextMuted
                        )
                    }
                    FonoStatusBadge(text = status.displayName, containerColor = MockupSurface, contentColor = MockupDarkPill)
                }
            }
        }

        if (state.assessments.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Avaliações e Protocolos Registrados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MockupTextDark)
            }
            items(state.assessments) { a ->
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(a.title, fontWeight = FontWeight.Bold, color = MockupTextDark)
                        Text("Protocolo: ${a.protocolOrInstrument} • Data: ${a.assessmentDate}", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(a.findingsSummary, style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                        if (a.clinicalHypothesis.isNotBlank()) {
                            Text("Hipótese: ${a.clinicalHypothesis}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MockupDarkPill)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TherapiesAndGeneralizationTab(
    state: PatientDetailsUiState,
    onNewTherapy: () -> Unit,
    onNewProbe: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Planos Terapêuticos (${state.therapyPlans.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MockupTextDark)
                FonoPrimaryButton(
                    text = "Nova Terapia",
                    onClick = onNewTherapy,
                    icon = Icons.Default.Add,
                    modifier = Modifier.height(40.dp)
                )
            }
        }

        if (state.therapyPlans.isEmpty()) {
            item {
                FonoEmptyState(
                    icon = Icons.Default.Flag,
                    title = "Nenhuma terapia cadastrada",
                    description = "Crie uma terapia com frequência semanal, fonemas-alvo e critérios de alta.",
                    actionLabel = "Criar Terapia",
                    onActionClick = onNewTherapy
                )
            }
        } else {
            items(state.therapyPlans) { plan ->
                FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupGreenCard.copy(alpha = 0.45f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(plan.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupDarkPill)
                            FonoStatusBadge(text = plan.status.displayName)
                        }
                        Text("${plan.clinicalArea.displayName} • ${plan.weeklyFrequency}x/semana (${plan.sessionDurationMinutes} min)", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Objetivo: ${plan.generalObjective}", style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                        Text("Alvos: ${plan.targetSkillsOrPhonemes}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MockupDarkPill)
                    }
                }
            }
        }

        // Generalization Matrix Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Aquisição, Manutenção e Generalização", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MockupTextDark)
                FonoSecondaryButton(
                    text = "Sondagem",
                    onClick = onNewProbe,
                    icon = Icons.Default.Add,
                    modifier = Modifier.height(38.dp)
                )
            }
        }

        items(state.generalizationProbes) { probe ->
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(probe.targetSkillOrPhoneme, fontWeight = FontWeight.Bold, color = MockupTextDark)
                        Text("Ambiente: ${probe.context.displayName} • Interlocutor: ${probe.interlocutor.displayName}", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                        Text("Estágio: ${probe.stage.displayName} (${probe.accuracyPercentage}%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MockupDarkPill)
                    }
                    FonoStatusBadge(text = if (probe.isSpontaneous) "Espontânea" else "Dirigida")
                }
            }
        }
    }
}

@Composable
private fun AdvanceSessionsTab(
    state: PatientDetailsUiState,
    onPlanSession: () -> Unit,
    onStartPlannedSession: (Long) -> Unit,
    onDuplicateSession: (Long) -> Unit,
    onOpenSessionHub: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupBlueCard) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Construtor de Sessões Antecipadas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupDarkPill)
                    Text(
                        text = "Monte a sessão antes do atendimento escolhendo jogos e objetivos. Sessões planejadas nunca iniciam sozinhas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MockupDarkPill
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FonoPrimaryButton(
                        text = "Planejar Nova Sessão com Antecedência",
                        onClick = onPlanSession,
                        icon = Icons.Default.EventNote,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        items(state.sessions) { session ->
            val isPlanned = session.status == SessionStatus.PLANNED || session.status == SessionStatus.DRAFT
            FonoCard(
                onClick = { onOpenSessionHub(session.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Sessão #${session.id}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupTextDark)
                        FonoStatusBadge(
                            text = session.status.displayName,
                            containerColor = if (isPlanned) MockupYellowCard else MockupGreenCard,
                            contentColor = MockupDarkPill
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPlanned) "Sessão planejada (aguardando início manual)" else "Duração registrada: ${session.durationSeconds / 60} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MockupTextMuted
                    )
                    if (session.clinicalSummary.isNotBlank()) {
                        Text(session.clinicalSummary, style = MaterialTheme.typography.bodySmall, color = MockupTextDark)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isPlanned || session.status == SessionStatus.PAUSED) {
                            FonoPrimaryButton(
                                text = "Iniciar Sessão",
                                onClick = { onStartPlannedSession(session.id) },
                                icon = Icons.Default.PlayArrow,
                                modifier = Modifier.weight(1f).height(40.dp)
                            )
                        }
                        FonoSecondaryButton(
                            text = "Duplicar",
                            onClick = { onDuplicateSession(session.id) },
                            icon = Icons.Default.ContentCopy,
                            modifier = Modifier.weight(1f).height(40.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomePracticeAndDocumentsTab(
    state: PatientDetailsUiState,
    onNewHomeTask: () -> Unit,
    onUpdateHomeFeedback: (Long, HomePracticeStatus, HomeDifficultyFeedback, String) -> Unit,
    onNewDocument: () -> Unit,
    onExportDocumentPdf: (ClinicalDocument) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Atividades para Casa (${state.homePracticeTasks.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MockupTextDark)
                FonoSecondaryButton(
                    text = "Prescrever Casa",
                    onClick = onNewHomeTask,
                    icon = Icons.Default.HomeWork,
                    modifier = Modifier.height(38.dp)
                )
            }
        }

        items(state.homePracticeTasks) { task ->
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(task.title, fontWeight = FontWeight.Bold, color = MockupTextDark)
                        FonoStatusBadge(text = task.status.displayName)
                    }
                    Text("${task.targetSkill} • ${task.recommendedFrequency}", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                    Text(task.caregiverInstructions, style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                    if (task.status != HomePracticeStatus.COMPLETED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FonoSecondaryButton(
                            text = "Marcar Concluída pela Família (Adequado)",
                            onClick = {
                                onUpdateHomeFeedback(
                                    task.id,
                                    HomePracticeStatus.COMPLETED,
                                    HomeDifficultyFeedback.ADEQUATE,
                                    "Realizado com boa participação em casa."
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Documentos & Evolução SOAP (${state.documents.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MockupTextDark)
                FonoPrimaryButton(
                    text = "Novo SOAP / Doc",
                    onClick = onNewDocument,
                    icon = Icons.Default.Description,
                    modifier = Modifier.height(40.dp)
                )
            }
        }

        items(state.documents) { doc ->
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(doc.title, fontWeight = FontWeight.Bold, color = MockupTextDark)
                        IconButton(onClick = { onExportDocumentPdf(doc) }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "Exportar PDF", tint = MockupDarkPill)
                        }
                    }
                    Text("${doc.category.displayName} • ${doc.documentDate} • ${doc.professionalCrfa}", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(doc.bodyContent, style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                }
            }
        }
    }
}

@Composable
private fun LongitudinalTimelineTab(
    state: PatientDetailsUiState,
    onSelectFilter: (TimelineFilterCategory) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimelineFilterCategory.entries.forEach { cat ->
                val selected = state.selectedTimelineFilter == cat
                FilterChip(
                    selected = selected,
                    onClick = { onSelectFilter(cat) },
                    label = { Text(cat.displayName) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (state.timelineEvents.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.Timeline,
                title = "Nenhum evento neste filtro",
                description = "Registre anamnese, avaliações, terapias, sessões ou documentos SOAP para visualizar a linha do tempo."
            )
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.timelineEvents) { ev ->
                    FonoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ev.title, fontWeight = FontWeight.Bold, color = MockupTextDark)
                                FonoStatusBadge(text = ev.badgeText)
                            }
                            Text(ev.subtitle, style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(ev.details, style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                        }
                    }
                }
            }
        }
    }
}
