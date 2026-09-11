package com.repforge.data.local.dao

import androidx.room.*
import com.repforge.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile ORDER BY lastLogin DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE user_profile SET isLoggedIn = 0")
    suspend fun logOutAll()

    @Query("DELETE FROM user_profile")
    suspend fun clearAll()
    
    @Query("DELETE FROM user_profile WHERE email = :email")
    suspend fun removeUser(email: String)
}
