package com.healthcare.fonolab.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.healthcare.fonolab.shared.App
import java.awt.Dimension

fun main() {
    // Configuracoes de alta qualidade de renderizacao gráfica e fontes para Windows/Desktop
    System.setProperty("sun.java2d.uiScale.enabled", "true")
    System.setProperty("sun.java2d.dpiaware", "true")
    System.setProperty("awt.useSystemAAFontSettings", "lcd")
    System.setProperty("swing.aatext", "true")
    System.setProperty("prism.lcdtext", "true")
    System.setProperty("skiko.renderApi", "DIRECT3D")
    System.setProperty("skiko.win.directx.vsync", "true")
    System.setProperty("sun.java2d.d3d", "true")

    application {
        val platformServices = remember { createDesktopPlatformServices() }
        val windowState = rememberWindowState(width = 1280.dp, height = 820.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "FonoLab — Gestão Clínica & Terapia Fonoaudiológica",
            state = windowState
        ) {
            window.minimumSize = Dimension(960, 640)

            App(platformServices = platformServices)
        }
    }
}
