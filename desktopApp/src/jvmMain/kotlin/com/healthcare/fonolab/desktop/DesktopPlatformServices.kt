package com.healthcare.fonolab.desktop

import com.healthcare.fonolab.core.platform.*
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.io.File
import java.io.FileWriter

class DesktopAudioService : PlatformAudioService {
    override fun speak(text: String, language: String, onDone: () -> Unit) {
        println("[DesktopAudio] Fala sintetizada ($language): $text")
        // Trigger system audio beep or native speech synthesis if available
        Toolkit.getDefaultToolkit().beep()
        onDone()
    }

    override fun playAuditoryCue(cueName: String) {
        println("[DesktopAudio] Som clínico executado: $cueName")
        Toolkit.getDefaultToolkit().beep()
    }

    override fun stop() {
        // Stop any active audio stream
    }
}

class DesktopPdfExporter : PlatformPdfExporter {
    override fun exportAnamnesis(
        patientName: String,
        patientDoc: String,
        sections: Map<String, List<Pair<String, String>>>,
        onSuccess: (File) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            val userHome = System.getProperty("user.home") ?: "."
            val exportDir = File(userHome, "Documents/FonoLab/Anamneses").apply { mkdirs() }
            val cleanName = patientName.replace(" ", "_")
            val targetFile = File(exportDir, "Anamnese_${cleanName}_v1.txt")

            FileWriter(targetFile).use { writer ->
                writer.write("============================================================\n")
                writer.write("           FONOLAB — PRONTUÁRIO CLÍNICO DE ANAMNESE         \n")
                writer.write("============================================================\n\n")
                writer.write("Paciente: $patientName\n")
                writer.write("Código/Doc: $patientDoc\n")
                writer.write("Data de Emissão: ${java.time.LocalDate.now()}\n\n")

                sections.forEach { (sectionTitle, questions) ->
                    writer.write("------------------------------------------------------------\n")
                    writer.write("SEÇÃO: ${sectionTitle.uppercase()}\n")
                    writer.write("------------------------------------------------------------\n")
                    questions.forEach { (q, a) ->
                        writer.write("• $q:\n  -> $a\n\n")
                    }
                }
                writer.write("============================================================\n")
                writer.write("Documento gerado automaticamente pelo FonoLab Desktop.\n")
            }
            onSuccess(targetFile)
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
            val userHome = System.getProperty("user.home") ?: "."
            val exportDir = File(userHome, "Documents/FonoLab/Relatorios").apply { mkdirs() }
            val cleanName = patientName.replace(" ", "_")
            val targetFile = File(exportDir, "Relatorio_${cleanName}.txt")

            FileWriter(targetFile).use { writer ->
                writer.write("============================================================\n")
                writer.write("           FONOLAB — RELATÓRIO FONOAUDIOLÓGICO            \n")
                writer.write("============================================================\n\n")
                writer.write("Paciente: $patientName\n")
                writer.write("Título: $reportTitle\n")
                writer.write("Data: ${java.time.LocalDate.now()}\n\n")
                writer.write(summaryContent)
                writer.write("\n\n============================================================\n")
            }
            onSuccess(targetFile)
        } catch (t: Throwable) {
            onError(t)
        }
    }
}

class DesktopShareManager : PlatformShareManager {
    override fun shareText(title: String, text: String) {
        DesktopClipboardManager().copyToClipboard(text)
        println("[DesktopShare] Texto copiado para a área de transferência: $title")
    }

    override fun shareFile(file: File, title: String, mimeType: String) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(file.parentFile ?: file)
            }
        } catch (e: Exception) {
            println("[DesktopShare] Não foi possível abrir o explorador: ${e.message}")
        }
    }
}

class DesktopClipboardManager : PlatformClipboardManager {
    override fun copyToClipboard(text: String) {
        try {
            val selection = StringSelection(text)
            Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        } catch (e: Exception) {
            System.err.println("[DesktopClipboard] Erro ao copiar: ${e.message}")
        }
    }

    override fun getClipboardText(): String? {
        return try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.getData(DataFlavor.stringFlavor) as? String
        } catch (e: Exception) {
            null
        }
    }
}

fun createDesktopPlatformServices(): PlatformServices {
    return PlatformServices(
        audioService = DesktopAudioService(),
        pdfExporter = DesktopPdfExporter(),
        shareManager = DesktopShareManager(),
        clipboardManager = DesktopClipboardManager(),
        logger = DefaultPlatformLogger(),
        clock = DefaultPlatformClock()
    )
}
