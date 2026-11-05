package com.healthcare.fonolab.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.healthcare.fonolab.shared.App
import java.awt.Dimension

fun main() = application {
    val platformServices = remember { createDesktopPlatformServices() }
    val windowState = rememberWindowState(width = 1280.dp, height = 820.dp)

    Window(
        onCloseRequest = ::exitApplication,
        title = "FonoLab — Gestão Clínica & Terapia Fonoaudiológica",
        state = windowState
    ) {
        window.minimumSize = Dimension(800, 600)

        App(platformServices = platformServices)
    }
}
