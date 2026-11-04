package com.healthcare.fonolab.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
import com.healthcare.fonolab.feature.sessions.SessionsScreen
import com.healthcare.fonolab.feature.sessions.hub.SessionHubScreen
import com.healthcare.fonolab.feature.sessions.hub.SessionHubViewModel
import com.healthcare.fonolab.feature.settings.SettingsScreen

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
            .padding(WindowInsets.safeDrawing.asPaddingValues())
    ) {
        val isTabletOrDesktop = maxWidth >= 600.dp

        if (isTabletOrDesktop) {
            // Adaptive Medium/Expanded Layout (Tablet & Desktop NavigationRail)
            Row(modifier = Modifier.fillMaxSize()) {
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
                FonoLabNavHost(
                    navController = navController,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        } else {
            // Compact Layout (Smartphone Floating Dark Pill Navigation Bar)
            Box(modifier = Modifier.fillMaxSize()) {
                FonoLabNavHost(
                    navController = navController,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                )

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
            val viewModel: DashboardViewModel = hiltViewModel()
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
            val viewModel: PatientsViewModel = hiltViewModel()
            PatientsScreen(
                viewModel = viewModel,
                onNavigateToNewPatient = { navController.navigate(Screen.PatientForm()) },
                onNavigateToPatientDetail = { id -> navController.navigate(Screen.PatientDetail(id)) }
            )
        }

        composable<Screen.PatientForm> {
            val viewModel: PatientFormViewModel = hiltViewModel()
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

        composable<Screen.PatientDetail> {
            val viewModel: PatientDetailsViewModel = hiltViewModel()
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

        composable<Screen.Anamnesis> {
            val viewModel: AnamnesisViewModel = hiltViewModel()
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

        composable<Screen.GamePlay> {
            val viewModel: GamePlayViewModel = hiltViewModel()
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

        composable<Screen.SessionHub> {
            val viewModel: SessionHubViewModel = hiltViewModel()
            SessionHubScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onPlayActivity = { gameId, activityId ->
                    navController.navigate(Screen.GamePlay(gameId = gameId, activityId = activityId))
                }
            )
        }

        composable<Screen.Reports> {
            ReportsScreen()
        }

        composable<Screen.Settings> {
            SettingsScreen()
        }
    }
}
