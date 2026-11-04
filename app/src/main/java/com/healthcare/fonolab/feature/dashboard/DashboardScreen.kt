package com.healthcare.fonolab.feature.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.healthcare.fonolab.core.designsystem.component.CalendarWeekStrip
import com.healthcare.fonolab.core.designsystem.component.FonoEmptyState
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoSecondaryButton
import com.healthcare.fonolab.core.designsystem.component.MockupHeader
import com.healthcare.fonolab.core.designsystem.component.MockupHeroChallengeCard
import com.healthcare.fonolab.core.designsystem.component.MockupListItem
import com.healthcare.fonolab.core.designsystem.component.PastelActivityCard
import com.healthcare.fonolab.core.designsystem.component.PastelMetricCard
import com.healthcare.fonolab.core.designsystem.theme.FonoErrorContainer
import com.healthcare.fonolab.core.designsystem.theme.FonoOnErrorContainer
import com.healthcare.fonolab.core.designsystem.theme.MockupBlueCard
import com.healthcare.fonolab.core.designsystem.theme.MockupCyanCard
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPeachCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPinkCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.core.designsystem.theme.MockupYellowCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToNewPatient: () -> Unit,
    onNavigateToNewSession: () -> Unit,
    onNavigateToGameCatalog: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToPatientDetail: (Long) -> Unit,
    onNavigateToSessionHub: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    if (state.isLoading) {
        FonoLoadingScreen(message = "Preparando painel clínico...")
        return
    }

    val ptBr = Locale.forLanguageTag("pt-BR")
    val formattedDate = remember {
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val month = today.format(DateTimeFormatter.ofPattern("MMM", ptBr)).replace(".", "")
        "Hoje, $day de $month"
    }

    val participantInitials = state.recentPatients
        .take(3)
        .map { it.fullName.take(1).uppercase() }
        .ifEmpty { listOf("L", "M", "G") }

    val extraCount = (state.totalActivePatients - 3).coerceAtLeast(0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Mockup Top Bar Header
        item {
            MockupHeader(
                title = "Olá, Fonoaudiólogo(a)",
                subtitle = formattedDate,
                avatarInitial = "F",
                actionIcon = Icons.Default.Search,
                onAvatarClick = onNavigateToReports,
                onActionClick = onNavigateToNewPatient
            )
        }

        // 2. Active Interrupted Session Alert Banner (if any)
        state.activeSession?.let { active ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = FonoErrorContainer),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sessão em Andamento",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FonoOnErrorContainer
                            )
                            Text(
                                text = "Há uma sessão aberta aguardando retomada.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FonoOnErrorContainer.copy(alpha = 0.85f)
                            )
                        }
                        FonoPrimaryButton(
                            text = "Retomar",
                            onClick = { onNavigateToSessionHub(active.session.id) },
                            modifier = Modifier.height(42.dp)
                        )
                    }
                }
            }
        }

        // 3. Featured Hero Card ("Desafio do Dia / Meta Clínica Diária")
        item {
            MockupHeroChallengeCard(
                tag = "META CLÍNICA DIÁRIA",
                title = "Realize os planos fonoaudiológicos",
                subtitle = "Estimulação articulatória, voz e linguagem hoje",
                participantInitials = participantInitials,
                extraCount = extraCount,
                onClick = onNavigateToNewSession
            )
        }

        // 4. Calendar Week Strip (Interactive 7-day strip)
        item {
            CalendarWeekStrip(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )
        }

        // 5. Section Header: "Seu plano terapêutico"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Seu plano terapêutico",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MockupTextDark
                )
                Text(
                    text = "Ver catálogo",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MockupTextMuted,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                )
            }
        }

        // 6. Multiple Pastel Activity Cards in "Seu plano"
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Card 1: Warm Yellow Pastel Card (Primary Clinical Activity)
                PastelActivityCard(
                    tag = "ARTICULAÇÃO • INTENSIVO",
                    title = "Treino Articulatório de Fonemas Oclusivos",
                    details = "14:00 • Sala 02 • 20 min",
                    participantName = state.recentPatients.firstOrNull()?.fullName ?: "Lucas Silva",
                    participantInitial = state.recentPatients.firstOrNull()?.fullName?.take(1)?.uppercase() ?: "L",
                    containerColor = MockupYellowCard,
                    onClick = onNavigateToNewSession
                )

                // Card 2: Sky Blue Pastel Card (Complementary Audio Discrimination)
                PastelActivityCard(
                    tag = "AUDITIVO • LEVE",
                    title = "Caça aos Sons: Discriminação Auditiva",
                    details = "15:30 • Sala 01 • 15 min",
                    participantName = state.recentPatients.getOrNull(1)?.fullName ?: "Mariana Costa",
                    participantInitial = state.recentPatients.getOrNull(1)?.fullName?.take(1)?.uppercase() ?: "M",
                    containerColor = MockupBlueCard,
                    onClick = onNavigateToGameCatalog
                )

                // Card 3: Pink Pastel Card (Gamified Phonological Awareness)
                PastelActivityCard(
                    tag = "GAMIFICADO • LÚDICO",
                    title = "Monstro Guloso: Consciência Fonológica",
                    details = "16:15 • Sala Lúdica • 15 min",
                    participantName = state.recentPatients.getOrNull(2)?.fullName ?: "Gabriel Santos",
                    participantInitial = state.recentPatients.getOrNull(2)?.fullName?.take(1)?.uppercase() ?: "G",
                    containerColor = MockupPinkCard,
                    onClick = onNavigateToGameCatalog
                )

                // Card 4: Mint Green Pastel Card (Syllables & Vocabulary)
                PastelActivityCard(
                    tag = "LINGUAGEM • EXPRESSÃO",
                    title = "Fábrica de Palavras: Segmentação Silábica",
                    details = "17:00 • Sala 03 • 25 min",
                    participantName = "Sofia Lima",
                    participantInitial = "S",
                    containerColor = MockupGreenCard,
                    onClick = onNavigateToGameCatalog
                )
            }
        }

        // 7. Trio Pastel Metrics Row (Mint Green, Sky Cyan, Peach)
        item {
            Text(
                text = "Indicadores da clínica",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PastelMetricCard(
                    label = "Acurácia",
                    value = "88%",
                    unit = "média",
                    containerColor = MockupGreenCard,
                    modifier = Modifier.weight(1f)
                )
                PastelMetricCard(
                    label = "Planejadas",
                    value = state.plannedSessionsCount.toString(),
                    unit = "metas",
                    containerColor = MockupCyanCard,
                    modifier = Modifier.weight(1f)
                )
                PastelMetricCard(
                    label = "Concluídas",
                    value = state.completedSessionsCount.toString(),
                    unit = "sessões",
                    containerColor = MockupPeachCard,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 8. Action Shortcuts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FonoPrimaryButton(
                    text = "Iniciar Sessão",
                    onClick = onNavigateToNewSession,
                    icon = Icons.Default.PlayArrow,
                    modifier = Modifier.weight(1f)
                )
                FonoSecondaryButton(
                    text = "Novo Paciente",
                    onClick = onNavigateToNewPatient,
                    icon = Icons.Default.Add,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 9. Recent Patients Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pacientes Recentes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MockupTextDark
                )
                Text(
                    text = "${state.totalActivePatients} ativos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockupTextMuted
                )
            }
        }

        if (state.recentPatients.isEmpty()) {
            item {
                FonoEmptyState(
                    title = "Nenhum paciente cadastrado",
                    description = "Cadastre o primeiro paciente para iniciar planos terapêuticos e acompanhar evolução.",
                    actionLabel = "Cadastrar Primeiro Paciente",
                    onActionClick = onNavigateToNewPatient
                )
            }
        } else {
            itemsIndexed(state.recentPatients) { index, patient ->
                MockupListItem(
                    title = patient.fullName,
                    subtitle = "Prontuário: ${patient.internalCode}",
                    leadingText = patient.fullName.take(1).uppercase(),
                    leadingBgColor = when (index % 3) {
                        0 -> MockupYellowCard.copy(alpha = 0.5f)
                        1 -> MockupBlueCard.copy(alpha = 0.5f)
                        else -> MockupPurpleHero.copy(alpha = 0.5f)
                    },
                    leadingTextColor = MockupDarkPill,
                    badgeText = if (patient.isDemo) "DEMO" else null,
                    onClick = { onNavigateToPatientDetail(patient.id) }
                )
            }
        }
    }
}

