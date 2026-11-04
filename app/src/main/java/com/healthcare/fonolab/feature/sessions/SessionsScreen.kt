package com.healthcare.fonolab.feature.sessions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoEmptyState
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.domain.session.SessionStatus
import com.healthcare.fonolab.domain.session.TherapySession
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionsViewModel @Inject constructor(
    sessionRepository: TherapySessionRepository
) : ViewModel() {
    val sessions: StateFlow<List<TherapySession>> = sessionRepository.observeRecentSessions(50).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}

@Composable
fun SessionsScreen(
    viewModel: SessionsViewModel = hiltViewModel(),
    onSessionClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val list by viewModel.sessions.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Histórico de Sessões",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Registro cronológico de intervenções e atendimentos",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (list.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.History,
                title = "Nenhuma sessão registrada",
                description = "As sessões realizadas e planejadas serão catalogadas aqui com métricas de evolução."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(list) { session ->
                    FonoCard(
                        onClick = { onSessionClick(session.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sessão #${session.id}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MockupTextDark
                                    )
                                    FonoStatusBadge(
                                        text = session.status.name,
                                        containerColor = if (session.status == SessionStatus.IN_PROGRESS) MockupPurpleHero.copy(alpha = 0.35f) else MockupGreenCard.copy(alpha = 0.5f),
                                        contentColor = MockupDarkPill
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Duração registrada: ${session.durationSeconds / 60} minutos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MockupTextMuted
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(start = 12.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MockupBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = MockupTextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

