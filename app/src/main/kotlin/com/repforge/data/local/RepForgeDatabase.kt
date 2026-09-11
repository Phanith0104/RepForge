package com.repforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.dao.UserDao
import com.repforge.data.local.dao.WorkoutDao
import com.repforge.data.local.entities.StepEntity
import com.repforge.data.local.entities.UserEntity
import com.repforge.data.local.entities.WorkoutEntity

@Database(entities = [StepEntity::class, WorkoutEntity::class, UserEntity::class], version = 8, exportSchema = false)
abstract class RepForgeDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun userDao(): UserDao
}
