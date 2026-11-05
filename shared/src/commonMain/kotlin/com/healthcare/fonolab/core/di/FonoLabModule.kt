package com.healthcare.fonolab.core.di

import com.healthcare.fonolab.domain.datetime.Clock
import com.healthcare.fonolab.domain.datetime.systemClock
import com.healthcare.fonolab.core.navigation.Screen
import com.healthcare.fonolab.core.platform.PlatformAudioService
import com.healthcare.fonolab.core.platform.PlatformServices
import com.healthcare.fonolab.domain.clinical.ClinicalPlatformRepository
import com.healthcare.fonolab.domain.datetime.DateTimeManager
import com.healthcare.fonolab.domain.datetime.PlatformClock
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.goal.GoalValidator
import com.healthcare.fonolab.domain.goal.SaveGoalUseCase
import com.healthcare.fonolab.domain.manager.*
import com.healthcare.fonolab.domain.patient.*
import com.healthcare.fonolab.domain.session.*
import com.healthcare.fonolab.feature.anamnesis.AnamnesisViewModel
import com.healthcare.fonolab.feature.dashboard.DashboardViewModel
import com.healthcare.fonolab.feature.games.play.GamePlayViewModel
import com.healthcare.fonolab.feature.patientdetails.PatientDetailsViewModel
import com.healthcare.fonolab.feature.patients.PatientsViewModel
import com.healthcare.fonolab.feature.patients.form.PatientFormViewModel
import com.healthcare.fonolab.feature.reports.ReportsViewModel
import com.healthcare.fonolab.feature.sessions.SessionsViewModel
import com.healthcare.fonolab.feature.sessions.hub.SessionHubViewModel
import com.healthcare.fonolab.feature.settings.SettingsViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import kotlin.random.Random

internal fun fonoLabModule(platformServices: PlatformServices) = module {
    single<PlatformClock> { platformServices.clock }
    single<PlatformAudioService> { platformServices.audioService }
    single<Clock> { systemClock("America/Sao_Paulo") }
    single<Random> { Random.Default }

    single<PatientRepository> { InMemoryPatientRepository(get()) }
    single<TherapySessionRepository> { InMemoryTherapySessionRepository(get()) }
    single<ClinicalGoalRepository> { InMemoryClinicalGoalRepository(get()) }
    single<ClinicalPlatformRepository> { InMemoryClinicalPlatformRepository(get()) }

    singleOf(::DateTimeManager)
    singleOf(::PatientValidator)
    singleOf(::InternalCodeGenerator)
    singleOf(::GoalValidator)
    singleOf(::SavePatientUseCase)
    singleOf(::ChangePatientStatusUseCase)
    singleOf(::DeletePatientUseCase)
    singleOf(::SaveGoalUseCase)
    singleOf(::PlanSessionUseCase)
    singleOf(::StartPlannedSessionUseCase)
    singleOf(::PauseSessionUseCase)
    singleOf(::StartSessionUseCase)
    singleOf(::FinishSessionUseCase)
    singleOf(::CancelSessionUseCase)

    singleOf(::AuditManager)
    singleOf(::AccountManager)
    singleOf(::FormManager)
    singleOf(::PatientManager)
    singleOf(::SessionManager)
    singleOf(::AnamnesisManager)
    singleOf(::ClinicalDataManager)
    singleOf(::BillingManager)
    singleOf(::ScheduleManager)
    singleOf(::ReferralManager)
    singleOf(::ContentLibraryManager)
    singleOf(::AssessmentManager)
    singleOf(::PhonologyManager)
    singleOf(::TherapyManager)
    singleOf(::GeneralizationManager)
    singleOf(::SessionPlanningAssistantManager)
    singleOf(::HomePracticeManager)
    singleOf(::CaregiverManager)
    singleOf(::DocumentManager)
    singleOf(::TemplateManager)
    singleOf(::DocumentationAssistantManager)
    singleOf(::HistoryManager)
    singleOf(::QualityControlManager)

    viewModelOf(::DashboardViewModel)
    viewModelOf(::PatientsViewModel)
    viewModelOf(::SessionsViewModel)
    viewModelOf(::ReportsViewModel)
    viewModelOf(::SettingsViewModel)

    viewModel { (route: Screen.PatientForm) ->
        PatientFormViewModel(route.patientId, get(), get())
    }
    viewModel { (route: Screen.PatientDetail) ->
        PatientDetailsViewModel(
            patientId = route.patientId,
            patientRepository = get(),
            goalRepository = get(),
            sessionRepository = get(),
            startSessionUseCase = get(),
            changeStatusUseCase = get(),
            deletePatientUseCase = get(),
            dateTimeManager = get(),
            anamnesisManager = get(),
            assessmentManager = get(),
            phonologyManager = get(),
            therapyManager = get(),
            generalizationManager = get(),
            sessionManager = get(),
            planningAssistant = get(),
            homePracticeManager = get(),
            caregiverManager = get(),
            documentManager = get(),
            templateManager = get(),
            docAssistant = get(),
            historyManager = get(),
            clinicalDataManager = get()
        )
    }
    viewModel { (route: Screen.Anamnesis) ->
        AnamnesisViewModel(route.patientId, get(), get(), get(), get())
    }
    viewModel { (route: Screen.GamePlay) ->
        GamePlayViewModel(route.gameId, route.sessionId, route.activityId, get(), get(), get())
    }
    viewModel { (route: Screen.SessionHub) ->
        SessionHubViewModel(route.sessionId, get(), get(), get(), get(), get())
    }
}
