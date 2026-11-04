package com.healthcare.fonolab.core.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.healthcare.fonolab.domain.clinical.AnamnesisRecord
import com.healthcare.fonolab.domain.clinical.ClinicalDocument
import com.healthcare.fonolab.domain.clinical.ProfessionalProfile
import com.healthcare.fonolab.domain.patient.AgeCalculator
import com.healthcare.fonolab.domain.patient.Patient
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Generates real, multi-page A4 PDF documents for the 12-Section Speech-Language Pathology Anamnesis
 * and Clinical Documents (SOAP notes, School Reports, Referrals, Declarations).
 */
object AnamnesisPdfExporter {

    private val brDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun exportAnamnesisToFile(
        context: Context,
        patient: Patient,
        anamnesis: AnamnesisRecord,
        profile: ProfessionalProfile = ProfessionalProfile()
    ): File {
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val safeCode = patient.internalCode.replace(Regex("[^A-Za-z0-9\\-]"), "_")
        val file = File(dir, "Anamnese_${safeCode}_v${anamnesis.versionNumber}.pdf")
        FileOutputStream(file).use { out ->
            generateAnamnesisPdf(patient, anamnesis, profile, out)
        }
        return file
    }

    fun generateAnamnesisPdf(
        patient: Patient,
        anamnesis: AnamnesisRecord,
        profile: ProfessionalProfile,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 40f
        val maxContentY = 770f

        var pageNumber = 1
        var currentPage = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = currentPage.canvas
        var y = 48f

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }

        fun drawFooter(pNum: Int) {
            val footerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.DKGRAY
                textSize = 9f
            }
            canvas.drawLine(margin, 795f, pageWidth - margin, 795f, footerPaint)
            canvas.drawText(
                "${profile.clinicName} • ${profile.fullName} (${profile.crfaRegistration}) — Página $pNum",
                margin,
                812f,
                footerPaint
            )
        }

        fun ensureSpace(requiredHeight: Float) {
            if (y + requiredHeight > maxContentY) {
                drawFooter(pageNumber)
                document.finishPage(currentPage)
                pageNumber++
                currentPage = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
                canvas = currentPage.canvas
                y = 50f
            }
        }

        fun drawWrappedText(text: String, x: Float, maxWidth: Float, textSize: Float, bold: Boolean = false, color: Int = Color.BLACK) {
            paint.textSize = textSize
            paint.isFakeBoldText = bold
            paint.color = color
            val words = text.replace("\n", " \n ").split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                if (word == "\n") {
                    ensureSpace(textSize + 5f)
                    canvas.drawText(currentLine.toString().trimEnd(), x, y, paint)
                    y += textSize + 5f
                    currentLine = StringBuilder()
                    continue
                }
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) > maxWidth) {
                    ensureSpace(textSize + 5f)
                    canvas.drawText(currentLine.toString().trimEnd(), x, y, paint)
                    y += textSize + 5f
                    currentLine = StringBuilder(word)
                } else {
                    currentLine = StringBuilder(testLine)
                }
            }
            if (currentLine.isNotEmpty()) {
                ensureSpace(textSize + 5f)
                canvas.drawText(currentLine.toString().trimEnd(), x, y, paint)
                y += textSize + 5f
            }
        }

        // 1. Institutional Header
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.color = Color.rgb(19, 21, 27)
        canvas.drawText("ANAMNESE FONOAUDIOLÓGICA COMPLETA", margin, y, paint)
        y += 18f

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("${profile.clinicName} • ${profile.fullName} (${profile.crfaRegistration})", margin, y, paint)
        y += 14f
        canvas.drawText("Data da Anamnese: ${anamnesis.anamnesisDate.format(brDateFormatter)} • Versão: v${anamnesis.versionNumber} (${anamnesis.status.displayName})", margin, y, paint)
        y += 12f
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 18f

        // 2. Patient Identification Box
        val age = AgeCalculator.ageOn(patient.birthDate, LocalDate.now())
        drawWrappedText("IDENTIFICAÇÃO DO PACIENTE", margin, 515f, 12f, bold = true)
        drawWrappedText(
            "Nome: ${patient.fullName}${patient.socialName?.let { " (Nome social: $it)" } ?: ""} | Prontuário: ${patient.internalCode}",
            margin, 515f, 10f
        )
        drawWrappedText(
            "Data de Nascimento: ${patient.birthDate.format(brDateFormatter)} (${age.years} anos e ${age.months} meses) | Status: ${patient.status.displayName}",
            margin, 515f, 10f
        )
        drawWrappedText(
            "Responsável: ${patient.guardianName ?: "Não informado"} (${patient.guardianKinship ?: "Responsável"}) | Contato: ${patient.guardianContact ?: patient.phone ?: "Não informado"}",
            margin, 515f, 10f
        )
        drawWrappedText(
            "Escola / Escolaridade: ${patient.school ?: "Não informada"} (${patient.educationLevel ?: "-"}) | Idiomas: ${patient.homeLanguages}",
            margin, 515f, 10f
        )
        drawWrappedText(
            "Informante da Anamnese: ${anamnesis.informantName.ifBlank { patient.guardianName ?: "Responsável" }} (${anamnesis.informantKinship}) | Encaminhamento: ${anamnesis.referredBy}",
            margin, 515f, 10f
        )
        y += 8f

        // 3. All 12 Structured Sections
        anamnesis.sections.forEach { sec ->
            ensureSpace(55f)
            drawWrappedText(sec.sectionType.title, margin, 515f, 11f, bold = true, color = Color.rgb(30, 41, 59))
            if (sec.quickStatus.isNotBlank()) {
                drawWrappedText("Classificação rápida: ${sec.quickStatus}", margin + 8f, 505f, 9.5f, bold = true, color = Color.DKGRAY)
            }
            val familyText = sec.familyReport.ifBlank { "Não informado / Sem particularidades relatadas." }
            drawWrappedText("Relato Familiar: $familyText", margin + 8f, 505f, 10f)
            if (sec.clinicianObservation.isNotBlank()) {
                drawWrappedText("Observação Clínica Profissional: ${sec.clinicianObservation}", margin + 8f, 505f, 10f, color = Color.rgb(55, 65, 81))
            }
            y += 6f
        }

        // 4. General Summary
        if (anamnesis.generalSummary.isNotBlank()) {
            ensureSpace(50f)
            drawWrappedText("SÍNTESE E CONDUTA FONOAUDIOLÓGICA INICIAL", margin, 515f, 11f, bold = true)
            drawWrappedText(anamnesis.generalSummary, margin + 8f, 505f, 10f)
            y += 10f
        }

        // 5. Signature Space
        ensureSpace(75f)
        y += 28f
        canvas.drawLine(margin + 120f, y, pageWidth - margin - 120f, y, paint)
        y += 14f
        drawWrappedText("${profile.fullName} — ${profile.crfaRegistration}", margin + 140f, 300f, 10f, bold = true)
        drawWrappedText("Assinatura e Carimbo do(a) Fonoaudiólogo(a)", margin + 150f, 300f, 9f, color = Color.DKGRAY)

        drawFooter(pageNumber)
        document.finishPage(currentPage)
        document.writeTo(outputStream)
        document.close()
    }

    fun exportClinicalDocumentToFile(
        context: Context,
        patient: Patient,
        doc: ClinicalDocument
    ): File {
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val file = File(dir, "Documento_${patient.internalCode}_${doc.id}.pdf")
        FileOutputStream(file).use { out ->
            val pdf = PdfDocument()
            val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
            val canvas = page.canvas
            val paint = Paint().apply { isAntiAlias = true; color = Color.BLACK }
            var y = 55f
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText(doc.title.uppercase(), 40f, y, paint)
            y += 22f
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("Paciente: ${patient.fullName} (Prontuário: ${patient.internalCode}) • Data: ${doc.documentDate.format(brDateFormatter)}", 40f, y, paint)
            y += 16f
            canvas.drawText("Profissional: ${doc.professionalName} (${doc.professionalCrfa})", 40f, y, paint)
            y += 24f

            paint.textSize = 11f
            val lines = doc.bodyContent.chunked(82)
            lines.forEach { line ->
                if (y < 740f) {
                    canvas.drawText(line, 40f, y, paint)
                    y += 16f
                }
            }
            if (doc.addendumNotes.isNotBlank() && y < 720f) {
                y += 12f
                paint.isFakeBoldText = true
                canvas.drawText("Adendos Clínicos:", 40f, y, paint)
                y += 16f
                paint.isFakeBoldText = false
                doc.addendumNotes.chunked(82).forEach { line ->
                    if (y < 760f) {
                        canvas.drawText(line, 40f, y, paint)
                        y += 15f
                    }
                }
            }
            y = 770f
            canvas.drawLine(160f, y, 435f, y, paint)
            y += 15f
            canvas.drawText("${doc.professionalName} — ${doc.professionalCrfa}", 190f, y, paint)
            pdf.finishPage(page)
            pdf.writeTo(out)
            pdf.close()
        }
        return file
    }
}
