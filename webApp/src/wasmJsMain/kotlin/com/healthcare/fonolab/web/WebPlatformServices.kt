package com.healthcare.fonolab.web

import com.healthcare.fonolab.core.platform.*
import java.io.File
import java.time.LocalDate

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(filename, text) => { const blob = new Blob([text], {type: 'text/plain;charset=utf-8'}); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = filename; a.click(); URL.revokeObjectURL(url); }")
private external fun downloadTextFile(filename: String, text: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(text) => { if (navigator.clipboard && navigator.clipboard.writeText) { navigator.clipboard.writeText(text).catch(err => console.error(err)); } }")
private external fun copyToClipboardJs(text: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("(text, lang) => { if ('speechSynthesis' in window) { window.speechSynthesis.cancel(); const u = new SpeechSynthesisUtterance(text); u.lang = lang; window.speechSynthesis.speak(u); } }")
private external fun speakJs(text: String, lang: String)

@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@JsFun("() => { if ('speechSynthesis' in window) { window.speechSynthesis.cancel(); } }")
private external fun stopSpeechJs()

class WebAudioService : PlatformAudioService {
    override fun speak(text: String, language: String, onDone: () -> Unit) {
        println("[WebAudio] Fala sintetizada ($language): $text")
        speakJs(text, language)
        onDone()
    }

    override fun playAuditoryCue(cueName: String) {
        println("[WebAudio] Efeito auditivo: $cueName")
    }

    override fun stop() {
        stopSpeechJs()
    }
}

class WebPdfExporter : PlatformPdfExporter {
    override fun exportAnamnesis(
        patientName: String,
        patientDoc: String,
        sections: Map<String, List<Pair<String, String>>>,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val cleanName = patientName.replace(" ", "_")
            val fileName = "Anamnese_${cleanName}_v1.txt"
            val content = buildString {
                appendLine("============================================================")
                appendLine("           FONOLAB — PRONTUÁRIO CLÍNICO DE ANAMNESE         ")
                appendLine("============================================================")
                appendLine()
                appendLine("Paciente: $patientName")
                appendLine("Código/Doc: $patientDoc")
                appendLine("Data de Emissão: ${LocalDate.now()}")
                appendLine()
                sections.forEach { (sectionTitle, questions) ->
                    appendLine("------------------------------------------------------------")
                    appendLine("SEÇÃO: ${sectionTitle.uppercase()}")
                    appendLine("------------------------------------------------------------")
                    questions.forEach { (q, a) ->
                        appendLine("• $q:\n  -> $a")
                        appendLine()
                    }
                }
                appendLine("============================================================")
                appendLine("Documento gerado automaticamente pelo FonoLab Web.")
            }
            downloadTextFile(fileName, content)
            val file = File(fileName)
            onSuccess(file)
        } catch (t: Throwable) {
            onError(t)
        }
    }

    override fun exportReport(
        patientName: String,
        reportTitle: String,
        summaryContent: String,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val cleanName = patientName.replace(" ", "_")
            val fileName = "Relatorio_${cleanName}.txt"
            val content = buildString {
                appendLine("============================================================")
                appendLine("           FONOLAB — RELATÓRIO FONOAUDIOLÓGICO            ")
                appendLine("============================================================")
                appendLine()
                appendLine("Paciente: $patientName")
                appendLine("Título: $reportTitle")
                appendLine("Data: ${LocalDate.now()}")
                appendLine()
                appendLine(summaryContent)
                appendLine()
                appendLine("============================================================")
            }
            downloadTextFile(fileName, content)
            val file = File(fileName)
            onSuccess(file)
        } catch (t: Throwable) {
            onError(t)
        }
    }
}

class WebShareManager : PlatformShareManager {
    override fun shareText(title: String, text: String) {
        copyToClipboardJs(text)
        println("[WebShare] Texto copiado para área de transferência: $title")
    }

    override fun shareFile(file: File, title: String, mimeType: String) {
        println("[WebShare] Arquivo compartilhado: ${file.name}")
    }
}

class WebClipboardManager : PlatformClipboardManager {
    override fun copyToClipboard(text: String) {
        copyToClipboardJs(text)
    }

    override fun getClipboardText(): String? = null
}

fun createWebPlatformServices(): PlatformServices {
    return PlatformServices(
        audioService = WebAudioService(),
        pdfExporter = WebPdfExporter(),
        shareManager = WebShareManager(),
        clipboardManager = WebClipboardManager(),
        logger = DefaultPlatformLogger(),
        clock = DefaultPlatformClock()
    )
}
