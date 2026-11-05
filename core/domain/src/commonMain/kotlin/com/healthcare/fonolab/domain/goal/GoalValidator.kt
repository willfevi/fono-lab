package com.healthcare.fonolab.domain.goal

import com.healthcare.fonolab.domain.common.DomainError
import com.healthcare.fonolab.domain.common.DomainResult
import com.healthcare.fonolab.domain.common.ValidationIssue

enum class GoalField { DESCRIPTION, TARGET_SKILL, CUSTOM_AREA_NAME, ACCURACY, NOTES }

enum class GoalValidationError(val field: GoalField) : ValidationIssue {
    DESCRIPTION_REQUIRED(GoalField.DESCRIPTION),
    DESCRIPTION_TOO_LONG(GoalField.DESCRIPTION),
    TARGET_SKILL_REQUIRED(GoalField.TARGET_SKILL),
    TARGET_SKILL_TOO_LONG(GoalField.TARGET_SKILL),
    CUSTOM_AREA_NAME_REQUIRED(GoalField.CUSTOM_AREA_NAME),
    ACCURACY_OUT_OF_RANGE(GoalField.ACCURACY),
    NOTES_TOO_LONG(GoalField.NOTES)
}

class GoalValidator() {

    fun validate(draft: ClinicalGoalDraft): DomainResult<ValidGoalData> {
        val errors = mutableListOf<GoalValidationError>()

        val desc = draft.description.trim()
        if (desc.isEmpty()) errors += GoalValidationError.DESCRIPTION_REQUIRED
        else if (desc.length > 500) errors += GoalValidationError.DESCRIPTION_TOO_LONG

        val skill = draft.targetSkill.trim()
        if (skill.isEmpty()) errors += GoalValidationError.TARGET_SKILL_REQUIRED
        else if (skill.length > 100) errors += GoalValidationError.TARGET_SKILL_TOO_LONG

        val customArea = draft.customAreaName.trim().takeIf { it.isNotEmpty() }
        if (draft.clinicalArea == ClinicalArea.OTHER && customArea == null) {
            errors += GoalValidationError.CUSTOM_AREA_NAME_REQUIRED
        }

        val accuracy = draft.targetAccuracyPercentage
        if (accuracy != null && (accuracy < 1 || accuracy > 100)) {
            errors += GoalValidationError.ACCURACY_OUT_OF_RANGE
        }

        val notes = draft.clinicalNotes.trim()
        if (notes.length > 2000) errors += GoalValidationError.NOTES_TOO_LONG

        if (errors.isNotEmpty()) {
            return DomainResult.Failure(DomainError.Validation(errors))
        }

        return DomainResult.Success(
            ValidGoalData(
                clinicalArea = draft.clinicalArea,
                customAreaName = customArea,
                description = desc,
                targetSkill = skill,
                targetStimulus = draft.targetStimulus.trim().takeIf { it.isNotEmpty() },
                phonemePosition = draft.phonemePosition,
                initialDifficulty = draft.initialDifficulty,
                targetAccuracyPercentage = accuracy,
                masteryCriterionDescription = draft.masteryCriterionDescription.trim().takeIf { it.isNotEmpty() },
                clinicalNotes = notes
            )
        )
    }
}
