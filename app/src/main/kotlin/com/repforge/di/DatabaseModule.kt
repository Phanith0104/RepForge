package com.repforge.di

import android.content.Context
import androidx.room.Room
import com.repforge.data.local.RepForgeDatabase
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.dao.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RepForgeDatabase {
        return Room.databaseBuilder(
            context,
            RepForgeDatabase::class.java,
            "repforge_db"
        ).build()
    }

    @Provides
    fun provideStepDao(database: RepForgeDatabase): StepDao {
        return database.stepDao()
    }

    @Provides
    fun provideWorkoutDao(database: RepForgeDatabase): WorkoutDao {
        return database.workoutDao()
    }
}
