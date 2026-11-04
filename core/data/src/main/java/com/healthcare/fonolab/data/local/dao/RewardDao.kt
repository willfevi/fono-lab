package com.healthcare.fonolab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthcare.fonolab.data.local.entity.GameFavoriteEntity
import com.healthcare.fonolab.data.local.entity.RewardProgressEntity
import com.healthcare.fonolab.data.local.entity.UnlockedCollectibleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM reward_progress WHERE patient_id = :patientId")
    fun observeProgress(patientId: Long): Flow<RewardProgressEntity?>

    @Query("SELECT * FROM reward_progress WHERE patient_id = :patientId")
    suspend fun getProgress(patientId: Long): RewardProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: RewardProgressEntity)

    @Query("SELECT * FROM unlocked_collectibles WHERE patient_id = :patientId")
    fun observeCollectibles(patientId: Long): Flow<List<UnlockedCollectibleEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollectible(entity: UnlockedCollectibleEntity)

    @Query("SELECT game_id FROM game_favorites")
    fun observeFavorites(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(entity: GameFavoriteEntity)

    @Query("DELETE FROM game_favorites WHERE game_id = :gameId")
    suspend fun removeFavorite(gameId: String)
}
