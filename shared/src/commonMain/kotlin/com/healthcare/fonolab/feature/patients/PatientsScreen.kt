package com.healthcare.fonolab.feature.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoEmptyState
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupSurface
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.domain.patient.AgeCalculator
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientStatus
import java.time.LocalDate

@Composable
fun PatientsScreen(
    viewModel: PatientsViewModel,
    onNavigateToNewPatient: () -> Unit,
    onNavigateToPatientDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MockupBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewPatient,
                containerColor = MockupDarkPill,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Paciente")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Pacientes",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Gerenciamento de prontuários e planos de terapia",
                style = MaterialTheme.typography.bodyMedium,
                color = MockupTextMuted
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar with clean rounded pill shape
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nome do paciente...", color = MockupTextMuted) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MockupTextMuted
                    )
                },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca", tint = MockupTextDark)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MockupDarkPill,
                    unfocusedBorderColor = MockupBorder,
                    focusedContainerColor = MockupSurface,
                    unfocusedContainerColor = MockupSurface
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter Tabs (Ativos / Arquivados / Todos)
            val tabs = listOf(
                "Ativos" to PatientStatus.ACTIVE,
                "Arquivados" to PatientStatus.ARCHIVED,
                "Todos" to null
            )
            val selectedTabIndex = tabs.indexOfFirst { it.second == state.statusFilter }.coerceAtLeast(0)

            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = MockupDarkPill,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(selectedTabIndex),
                        color = MockupDarkPill,
                        width = 44.dp
                    )
                },
                divider = { Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(MockupBorder)) }
            ) {
                tabs.forEachIndexed { index, pair ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.onStatusFilterChanged(pair.second) },
                        text = {
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedTabIndex == index) MockupDarkPill else MockupTextMuted,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                FonoLoadingScreen(message = "Carregando pacientes...")
            } else if (state.patients.isEmpty()) {
                FonoEmptyState(
                    title = "Nenhum paciente encontrado",
                    description = if (state.query.isNotEmpty()) "Nenhum resultado corresponde aos termos da pesquisa." else "Cadastre novos pacientes para gerenciar sessões e acompanhar o progresso.",
                    actionLabel = if (state.query.isEmpty()) "Cadastrar Novo Paciente" else null,
                    onActionClick = onNavigateToNewPatient
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.patients) { patient ->
                        PatientListItem(
                            patient = patient,
                            onClick = { onNavigateToPatientDetail(patient.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientListItem(
    patient: Patient,
    onClick: () -> Unit
) {
    val age = AgeCalculator.ageOn(patient.birthDate, LocalDate.now())

    FonoCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
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
                    .background(
                        if (patient.status == PatientStatus.ACTIVE) MockupPurpleHero.copy(alpha = 0.35f)
                        else MockupBorder
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = patient.fullName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MockupDarkPill
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = patient.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MockupTextDark
                    )
                    if (patient.isDemo) {
                        Spacer(modifier = Modifier.width(8.dp))
                        FonoStatusBadge(text = "DEMO")
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${age.years} anos, ${age.months} meses • Código: ${patient.internalCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockupTextMuted
                )
            }
            Box(
                modifier = Modifier
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

