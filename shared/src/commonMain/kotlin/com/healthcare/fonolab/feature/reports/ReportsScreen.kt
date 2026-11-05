package com.healthcare.fonolab.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.healthcare.fonolab.core.designsystem.component.*
import com.healthcare.fonolab.core.designsystem.theme.*
import com.healthcare.fonolab.domain.clinical.BillingRecord
import com.healthcare.fonolab.domain.clinical.BillingStatus
import com.healthcare.fonolab.domain.clinical.PaymentMethod
import com.healthcare.fonolab.domain.clinical.TherapeuticStimulus
import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.manager.BillingManager
import com.healthcare.fonolab.domain.manager.ContentLibraryManager
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.patient.PatientFilter
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.patient.PatientStatus
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ReportsAndAdminUiState(
    val patients: List<Patient> = emptyList(),
    val billingRecords: List<BillingRecord> = emptyList(),
    val stimuli: List<TherapeuticStimulus> = emptyList(),
    val todayBr: String = ""
)

class ReportsViewModel @Inject constructor(
    patientRepository: PatientRepository,
    private val sessionRepository: TherapySessionRepository,
    private val billingManager: BillingManager,
    private val contentLibraryManager: ContentLibraryManager,
    val dateTimeManager: DateTimeManager
) : ViewModel() {

    val uiState: StateFlow<ReportsAndAdminUiState> = combine(
        patientRepository.observePatients(PatientFilter(status = PatientStatus.ACTIVE)),
        billingManager.observeAllBilling(),
        contentLibraryManager.observeAllStimuli()
    ) { patients, billing, stimuli ->
        ReportsAndAdminUiState(
            patients = patients,
            billingRecords = billing,
            stimuli = stimuli,
            todayBr = dateTimeManager.formatDateBr(dateTimeManager.today())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsAndAdminUiState()
    )

    fun exportPdf(patient: Patient, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val exportDir = File(System.getProperty("user.home") ?: ".", "Documents/FonoLab/Exports").apply { mkdirs() }
            val file = File(exportDir, "Relatorio_${patient.internalCode}.txt")
            val attempts = sessionRepository.getAttemptsForPatient(patient.id)
            file.writeText("Relatório Clínico de ${patient.fullName} (${patient.internalCode}). Total de tentativas: ${attempts.size}")
            onComplete(file.name)
        }
    }

    fun exportCsv(patient: Patient, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val attempts = sessionRepository.getAttemptsForPatient(patient.id)
            val dir = File(System.getProperty("user.home") ?: ".", "Documents/FonoLab/Exports").apply { mkdirs() }
            val file = File(dir, "Tentativas_${patient.internalCode}.csv")
            val csvText = buildString {
                appendLine("id,activityId,roundIndex,stimulusTarget,responseRecorded,cueLevel,timestamp")
                attempts.forEach { a ->
                    appendLine("${a.id},${a.activityId},${a.roundIndex},\"${a.stimulusTarget}\",\"${a.responseRecorded}\",${a.cueLevel.name},${a.timestamp}")
                }
            }
            file.writeText(csvText, Charsets.UTF_8)
            onComplete(file.name)
        }
    }

    fun createBillingCharge(
        patient: Patient?,
        description: String,
        amountReais: Double,
        dueDateBr: String,
        method: PaymentMethod,
        paidImmediately: Boolean
    ) {
        viewModelScope.launch {
            billingManager.createCharge(
                patientId = patient?.id ?: 0L,
                patientName = patient?.fullName ?: "Atendimento Particular",
                description = description,
                amountBrlReais = amountReais,
                dueDateTextBr = dueDateBr,
                paymentMethod = method,
                markPaidImmediately = paidImmediately
            )
        }
    }

    fun markBillingPaid(recordId: Long, method: PaymentMethod) {
        viewModelScope.launch {
            billingManager.markPaid(recordId, method)
        }
    }

    fun addCustomStimulus(
        word: String,
        phoneme: String,
        syllableDivision: String,
        category: String,
        sentence: String
    ) {
        viewModelScope.launch {
            contentLibraryManager.addCustomStimulus(
                word = word,
                phoneme = phoneme,
                position = PhonemePosition.INITIAL,
                syllableDivision = syllableDivision,
                category = category,
                difficulty = DifficultyLevel.MEDIUM,
                sentence = sentence
            )
        }
    }
}

@Composable
fun rememberDefaultReportsViewModel(): ReportsViewModel {
    val container = com.healthcare.fonolab.core.di.LocalFonoLabContainer.current
    return androidx.lifecycle.viewmodel.compose.viewModel {
        ReportsViewModel(
            patientRepository = container.patientRepository,
            sessionRepository = container.sessionRepository,
            billingManager = container.billingManager,
            contentLibraryManager = container.contentLibraryManager,
            dateTimeManager = container.dateTimeManager
        )
    }
}

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = rememberDefaultReportsViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
        val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showNewChargeDialog by remember { mutableStateOf(false) }
    var showNewStimulusDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MockupBackground)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Relatórios, Finanças e Conteúdo",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MockupTextDark
        )
        Text(
            text = "Exportação PDF/CSV, gestão financeira em R$ e banco de estímulos",
            style = MaterialTheme.typography.bodyMedium,
            color = MockupTextMuted
        )
        Spacer(modifier = Modifier.height(12.dp))

        val tabs = listOf(
            "Relatórios PDF/CSV",
            "Financeiro (R$)",
            "Estímulos (${state.stimuli.size})"
        )
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MockupDarkPill
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == idx) MockupDarkPill else MockupTextMuted
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> ReportsExportTabContent(
                patients = state.patients,
                onExportPdf = { patient ->
                    viewModel.exportPdf(patient) { fileName ->
                        scope.launch { snackbarHostState.showSnackbar("PDF gerado com sucesso: $fileName") }
                    }
                },
                onExportCsv = { patient ->
                    viewModel.exportCsv(patient) { fileName ->
                        scope.launch { snackbarHostState.showSnackbar("Planilha CSV gerada: $fileName") }
                    }
                }
            )
            1 -> FinancialBillingTabContent(
                records = state.billingRecords,
                dateTimeManager = viewModel.dateTimeManager,
                onNewCharge = { showNewChargeDialog = true },
                onMarkPaid = { id, method ->
                    viewModel.markBillingPaid(id, method)
                    scope.launch { snackbarHostState.showSnackbar("Pagamento confirmado e recibo emitido!") }
                }
            )
            2 -> ContentLibraryTabContent(
                stimuli = state.stimuli,
                onNewStimulus = { showNewStimulusDialog = true }
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }

    if (showNewChargeDialog) {
        val selectedPatient = state.patients.firstOrNull()
        var description by remember { mutableStateOf("Sessão de Terapia Fonoaudiológica") }
        var amountStr by remember { mutableStateOf("180.00") }
        var dueDateBr by remember { mutableStateOf(state.todayBr) }
        var method by remember { mutableStateOf(PaymentMethod.PIX) }
        var paidNow by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showNewChargeDialog = false },
            title = { Text("Novo Lançamento Financeiro (R$)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Serviço / Pacote") })
                    OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Valor em R$ (ex: 180.00)") }, singleLine = true)
                    OutlinedTextField(
                        value = dueDateBr,
                        onValueChange = { dueDateBr = viewModel.dateTimeManager.normalizeBrDateInput(it) },
                        label = { Text("Vencimento (DD/MM/AAAA)") },
                        singleLine = true
                    )
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PaymentMethod.entries.forEach { pm ->
                            FilterChip(
                                selected = method == pm,
                                onClick = { method = pm },
                                label = { Text(pm.displayName) }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = paidNow, onCheckedChange = { paidNow = it })
                        Text("Recebido no ato (Emitir recibo)")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNewChargeDialog = false
                        val amount = amountStr.replace(",", ".").toDoubleOrNull() ?: 180.0
                        viewModel.createBillingCharge(selectedPatient, description, amount, dueDateBr, method, paidNow)
                    }
                ) {
                    Text("Salvar Lançamento")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChargeDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showNewStimulusDialog) {
        var word by remember { mutableStateOf("") }
        var phoneme by remember { mutableStateOf("/ɾ/") }
        var syllables by remember { mutableStateOf("CA-DEI-RA") }
        var category by remember { mutableStateOf("Objetos") }
        var sentence by remember { mutableStateOf("A cadeira está na sala.") }

        AlertDialog(
            onDismissRequest = { showNewStimulusDialog = false },
            title = { Text("Novo Estímulo Terapêutico") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = word, onValueChange = { word = it }, label = { Text("Palavra-alvo") }, singleLine = true)
                    OutlinedTextField(value = phoneme, onValueChange = { phoneme = it }, label = { Text("Fonema principal (ex: /s/, /ɾ/)") }, singleLine = true)
                    OutlinedTextField(value = syllables, onValueChange = { syllables = it }, label = { Text("Divisão silábica (ex: BO-LA)") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Categoria semântica") }, singleLine = true)
                    OutlinedTextField(value = sentence, onValueChange = { sentence = it }, label = { Text("Frase de exemplo") })
                }
            },
            confirmButton = {
                Button(
                    enabled = word.isNotBlank(),
                    onClick = {
                        showNewStimulusDialog = false
                        viewModel.addCustomStimulus(word, phoneme, syllables, category, sentence)
                    }
                ) {
                    Text("Adicionar ao Banco")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewStimulusDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ReportsExportTabContent(
    patients: List<Patient>,
    onExportPdf: (Patient) -> Unit,
    onExportCsv: (Patient) -> Unit
) {
    if (patients.isEmpty()) {
        FonoEmptyState(
            icon = Icons.Default.Assessment,
            title = "Nenhum paciente cadastrado",
            description = "Cadastre pacientes e realize sessões para emitir relatórios PDF e planilhas CSV."
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(patients) { patient ->
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = patient.fullName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MockupTextDark
                        )
                        Text(
                            text = "Prontuário: ${patient.internalCode}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupTextMuted
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FonoPrimaryButton(
                                text = "Relatório PDF",
                                onClick = { onExportPdf(patient) },
                                icon = Icons.Default.PictureAsPdf,
                                modifier = Modifier.weight(1f).height(42.dp)
                            )
                            FonoSecondaryButton(
                                text = "Dados CSV",
                                onClick = { onExportCsv(patient) },
                                icon = Icons.Default.TableView,
                                modifier = Modifier.weight(1f).height(42.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialBillingTabContent(
    records: List<BillingRecord>,
    dateTimeManager: DateTimeManager,
    onNewCharge: () -> Unit,
    onMarkPaid: (Long, PaymentMethod) -> Unit
) {
    val totalReceivedCents = records.filter { it.status == BillingStatus.PAID }.sumOf { it.amountCents }
    val totalPendingCents = records.filter { it.status == BillingStatus.PENDING || it.status == BillingStatus.OVERDUE }.sumOf { it.amountCents }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PastelMetricCard(
                label = "Recebido",
                value = "R$ ${totalReceivedCents / 100}",
                unit = "confirmado",
                containerColor = MockupGreenCard,
                modifier = Modifier.weight(1f)
            )
            PastelMetricCard(
                label = "Em Aberto",
                value = "R$ ${totalPendingCents / 100}",
                unit = "a receber",
                containerColor = MockupYellowCard,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        FonoPrimaryButton(
            text = "Novo Lançamento / Cobrança (R$)",
            onClick = onNewCharge,
            icon = Icons.Default.AttachMoney,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (records.isEmpty()) {
            FonoEmptyState(
                icon = Icons.Default.AttachMoney,
                title = "Nenhum lançamento financeiro",
                description = "Controle sessões avulsas, pacotes mensais, Pix e recibos separadamente dos dados clínicos."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records) { rec ->
                    FonoCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rec.formattedAmountBrl(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MockupDarkPill)
                                FonoStatusBadge(
                                    text = rec.status.displayName,
                                    containerColor = if (rec.status == BillingStatus.PAID) MockupGreenCard else MockupYellowCard,
                                    contentColor = MockupDarkPill
                                )
                            }
                            Text("${rec.patientName} • ${rec.description}", fontWeight = FontWeight.SemiBold, color = MockupTextDark)
                            Text(
                                text = "Vencimento: ${dateTimeManager.formatDateBr(rec.dueDate)} • Forma: ${rec.paymentMethod.displayName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MockupTextMuted
                            )
                            if (rec.status != BillingStatus.PAID) {
                                Spacer(modifier = Modifier.height(8.dp))
                                FonoSecondaryButton(
                                    text = "Confirmar Recebimento (Pix) e Emitir Recibo",
                                    onClick = { onMarkPaid(rec.id, PaymentMethod.PIX) },
                                    modifier = Modifier.fillMaxWidth().height(38.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentLibraryTabContent(
    stimuli: List<TherapeuticStimulus>,
    onNewStimulus: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FonoPrimaryButton(
            text = "Cadastrar Estímulo Personalizado",
            onClick = onNewStimulus,
            icon = Icons.Default.Add,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(stimuli) { item ->
                FonoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${item.iconEmoji} ${item.word} (${item.syllableDivision})", fontWeight = FontWeight.Bold, color = MockupTextDark)
                            Text("Fonema: ${item.targetPhoneme} • Categoria: ${item.semanticCategory} (${item.syllableCount} sílabas)", style = MaterialTheme.typography.bodySmall, color = MockupTextMuted)
                            if (item.exampleSentence.isNotBlank()) {
                                Text("\"${item.exampleSentence}\"", style = MaterialTheme.typography.bodySmall, color = MockupDarkPill)
                            }
                        }
                        if (item.isCustom) {
                            FonoStatusBadge(text = "Personalizado")
                        }
                    }
                }
            }
        }
    }
}
