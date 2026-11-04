package com.healthcare.fonolab.feature.sessions.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoSecondaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.theme.FonoBorder
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryDark
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimaryLight
import com.healthcare.fonolab.core.designsystem.theme.FonoPrimarySoft
import com.healthcare.fonolab.core.designsystem.theme.FonoSecondary
import com.healthcare.fonolab.core.designsystem.theme.FonoSecondaryContainer
import com.healthcare.fonolab.core.designsystem.theme.FonoTextPrimary
import com.healthcare.fonolab.core.designsystem.theme.FonoTextSecondary
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

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            when (event) {
                is SessionHubEvent.SessionFinished -> onNavigateBack()
                is SessionHubEvent.LaunchGame -> onPlayActivity(event.gameId, event.activityId)
            }
        }
    }

    if (state.isLoading) {
        FonoLoadingScreen(message = "Carregando sessão clínica...")
        return
    }

    if (state.isNotFound || state.details == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            com.healthcare.fonolab.core.designsystem.component.FonoEmptyState(
                title = "Sessão não encontrada",
                description = "Não foi possível localizar o registro desta sessão clínica ou ela já foi encerrada.",
                actionLabel = "Voltar",
                onActionClick = onNavigateBack
            )
        }
        return
    }

    val session = state.details!!.session
    val activities = state.details!!.activities

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Painel da Sessão #${session.id}",
                        style = MaterialTheme.typography.titleLarge,
                        color = FonoTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = FonoTextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Summary Card
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status da Sessão",
                            style = MaterialTheme.typography.labelLarge,
                            color = FonoTextSecondary
                        )
                        FonoStatusBadge(
                            text = session.status.name,
                            containerColor = if (session.status == SessionStatus.IN_PROGRESS) FonoPrimaryLight else FonoSecondaryContainer,
                            contentColor = if (session.status == SessionStatus.IN_PROGRESS) FonoPrimaryDark else FonoSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Atividades vinculadas: ${activities.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FonoTextPrimary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Atividades e Minijogos",
                    style = MaterialTheme.typography.titleLarge,
                    color = FonoTextPrimary
                )
                if (session.status == SessionStatus.IN_PROGRESS) {
                    FonoSecondaryButton(
                        text = "Adicionar",
                        onClick = { showSelectGameDialog = true },
                        icon = Icons.Default.Add,
                        modifier = Modifier.height(42.dp)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
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
                                    fontWeight = FontWeight.SemiBold,
                                    color = FonoTextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Status: ${act.status.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FonoTextSecondary
                                )
                            }
                            if (session.status == SessionStatus.IN_PROGRESS && act.status != ActivityStatus.COMPLETED) {
                                FonoPrimaryButton(
                                    text = "Jogar",
                                    onClick = { onPlayActivity(act.gameId, act.id) },
                                    icon = Icons.Default.PlayArrow,
                                    modifier = Modifier.height(42.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (session.status == SessionStatus.IN_PROGRESS) {
                FonoPrimaryButton(
                    text = "Finalizar Sessão Clínica",
                    onClick = { showFinishDialog = true },
                    icon = Icons.Default.Check,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showSelectGameDialog) {
        AlertDialog(
            onDismissRequest = { showSelectGameDialog = false },
            title = { Text("Escolher Minijogo Clínico") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameCatalogRegistry.games.forEach { game ->
                        OutlinedButton(
                            onClick = {
                                showSelectGameDialog = false
                                viewModel.addGameActivity(game.id)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("${game.iconEmoji}  ${game.title}")
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
            title = { Text("Concluir Sessão Clínica") },
            text = {
                Column {
                    Text("Deseja registrar observações e devolutivas sobre a sessão?")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Observações Clínicas") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FonoPrimary,
                            unfocusedBorderColor = FonoBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFinishDialog = false
                        viewModel.finishSession(notesText)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FonoSecondary)
                ) {
                    Text("Salvar e Concluir")
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

