package com.healthcare.fonolab.core.di

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.healthcare.fonolab.domain.clinical.ClinicalPlatformRepository
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.manager.*
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.session.TherapySessionRepository

/**
 * Universal service container providing repositories and managers across Android and Desktop.
 */
interface FonoLabContainer {
    val patientRepository: PatientRepository
    val sessionRepository: TherapySessionRepository
    val goalRepository: ClinicalGoalRepository
    val platformRepository: ClinicalPlatformRepository

    val dateTimeManager: DateTimeManager
    val qualityControlManager: QualityControlManager
    val patientManager: PatientManager
    val formManager: FormManager
    val sessionManager: SessionManager
    val anamnesisManager: AnamnesisManager
    val gameManager: GameManager
    val reportManager: ReportManager
    val billingManager: BillingManager
    val scheduleManager: ScheduleManager
    val referralManager: ReferralManager
    val contentLibraryManager: ContentLibraryManager
    val clinicalDataManager: ClinicalDataManager
    val assessmentManager: AssessmentManager
    val phonologyManager: PhonologyManager
    val therapyManager: TherapyManager
    val generalizationManager: GeneralizationManager
    val planningAssistant: SessionPlanningAssistantManager
    val homePracticeManager: HomePracticeManager
    val caregiverManager: CaregiverManager
    val documentManager: DocumentManager
    val templateManager: TemplateManager
    val docAssistant: DocumentationAssistantManager
    val historyManager: HistoryManager
    val auditManager: AuditManager
    val accountManager: AccountManager
}

val LocalFonoLabContainer: ProvidableCompositionLocal<FonoLabContainer> = staticCompositionLocalOf {
    error("LocalFonoLabContainer not provided!")
}
