package com.healthcare.fonolab.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.healthcare.fonolab.core.designsystem.theme.FonoLabTheme
import com.healthcare.fonolab.core.di.fonoLabModule
import com.healthcare.fonolab.core.navigation.FonoLabApp
import com.healthcare.fonolab.core.platform.*
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import com.healthcare.fonolab.domain.files.File

/**
 * Shared Compose entry point for Android, Windows and Web.
 */
@Composable
fun App(
    platformServices: PlatformServices? = null,
    platformModule: Module? = null
) {
    val effectiveServices = platformServices ?: remember { createFallbackPlatformServices() }
    val configuration = remember(effectiveServices, platformModule) {
        koinConfiguration {
            allowOverride(false)
            modules(listOfNotNull(fonoLabModule(effectiveServices), platformModule))
        }
    }
    KoinApplication(configuration = configuration) {
        FonoLabTheme {
            FonoLabApp()
        }
    }
}

private fun createFallbackPlatformServices(): PlatformServices {
    val clock = DefaultPlatformClock()
    return PlatformServices(
        audioService = object : PlatformAudioService {
            override fun speak(text: String, language: String, onDone: () -> Unit) = onDone()
            override fun playAuditoryCue(cueName: String) {}
            override fun stop() {}
        },
        pdfExporter = object : PlatformPdfExporter {
            override fun exportAnamnesis(
                patientName: String,
                patientDoc: String,
                sections: Map<String, List<Pair<String, String>>>,
                onSuccess: (File) -> Unit,
                onError: (Throwable) -> Unit
            ) {
                onError(UnsupportedOperationException("PDF exporter not configured."))
            }

            override fun exportReport(
                patientName: String,
                reportTitle: String,
                summaryContent: String,
                onSuccess: (File) -> Unit,
                onError: (Throwable) -> Unit
            ) {
                onError(UnsupportedOperationException("PDF exporter not configured."))
            }
        },
        shareManager = object : PlatformShareManager {
            override fun shareText(title: String, text: String) {}
            override fun shareFile(file: File, title: String, mimeType: String) {}
        },
        clipboardManager = object : PlatformClipboardManager {
            private var clip: String? = null
            override fun copyToClipboard(text: String) { clip = text }
            override fun getClipboardText(): String? = clip
        },
        logger = DefaultPlatformLogger(),
        clock = clock
    )
}
