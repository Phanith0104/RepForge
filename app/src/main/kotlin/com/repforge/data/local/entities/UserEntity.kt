package com.repforge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String = "",
    val age: Int = 0,
    val gender: String = "Male",
    val phoneNumber: String = "",
    val weight: Float = 0f,
    val height: Float = 0f,
    val photoUrl: String? = null,
    val provider: String = "email",
    val lastLogin: Long = System.currentTimeMillis(),
    val isLoggedIn: Boolean = false,
    val isProfileComplete: Boolean = false,
    val isGuest: Boolean = false
)
