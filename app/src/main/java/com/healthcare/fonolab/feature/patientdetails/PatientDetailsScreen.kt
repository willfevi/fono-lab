package com.healthcare.fonolab.feature.patientdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoEmptyState
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.component.PastelMetricCard
import com.healthcare.fonolab.core.designsystem.theme.FonoBorder
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryDark
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryLight
import com.healthcare.fonolab.core.designsystem.theme.FonoSecondary
import com.healthcare.fonolab.core.designsystem.theme.FonoSecondaryContainer
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupCyanCard
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPeachCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupSurface
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.domain.goal.ClinicalGoal
import com.healthcare.fonolab.domain.patient.AgeCalculator
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.TherapySession
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    viewModel: PatientDetailsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToStartSession: (Long) -> Unit,
    onNavigateToSessionHub: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is PatientDetailsEvent.PatientDeleted -> onNavigateBack()
                is PatientDetailsEvent.SessionStarted -> onNavigateToSessionHub(event.sessionId)
                is PatientDetailsEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (state.isLoading || state.patient == null) {
        FonoLoadingScreen(message = "Carregando prontuário do paciente...")
        return
    }

    val patient = state.patient!!
    val age = AgeCalculator.ageOn(patient.birthDate, LocalDate.now())

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { androidx.compose.material3.SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Prontuário",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextDark
                    )
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MockupTextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
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
                                contentDescription = if (patient.status == PatientStatus.ACTIVE) "Arquivar" else "Reativar",
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
            // Patient Profile Hero Card (Mockup Profile Screen style)
            FonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Large Monogram Avatar with pastel container & white border
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MockupPurpleHero.copy(alpha = 0.45f))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = patient.fullName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MockupDarkPill
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = patient.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MockupTextDark
                        )
                        if (patient.isDemo) {
                            Spacer(modifier = Modifier.width(8.dp))
                            FonoStatusBadge(text = "DEMO")
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${age.years} anos • Prontuário #${patient.internalCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MockupTextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trio of Pastel Metric Cards (Mint Green, Sky Cyan, Peach)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PastelMetricCard(
                            label = "Acurácia",
                            value = "88%",
                            unit = "média",
                            containerColor = MockupGreenCard,
                            modifier = Modifier.weight(1f)
                        )
                        PastelMetricCard(
                            label = "Metas",
                            value = state.goals.size.toString(),
                            unit = "ativas",
                            containerColor = MockupCyanCard,
                            modifier = Modifier.weight(1f)
                        )
                        PastelMetricCard(
                            label = "Sessões",
                            value = state.sessions.size.toString(),
                            unit = "totais",
                            containerColor = MockupPeachCard,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    FonoPrimaryButton(
                        text = "Iniciar Nova Sessão Clínica",
                        onClick = viewModel::startSession,
                        icon = Icons.Default.PlayArrow,
                        isLoading = state.isStartingSession,
                        enabled = !state.isStartingSession,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Tab Row with Lilac styling
            val tabs = listOf("Visão Geral", "Metas (${state.goals.size})", "Sessões (${state.sessions.size})", "Notas")
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MockupDarkPill,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTab),
                        color = MockupDarkPill,
                        width = 40.dp
                    )
                },
                divider = { Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(MockupBorder)) }
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

            // Tab Content
            when (selectedTab) {
                0 -> PatientOverviewTab(patient = patient)
                1 -> PatientGoalsTab(goals = state.goals)
                2 -> PatientSessionsTab(sessions = state.sessions, onSessionClick = onNavigateToSessionHub)
                3 -> PatientNotesTab(notes = patient.clinicalNotes)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Prontuário?") },
            text = {
                Text("Esta ação é permanente e removerá todas as metas, registros de sessões e tentativas associadas a ${patient.fullName}.\n\nSe o paciente apenas teve alta ou suspensão de terapia, utilize a função 'Arquivar'.")
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
private fun PatientOverviewTab(patient: com.healthcare.fonolab.domain.patient.Patient) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Responsável Legal", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge, color = FonoTextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(patient.guardianName ?: "Não informado", style = MaterialTheme.typography.bodyLarge, color = FonoTextPrimary)
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Contato do Responsável", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge, color = FonoTextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(patient.guardianContact ?: "Não informado", style = MaterialTheme.typography.bodyLarge, color = FonoTextPrimary)
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Início do Acompanhamento", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge, color = FonoTextSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(patient.followUpStartDate.toString(), style = MaterialTheme.typography.bodyLarge, color = FonoTextPrimary)
                }
            }
        }
    }
}

@Composable
private fun PatientGoalsTab(goals: List<ClinicalGoal>) {
    if (goals.isEmpty()) {
        FonoEmptyState(
            icon = Icons.Default.Flag,
            title = "Nenhuma meta cadastrada",
            description = "Defina metas clínicas específicas para monitorar a evolução do paciente ao longo dos minijogos."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(goals) { goal ->
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.clinicalArea.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FonoPrimaryDark
                            )
                            FonoStatusBadge(text = goal.status.name)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(goal.description, style = MaterialTheme.typography.bodyLarge, color = FonoTextPrimary)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Habilidade-alvo: ${goal.targetSkill}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FonoTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientSessionsTab(
    sessions: List<TherapySession>,
    onSessionClick: (Long) -> Unit
) {
    if (sessions.isEmpty()) {
        FonoEmptyState(
            icon = Icons.Default.History,
            title = "Nenhuma sessão realizada",
            description = "Inicie a primeira sessão com este paciente para registrar atividades e acurácia clínica."
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sessions) { session ->
                FonoCard(
                    onClick = { onSessionClick(session.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sessão #${session.id}",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = FonoTextPrimary
                            )
                            FonoStatusBadge(
                                text = session.status.name,
                                containerColor = FonoSecondaryContainer,
                                contentColor = FonoSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Duração registrada: ${session.durationSeconds / 60} min",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FonoTextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientNotesTab(notes: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        item {
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Observações Clínicas Gerais", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium, color = FonoTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (notes.isNotBlank()) notes else "Nenhuma observação clínica registrada até o momento.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (notes.isNotBlank()) FonoTextPrimary else FonoTextSecondary
                    )
                }
            }
        }
    }
}

