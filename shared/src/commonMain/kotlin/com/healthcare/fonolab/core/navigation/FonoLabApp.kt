package com.healthcare.fonolab.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (isTopLevelDestination) Modifier.statusBarsPadding()
                        else Modifier
                    )
            ) {
                FonoLabNavHost(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (isTopLevelDestination) {
                                Modifier
                                    .navigationBarsPadding()
                                    .padding(bottom = 88.dp)
                            } else {
                                Modifier
                            }
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
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard,
        modifier = modifier
    ) {
        composable<Screen.Dashboard> {
            val viewModel: DashboardViewModel = koinViewModel()
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
            val viewModel: PatientsViewModel = koinViewModel()
            PatientsScreen(
                viewModel = viewModel,
                onNavigateToNewPatient = { navController.navigate(Screen.PatientForm()) },
                onNavigateToPatientDetail = { id -> navController.navigate(Screen.PatientDetail(id)) }
            )
        }

        composable<Screen.PatientForm> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PatientForm>()
            val viewModel: PatientFormViewModel = koinViewModel(viewModelStoreOwner = backStackEntry) { parametersOf(route) }
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
            val viewModel: PatientDetailsViewModel = koinViewModel(viewModelStoreOwner = backStackEntry) { parametersOf(route) }
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
            val viewModel: AnamnesisViewModel = koinViewModel(viewModelStoreOwner = backStackEntry) { parametersOf(route) }
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
            val viewModel: GamePlayViewModel = koinViewModel(viewModelStoreOwner = backStackEntry) { parametersOf(route) }
            GamePlayScreen(
                viewModel = viewModel,
                onExitGame = { navController.popBackStack() }
            )
        }

        composable<Screen.Sessions> {
            SessionsScreen(
                viewModel = koinViewModel(),
                onSessionClick = { id -> navController.navigate(Screen.SessionHub(id)) }
            )
        }

        composable<Screen.SessionHub> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.SessionHub>()
            val viewModel: SessionHubViewModel = koinViewModel(viewModelStoreOwner = backStackEntry) { parametersOf(route) }
            SessionHubScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlayActivity = { gameId, activityId ->
                    navController.navigate(Screen.GamePlay(gameId = gameId, activityId = activityId))
                }
            )
        }

        composable<Screen.Reports> {
            val viewModel: ReportsViewModel = koinViewModel()
            ReportsScreen(viewModel = viewModel)
        }

        composable<Screen.Settings> {
            SettingsScreen(viewModel = koinViewModel())
        }
    }
}
