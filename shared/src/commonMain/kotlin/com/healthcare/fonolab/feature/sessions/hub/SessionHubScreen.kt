package com.healthcare.fonolab.feature.sessions.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.*
import com.healthcare.fonolab.core.designsystem.theme.*
import com.healthcare.fonolab.domain.session.ActivityStatus
import com.healthcare.fonolab.domain.session.SessionStatus
import com.healthcare.fonolab.feature.games.GameCatalogRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHubScreen(
    viewModel: SessionHubViewModel,
    onNavigateBack: () -> Unit,
    onPlayActivity: (String, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSelectGameDialog by remember { mutableStateOf(false) }
    var showFinishDialog by remember { mutableStateOf(false) }
    var notesText by remember { mutableStateOf("") }
    var soapSubjective by remember { mutableStateOf("Paciente compareceu colaborativo(a) e motivado(a).") }
    var soapPlan by remember { mutableStateOf("Dar continuidade aos alvos trabalhados e treino domiciliar.") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is SessionHubEvent.SessionFinished -> onNavigateBack()
                is SessionHubEvent.LaunchGame -> onPlayActivity(event.gameId, event.activityId)
                is SessionHubEvent.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    if (state.isLoading) {
        FonoLoadingScreen(message = "Carregando sessão clínica...")
        return
    }

    if (state.isNotFound || state.details == null) {
        Column(
            modifier = modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FonoEmptyState(
                title = "Sessão não encontrada",
                description = "Não foi possível localizar o registro desta sessão clínica.",
                actionLabel = "Voltar",
                onActionClick = onNavigateBack
            )
        }
        return
    }

    val session = state.details!!.session
    val activities = state.details!!.activities
    val isPlannedOrPaused = session.status == SessionStatus.PLANNED ||
        session.status == SessionStatus.DRAFT ||
        session.status == SessionStatus.PAUSED

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MockupBackground,
        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.navigationBars),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                windowInsets = TopAppBarDefaults.windowInsets,
                title = {
                    Text(
                        text = "Sessão #${session.id} (${session.status.displayName})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextDark
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MockupTextDark
                        )
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
                .consumeWindowInsets(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FonoCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (session.status == SessionStatus.IN_PROGRESS) MockupGreenCard else MockupYellowCard
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status: ${session.status.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MockupDarkPill
                        )
                        FonoStatusBadge(
                            text = "${activities.size} atividade(s)",
                            containerColor = MockupSurface,
                            contentColor = MockupDarkPill
                        )
                    }
                    if (isPlannedOrPaused) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Esta sessão está planejada/pausada. Toque em 'Iniciar Atendimento' quando o paciente estiver pronto.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupDarkPill
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FonoPrimaryButton(
                            text = if (session.status == SessionStatus.PAUSED) "Retomar Atendimento" else "Iniciar Atendimento Agora",
                            onClick = viewModel::startPlannedOrPausedSession,
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Roteiro de Jogos e Atividades",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MockupTextDark
                )
                if (session.status != SessionStatus.COMPLETED && session.status != SessionStatus.CANCELLED) {
                    FonoSecondaryButton(
                        text = "Adicionar Jogo",
                        onClick = { showSelectGameDialog = true },
                        icon = Icons.Default.Add,
                        modifier = Modifier.height(40.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(activities) { act ->
                    val def = GameCatalogRegistry.getById(act.gameId)
                    FonoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(FonoPrimarySoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = def?.iconEmoji ?: "🎮", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = def?.title ?: act.gameId,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupTextDark
                                )
                                Text(
                                    text = "${def?.clinicalArea?.displayName ?: "Atividade"} • ${act.status.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MockupTextMuted
                                )
                            }
                            if (session.status == SessionStatus.IN_PROGRESS && act.status != ActivityStatus.COMPLETED) {
                                FonoPrimaryButton(
                                    text = "Jogar",
                                    onClick = { onPlayActivity(act.gameId, act.id) },
                                    icon = Icons.Default.PlayArrow,
                                    modifier = Modifier.height(40.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (session.status == SessionStatus.IN_PROGRESS) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FonoSecondaryButton(
                        text = "Pausar",
                        onClick = viewModel::pauseSession,
                        icon = Icons.Default.Pause,
                        modifier = Modifier.weight(1f)
                    )
                    FonoPrimaryButton(
                        text = "Finalizar Sessão + SOAP",
                        onClick = { showFinishDialog = true },
                        icon = Icons.Default.Check,
                        modifier = Modifier.weight(1.4f)
                    )
                }
            }
        }
    }

    if (showSelectGameDialog) {
        val isRunning = session.status == SessionStatus.IN_PROGRESS
        AlertDialog(
            onDismissRequest = { showSelectGameDialog = false },
            title = { Text("Escolher Minijogo Terapêutico (10 Jogos)") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameCatalogRegistry.games.forEach { game ->
                        OutlinedButton(
                            onClick = {
                                showSelectGameDialog = false
                                viewModel.addGameActivity(game.id, launchImmediately = isRunning)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("${game.iconEmoji}  ${game.title} (${game.estimatedDurationMinutes} min)")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showSelectGameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            modifier = Modifier.imePadding(),
            title = { Text("Concluir Sessão & Evolução SOAP") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Registre a síntese clínica e evolução SOAP da sessão:")
                    OutlinedTextField(
                        value = soapSubjective,
                        onValueChange = { soapSubjective = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("S — Subjetivo (Estado / Engajamento)") }
                    )
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("A — Avaliação e Observações Clínicas") },
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = soapPlan,
                        onValueChange = { soapPlan = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("P — Plano (Próxima sessão / Casa)") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishSession(notesText, soapSubjective, soapPlan)
                    }
                ) {
                    Text("Salvar Evolução e Concluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFinishDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
