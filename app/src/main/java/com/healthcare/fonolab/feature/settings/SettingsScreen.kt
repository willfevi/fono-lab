package com.healthcare.fonolab.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.background
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupPurpleHero
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.domain.patient.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    fun clearDemoData(onComplete: () -> Unit) {
        viewModelScope.launch {
            patientRepository.deleteDemoPatients()
            onComplete()
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    var isAudioEffectsEnabled by remember { mutableStateOf(true) }
    var isTtsEnabled by remember { mutableStateOf(true) }
    var showClearDemoDialog by remember { mutableStateOf(false) }

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
            text = "Ajustes e Configurações",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Preferências de execução, áudio e segurança de dados",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Preferences Card
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Preferências de Áudio e Acessibilidade",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MockupTextDark
                )
                Spacer(modifier = Modifier.height(14.dp))
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

        // Privacy and Security Card
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MockupDarkPill, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Privacidade e Conformidade Clínica",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MockupTextDark
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "O FonoLab opera de modo 100% offline. Nenhum dado do paciente, áudio ou métrica é transmitido a redes externas ou nuvem. O aplicativo foi desenhado em conformidade com as boas práticas de sigilo em saúde.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockupTextMuted,
                    lineHeight = 20.sp
                )
            }
        }

        // Data Management Card
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Gerenciamento de Prontuários",
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MockupTextDark
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showClearDemoDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MockupBorder)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remover Pacientes de Demonstração", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // App Info
        FonoCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MockupDarkPill, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("FonoLab v1.0.0", fontWeight = FontWeight.Bold, color = MockupTextDark)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Plataforma Gamificada de Apoio à Terapia Fonoaudiológica", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    if (showClearDemoDialog) {
        AlertDialog(
            onDismissRequest = { showClearDemoDialog = false },
            title = { Text("Remover Dados de Demonstração?") },
            text = { Text("Esta ação removerá todos os pacientes e sessões marcados como demonstração para liberar espaço limpo.") },
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
                OutlinedButton(onClick = { showClearDemoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

