package com.healthcare.fonolab.feature.anamnesis

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoLoadingScreen
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoSecondaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.theme.*
import com.healthcare.fonolab.core.pdf.AnamnesisPdfExporter
import com.healthcare.fonolab.domain.clinical.*
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.manager.AnamnesisManager
import com.healthcare.fonolab.domain.manager.FormManager
import com.healthcare.fonolab.domain.manager.PatientManager
import com.healthcare.fonolab.domain.patient.Patient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AnamnesisUiState(
    val patient: Patient? = null,
    val record: AnamnesisRecord? = null,
    val anamnesisDateTextBr: String = "",
    val versions: List<AnamnesisVersion> = emptyList(),
    val expandedSectionIndex: Int = 0,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class AnamnesisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val patientManager: PatientManager,
    private val anamnesisManager: AnamnesisManager,
    val formManager: FormManager,
    private val dateTimeManager: DateTimeManager
) : ViewModel() {

    val patientId: Long = savedStateHandle.get<Long>("patientId") ?: 0L

    private val _uiState = MutableStateFlow(AnamnesisUiState())
    val uiState: StateFlow<AnamnesisUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val patient = patientManager.getPatient(patientId)
            combine(
                anamnesisManager.observeAnamnesis(patientId),
                anamnesisManager.observeVersions(patientId)
            ) { existingRecord, versionList ->
                existingRecord to versionList
            }.collect { (existing, versionList) ->
                val currentDraft = _uiState.value.record
                val resolved = existing ?: currentDraft ?: anamnesisManager.createEmptyTemplate(
                    patientId = patientId,
                    defaultInformant = patient?.guardianName.orEmpty()
                )
                _uiState.value = _uiState.value.copy(
                    patient = patient,
                    record = resolved,
                    anamnesisDateTextBr = dateTimeManager.formatDateBr(resolved.anamnesisDate),
                    versions = versionList,
                    isLoading = false
                )
            }
        }
    }

    fun onToggleSection(index: Int) {
        val current = _uiState.value.expandedSectionIndex
        _uiState.value = _uiState.value.copy(expandedSectionIndex = if (current == index) -1 else index)
    }

    fun onInformantNameChange(value: String) {
        val rec = _uiState.value.record ?: return
        _uiState.value = _uiState.value.copy(record = rec.copy(informantName = value))
    }

    fun onInformantKinshipChange(value: String) {
        val rec = _uiState.value.record ?: return
        _uiState.value = _uiState.value.copy(record = rec.copy(informantKinship = value))
    }

    fun onReferredByChange(value: String) {
        val rec = _uiState.value.record ?: return
        _uiState.value = _uiState.value.copy(record = rec.copy(referredBy = value))
    }

    fun onAnamnesisDateBrChange(raw: String) {
        val normalized = dateTimeManager.normalizeBrDateInput(raw)
        val parsed = dateTimeManager.parseDate(normalized)
        val rec = _uiState.value.record ?: return
        _uiState.value = _uiState.value.copy(
            anamnesisDateTextBr = normalized,
            record = if (parsed != null) rec.copy(anamnesisDate = parsed) else rec
        )
    }

    fun onSectionUpdate(
        sectionType: AnamnesisSectionType,
        familyReport: String? = null,
        clinicianObservation: String? = null,
        quickStatus: String? = null
    ) {
        val rec = _uiState.value.record ?: return
        val updatedSections = rec.sections.map { sec ->
            if (sec.sectionType == sectionType) {
                sec.copy(
                    familyReport = familyReport ?: sec.familyReport,
                    clinicianObservation = clinicianObservation ?: sec.clinicianObservation,
                    quickStatus = quickStatus ?: sec.quickStatus
                )
            } else {
                sec
            }
        }
        _uiState.value = _uiState.value.copy(record = rec.copy(sections = updatedSections))
    }

    fun onGeneralSummaryChange(value: String) {
        val rec = _uiState.value.record ?: return
        _uiState.value = _uiState.value.copy(record = rec.copy(generalSummary = value))
    }

    fun save(markCompleted: Boolean) {
        val rec = _uiState.value.record ?: return
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            anamnesisManager.saveAnamnesis(
                record = rec,
                markCompleted = markCompleted,
                changeSummary = if (markCompleted) "Anamnese finalizada (12 seções)" else "Salvamento parcial da anamnese"
            )
            _uiState.value = _uiState.value.copy(
                isSaving = false,
                snackbarMessage = if (markCompleted) "Anamnese concluída e versionada!" else "Rascunho da anamnese salvo com sucesso!"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun notifyMessage(msg: String) {
        _uiState.value = _uiState.value.copy(snackbarMessage = msg)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnamnesisScreen(
    viewModel: AnamnesisViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showVersionsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    if (state.isLoading || state.record == null) {
        FonoLoadingScreen(message = "Carregando anamnese fonoaudiológica...")
        return
    }

    val record = state.record!!
    val patient = state.patient

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MockupBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Anamnese Fonoaudiológica",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextDark
                        )
                        Text(
                            text = "${patient?.fullName ?: "Paciente"} • v${record.versionNumber} (${record.filledSectionsCount}/12 seções)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupTextMuted
                        )
                    }
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MockupTextDark, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                actions = {
                    if (state.versions.isNotEmpty()) {
                        IconButton(onClick = { showVersionsDialog = true }) {
                            Icon(Icons.Default.History, contentDescription = "Histórico de versões", tint = MockupDarkPill)
                        }
                    }
                    IconButton(
                        onClick = {
                            if (patient != null) {
                                val file = AnamnesisPdfExporter.exportAnamnesisToFile(context, patient, record)
                                viewModel.notifyMessage("PDF gerado em: ${file.name} (${file.length() / 1024} KB)")
                                runCatching {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_SUBJECT,
                                            "Anamnese Fonoaudiológica - ${patient.fullName}"
                                        )
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Anamnese Fonoaudiológica de ${patient.fullName} (v${record.versionNumber}) gerada em PDF (${file.absolutePath})."
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Anamnese"))
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Exportar PDF", tint = MockupDarkPill)
                    }
                },
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MockupBackground)
            )
        }
    ) { padding ->
        val fieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MockupDarkPill,
            unfocusedBorderColor = MockupBorder,
            focusedContainerColor = MockupSurface,
            unfocusedContainerColor = MockupSurface
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Progress Card
            item {
                FonoCard(modifier = Modifier.fillMaxWidth(), containerColor = MockupYellowCard) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Progresso: ${record.completionPercentage}% (${record.filledSectionsCount}/12 seções)",
                                fontWeight = FontWeight.Bold,
                                color = MockupDarkPill
                            )
                            FonoStatusBadge(
                                text = record.status.displayName,
                                containerColor = MockupSurface,
                                contentColor = MockupDarkPill
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { record.completionPercentage / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = MockupDarkPill,
                            trackColor = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // General Metadata Card
            item {
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Dados da Entrevista de Anamnese",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextDark
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = record.informantName,
                                onValueChange = viewModel::onInformantNameChange,
                                modifier = Modifier.weight(1.3f),
                                label = { Text("Informante") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            OutlinedTextField(
                                value = record.informantKinship,
                                onValueChange = viewModel::onInformantKinshipChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Parentesco") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = state.anamnesisDateTextBr,
                                onValueChange = viewModel::onAnamnesisDateBrChange,
                                modifier = Modifier.weight(1f),
                                label = { Text("Data (DD/MM/AAAA)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            OutlinedTextField(
                                value = record.referredBy,
                                onValueChange = viewModel::onReferredByChange,
                                modifier = Modifier.weight(1.3f),
                                label = { Text("Encaminhado por") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                        }
                    }
                }
            }

            // 12 Clinical Sections Accordion
            itemsIndexed(record.sections) { idx, sec ->
                val isExpanded = state.expandedSectionIndex == idx
                FonoCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = if (sec.isFilled) MockupGreenCard.copy(alpha = 0.45f) else MockupSurface
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onToggleSection(idx) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sec.sectionType.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MockupTextDark
                                )
                                Text(
                                    text = if (sec.isFilled) "Preenchida ${sec.quickStatus.let { if (it.isNotBlank()) "• $it" else "" }}" else "Toque para preencher",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MockupTextMuted
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MockupDarkPill
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Roteiro clínico: ${sec.sectionType.guidingQuestions}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MockupTextMuted
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Quick Status Chips
                            Row(
                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                viewModel.formManager.quickAnswerOptions.forEach { option ->
                                    val selected = sec.quickStatus == option
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(if (selected) MockupDarkPill else MockupBackground)
                                            .clickable {
                                                viewModel.onSectionUpdate(sec.sectionType, quickStatus = option)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) Color.White else MockupTextDark,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = sec.familyReport,
                                onValueChange = { viewModel.onSectionUpdate(sec.sectionType, familyReport = it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Relato da Família / Informante") },
                                minLines = 3,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = sec.clinicianObservation,
                                onValueChange = { viewModel.onSectionUpdate(sec.sectionType, clinicianObservation = it) },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Observação Clínica do Fonoaudiólogo") },
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors
                            )
                        }
                    }
                }
            }

            // General Summary & Actions
            item {
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Síntese Fonoaudiológica e Conduta Inicial",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextDark
                        )
                        OutlinedTextField(
                            value = record.generalSummary,
                            onValueChange = viewModel::onGeneralSummaryChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Hipótese clínica inicial, encaminhamentos e plano terapêutico sugerido") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = fieldColors
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FonoSecondaryButton(
                        text = "Salvar Parcial",
                        onClick = { viewModel.save(markCompleted = false) },
                        icon = Icons.Default.Save,
                        modifier = Modifier.weight(1f)
                    )
                    FonoPrimaryButton(
                        text = "Concluir Anamnese",
                        onClick = { viewModel.save(markCompleted = true) },
                        icon = Icons.Default.Check,
                        isLoading = state.isSaving,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FonoSecondaryButton(
                    text = "Exportar Anamnese Completa em PDF",
                    onClick = {
                        if (patient != null) {
                            val file = AnamnesisPdfExporter.exportAnamnesisToFile(context, patient, record)
                            viewModel.notifyMessage("PDF gerado com sucesso: ${file.name}")
                        }
                    },
                    icon = Icons.Default.PictureAsPdf,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    if (showVersionsDialog) {
        AlertDialog(
            onDismissRequest = { showVersionsDialog = false },
            title = { Text("Histórico de Versões da Anamnese") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.versions.take(8).forEach { v ->
                        FonoCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Versão v${v.versionNumber}", fontWeight = FontWeight.Bold, color = MockupTextDark)
                                Text(v.changeSummary, style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionsDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}
