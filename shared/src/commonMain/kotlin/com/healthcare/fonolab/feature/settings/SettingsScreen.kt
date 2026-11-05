package com.healthcare.fonolab.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoSecondaryButton
import com.healthcare.fonolab.core.designsystem.theme.*
import com.healthcare.fonolab.domain.clinical.AuditEvent
import com.healthcare.fonolab.domain.clinical.ProfessionalProfile
import com.healthcare.fonolab.domain.manager.AccountManager
import com.healthcare.fonolab.domain.manager.AuditManager
import com.healthcare.fonolab.domain.patient.PatientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val profile: ProfessionalProfile = ProfessionalProfile(),
    val auditLogs: List<AuditEvent> = emptyList()
)

class SettingsViewModel(
    private val patientRepository: PatientRepository,
    private val accountManager: AccountManager,
    auditManager: AuditManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        accountManager.observeProfile(),
        auditManager.observeRecentLogs(15)
    ) { profile, logs ->
        SettingsUiState(profile = profile, auditLogs = logs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun saveProfile(profile: ProfessionalProfile) {
        viewModelScope.launch {
            accountManager.saveProfile(profile)
        }
    }

    fun clearDemoData(onComplete: () -> Unit) {
        viewModelScope.launch {
            patientRepository.deleteDemoPatients()
            onComplete()
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isAudioEffectsEnabled by remember { mutableStateOf(true) }
    var isTtsEnabled by remember { mutableStateOf(true) }
    var showClearDemoDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showLoginPreviewDialog by remember { mutableStateOf(false) }

    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = MockupDarkPill,
        uncheckedThumbColor = MockupTextMuted,
        uncheckedTrackColor = MockupBackground
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Perfil, Conta e Configurações",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Text(
            text = "Identificação CRFa, conta profissional, áudio e auditoria",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )

        // 1. Professional Profile & Optional Login Screen Card
        FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupYellowCard) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = state.profile.fullName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MockupDarkPill
                )
                Text(
                    text = "${state.profile.crfaRegistration} • ${state.profile.clinicName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MockupDarkPill
                )
                Text(
                    text = state.profile.specialty,
                    style = MaterialTheme.typography.bodySmall,
                    color = MockupTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FonoPrimaryButton(
                        text = "Editar Perfil CRFa",
                        onClick = { showEditProfileDialog = true },
                        icon = Icons.Default.Badge,
                        modifier = Modifier.weight(1f).height(42.dp)
                    )
                    FonoSecondaryButton(
                        text = "Tela de Login",
                        onClick = { showLoginPreviewDialog = true },
                        icon = Icons.Default.LockOpen,
                        modifier = Modifier.weight(1f).height(42.dp)
                    )
                }
            }
        }

        // 2. Audio & Accessibility Card
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Preferências de Áudio e Acessibilidade",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MockupTextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sons e Efeitos Lúdicos", style = MaterialTheme.typography.bodyLarge, color = MockupTextDark)
                        Text("Feedback auditivo tátil durante as rodadas", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                    }
                    Switch(
                        checked = isAudioEffectsEnabled,
                        onCheckedChange = { isAudioEffectsEnabled = it },
                        colors = switchColors
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Instruções em Voz (Text-to-Speech pt-BR)", style = MaterialTheme.typography.bodyLarge, color = MockupTextDark)
                        Text("Leitura auditiva com sotaque nativo em português", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                    }
                    Switch(
                        checked = isTtsEnabled,
                        onCheckedChange = { isTtsEnabled = it },
                        colors = switchColors
                    )
                }
            }
        }

        // 3. Recent Audit Trail Card
        if (state.auditLogs.isNotEmpty()) {
            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Trilha de Auditoria Clínica Recente",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MockupTextDark
                    )
                    state.auditLogs.take(5).forEach { ev ->
                        Text(
                            text = "• [${ev.entityType}/${ev.action}] ${ev.description}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupTextMuted
                        )
                    }
                }
            }
        }

        // 4. Privacy & Data Management
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MockupDarkPill, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Privacidade e Conformidade Clínica (Offline)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MockupTextDark
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Persistência local Room SQLite 100% offline. Arquitetura preparada para sincronização multiplataforma futura.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockupTextMuted,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showClearDemoDialog = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MockupBorder)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remover Pacientes de Demonstração", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showEditProfileDialog) {
        var fullName by remember { mutableStateOf(state.profile.fullName) }
        var crfa by remember { mutableStateOf(state.profile.crfaRegistration) }
        var clinic by remember { mutableStateOf(state.profile.clinicName) }
        var specialty by remember { mutableStateOf(state.profile.specialty) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Perfil Profissional e Cabeçalho PDF") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Nome completo do(a) Fonoaudiólogo(a)") })
                    OutlinedTextField(value = crfa, onValueChange = { crfa = it }, label = { Text("Registro CRFa") })
                    OutlinedTextField(value = clinic, onValueChange = { clinic = it }, label = { Text("Nome da Clínica / Consultório") })
                    OutlinedTextField(value = specialty, onValueChange = { specialty = it }, label = { Text("Especialidade") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditProfileDialog = false
                        viewModel.saveProfile(
                            state.profile.copy(
                                fullName = fullName,
                                crfaRegistration = crfa,
                                clinicName = clinic,
                                specialty = specialty
                            )
                        )
                    }
                ) {
                    Text("Salvar Perfil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancelar") }
            }
        )
    }

    // Visual Login Screen Preview (Accessible from Profile/Settings without blocking offline app usage)
    if (showLoginPreviewDialog) {
        var email by remember { mutableStateOf(state.profile.email) }
        var password by remember { mutableStateOf("") }
        var rememberMe by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showLoginPreviewDialog = false },
            title = { Text("Acesso Profissional FonoLab") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Interface de autenticação preparada para sincronização remota futura. O uso local offline permanece liberado sem bloqueio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MockupTextMuted
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail ou usuário profissional") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text("Lembrar acesso neste dispositivo")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLoginPreviewDialog = false }) {
                    Text("Continuar no Modo Local")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLoginPreviewDialog = false }) {
                    Text("Recuperar Senha")
                }
            }
        )
    }

    if (showClearDemoDialog) {
        AlertDialog(
            onDismissRequest = { showClearDemoDialog = false },
            title = { Text("Remover Dados de Demonstração?") },
            text = { Text("Esta ação removerá todos os pacientes e sessões marcados como demonstração.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDemoDialog = false
                        viewModel.clearDemoData {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirmar Exclusão")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearDemoDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
