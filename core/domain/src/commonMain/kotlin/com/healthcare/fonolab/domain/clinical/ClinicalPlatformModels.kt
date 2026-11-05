package com.healthcare.fonolab.domain.clinical

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import com.healthcare.fonolab.domain.goal.ClinicalArea
import com.healthcare.fonolab.domain.performance.CueLevel
import java.time.Instant
import java.time.LocalDate

// ============================================================================
// 1. ANAMNESIS (12 STRUCTURED SPEECH-LANGUAGE PATHOLOGY SECTIONS + VERSIONING)
// ============================================================================

enum class AnamnesisStatus(val displayName: String) {
    NOT_STARTED("Não iniciada"),
    IN_PROGRESS("Em preenchimento"),
    COMPLETED("Concluída")
}

enum class AnamnesisSectionType(
    val number: Int,
    val title: String,
    val guidingQuestions: String
) {
    IDENTIFICATION(
        1,
        "1. Identificação e Contexto Familiar",
        "Informante, parentesco, com quem a criança reside, irmãos, rotina familiar e rede de apoio."
    ),
    MAIN_COMPLAINT(
        2,
        "2. Queixa Principal e Demanda",
        "Descrição detalhada da queixa, início percebido, evolução, quem encaminhou, impacto na rotina e expectativas da família."
    ),
    GESTATIONAL_PERINATAL(
        3,
        "3. Histórico Gestacional e Perinatal",
        "Planejamento, pré-natal, intercorrências na gestação, medicamentos, tipo de parto, idade gestacional, peso ao nascer, APGAR, UTI neonatal, icterícia e triagens neonatais."
    ),
    NEUROPSYCHOMOTOR(
        4,
        "4. Desenvolvimento Neuropsicomotor",
        "Sustento cefálico, sentar sem apoio, engatinhar, andar, controle esfincteriano, coordenação motora ampla/fina e lateralidade."
    ),
    LANGUAGE_COMMUNICATION(
        5,
        "5. Desenvolvimento da Linguagem e Comunicação",
        "Balbucio, primeiras palavras, primeiras frases, compreensão de ordens, gestos, contato visual, intenção comunicativa, inteligibilidade da fala, trocas/omissões fonêmicas, vocabulário, narrativa e fluência."
    ),
    HEALTH_HISTORY(
        6,
        "6. Histórico de Saúde Geral",
        "Doenças prévias, internações, cirurgias, convulsões, alergias, medicamentos em uso, acompanhamento médico/terapêutico, diagnósticos existentes e histórico familiar."
    ),
    HEARING_VISION(
        7,
        "7. Audição e Visão",
        "Teste da orelhinha (TAN), otites de repetição, reação a sons, necessidade de repetição, volume de TV, exames audiológicos (audiometria/impedanciometria) e avaliação oftalmológica."
    ),
    FEEDING_OROFACIAL(
        8,
        "8. Alimentação e Motricidade Orofacial",
        "Amamentação natural/mamadeira, introdução alimentar, aceitação de consistências/texturas, mastigação, deglutição, engasgos, respiração (nasal/oral/mista), ronco, sialorreia, chupeta, sucção digital, bruxismo e freio lingual."
    ),
    BEHAVIORAL_SOCIAL(
        9,
        "9. Aspectos Comportamentais e Sociais",
        "Interação com pares e adultos, brincadeira simbólica, atenção, tolerância à frustração, interesses restritos, sensibilidade sensorial (auditiva/tátil/alimentar) e qualidade do sono."
    ),
    SCHOOL_HISTORY(
        10,
        "10. Histórico Escolar e Aprendizagem",
        "Escola/série/turno, adaptação, participação, socialização, leitura, escrita, consciência fonológica, mediador/apoio escolar e queixas pedagógicas."
    ),
    SPEECH_THERAPY_HISTORY(
        11,
        "11. Histórico Fonoaudiológico Anterior",
        "Avaliações e terapias fonoaudiológicas prévias, duração, objetivos trabalhados, evolução percebida e motivo de encerramento."
    ),
    FINAL_OBSERVATIONS(
        12,
        "12. Observações Finais e Hipótese Clínica",
        "Informações complementares da família, impressão clínica inicial do fonoaudiólogo, hipóteses diagnósticas e conduta/encaminhamentos."
    )
}

data class AnamnesisSectionContent(
    val sectionType: AnamnesisSectionType,
    val familyReport: String = "",
    val clinicianObservation: String = "",
    val quickStatus: String = "" // Ex: "Dentro do esperado", "Alterado", "Não informado", "Não se aplica"
) {
    val isFilled: Boolean
        get() = familyReport.isNotBlank() || clinicianObservation.isNotBlank() || quickStatus.isNotBlank()
}

data class AnamnesisRecord(
    val id: Long,
    val patientId: Long,
    val status: AnamnesisStatus,
    val informantName: String,
    val informantKinship: String,
    val referredBy: String,
    val anamnesisDate: LocalDate,
    val sections: List<AnamnesisSectionContent>,
    val generalSummary: String,
    val versionNumber: Int,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val filledSectionsCount: Int
        get() = sections.count { it.isFilled }

    val completionPercentage: Int
        get() = if (sections.isEmpty()) 0 else ((filledSectionsCount * 100) / sections.size)
}

data class AnamnesisVersion(
    val id: Long,
    val anamnesisId: Long,
    val patientId: Long,
    val versionNumber: Int,
    val snapshotJson: String,
    val changeSummary: String,
    val savedAt: Instant
)

// ============================================================================
// 2. CLINICAL ASSESSMENTS & PHONETIC-PHONOLOGICAL MAP
// ============================================================================

enum class AssessmentStatus(val displayName: String) {
    IN_PROGRESS("Em andamento"),
    COMPLETED("Concluída"),
    REASSESSMENT("Reavaliação")
}

data class ClinicalAssessment(
    val id: Long,
    val patientId: Long,
    val title: String,
    val clinicalArea: ClinicalArea,
    val protocolOrInstrument: String,
    val assessmentDate: LocalDate,
    val baselineScorePercentage: Int?,
    val currentScorePercentage: Int?,
    val findingsSummary: String,
    val clinicalHypothesis: String,
    val recommendations: String,
    val status: AssessmentStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class PhonemeWordPosition(val displayName: String) {
    INITIAL("Onset Inicial"),
    MEDIAL("Onset Medial"),
    FINAL("Coda Final"),
    CLUSTER("Encontro Consonantal (CCV)")
}

enum class PhonemeAcquisitionStatus(val displayName: String) {
    NOT_EVALUATED("Não avaliado"),
    EVALUATED_ABSENT("Ausente / Alterado"),
    IN_INTERVENTION("Em intervenção"),
    IN_ACQUISITION("Em aquisição"),
    GENERALIZED("Generalizado"),
    IN_MAINTENANCE("Em manutenção / Adquirido")
}

enum class PhonologicalErrorType(val displayName: String) {
    NONE("Produção Adequada"),
    SUBSTITUTION("Substituição"),
    OMISSION("Omissão"),
    DISTORTION("Distorção"),
    CLUSTER_REDUCTION("Simplificação de Encontro Consonantal"),
    FINAL_CONSONANT_DELETION("Simplificação de Consoante Final"),
    FRONTING("Anteriorização"),
    BACKING("Posteriorização"),
    STOPPING("Plosivização"),
    DEVOICING("Ensurdecimento")
}

data class PhonemeInventoryEntry(
    val id: Long,
    val patientId: Long,
    val phoneme: String, // e.g. "/p/", "/r/", "/s/", "/ʃ/"
    val position: PhonemeWordPosition,
    val status: PhonemeAcquisitionStatus,
    val errorType: PhonologicalErrorType,
    val isStimulable: Boolean,
    val bestCueLevel: CueLevel,
    val sampleWords: String,
    val clinicianNotes: String,
    val updatedAt: Instant
)

// ============================================================================
// 3. THERAPIES & GENERALIZATION ACROSS CONTEXTS AND INTERLOCUTORS
// ============================================================================

enum class TherapyPlanStatus(val displayName: String) {
    ACTIVE("Ativa"),
    PAUSED("Pausada"),
    COMPLETED("Concluída / Alta"),
    CANCELLED("Cancelada")
}

data class TherapyPlan(
    val id: Long,
    val patientId: Long,
    val title: String,
    val clinicalArea: ClinicalArea,
    val generalObjective: String,
    val specificObjectives: String,
    val targetSkillsOrPhonemes: String,
    val weeklyFrequency: Int,
    val sessionDurationMinutes: Int,
    val startDate: LocalDate,
    val expectedEndDate: LocalDate?,
    val status: TherapyPlanStatus,
    val clinicalNotes: String,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class GeneralizationContext(val displayName: String) {
    CLINIC("Consultório Clínico"),
    HOME("Ambiente Familiar / Casa"),
    SCHOOL("Escola"),
    COMMUNITY("Comunidade / Social")
}

enum class InterlocutorType(val displayName: String) {
    THERAPIST("Fonoaudiólogo(a)"),
    CAREGIVER("Familiar / Cuidador"),
    TEACHER("Professor(a)"),
    PEER("Pares / Crianças")
}

enum class AcquisitionStage(val displayName: String) {
    EVALUATED("Avaliado (Linha de Base)"),
    IN_INTERVENTION("Em Intervenção"),
    IN_ACQUISITION("Em Aquisição"),
    GENERALIZED("Generalizado"),
    IN_MAINTENANCE("Em Manutenção")
}

data class GeneralizationProbe(
    val id: Long,
    val patientId: Long,
    val goalId: Long?,
    val targetSkillOrPhoneme: String,
    val context: GeneralizationContext,
    val interlocutor: InterlocutorType,
    val stage: AcquisitionStage,
    val accuracyPercentage: Int,
    val isSpontaneous: Boolean,
    val probeDate: LocalDate,
    val notes: String,
    val createdAt: Instant
)

// ============================================================================
// 4. SCHEDULE, WAITLIST & REFERRALS
// ============================================================================

enum class AppointmentStatus(val displayName: String) {
    SCHEDULED("Agendado"),
    CONFIRMED("Confirmado"),
    COMPLETED("Realizado"),
    CANCELLED("Cancelado"),
    NO_SHOW("Falta"),
    RESCHEDULED("Remarcado")
}

enum class AppointmentRecurrence(val displayName: String) {
    SINGLE("Sessão Avulsa"),
    WEEKLY("Semanal"),
    TWICE_WEEKLY("2x por Semana"),
    BIWEEKLY("Quinzenal")
}

data class ClinicalAppointment(
    val id: Long,
    val patientId: Long?,
    val patientName: String,
    val linkedSessionId: Long?,
    val date: LocalDate,
    val startTime: String, // "14:00"
    val endTime: String,   // "14:50"
    val serviceType: String, // "Terapia Fonoaudiológica", "Avaliação", "Anamnese", "Retorno"
    val recurrence: AppointmentRecurrence,
    val status: AppointmentStatus,
    val notes: String,
    val createdAt: Instant
)

enum class ReferralUrgency(val displayName: String) {
    LOW("Rotina"),
    MEDIUM("Prioridade Moderada"),
    HIGH("Prioridade Alta / Precoce")
}

data class WaitlistAndReferralEntry(
    val id: Long,
    val candidateName: String,
    val birthDateText: String,
    val guardianContact: String,
    val referredBy: String, // e.g., "Pediatra", "Escola", "Otorrinolaringologista"
    val mainComplaint: String,
    val preferredSchedule: String, // "Manhã", "Tarde", "Flexível"
    val urgency: ReferralUrgency,
    val isConvertedToPatient: Boolean,
    val convertedPatientId: Long?,
    val createdAt: Instant
)

// ============================================================================
// 5. HOME PRACTICE & CAREGIVER ORIENTATION
// ============================================================================

enum class HomePracticeStatus(val displayName: String) {
    PENDING("Pendente"),
    IN_PROGRESS("Em realização"),
    COMPLETED("Concluída pela família")
}

enum class HomeDifficultyFeedback(val displayName: String) {
    NOT_RATED("Sem retorno ainda"),
    EASY("Fácil para a criança"),
    ADEQUATE("Adequado"),
    DIFFICULT("Difícil / Precisou de ajuda")
}

data class HomePracticeTask(
    val id: Long,
    val patientId: Long,
    val title: String,
    val linkedGameId: String?,
    val targetSkill: String,
    val caregiverInstructions: String,
    val recommendedFrequency: String, // e.g. "10 min por dia, 3x na semana"
    val assignedDate: LocalDate,
    val dueDate: LocalDate?,
    val status: HomePracticeStatus,
    val difficultyFeedback: HomeDifficultyFeedback,
    val familyNotes: String,
    val createdAt: Instant
)

// ============================================================================
// 6. CLINICAL DOCUMENTS, SOAP EVOLUTION NOTES & TEMPLATES
// ============================================================================

enum class DocumentCategory(val displayName: String) {
    SOAP_EVOLUTION("Evolução Clínica (SOAP)"),
    ANAMNESIS_DOCUMENT("Relatório de Anamnese"),
    ASSESSMENT_REPORT("Laudo / Relatório de Avaliação"),
    EVOLUTION_REPORT("Relatório de Evolução Terapêutica"),
    SCHOOL_REPORT("Relatório / Orientação Escolar"),
    REFERRAL_LETTER("Encaminhamento Multiprofissional"),
    ATTENDANCE_DECLARATION("Declaração de Comparecimento"),
    DISCHARGE_SUMMARY("Relatório de Alta Fonoaudiológica"),
    HOME_ORIENTATION("Guia de Orientação Familiar"),
    CONSENT_TERM("Termo de Consentimento / Uso Clínico")
}

data class ClinicalDocument(
    val id: Long,
    val patientId: Long,
    val sessionId: Long?,
    val category: DocumentCategory,
    val title: String,
    val documentDate: LocalDate,
    val professionalName: String,
    val professionalCrfa: String,
    // SOAP structured fields (used when category == SOAP_EVOLUTION or structured reports)
    val soapSubjective: String = "",
    val soapObjective: String = "",
    val soapAssessment: String = "",
    val soapPlan: String = "",
    // General document body & addendum
    val bodyContent: String,
    val addendumNotes: String = "",
    val isSignedAndLocked: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ============================================================================
// 7. FINANCIAL MANAGEMENT (BRL, SEPARATED FROM CLINICAL RECORDS)
// ============================================================================

enum class BillingStatus(val displayName: String) {
    PENDING("Pendente"),
    PAID("Pago"),
    OVERDUE("Em atraso"),
    CANCELLED("Cancelado")
}

enum class PaymentMethod(val displayName: String) {
    PIX("Pix"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    CASH("Dinheiro"),
    BANK_TRANSFER("Transferência / Boleto")
}

data class BillingRecord(
    val id: Long,
    val patientId: Long,
    val patientName: String,
    val sessionId: Long?,
    val description: String, // e.g. "Sessão de Terapia Fonoaudiológica", "Pacote Mensal (4 sessões)"
    val amountCents: Long,   // BRL in centavos (e.g., 18000 = R$ 180,00)
    val dueDate: LocalDate,
    val paymentDate: LocalDate?,
    val paymentMethod: PaymentMethod,
    val status: BillingStatus,
    val receiptIssued: Boolean,
    val notes: String,
    val createdAt: Instant
) {
    fun formattedAmountBrl(): String {
        val reais = amountCents / 100
        val centavos = kotlin.math.abs(amountCents % 100)
        return "R$ $reais,${centavos.toString().padStart(2, '0')}"
    }
}

// ============================================================================
// 8. CONTENT LIBRARY, TASKS, AUDIT, PROFESSIONAL PROFILE & TIMELINE
// ============================================================================

data class TherapeuticStimulus(
    val id: Long,
    val word: String,
    val targetPhoneme: String,
    val phonemePosition: PhonemePosition,
    val syllableDivision: String,
    val syllableCount: Int,
    val semanticCategory: String,
    val difficulty: DifficultyLevel,
    val iconEmoji: String,
    val exampleSentence: String,
    val isCustom: Boolean,
    val isFavorite: Boolean
)

enum class TaskPriority(val displayName: String) {
    HIGH("Alta"),
    MEDIUM("Média"),
    LOW("Baixa")
}

data class ClinicalTask(
    val id: Long,
    val patientId: Long?,
    val title: String,
    val description: String,
    val dueDate: LocalDate?,
    val priority: TaskPriority,
    val isCompleted: Boolean,
    val category: String, // "Anamnese", "Evolução", "Reavaliação", "Financeiro", "Lembrete"
    val createdAt: Instant
)

data class ProfessionalProfile(
    val fullName: String = "Dra. Mariana Fonoaudióloga",
    val crfaRegistration: String = "CRFa 2-12345",
    val specialty: String = "Fonoaudiologia Infantil • Linguagem e Motricidade Orofacial",
    val clinicName: String = "Clínica FonoLab Saúde Integrada",
    val phone: String = "(11) 99876-5432",
    val email: String = "contato@fonolab.com.br",
    val cityState: String = "São Paulo - SP",
    val isAuthenticatedPreview: Boolean = false
)

enum class TimelineFilterCategory(val displayName: String) {
    ALL("Todos"),
    ANAMNESIS("Anamnese"),
    ASSESSMENTS("Avaliações"),
    THERAPIES("Terapias e Metas"),
    SESSIONS("Sessões"),
    GAMES("Jogos"),
    DOCUMENTS("Documentos / SOAP"),
    HOME_PRACTICE("Casa")
}

data class TimelineEvent(
    val id: String,
    val patientId: Long,
    val category: TimelineFilterCategory,
    val title: String,
    val subtitle: String,
    val details: String,
    val date: LocalDate,
    val timestamp: Instant,
    val badgeText: String
)

data class AuditEvent(
    val id: Long,
    val entityType: String,
    val entityId: Long,
    val action: String,
    val description: String,
    val timestamp: Instant
)
