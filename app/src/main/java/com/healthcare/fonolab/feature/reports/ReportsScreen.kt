package com.healthcare.fonolab.feature.reports

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.background
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoEmptyState
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.core.pdf.ReportPdfExporter
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    patientRepository: PatientRepository,
    private val sessionRepository: TherapySessionRepository
) : ViewModel() {

    val patients: StateFlow<List<Patient>> = patientRepository.observePatients(
        PatientFilter(status = PatientStatus.ACTIVE)
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun exportPdf(context: Context, patient: Patient, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val attempts = sessionRepository.getAttemptsForPatient(patient.id)
            val file = File(context.getExternalFilesDir(null), "Relatorio_${patient.internalCode}.pdf")
            FileOutputStream(file).use { out ->
                ReportPdfExporter.generateReport(context, patient, emptyList(), attempts, out)
            }
            onComplete(file.absolutePath)
        }
    }
}

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Relatórios de Evolução",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Emissão local em PDF para acompanhamento clínico offline",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (patients.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.Assessment,
                title = "Nenhum paciente cadastrado",
                description = "Cadastre pacientes e realize sessões estruturadas para emitir relatórios de acurácia clínica."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(patients) { patient ->
                    FonoCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = patient.fullName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MockupTextDark
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Prontuário: ${patient.internalCode}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MockupTextMuted
                                )
                            }
                            FonoPrimaryButton(
                                text = "Exportar PDF",
                                onClick = {
                                    viewModel.exportPdf(context, patient) { path ->
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Relatório PDF emitido com sucesso em: $path")
                                        }
                                    }
                                },
                                icon = Icons.Default.PictureAsPdf,
                                modifier = Modifier.height(44.dp)
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

