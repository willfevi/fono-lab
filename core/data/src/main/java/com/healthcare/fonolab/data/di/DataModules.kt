package com.healthcare.fonolab.data.di

import android.content.Context
import androidx.room.Room
import com.healthcare.fonolab.data.local.FonoLabDatabase
import com.healthcare.fonolab.data.local.dao.ClinicalGoalDao
import com.healthcare.fonolab.data.local.dao.ClinicalPlatformDao
import com.healthcare.fonolab.data.local.dao.PatientDao
import com.healthcare.fonolab.data.local.dao.RewardDao
import com.healthcare.fonolab.data.local.dao.TherapySessionDao
import com.healthcare.fonolab.data.repository.ClinicalGoalRepositoryImpl
import com.healthcare.fonolab.data.repository.ClinicalPlatformRepositoryImpl
import com.healthcare.fonolab.data.repository.PatientRepositoryImpl
import com.healthcare.fonolab.data.repository.TherapySessionRepositoryImpl
import com.healthcare.fonolab.domain.clinical.ClinicalPlatformRepository
import com.healthcare.fonolab.domain.datetime.PlatformClock
import com.healthcare.fonolab.domain.datetime.SystemPlatformClock
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FonoLabDatabase {
        return Room.databaseBuilder(
            context,
            FonoLabDatabase::class.java,
            "fonolab.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun providePatientDao(db: FonoLabDatabase): PatientDao = db.patientDao()

    @Provides
    fun provideClinicalGoalDao(db: FonoLabDatabase): ClinicalGoalDao = db.clinicalGoalDao()

    @Provides
    fun provideTherapySessionDao(db: FonoLabDatabase): TherapySessionDao = db.therapySessionDao()

    @Provides
    fun provideRewardDao(db: FonoLabDatabase): RewardDao = db.rewardDao()

    @Provides
    fun provideClinicalPlatformDao(db: FonoLabDatabase): ClinicalPlatformDao = db.clinicalPlatformDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @Singleton
    fun provideRandom(): Random = Random.Default
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPatientRepository(impl: PatientRepositoryImpl): PatientRepository

    @Binds
    @Singleton
    abstract fun bindClinicalGoalRepository(impl: ClinicalGoalRepositoryImpl): ClinicalGoalRepository

    @Binds
    @Singleton
    abstract fun bindTherapySessionRepository(impl: TherapySessionRepositoryImpl): TherapySessionRepository

    @Binds
    @Singleton
    abstract fun bindClinicalPlatformRepository(impl: ClinicalPlatformRepositoryImpl): ClinicalPlatformRepository

    @Binds
    @Singleton
    abstract fun bindPlatformClock(impl: SystemPlatformClock): PlatformClock
}
