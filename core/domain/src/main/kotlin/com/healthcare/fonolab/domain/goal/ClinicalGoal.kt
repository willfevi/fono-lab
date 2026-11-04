package com.healthcare.fonolab.domain.goal

import com.healthcare.fonolab.domain.common.DifficultyLevel
import com.healthcare.fonolab.domain.common.PhonemePosition
import java.time.Instant
import java.time.LocalDate

enum class GoalStatus { ACTIVE, COMPLETED, PAUSED }

enum class ClinicalArea(val displayName: String) {
    SPEECH_AND_PHONOLOGY("Fala e Fonologia"),
    RECEPTIVE_LANGUAGE("Linguagem Receptiva"),
    EXPRESSIVE_LANGUAGE("Linguagem Expressiva"),
    VOCABULARY_AND_SEMANTICS("Vocabulário e Semântica"),
    MORPHOSYNTAX("Morfossintaxe"),
    PRAGMATICS("Pragmática e Comunicação Social"),
    PHONOLOGICAL_AWARENESS("Consciência Fonológica"),
    NARRATIVE_AND_SEQUENCING("Narrativa e Sequenciamento"),
    MEMORY_AND_ATTENTION("Memória e Atenção"),
    OROFACIAL_MYOFUNCTIONAL("Motricidade Orofacial"),
    VOICE("Voz"),
    OTHER("Outra Área Personalizada")
}

data class ClinicalGoal(
    val id: Long,
    val patientId: Long,
    val clinicalArea: ClinicalArea,
    val customAreaName: String?,
    val description: String,
    val targetSkill: String,
    val targetStimulus: String?,
    val phonemePosition: PhonemePosition?,
    val initialDifficulty: DifficultyLevel,
    val targetAccuracyPercentage: Int?,
    val masteryCriterionDescription: String?,
    val clinicalNotes: String,
    val status: GoalStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class ClinicalGoalDraft(
    val clinicalArea: ClinicalArea = ClinicalArea.SPEECH_AND_PHONOLOGY,
    val customAreaName: String = "",
    val description: String = "",
    val targetSkill: String = "",
    val targetStimulus: String = "",
    val phonemePosition: PhonemePosition? = null,
    val initialDifficulty: DifficultyLevel = DifficultyLevel.EASY,
    val targetAccuracyPercentage: Int? = null,
    val masteryCriterionDescription: String = "",
    val clinicalNotes: String = ""
)

data class ValidGoalData(
    val clinicalArea: ClinicalArea,
    val customAreaName: String?,
    val description: String,
    val targetSkill: String,
    val targetStimulus: String?,
    val phonemePosition: PhonemePosition?,
    val initialDifficulty: DifficultyLevel,
    val targetAccuracyPercentage: Int?,
    val masteryCriterionDescription: String?,
    val clinicalNotes: String
)
