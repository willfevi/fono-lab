package com.healthcare.fonolab.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Dashboard : Screen

    @Serializable
    data object Patients : Screen

    @Serializable
    data class PatientDetail(val patientId: Long) : Screen

    @Serializable
    data class PatientForm(val patientId: Long? = null) : Screen

    @Serializable
    data object GameCatalog : Screen

    @Serializable
    data class GamePlay(
        val gameId: String,
        val sessionId: Long? = null,
        val activityId: Long? = null
    ) : Screen

    @Serializable
    data object Sessions : Screen

    @Serializable
    data class SessionHub(val sessionId: Long) : Screen

    @Serializable
    data class SessionForm(val patientId: Long? = null) : Screen

    @Serializable
    data object Reports : Screen

    @Serializable
    data class PatientReport(val patientId: Long) : Screen

    @Serializable
    data object Settings : Screen
}

enum class TopLevelDestination(
    val title: String,
    val icon: ImageVector,
    val route: Screen
) {
    DASHBOARD("Início", Icons.Default.Home, Screen.Dashboard),
    PATIENTS("Pacientes", Icons.Default.People, Screen.Patients),
    GAMES("Jogos", Icons.Default.Casino, Screen.GameCatalog),
    SESSIONS("Sessões", Icons.Default.History, Screen.Sessions),
    REPORTS("Relatórios", Icons.Default.BarChart, Screen.Reports),
    SETTINGS("Ajustes", Icons.Default.Settings, Screen.Settings)
}
