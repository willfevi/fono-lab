package com.healthcare.fonolab.feature.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QueuePlayNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.designsystem.component.*
import com.healthcare.fonolab.core.designsystem.theme.*
import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.manager.ReferralManager
import com.healthcare.fonolab.domain.manager.ScheduleManager
import com.healthcare.fonolab.domain.manager.SessionManager
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.SessionStatus
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.feature.games.GameCatalogRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionsAgendaUiState(
    val sessions: List<TherapySession> = emptyList(),
    val appointments: List<ClinicalAppointment> = emptyList(),
    val waitlistEntries: List<WaitlistAndReferralEntry> = emptyList(),
    val activePatients: List<Patient> = emptyList(),
    val todayBr: String = ""
)

@HiltViewModel
class SessionsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val scheduleManager: ScheduleManager,
    private val referralManager: ReferralManager,
    patientRepository: PatientRepository,
    val dateTimeManager: DateTimeManager
) : ViewModel() {

    val uiState: StateFlow<SessionsAgendaUiState> = combine(
        sessionManager.observeRecentSessions(50),
        scheduleManager.observeAppointments(),
        referralManager.observeWaitlistAndReferrals(),
        patientRepository.observePatients(PatientFilter(status = PatientStatus.ACTIVE))
    ) { sessions, appointments, waitlist, patients ->
        SessionsAgendaUiState(
            sessions = sessions,
            appointments = appointments,
            waitlistEntries = waitlist,
            activePatients = patients,
            todayBr = dateTimeManager.formatDateBr(dateTimeManager.today())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionsAgendaUiState()
    )

    fun createPlannedSession(
        patientId: Long,
        dateBr: String,
        gameIds: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            sessionManager.createPlannedSession(
                patientId = patientId,
                scheduledDateTextBr = dateBr,
                linkedGoalIds = emptyList(),
                plannedGameIds = gameIds,
                notes = notes,
                summary = "Sessão planejada (${gameIds.size} jogos)"
            )
        }
    }

    fun scheduleAppointment(
        patientId: Long?,
        patientName: String,
        dateBr: String,
        startTime: String,
        endTime: String,
        serviceType: String,
        recurrence: AppointmentRecurrence,
        notes: String
    ) {
        viewModelScope.launch {
            scheduleManager.scheduleAppointment(
                patientId = patientId,
                patientName = patientName,
                dateTextBr = dateBr,
                startTime = startTime,
                endTime = endTime,
                serviceType = serviceType,
                recurrence = recurrence,
                notes = notes
            )
        }
    }

    fun updateAppointmentStatus(id: Long, status: AppointmentStatus) {
        viewModelScope.launch {
            scheduleManager.updateStatus(id, status)
        }
    }

    fun addWaitlistEntry(
        candidateName: String,
        birthDateBr: String,
        guardianContact: String,
        referredBy: String,
        complaint: String,
        urgency: ReferralUrgency
    ) {
        viewModelScope.launch {
            referralManager.registerReferralOrWaitlist(
                candidateName = candidateName,
                birthDateText = birthDateBr,
                guardianContact = guardianContact,
                referredBy = referredBy,
                mainComplaint = complaint,
                preferredSchedule = "Tarde / Flexível",
                urgency = urgency
            )
        }
    }
}

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel = hiltViewModel(),
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showPlanSessionDialog by remember { mutableStateOf(false) }
    var showNewAppointmentDialog by remember { mutableStateOf(false) }
    var showNewWaitlistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Sessões, Agenda e Triagem",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Text(
            text = "Planejamento antecipado de sessões, agendamentos e lista de espera",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        val tabs = listOf(
            "Sessões (${state.sessions.size})",
            "Agenda (${state.appointments.size})",
            "Fila & Triagem (${state.waitlistEntries.size})"
        )
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MockupDarkPill
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == idx) MockupDarkPill else MockupTextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> SessionsListContent(
                sessions = state.sessions,
                patients = state.activePatients,
                dateTimeManager = viewModel.dateTimeManager,
                onPlanClick = { showPlanSessionDialog = true },
                onSessionClick = onSessionClick
            )
            1 -> AgendaContent(
                appointments = state.appointments,
                dateTimeManager = viewModel.dateTimeManager,
                onNewAppointment = { showNewAppointmentDialog = true },
                onUpdateStatus = viewModel::updateAppointmentStatus
            )
            2 -> WaitlistAndReferralsContent(
                entries = state.waitlistEntries,
                onNewEntry = { showNewWaitlistDialog = true }
            )
        }
    }

    if (showPlanSessionDialog) {
        val firstPatient = state.activePatients.firstOrNull()
        var selectedPatientId by remember { mutableLongStateOf(firstPatient?.id ?: 0L) }
        var dateBr by remember { mutableStateOf(state.todayBr) }
        val selectedGames = remember { mutableStateListOf("sound_hunt", "magic_mouth") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPlanSessionDialog = false },
            title = { Text("Montar Sessão Planejada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sessões planejadas ficam salvas com status PLANEJADA e nunca iniciam automaticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MockupTextMuted
                    )
                    if (state.activePatients.isEmpty()) {
                        Text("Cadastre um paciente primeiro para planejar sessões.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Paciente:", fontWeight = FontWeight.Bold)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.activePatients.forEach { p ->
                                FilterChip(
                                    selected = selectedPatientId == p.id,
                                    onClick = { selectedPatientId = p.id },
                                    label = { Text(p.fullName) }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = dateBr,
                        onValueChange = { dateBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Data da sessão (DD/MM/AAAA)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Roteiro e preparação") }
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = selectedPatientId > 0L,
                    onClick = {
                        showPlanSessionDialog = false
                        viewModel.createPlannedSession(selectedPatientId, dateBr, selectedGames.toList(), notes)
                    }
                ) {
                    Text("Salvar Sessão Planejada")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPlanSessionDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showNewAppointmentDialog) {
        var patientName by remember { mutableStateOf(state.activePatients.firstOrNull()?.fullName.orEmpty()) }
        var dateBr by remember { mutableStateOf(state.todayBr) }
        var startTime by remember { mutableStateOf("14:00") }
        var endTime by remember { mutableStateOf("14:50") }
        var serviceType by remember { mutableStateOf("Terapia Fonoaudiológica") }
        var selectedRecurrence by remember { mutableStateOf(AppointmentRecurrence.WEEKLY) }

        AlertDialog(
            onDismissRequest = { showNewAppointmentDialog = false },
            title = { Text("Novo Agendamento Clínico") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = patientName, onValueChange = { patientName = it }, label = { Text("Paciente") }, singleLine = true)
                    OutlinedTextField(
                        value = dateBr,
                        onValueChange = { dateBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Data (DD/MM/AAAA)") },
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Início") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("Término") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = serviceType, onValueChange = { serviceType = it }, label = { Text("Tipo de atendimento") }, singleLine = true)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppointmentRecurrence.entries.forEach { rec ->
                            FilterChip(
                                selected = selectedRecurrence == rec,
                                onClick = { selectedRecurrence = rec },
                                label = { Text(rec.displayName) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewAppointmentDialog = false
                        viewModel.scheduleAppointment(
                            patientId = state.activePatients.firstOrNull { it.fullName == patientName }?.id,
                            patientName = patientName.ifBlank { "Paciente Agendado" },
                            dateBr = dateBr,
                            startTime = startTime,
                            endTime = endTime,
                            serviceType = serviceType,
                            recurrence = selectedRecurrence,
                            notes = ""
                        )
                    }
                ) {
                    Text("Agendar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewAppointmentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showNewWaitlistDialog) {
        var name by remember { mutableStateOf("") }
        var birthBr by remember { mutableStateOf("") }
        var contact by remember { mutableStateOf("") }
        var referredBy by remember { mutableStateOf("Pediatra / Escola") }
        var complaint by remember { mutableStateOf("Atraso de fala e trocas fonêmicas") }
        var urgency by remember { mutableStateOf(ReferralUrgency.HIGH) }

        AlertDialog(
            onDismissRequest = { showNewWaitlistDialog = false },
            title = { Text("Registrar na Fila de Espera / Triagem") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome da criança / paciente") }, singleLine = true)
                    OutlinedTextField(
                        value = birthBr,
                        onValueChange = { birthBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Data de nascimento (DD/MM/AAAA)") },
                        singleLine = true
                    )
                    OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Telefone do responsável") }, singleLine = true)
                    OutlinedTextField(value = referredBy, onValueChange = { referredBy = it }, label = { Text("Origem do encaminhamento") }, singleLine = true)
                    OutlinedTextField(value = complaint, onValueChange = { complaint = it }, label = { Text("Queixa principal") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewWaitlistDialog = false
                        viewModel.addWaitlistEntry(
                            candidateName = name.ifBlank { "Candidato em Triagem" },
                            birthDateBr = birthBr,
                            guardianContact = contact,
                            referredBy = referredBy,
                            complaint = complaint,
                            urgency = urgency
                        )
                    }
                ) {
                    Text("Adicionar à Fila")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewWaitlistDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun SessionsListContent(
    sessions: List<TherapySession>,
    patients: List<Patient>,
    dateTimeManager: DateTimeManager,
    onPlanClick: () -> Unit,
    onSessionClick: (Long) -> Unit
) {
    val patientMap = remember(patients) { patients.associateBy { it.id } }
    Column(modifier = Modifier.fillMaxSize()) {
        FonoPrimaryButton(
            text = "Planejar Sessão com Antecedência",
            onClick = onPlanClick,
            icon = Icons.Default.EventNote,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (sessions.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.History,
                title = "Nenhuma sessão registrada",
                description = "Monte sessões com antecedência ou inicie atendimentos a partir do prontuário."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sessions) { session ->
                    val pName = patientMap[session.patientId]?.fullName ?: "Paciente #${session.patientId}"
                    val dateBr = dateTimeManager.formatDateTimeBr(session.scheduledAt)
                    FonoCard(
                        onClick = { onSessionClick(session.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sessão #${session.id} • $pName",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MockupTextDark
                                    )
                                    FonoStatusBadge(
                                        text = session.status.displayName,
                                        containerColor = when (session.status) {
                                            SessionStatus.PLANNED, SessionStatus.DRAFT -> MockupYellowCard
                                            SessionStatus.IN_PROGRESS -> MockupPurpleHero.copy(alpha = 0.4f)
                                            else -> MockupGreenCard
                                        },
                                        contentColor = MockupDarkPill
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Data: $dateBr • Duração: ${session.durationSeconds / 60} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MockupTextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MockupBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MockupTextMuted, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaContent(
    appointments: List<ClinicalAppointment>,
    dateTimeManager: DateTimeManager,
    onNewAppointment: () -> Unit,
    onUpdateStatus: (Long, AppointmentStatus) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FonoPrimaryButton(
            text = "Novo Agendamento na Agenda",
            onClick = onNewAppointment,
            icon = Icons.Default.CalendarMonth,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (appointments.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.CalendarMonth,
                title = "Agenda livre",
                description = "Registre atendimentos avulsos ou recorrentes com controle de presença e faltas."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appointments) { appt ->
                    FonoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(appt.patientName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupTextDark)
                                FonoStatusBadge(text = appt.status.displayName)
                            }
                            Text(
                                text = "${dateTimeManager.formatDateBr(appt.date)} • ${appt.startTime} às ${appt.endTime} (${appt.recurrence.displayName})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MockupTextMuted
                            )
                            Text(appt.serviceType, style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { onUpdateStatus(appt.id, AppointmentStatus.CONFIRMED) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Confirmar") }
                                OutlinedButton(
                                    onClick = { onUpdateStatus(appt.id, AppointmentStatus.COMPLETED) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Realizado") }
                                OutlinedButton(
                                    onClick = { onUpdateStatus(appt.id, AppointmentStatus.NO_SHOW) },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Falta") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaitlistAndReferralsContent(
    entries: List<WaitlistAndReferralEntry>,
    onNewEntry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FonoPrimaryButton(
            text = "Adicionar Criança à Fila / Triagem",
            onClick = onNewEntry,
            icon = Icons.Default.QueuePlayNext,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (entries.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.QueuePlayNext,
                title = "Nenhum encaminhamento na fila",
                description = "Registre crianças encaminhadas por pediatras ou escolas aguardando vaga clínica."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(entries) { entry ->
                    FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupYellowCard.copy(alpha = 0.5f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(entry.candidateName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MockupDarkPill)
                                FonoStatusBadge(text = entry.urgency.displayName)
                            }
                            Text("Nasc.: ${entry.birthDateText} • Contato: ${entry.guardianContact}", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                            Text("Encaminhamento: ${entry.referredBy}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MockupDarkPill)
                            Text("Queixa: ${entry.mainComplaint}", style = MaterialTheme.typography.bodyMedium, color = MockupTextDark)
                        }
                    }
                }
            }
        }
    }
}
