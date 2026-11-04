package com.healthcare.fonolab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthcare.fonolab.data.local.converter.DateTimeConverters
import com.healthcare.fonolab.data.local.dao.ClinicalGoalDao
import com.healthcare.fonolab.data.local.dao.PatientDao
import com.healthcare.fonolab.data.local.dao.RewardDao
import com.healthcare.fonolab.data.local.dao.TherapySessionDao
import com.healthcare.fonolab.data.local.entity.ClinicalGoalEntity
import com.healthcare.fonolab.data.local.entity.GameAttemptEntity
import com.healthcare.fonolab.data.local.entity.GameFavoriteEntity
import com.healthcare.fonolab.data.local.entity.PatientEntity
import com.healthcare.fonolab.data.local.entity.RewardProgressEntity
import com.healthcare.fonolab.data.local.entity.SessionActivityEntity
import com.healthcare.fonolab.data.local.entity.SessionGoalCrossRef
import com.healthcare.fonolab.data.local.entity.TherapySessionEntity
import com.healthcare.fonolab.data.local.entity.UnlockedCollectibleEntity

@Database(
    entities = [
        PatientEntity::class,
        ClinicalGoalEntity::class,
        TherapySessionEntity::class,
        SessionGoalCrossRef::class,
        SessionActivityEntity::class,
        GameAttemptEntity::class,
        RewardProgressEntity::class,
        UnlockedCollectibleEntity::class,
        GameFavoriteEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class FonoLabDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun clinicalGoalDao(): ClinicalGoalDao
    abstract fun therapySessionDao(): TherapySessionDao
    abstract fun rewardDao(): RewardDao
}
