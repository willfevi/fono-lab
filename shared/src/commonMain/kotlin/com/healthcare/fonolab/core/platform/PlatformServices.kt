package com.healthcare.fonolab.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import com.healthcare.fonolab.domain.datetime.PlatformClock
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * Multiplatform contract for audio synthesis (TTS) and auditory feedback cues.
 */
interface PlatformAudioService {
    fun speak(text: String, language: String = "pt-BR", onDone: () -> Unit = {})
    fun playAuditoryCue(cueName: String)
    fun stop()
    fun isAvailable(): Boolean = true
}

/**
 * Multiplatform contract for exporting clinical documents (Anamnesis and SOAP Reports).
 */
interface PlatformPdfExporter {
    fun exportAnamnesis(
        patientName: String,
        patientDoc: String,
        sections: Map<String, List<Pair<String, String>>>,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun exportReport(
        patientName: String,
        reportTitle: String,
        summaryContent: String,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    )

    fun isAvailable(): Boolean = true
}

/**
 * Multiplatform contract for sharing text and clinical files.
 */
interface PlatformShareManager {
    fun shareText(title: String, text: String)
    fun shareFile(file: File, title: String, mimeType: String = "application/pdf")
}

/**
 * Multiplatform contract for clipboard access.
 */
interface PlatformClipboardManager {
    fun copyToClipboard(text: String)
    fun getClipboardText(): String?
}

/**
 * Multiplatform contract for structured clinical logging.
 */
interface PlatformLogger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable? = null)
}

/**
 * Default standard console logger.
 */
class DefaultPlatformLogger : PlatformLogger {
    override fun debug(tag: String, message: String) = println("[DEBUG][$tag] $message")
    override fun info(tag: String, message: String) = println("[INFO][$tag] $message")
    override fun warn(tag: String, message: String) = println("[WARN][$tag] $message")
    override fun error(tag: String, message: String, throwable: Throwable?) {
        println("[ERROR][$tag] $message")
        throwable?.printStackTrace()
    }
}

/**
 * Default standard system clock.
 */
class DefaultPlatformClock(private val clock: Clock = Clock.system(ZoneId.of("America/Sao_Paulo"))) : PlatformClock {
    private val delegate = com.healthcare.fonolab.domain.datetime.SystemPlatformClock(clock)
    override fun nowInstant(): Instant = delegate.nowInstant()
    override fun today(): java.time.LocalDate = delegate.today()
    override fun currentDateTime(): java.time.LocalDateTime = delegate.currentDateTime()
    override fun zoneId(): ZoneId = delegate.zoneId()
}

/**
 * Unified container for all platform-specific services.
 */
data class PlatformServices(
    val audioService: PlatformAudioService,
    val pdfExporter: PlatformPdfExporter,
    val shareManager: PlatformShareManager,
    val clipboardManager: PlatformClipboardManager,
    val logger: PlatformLogger = DefaultPlatformLogger(),
    val clock: PlatformClock = DefaultPlatformClock()
)

val LocalPlatformServices: ProvidableCompositionLocal<PlatformServices> = staticCompositionLocalOf {
    error("LocalPlatformServices not provided!")
}
