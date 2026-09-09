package com.repforge.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.repforge.data.local.dao.UserDao
import com.repforge.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun syncUserFromFirebase() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            try {
                val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                if (document.exists()) {
                    val user = UserEntity(
                        email = document.getString("email") ?: firebaseUser.email ?: "",
                        name = document.getString("name") ?: "",
                        age = document.getLong("age")?.toInt() ?: 0,
                        gender = document.getString("gender") ?: "Male",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        weight = document.getDouble("weight")?.toFloat() ?: 0f,
                        height = document.getDouble("height")?.toFloat() ?: 0f,
                        isLoggedIn = true,
                        isProfileComplete = document.getBoolean("isProfileComplete") ?: false
                    )
                    userDao.insertUser(user)
                }
            } catch (e: Exception) {
                // Handle fetch error
            }
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val authResult = try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                // If sign in fails, try creating the account
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            
            // Check if profile exists
            val userId = authResult.user?.uid ?: throw Exception("Authentication failed. Please check your credentials.")
            val doc = firestore.collection("users").document(userId).get().await()
            
            val user = if (doc.exists()) {
                UserEntity(
                    email = email,
                    name = doc.getString("name") ?: "",
                    age = doc.getLong("age")?.toInt() ?: 0,
                    gender = doc.getString("gender") ?: "Male",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    weight = doc.getDouble("weight")?.toFloat() ?: 0f,
                    height = doc.getDouble("height")?.toFloat() ?: 0f,
                    isLoggedIn = true,
                    isProfileComplete = doc.getBoolean("isProfileComplete") ?: false
                )
            } else {
                UserEntity(email = email, isLoggedIn = true, isProfileComplete = false)
            }
            
            userDao.insertUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String, age: Int, gender: String, phoneNumber: String, weight: Float, height: Float) {
        val firebaseUser = firebaseAuth.currentUser ?: return
        val user = UserEntity(
            email = firebaseUser.email ?: "",
            name = name,
            age = age,
            gender = gender,
            phoneNumber = phoneNumber,
            weight = weight,
            height = height,
            isLoggedIn = true,
            isProfileComplete = true
        )
        
        val userMap = mapOf(
            "name" to name,
            "age" to age,
            "gender" to gender,
            "phoneNumber" to phoneNumber,
            "weight" to weight,
            "height" to height,
            "email" to user.email,
            "isProfileComplete" to true
        )
        
        firestore.collection("users").document(firebaseUser.uid).set(userMap).await()
        userDao.insertUser(user)
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            
            val firebaseUser = authResult.user ?: throw Exception("Google Auth failed")
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            
            val user = if (doc.exists()) {
                UserEntity(
                    email = firebaseUser.email ?: "",
                    name = doc.getString("name") ?: "",
                    age = doc.getLong("age")?.toInt() ?: 0,
                    gender = doc.getString("gender") ?: "Male",
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    weight = doc.getDouble("weight")?.toFloat() ?: 0f,
                    height = doc.getDouble("height")?.toFloat() ?: 0f,
                    isLoggedIn = true,
                    isProfileComplete = doc.getBoolean("isProfileComplete") ?: false
                )
            } else {
                UserEntity(email = firebaseUser.email ?: "", isLoggedIn = true, isProfileComplete = false)
            }
            
            userDao.insertUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        firebaseAuth.signOut()
        userDao.clearUser()
    }
}
