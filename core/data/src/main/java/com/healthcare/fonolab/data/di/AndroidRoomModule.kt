package com.healthcare.fonolab.data.di

import android.content.Context
import androidx.room.Room
import com.healthcare.fonolab.data.local.FonoLabDatabase
import com.healthcare.fonolab.data.repository.ClinicalGoalRepositoryImpl
import com.healthcare.fonolab.data.repository.ClinicalPlatformRepositoryImpl
import com.healthcare.fonolab.data.repository.PatientRepositoryImpl
import com.healthcare.fonolab.data.repository.TherapySessionRepositoryImpl
import com.healthcare.fonolab.domain.clinical.ClinicalPlatformRepository
import com.healthcare.fonolab.domain.goal.ClinicalGoalRepository
import com.healthcare.fonolab.domain.patient.PatientRepository
import com.healthcare.fonolab.domain.session.TherapySessionRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.dsl.onClose
import java.time.Clock

// Keep Room opt-in until its migration and wiring to the clinical UI are validated.
private val room = named("room")

fun androidRoomModule(context: Context) = module {
    single {
        Room.databaseBuilder(context, FonoLabDatabase::class.java, "fonolab.db")
            .fallbackToDestructiveMigration(dropAllTables = false)
            .build()
    } onClose { it?.close() }

    single { get<FonoLabDatabase>().patientDao() }
    single { get<FonoLabDatabase>().clinicalGoalDao() }
    single { get<FonoLabDatabase>().therapySessionDao() }
    single { get<FonoLabDatabase>().rewardDao() }
    single { get<FonoLabDatabase>().clinicalPlatformDao() }

    single<PatientRepository>(room) { PatientRepositoryImpl(get()) }
    single<ClinicalGoalRepository>(room) { ClinicalGoalRepositoryImpl(get()) }
    single<TherapySessionRepository>(room) { TherapySessionRepositoryImpl(get(), get()) }
    single<ClinicalPlatformRepository>(room) {
        ClinicalPlatformRepositoryImpl(get(), Clock.systemDefaultZone())
    }
}
