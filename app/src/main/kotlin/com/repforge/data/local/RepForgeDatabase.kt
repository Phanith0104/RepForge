package com.repforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.dao.WorkoutDao
import com.repforge.data.local.entities.StepEntity
import com.repforge.data.local.entities.WorkoutEntity

@Database(entities = [StepEntity::class, WorkoutEntity::class], version = 1, exportSchema = false)
abstract class RepForgeDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
}
