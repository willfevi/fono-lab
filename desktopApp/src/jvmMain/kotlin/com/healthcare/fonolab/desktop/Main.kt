package com.healthcare.fonolab.desktop

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.healthcare.fonolab.core.di.DefaultFonoLabContainer
import com.healthcare.fonolab.core.di.LocalFonoLabContainer
import com.healthcare.fonolab.core.platform.LocalPlatformServices
import com.healthcare.fonolab.shared.App
import java.awt.Dimension

fun main() = application {
    val platformServices = createDesktopPlatformServices()
    val container = DefaultFonoLabContainer(clock = platformServices.clock)
    val windowState = rememberWindowState(width = 1280.dp, height = 820.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "FonoLab — Gestão Clínica & Terapia Fonoaudiológica",
        state = windowState
    ) {
        window.minimumSize = Dimension(800, 600)

        CompositionLocalProvider(
            LocalPlatformServices provides platformServices,
            LocalFonoLabContainer provides container
        ) {
            App(platformServices = platformServices, container = container)
        }
    }
}
