package com.healthcare.fonolab.domain.reward

import java.time.Instant

data class RewardProgress(
    val patientId: Long,
    val experiencePoints: Int,
    val level: Int,
    val activeThemeId: String,
    val updatedAt: Instant
)

data class Collectible(
    val id: String,
    val name: String,
    val description: String,
    val iconEmoji: String,
    val category: String,
    val requiredXp: Int
)

data class UnlockedCollectible(
    val patientId: Long,
    val collectibleId: String,
    val unlockedAt: Instant
)

object RewardCalculator {
    const val XP_PER_ACTIVITY_COMPLETION = 50
    const val XP_PER_ATTEMPT_PARTICIPATION = 5
    const val XP_BONUS_EFFORT = 20

    fun calculateXpGain(completedRounds: Int, wasCompleted: Boolean): Int {
        var xp = completedRounds * XP_PER_ATTEMPT_PARTICIPATION
        if (wasCompleted) xp += XP_PER_ACTIVITY_COMPLETION
        return xp
    }

    fun calculateLevel(totalXp: Int): Int {
        if (totalXp <= 0) return 1
        return (totalXp / 100) + 1
    }
}
