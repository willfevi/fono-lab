package com.healthcare.fonolab.feature.patients.form

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.healthcare.fonolab.core.designsystem.component.FonoPrimaryButton
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupBorder
import com.healthcare.fonolab.core.designsystem.theme.MockupDarkPill
import com.healthcare.fonolab.core.designsystem.theme.MockupSurface
import com.healthcare.fonolab.core.designsystem.theme.MockupTextDark
import com.healthcare.fonolab.core.designsystem.theme.MockupTextMuted
import com.healthcare.fonolab.domain.patient.PatientValidationError
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFormScreen(
    viewModel: PatientFormViewModel,
    onNavigateBack: () -> Unit,
    onPatientSaved: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.event.collect { event ->
            if (event is PatientFormEvent.Saved) {
                onPatientSaved(event.patientId)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MockupBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (state.isEditMode) "Editar Paciente" else "Novo Paciente",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MockupBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Informações do Prontuário",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MockupTextDark
            )

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MockupDarkPill,
                unfocusedBorderColor = MockupBorder,
                focusedContainerColor = MockupSurface,
                unfocusedContainerColor = MockupSurface
            )

            OutlinedTextField(
                value = state.draft.fullName,
                onValueChange = viewModel::onFullNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome completo *") },
                isError = state.errors.any { it == PatientValidationError.FULL_NAME_REQUIRED || it == PatientValidationError.FULL_NAME_TOO_SHORT },
                supportingText = {
                    if (state.errors.contains(PatientValidationError.FULL_NAME_REQUIRED)) {
                        Text("O nome é obrigatório", color = MaterialTheme.colorScheme.error)
                    } else if (state.errors.contains(PatientValidationError.FULL_NAME_TOO_SHORT)) {
                        Text("Mínimo de 3 caracteres", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            // Birth date field (ISO format YYYY-MM-DD for reliable clinical input)
            OutlinedTextField(
                value = state.draft.birthDate?.toString() ?: "",
                onValueChange = { str ->
                    try {
                        val parsed = LocalDate.parse(str.trim())
                        viewModel.onBirthDateChange(parsed)
                    } catch (_: Exception) {
                        // ignore parsing error while typing
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Data de nascimento (AAAA-MM-DD) *") },
                placeholder = { Text("Ex: 2020-05-15") },
                isError = state.errors.any { it == PatientValidationError.BIRTH_DATE_REQUIRED || it == PatientValidationError.BIRTH_DATE_IN_FUTURE },
                supportingText = {
                    if (state.errors.contains(PatientValidationError.BIRTH_DATE_REQUIRED)) {
                        Text("Data de nascimento é obrigatória", color = MaterialTheme.colorScheme.error)
                    } else if (state.errors.contains(PatientValidationError.BIRTH_DATE_IN_FUTURE)) {
                        Text("A data não pode ser futura", color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.guardianName,
                onValueChange = viewModel::onGuardianNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nome do responsável (opcional)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.guardianContact,
                onValueChange = viewModel::onGuardianContactChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Contato do responsável (telefone ou e-mail)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            OutlinedTextField(
                value = state.draft.clinicalNotes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Observações clínicas iniciais (opcional)") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors
            )

            Spacer(modifier = Modifier.height(12.dp))

            FonoPrimaryButton(
                text = if (state.isEditMode) "Salvar Alterações" else "Cadastrar Paciente",
                onClick = viewModel::save,
                icon = Icons.Default.Check,
                isLoading = state.isSaving,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

