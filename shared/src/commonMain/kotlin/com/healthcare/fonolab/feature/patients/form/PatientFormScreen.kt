package com.healthcare.fonolab.feature.patients.form

import com.healthcare.fonolab.domain.datetime.dateFormatter
import com.healthcare.fonolab.domain.datetime.utcDateAtEpochMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthcare.fonolab.core.designsystem.component.FonoCard
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoSecondaryButton
import com.healthcare.fonolab.core.designsystem.component.FonoStatusBadge
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupGreenCard
import com.healthcare.fonolab.core.designsystem.theme.MockupSurface
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.domain.patient.PatientValidationError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormScreen(
    viewModel: PatientFormViewModel,
    onNavigateBack: () -> Unit,
    onPatientSaved: (Long) -> Unit,
    onPatientSavedOpenAnamnesis: (Long) -> Unit = onPatientSaved,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showBirthDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            if (event is PatientFormEvent.Saved) {
                if (event.openAnamnesisImmediately) {
                    onPatientSavedOpenAnamnesis(event.patientId)
                } else {
                    onPatientSaved(event.patientId)
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MockupBackground,
        contentWindowInsets = WindowInsets.statusBars.union(WindowInsets.navigationBars),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Editar Prontuário" else "Novo Paciente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MockupTextDark
                    )
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
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MockupTextDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets,
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MockupBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MockupDarkPill,
                unfocusedBorderColor = MockupBorder,
                focusedContainerColor = MockupSurface,
                unfocusedContainerColor = MockupSurface
            )

            // SECTION 1: IDENTIFICAÇÃO E DATA DE NASCIMENTO (DD/MM/AAAA)
            Text(
                text = "1. Identificação do Paciente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )

            OutlinedTextField(
                value = state.draft.fullName,
                onValueChange = viewModel::onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome completo *") },
                isError = state.errors.any {
                    it == PatientValidationError.FULL_NAME_REQUIRED ||
                        it == PatientValidationError.FULL_NAME_TOO_SHORT ||
                        it == PatientValidationError.FULL_NAME_TOO_LONG
                },
                supportingText = {
                    when {
                        state.errors.contains(PatientValidationError.FULL_NAME_REQUIRED) ->
                            Text("O nome completo é obrigatório", color = MaterialTheme.colorScheme.error)
                        state.errors.contains(PatientValidationError.FULL_NAME_TOO_SHORT) ->
                            Text("Informe pelo menos 3 caracteres", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.socialName,
                onValueChange = viewModel::onSocialNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome social ou apelido de preferência (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            // Birth Date Field in Brazilian DD/MM/AAAA format + Calendar Picker + Live Age Calculation
            val hasBirthError = state.errors.any {
                it == PatientValidationError.BIRTH_DATE_REQUIRED ||
                    it == PatientValidationError.BIRTH_DATE_INVALID_FORMAT ||
                    it == PatientValidationError.BIRTH_DATE_IN_FUTURE ||
                    it == PatientValidationError.BIRTH_DATE_TOO_OLD
            } || state.birthDateHelperError != null

            OutlinedTextField(
                value = state.draft.birthDateText,
                onValueChange = viewModel::onBirthDateTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Data de nascimento (DD/MM/AAAA) *") },
                placeholder = { Text("Ex: 15/05/2020") },
                trailingIcon = {
                    IconButton(onClick = { showBirthDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Selecionar no calendário",
                            tint = MockupDarkPill
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = hasBirthError,
                supportingText = {
                    when {
                        state.birthDateHelperError != null ->
                            Text(state.birthDateHelperError!!, color = MaterialTheme.colorScheme.error)
                        state.errors.contains(PatientValidationError.BIRTH_DATE_REQUIRED) ->
                            Text("A data de nascimento é obrigatória (DD/MM/AAAA)", color = MaterialTheme.colorScheme.error)
                        state.errors.contains(PatientValidationError.BIRTH_DATE_INVALID_FORMAT) ->
                            Text("Data inválida. Use o formato brasileiro DD/MM/AAAA", color = MaterialTheme.colorScheme.error)
                        state.errors.contains(PatientValidationError.BIRTH_DATE_IN_FUTURE) ->
                            Text("A data de nascimento não pode ser futura", color = MaterialTheme.colorScheme.error)
                        state.errors.contains(PatientValidationError.BIRTH_DATE_TOO_OLD) ->
                            Text("Data de nascimento fora do limite clínico", color = MaterialTheme.colorScheme.error)
                        state.calculatedAgeText != null ->
                            Text("Idade calculada automaticamente: ${state.calculatedAgeText}", color = MockupDarkPill, fontWeight = FontWeight.SemiBold)
                        else ->
                            Text("Digite apenas números (ex: 15052020) ou DD/MM/AAAA", color = MockupTextMuted)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            if (state.calculatedAgeText != null) {
                FonoStatusBadge(
                    text = "Idade atual: ${state.calculatedAgeText}",
                    containerColor = MockupGreenCard,
                    contentColor = MockupDarkPill
                )
            }

            // SECTION 2: RESPONSÁVEL, PARENTESCO E CONTATOS
            Text(
                text = "2. Responsável, Emergência e Endereço",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.draft.guardianName,
                    onValueChange = viewModel::onGuardianNameChange,
                    modifier = Modifier.weight(1.4f),
                    label = { Text("Nome do responsável") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = state.draft.guardianKinship,
                    onValueChange = viewModel::onGuardianKinshipChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Parentesco") },
                    placeholder = { Text("Mãe/Pai") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.draft.guardianContact,
                    onValueChange = viewModel::onGuardianContactChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Telefone / WhatsApp") },
                    placeholder = { Text("(11) 99999-9999") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = state.draft.emergencyContact,
                    onValueChange = viewModel::onEmergencyContactChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Contato de emergência") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
            }

            OutlinedTextField(
                value = state.draft.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("E-mail da família / responsável (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.address,
                onValueChange = viewModel::onAddressChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Endereço residencial / Cidade (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            // SECTION 3: ESCOLA, PERFIL COMUNICATIVO E BILÍNGUE
            Text(
                text = "3. Contexto Escolar, Linguístico e Sensorial",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.draft.school,
                    onValueChange = viewModel::onSchoolChange,
                    modifier = Modifier.weight(1.3f),
                    label = { Text("Escola / Creche") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
                OutlinedTextField(
                    value = state.draft.educationLevel,
                    onValueChange = viewModel::onEducationLevelChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Série / Turma") },
                    placeholder = { Text("Infantil / 1º Ano") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors
                )
            }

            // Quick chips for Communication Modality
            Text(
                text = "Modalidade comunicativa principal:",
                style = MaterialTheme.typography.labelLarge,
                color = MockupTextMuted
            )
            val modalities = listOf("Fala oral", "CAA / Prancha", "Gestos + Fala", "Libras / Bimodal", "Em avaliação")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modalities.forEach { mod ->
                    val selected = state.draft.communicationModalities == mod
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) MockupDarkPill else MockupSurface)
                            .border(1.dp, if (selected) MockupDarkPill else MockupBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.onCommunicationModalitiesChange(mod) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = mod,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else MockupTextDark,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.draft.homeLanguages,
                onValueChange = viewModel::onHomeLanguagesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Idiomas em casa / Perfil bilíngue") },
                placeholder = { Text("Ex: Português Brasileiro, Inglês") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.effectiveReinforcers,
                onValueChange = viewModel::onEffectiveReinforcersChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Interesses e reforçadores favoritos (ex: dinossauros, música)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            FonoCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Modo de Baixo Estímulo Sensorial",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MockupTextDark
                        )
                        Text(
                            text = "Reduz animações e efeitos sonoros intensos nos jogos para crianças com hipersensibilidade.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MockupTextMuted
                        )
                    }
                    Switch(
                        checked = state.draft.lowStimulationMode,
                        onCheckedChange = viewModel::onLowStimulationToggle
                    )
                }
            }

            OutlinedTextField(
                value = state.draft.followUpStartDateText,
                onValueChange = viewModel::onFollowUpDateTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Data de início do acompanhamento (DD/MM/AAAA)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.clinicalNotes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observações clínicas iniciais / Queixa resumida") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (!state.isEditMode) {
                FonoPrimaryButton(
                    text = "Cadastrar e Iniciar Anamnese (12 Seções)",
                    onClick = { viewModel.save(openAnamnesisImmediately = true) },
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    isLoading = state.isSaving,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
                FonoSecondaryButton(
                    text = "Salvar e Abrir Prontuário",
                    onClick = { viewModel.save(openAnamnesisImmediately = false) },
                    icon = Icons.Default.Check,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                FonoPrimaryButton(
                    text = "Salvar Alterações do Prontuário",
                    onClick = { viewModel.save(openAnamnesisImmediately = false) },
                    icon = Icons.Default.Check,
                    isLoading = state.isSaving,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    if (showBirthDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showBirthDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val selectedLocalDate = utcDateAtEpochMillis(millis)
                            val formattedBr = selectedLocalDate.format(dateFormatter("dd/MM/yyyy"))
                            viewModel.onBirthDateTextChange(formattedBr)
                        }
                        showBirthDatePicker = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBirthDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
