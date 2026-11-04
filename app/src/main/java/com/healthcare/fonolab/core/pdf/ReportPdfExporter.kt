package com.healthcare.fonolab.core.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.healthcare.fonolab.domain.patient.AgeCalculator
import com.healthcare.fonolab.domain.patient.Patient
import com.healthcare.fonolab.domain.performance.GameAttempt
import com.healthcare.fonolab.domain.performance.PerformanceCalculator
import com.healthcare.fonolab.domain.session.TherapySession
import java.io.OutputStream
import java.time.LocalDate

object ReportPdfExporter {

    fun generateReport(
        context: Context,
        patient: Patient,
        sessions: List<TherapySession>,
        attempts: List<GameAttempt>,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }

        val age = AgeCalculator.ageOn(patient.birthDate, LocalDate.now())

        var y = 50f

        // Header
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("FonoLab — Relatório de Evolução Terapêutica", 40f, y, paint)
        y += 25f

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Documento emitido para suporte clínico e acompanhamento profissional", 40f, y, paint)
        y += 30f

        // Patient Info
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Identificação do Paciente", 40f, y, paint)
        y += 18f

        paint.isFakeBoldText = false
        canvas.drawText("Nome: ${patient.fullName} (Código: ${patient.internalCode})", 40f, y, paint)
        y += 16f
        canvas.drawText("Idade: ${age.years} anos e ${age.months} meses | Status: ${patient.status.name}", 40f, y, paint)
        y += 16f
        canvas.drawText("Responsável: ${patient.guardianName ?: "Não informado"}", 40f, y, paint)
        y += 30f

        // Performance Summary
        paint.isFakeBoldText = true
        canvas.drawText("Resumo de Atividades e Tarefas Objetivas", 40f, y, paint)
        y += 18f

        val summary = PerformanceCalculator.calculate(attempts)
        paint.isFakeBoldText = false
        canvas.drawText("Total de tentativas registradas: ${summary.totalAttempts}", 40f, y, paint)
        y += 16f
        if (summary.autoAccuracyPercentage != null) {
            canvas.drawText("Acurácia em tarefas de discriminação objetiva: ${String.format("%.1f", summary.autoAccuracyPercentage)}% (${summary.autoScoredCorrect}/${summary.autoScoredCount})", 40f, y, paint)
            y += 16f
        }
        canvas.drawText("Produções avaliadas pelo terapeuta: ${summary.clinicianRatedCount}", 40f, y, paint)
        y += 16f
        canvas.drawText("  • Independentes: ${summary.independentCorrectCount} | Com Pista: ${summary.cuedCorrectCount} | Incorretas: ${summary.incorrectCount}", 40f, y, paint)
        y += 30f

        // Sessions list
        paint.isFakeBoldText = true
        canvas.drawText("Histórico Recente de Sessões (${sessions.size})", 40f, y, paint)
        y += 18f

        paint.isFakeBoldText = false
        sessions.take(6).forEach { session ->
            canvas.drawText("• Sessão #${session.id} - ${session.status.name} (Duração: ${session.durationSeconds / 60} min)", 40f, y, paint)
            y += 16f
            if (session.clinicalNotes.isNotBlank()) {
                canvas.drawText("   Obs: ${session.clinicalNotes.take(60)}...", 40f, y, paint)
                y += 16f
            }
        }

        y += 30f
        // Clinical Disclaimer
        paint.textSize = 9f
        paint.color = Color.GRAY
        canvas.drawText("Atenção: Os dados aqui apresentados refletem o desempenho em atividades do aplicativo.", 40f, y, paint)
        y += 12f
        canvas.drawText("Eles apoiam o raciocínio terapêutico e não constituem diagnóstico ou conclusão clínica automatizada.", 40f, y, paint)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }
}
