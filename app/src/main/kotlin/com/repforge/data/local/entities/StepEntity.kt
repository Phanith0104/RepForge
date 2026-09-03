package com.repforge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val date: String, // format: YYYY-MM-DD
    val count: Int,
    val streak: Int = 0
)
