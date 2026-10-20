package com.repforge.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.repforge.data.local.dao.*
import com.repforge.data.local.entities.*

@Database(
    entities = [
        StepEntity::class, 
        WorkoutEntity::class, 
        UserEntity::class, 
        CustomExerciseEntity::class, 
        WorkoutTemplateEntity::class,
        CalisthenicsConfigEntity::class
    ], 
    version = 12, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RepForgeDatabase : RoomDatabase() {
    abstract fun stepDao(): StepDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun userDao(): UserDao
    abstract fun customExerciseDao(): CustomExerciseDao
    abstract fun workoutTemplateDao(): WorkoutTemplateDao
    abstract fun calisthenicsConfigDao(): CalisthenicsConfigDao
}
