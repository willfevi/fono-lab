package com.healthcare.fonolab.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "reward_progress",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RewardProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "experience_points")
    val experiencePoints: Int,

    @ColumnInfo(name = "level")
    val level: Int,

    @ColumnInfo(name = "active_theme_id")
    val activeThemeId: String,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)

@Entity(
    tableName = "unlocked_collectibles",
    primaryKeys = ["patient_id", "collectible_id"],
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patient_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UnlockedCollectibleEntity(
    @ColumnInfo(name = "patient_id")
    val patientId: Long,

    @ColumnInfo(name = "collectible_id")
    val collectibleId: String,

    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Instant
)

@Entity(
    tableName = "game_favorites",
    primaryKeys = ["game_id"]
)
data class GameFavoriteEntity(
    @ColumnInfo(name = "game_id")
    val gameId: String,

    @ColumnInfo(name = "added_at")
    val addedAt: Instant
)
