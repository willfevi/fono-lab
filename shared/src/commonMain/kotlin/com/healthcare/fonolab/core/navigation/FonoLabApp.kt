package com.healthcare.fonolab.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.healthcare.fonolab.core.designsystem.component.MockupFloatingNavBar
import com.healthcare.fonolab.core.designsystem.theme.MockupBackground
import com.healthcare.fonolab.core.designsystem.theme.MockupSurface
import com.healthcare.fonolab.core.di.LocalFonoLabContainer
import com.healthcare.fonolab.core.platform.LocalPlatformServices
import com.healthcare.fonolab.domain.manager.*
import com.healthcare.fonolab.domain.patient.ChangePatientStatusUseCase
import com.healthcare.fonolab.domain.patient.DeletePatientUseCase
import com.healthcare.fonolab.domain.session.FinishSessionUseCase
import com.healthcare.fonolab.domain.session.StartSessionUseCase
import com.healthcare.fonolab.feature.anamnesis.AnamnesisScreen
import com.healthcare.fonolab.feature.anamnesis.AnamnesisViewModel
import com.healthcare.fonolab.feature.dashboard.DashboardScreen
import com.healthcare.fonolab.feature.dashboard.DashboardViewModel
import com.healthcare.fonolab.feature.gamecatalog.GameCatalogScreen
import com.healthcare.fonolab.feature.games.play.GamePlayScreen
import com.healthcare.fonolab.feature.games.play.GamePlayViewModel
import com.healthcare.fonolab.feature.patientdetails.PatientDetailsScreen
import com.healthcare.fonolab.feature.patientdetails.PatientDetailsViewModel
import com.healthcare.fonolab.feature.patients.PatientsScreen
import com.healthcare.fonolab.feature.patients.PatientsViewModel
import com.healthcare.fonolab.feature.patients.form.PatientFormScreen
import com.healthcare.fonolab.feature.patients.form.PatientFormViewModel
import com.healthcare.fonolab.feature.reports.ReportsScreen
import com.healthcare.fonolab.feature.reports.ReportsViewModel
import com.healthcare.fonolab.feature.sessions.SessionsScreen
import com.healthcare.fonolab.feature.sessions.hub.SessionHubScreen
import com.healthcare.fonolab.feature.sessions.hub.SessionHubViewModel
import com.healthcare.fonolab.feature.settings.SettingsScreen
import java.time.Clock
import java.time.ZoneId

@Composable
fun FonoLabApp(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isGamePlayScreen = currentDestination?.route?.contains("GamePlay") == true

    if (isGamePlayScreen) {
        FonoLabNavHost(navController = navController, modifier = Modifier.fillMaxSize())
        return
    }

    val destinations = TopLevelDestination.entries
    val currentRoute = currentDestination?.route
    val isTopLevelDestination = destinations.any { dest ->
        currentRoute != null && currentRoute.contains(dest.route::class.simpleName ?: "")
    }
    val selectedIndex = destinations.indexOfFirst { dest ->
        currentRoute?.contains(dest.route::class.simpleName ?: "") == true
    }.let { if (it >= 0) it else 0 }

    val onSelectDestination: (Int) -> Unit = { index ->
        val destination = destinations[index]
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupBackground)
    ) {
        val isTabletOrDesktop = maxWidth >= 600.dp

        if (isTabletOrDesktop) {
            // Adaptive Medium/Expanded Layout (Tablet & Desktop Persistent Navigation Rail / Sidebar)
            Row(modifier = Modifier.fillMaxSize()) {
                if (isTopLevelDestination) {
                    NavigationRail(
                        containerColor = MockupSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        destinations.forEachIndexed { idx, dest ->
                            NavigationRailItem(
                                selected = selectedIndex == idx,
                                onClick = { onSelectDestination(idx) },
                                icon = { Icon(dest.icon, contentDescription = dest.title) },
                                label = { Text(dest.title) }
                            )
                        }
                    }
                }
                FonoLabNavHost(
                    navController = navController,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Compact Layout (Smartphone with Bottom Floating Pill Navigation)
            Box(modifier = Modifier.fillMaxSize()) {
                FonoLabNavHost(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isTopLevelDestination) Modifier.padding(bottom = 80.dp)
                            else Modifier
                        )
                )

                if (isTopLevelDestination) {
                    MockupFloatingNavBar(
                        items = destinations.map { it.icon to it.title },
                        selectedIndex = selectedIndex,
                        onItemSelected = onSelectDestination,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun FonoLabNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val container = LocalFonoLabContainer.current
    val platformServices = LocalPlatformServices.current
    val defaultClock = Clock.system(ZoneId.of("America/Sao_Paulo"))

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard,
        modifier = modifier
    ) {
        composable<Screen.Dashboard> {
            val viewModel: DashboardViewModel = viewModel {
                DashboardViewModel(
                    patientRepository = container.patientRepository,
                    sessionRepository = container.sessionRepository,
                    qualityControlManager = container.qualityControlManager
                )
            }
            DashboardScreen(
                viewModel = viewModel,
                onNavigateToNewPatient = { navController.navigate(Screen.PatientForm()) },
                onNavigateToNewSession = { navController.navigate(Screen.Patients) },
                onNavigateToGameCatalog = { navController.navigate(Screen.GameCatalog) },
                onNavigateToReports = { navController.navigate(Screen.Reports) },
                onNavigateToPatientDetail = { id -> navController.navigate(Screen.PatientDetail(id)) },
                onNavigateToSessionHub = { id -> navController.navigate(Screen.SessionHub(id)) }
            )
        }

        composable<Screen.Patients> {
            val viewModel: PatientsViewModel = viewModel {
                PatientsViewModel(container.patientRepository)
            }
            PatientsScreen(
                viewModel = viewModel,
                onNavigateToNewPatient = { navController.navigate(Screen.PatientForm()) },
                onNavigateToPatientDetail = { id -> navController.navigate(Screen.PatientDetail(id)) }
            )
        }

        composable<Screen.PatientForm> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PatientForm>()
            val viewModel: PatientFormViewModel = viewModel {
                PatientFormViewModel(
                    patientId = route.patientId,
                    patientManager = container.patientManager,
                    dateTimeManager = container.dateTimeManager
                )
            }
            PatientFormScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPatientSaved = { id ->
                    navController.popBackStack()
                    navController.navigate(Screen.PatientDetail(id))
                },
                onPatientSavedOpenAnamnesis = { id ->
                    navController.popBackStack()
                    navController.navigate(Screen.PatientDetail(id))
                    navController.navigate(Screen.Anamnesis(id))
                }
            )
        }

        composable<Screen.PatientDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PatientDetail>()
            val viewModel: PatientDetailsViewModel = viewModel {
                PatientDetailsViewModel(
                    patientId = route.patientId,
                    patientRepository = container.patientRepository,
                    goalRepository = container.goalRepository,
                    sessionRepository = container.sessionRepository,
                    startSessionUseCase = StartSessionUseCase(container.sessionRepository, defaultClock),
                    changeStatusUseCase = ChangePatientStatusUseCase(container.patientRepository, defaultClock),
                    deletePatientUseCase = DeletePatientUseCase(container.patientRepository),
                    dateTimeManager = container.dateTimeManager,
                    anamnesisManager = container.anamnesisManager,
                    assessmentManager = container.assessmentManager,
                    phonologyManager = container.phonologyManager,
                    therapyManager = container.therapyManager,
                    generalizationManager = container.generalizationManager,
                    sessionManager = container.sessionManager,
                    planningAssistant = container.planningAssistant,
                    homePracticeManager = container.homePracticeManager,
                    caregiverManager = container.caregiverManager,
                    documentManager = container.documentManager,
                    templateManager = container.templateManager,
                    docAssistant = container.docAssistant,
                    historyManager = container.historyManager,
                    clinicalDataManager = container.clinicalDataManager
                )
            }
            PatientDetailsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id -> navController.navigate(Screen.PatientForm(id)) },
                onNavigateToStartSession = { sessionId ->
                    navController.navigate(Screen.SessionHub(sessionId))
                },
                onNavigateToSessionHub = { id -> navController.navigate(Screen.SessionHub(id)) },
                onNavigateToAnamnesis = { id -> navController.navigate(Screen.Anamnesis(id)) }
            )
        }

        composable<Screen.Anamnesis> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.Anamnesis>()
            val viewModel: AnamnesisViewModel = viewModel {
                AnamnesisViewModel(
                    patientId = route.patientId,
                    patientManager = container.patientManager,
                    anamnesisManager = container.anamnesisManager,
                    formManager = container.formManager,
                    dateTimeManager = container.dateTimeManager
                )
            }
            AnamnesisScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.GameCatalog> {
            GameCatalogScreen(
                onPlayGame = { gameId ->
                    navController.navigate(Screen.GamePlay(gameId = gameId))
                }
            )
        }

        composable<Screen.GamePlay> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.GamePlay>()
            val viewModel: GamePlayViewModel = viewModel {
                GamePlayViewModel(
                    gameId = route.gameId,
                    sessionId = route.sessionId,
                    activityId = route.activityId,
                    sessionRepository = container.sessionRepository,
                    audioService = platformServices.audioService,
                    clock = defaultClock
                )
            }
            GamePlayScreen(
                viewModel = viewModel,
                onExitGame = { navController.popBackStack() }
            )
        }

        composable<Screen.Sessions> {
            SessionsScreen(
                onSessionClick = { id -> navController.navigate(Screen.SessionHub(id)) }
            )
        }

        composable<Screen.SessionHub> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.SessionHub>()
            val viewModel: SessionHubViewModel = viewModel {
                SessionHubViewModel(
                    sessionId = route.sessionId,
                    sessionRepository = container.sessionRepository,
                    sessionManager = container.sessionManager,
                    documentManager = container.documentManager,
                    finishSessionUseCase = FinishSessionUseCase(container.sessionRepository, defaultClock),
                    clock = defaultClock
                )
            }
            SessionHubScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlayActivity = { gameId, activityId ->
                    navController.navigate(Screen.GamePlay(gameId = gameId, activityId = activityId))
                }
            )
        }

        composable<Screen.Reports> {
            val viewModel: ReportsViewModel = viewModel {
                ReportsViewModel(
                    patientRepository = container.patientRepository,
                    sessionRepository = container.sessionRepository,
                    billingManager = container.billingManager,
                    contentLibraryManager = container.contentLibraryManager,
                    dateTimeManager = container.dateTimeManager
                )
            }
            ReportsScreen(viewModel = viewModel)
        }

        composable<Screen.Settings> {
            SettingsScreen()
        }
    }
}
