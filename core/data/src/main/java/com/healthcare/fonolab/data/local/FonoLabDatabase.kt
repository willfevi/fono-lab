package com.healthcare.fonolab.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.healthcare.fonolab.data.local.converter.DateTimeConverters
import com.healthcare.fonolab.data.local.dao.ClinicalGoalDao
import com.healthcare.fonolab.data.local.dao.ClinicalPlatformDao
import com.healthcare.fonolab.data.local.dao.PatientDao
import com.healthcare.fonolab.data.local.dao.RewardDao
import com.healthcare.fonolab.data.local.dao.TherapySessionDao
import com.healthcare.fonolab.data.local.entity.*

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
        GameFavoriteEntity::class,
        // Version 2 Extended Clinical & Administrative Platform Entities
        AnamnesisEntity::class,
        AnamnesisVersionEntity::class,
        ClinicalAssessmentEntity::class,
        PhonemeInventoryEntity::class,
        TherapyPlanEntity::class,
        GeneralizationProbeEntity::class,
        ClinicalAppointmentEntity::class,
        WaitlistAndReferralEntity::class,
        HomePracticeTaskEntity::class,
        ClinicalDocumentEntity::class,
        BillingRecordEntity::class,
        ClinicalTaskEntity::class,
        CustomStimulusEntity::class,
        AuditEventEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class FonoLabDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun clinicalGoalDao(): ClinicalGoalDao
    abstract fun therapySessionDao(): TherapySessionDao
    abstract fun rewardDao(): RewardDao
    abstract fun clinicalPlatformDao(): ClinicalPlatformDao
}
