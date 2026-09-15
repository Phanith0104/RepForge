package com.repforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.dao.UserDao
import com.repforge.data.local.dao.WorkoutDao
import com.repforge.data.local.dao.CustomExerciseDao
import com.repforge.data.local.dao.WorkoutTemplateDao
import com.repforge.data.local.entities.*

@Database(
    entities = [
        StepEntity::class, 
        WorkoutEntity::class, 
        UserEntity::class, 
        CustomExerciseEntity::class, 
        WorkoutTemplateEntity::class
    ], 
    version = 11, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RepForgeDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun userDao(): UserDao
    abstract fun customExerciseDao(): CustomExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
}
