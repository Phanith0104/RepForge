package com.repforge.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.repforge.data.local.dao.UserDao
import com.repforge.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AUTH_REPO"
private const val PERM_TAG = "FIREBASE_PERMISSION"

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    fun getUser(): Flow<UserEntity?> = userDao.getLoggedInUser()
    
    fun getAllSavedProfiles(): Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun syncUserFromFirebase() {
        val firebaseUser = firebaseAuth.currentUser ?: return
        try {
            Log.d(TAG, "Attempting to sync profile for UID: ${firebaseUser.uid}")
            val document = firestore.collection("users").document(firebaseUser.uid).get().await()
            
            if (document.exists()) {
                val user = UserEntity(
                    email = document.getString("email") ?: firebaseUser.email ?: firebaseUser.phoneNumber ?: "",
                    name = document.getString("name") ?: firebaseUser.displayName ?: "",
                    age = document.getLong("age")?.toInt() ?: 0,
                    gender = document.getString("gender") ?: "Male",
                    phoneNumber = document.getString("phoneNumber") ?: firebaseUser.phoneNumber ?: "",
                    weight = documentToFloat(document, "weight"),
                    height = documentToFloat(document, "height"),
                    photoUrl = document.getString("photoUrl") ?: firebaseUser.photoUrl?.toString(),
                    provider = firebaseUser.providerData.firstOrNull()?.providerId ?: "firebase",
                    lastLogin = System.currentTimeMillis(),
                    isLoggedIn = true,
                    isProfileComplete = document.getBoolean("isProfileComplete") ?: false,
                    isGuest = false
                )
                userDao.insertUser(user)
                Log.d(TAG, "Profile found and synced for: ${user.email}")
            } else {
                Log.d(TAG, "No Firestore document found for UID: ${firebaseUser.uid}. This is normal for new users.")
                val user = UserEntity(
                    email = firebaseUser.email ?: firebaseUser.phoneNumber ?: "user_${firebaseUser.uid}",
                    name = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    provider = firebaseUser.providerData.firstOrNull()?.providerId ?: "firebase",
                    lastLogin = System.currentTimeMillis(),
                    isLoggedIn = true,
                    isProfileComplete = false,
                    isGuest = false
                )
                userDao.insertUser(user)
            }
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Sync failed for ${firebaseUser.uid}: ${e.message}", e)
            // We don't re-throw here during initial sync to avoid blocking the app startup
            // unless it's a critical error.
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.w(TAG, "Permission denied during background sync. Continuing with local data.")
            } else {
                throw mapFirebaseException(e)
            }
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val authResult = try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                Log.d(TAG, "SignIn failed, trying to create account for $email")
                firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            }
            
            val userId = authResult.user?.uid ?: throw Exception("Auth failed: No UID")
            userDao.logOutAll()
            handleAuthResult(userId, email, "password")
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Email Login Error: ${e.message}", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    suspend fun loginAsGuest() {
        userDao.logOutAll()
        val guestUser = UserEntity(
            email = "guest_${System.currentTimeMillis()}@repforge.com",
            name = "Guest User",
            provider = "guest",
            isLoggedIn = true,
            isProfileComplete = false,
            isGuest = true
        )
        userDao.insertUser(guestUser)
    }

    suspend fun updateProfile(name: String, age: Int, gender: String, phoneNumber: String, weight: Float, height: Float): Result<Unit> {
        return try {
            val currentUser = userDao.getLoggedInUser().first() ?: throw Exception("No active user session")
            
            val user = currentUser.copy(
                name = name,
                age = age,
                gender = gender,
                phoneNumber = phoneNumber,
                weight = weight,
                height = height,
                isProfileComplete = true
            )
            
            if (!user.isGuest) {
                val firebaseUser = firebaseAuth.currentUser ?: throw Exception("Not authenticated with Firebase")
                val userMap = mapOf(
                    "name" to name,
                    "age" to age,
                    "gender" to gender,
                    "phoneNumber" to phoneNumber,
                    "weight" to weight,
                    "height" to height,
                    "email" to user.email,
                    "photoUrl" to user.photoUrl,
                    "provider" to user.provider,
                    "isProfileComplete" to true,
                    "lastLogin" to user.lastLogin
                )
                Log.d(TAG, "Writing profile to Firestore path: users/${firebaseUser.uid}")
                firestore.collection("users").document(firebaseUser.uid).set(userMap).await()
                Log.d(TAG, "Firestore write successful")
            }
            
            userDao.insertUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Profile Update Error: ${e.message}", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val userId = authResult.user?.uid ?: throw Exception("Google Auth failed: No UID")
            
            userDao.logOutAll()
            handleAuthResult(userId, authResult.user?.email ?: "", "google.com")
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Google Login Error: ${e.message}", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    fun verifyPhoneNumber(
        activity: Activity,
        phoneNumber: String,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (FirebaseException) -> Unit
    ) {
        val formattedNumber = if (!phoneNumber.startsWith("+")) "+91$phoneNumber" else phoneNumber
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(c: PhoneAuthCredential) = onVerificationCompleted(c)
                override fun onVerificationFailed(e: FirebaseException) = onVerificationFailed(e)
                override fun onCodeSent(id: String, t: PhoneAuthProvider.ForceResendingToken) = onCodeSent(id, t)
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithPhone(credential: PhoneAuthCredential): Result<Unit> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val userId = authResult.user?.uid ?: throw Exception("Phone Auth failed: No UID")
            
            userDao.logOutAll()
            handleAuthResult(userId, authResult.user?.phoneNumber ?: "", "phone")
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Phone Login Error: ${e.message}", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    private suspend fun handleAuthResult(userId: String, identifier: String, provider: String): Result<Unit> {
        return try {
            Log.d(TAG, "Fetching existing profile for UID: $userId")
            val doc = firestore.collection("users").document(userId).get().await()
            val firebaseUser = firebaseAuth.currentUser
            
            val user = if (doc.exists()) {
                UserEntity(
                    email = doc.getString("email") ?: identifier,
                    name = doc.getString("name") ?: firebaseUser?.displayName ?: "",
                    age = doc.getLong("age")?.toInt() ?: 0,
                    gender = doc.getString("gender") ?: "Male",
                    phoneNumber = doc.getString("phoneNumber") ?: firebaseUser?.phoneNumber ?: "",
                    weight = documentToFloat(doc, "weight"),
                    height = documentToFloat(doc, "height"),
                    photoUrl = doc.getString("photoUrl") ?: firebaseUser?.photoUrl?.toString(),
                    provider = provider,
                    lastLogin = System.currentTimeMillis(),
                    isLoggedIn = true,
                    isProfileComplete = doc.getBoolean("isProfileComplete") ?: false,
                    isGuest = false
                )
            } else {
                UserEntity(
                    email = identifier,
                    name = firebaseUser?.displayName ?: "",
                    photoUrl = firebaseUser?.photoUrl?.toString(),
                    provider = provider,
                    lastLogin = System.currentTimeMillis(),
                    isLoggedIn = true, 
                    isProfileComplete = false, 
                    isGuest = false
                )
            }
            
            userDao.insertUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Auth Profile Fetch Error: ${e.message}", e)
            // Even if profile fetch fails, we have authenticated with Auth.
            // But we should report the permission issue.
            Result.failure(mapFirebaseException(e))
        }
    }

    private fun documentToFloat(doc: DocumentSnapshot, field: String): Float {
        return when (val value = doc.get(field)) {
            is Double -> value.toFloat()
            is Long -> value.toFloat()
            is Float -> value
            is Int -> value.toFloat()
            else -> 0f
        }
    }

    private fun mapFirebaseException(e: Exception): Exception {
        Log.e(TAG, "Mapping Firebase Exception: ${e.message}", e)
        if (e is FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again."
                "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                "ERROR_USER_DISABLED" -> "This account has been disabled."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many attempts. Please try again later."
                "ERROR_OPERATION_NOT_ALLOWED" -> "Login method not enabled."
                "ERROR_WEAK_PASSWORD" -> "Password is too weak."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email."
                "ERROR_INVALID_EMAIL" -> "Invalid email address."
                "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid OTP code."
                "ERROR_SESSION_EXPIRED" -> "Verification code expired. Please request a new one."
                else -> e.message ?: "Authentication failed."
            }
            return Exception(message)
        }
        if (e is FirebaseFirestoreException) {
            return when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                    Exception("Access denied. Please check your account permissions.")
                FirebaseFirestoreException.Code.UNAVAILABLE ->
                    Exception("Network unavailable. Please check your internet connection.")
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED ->
                    Exception("Request timed out. Please try again.")
                else -> Exception("Database error: ${e.message}")
            }
        }
        if (e is UnknownHostException || e is ConnectException) {
            return Exception("No internet connection. Please check your connection and try again.")
        }
        return e
    }

    suspend fun logout() {
        firebaseAuth.signOut()
        userDao.logOutAll()
    }
    
    suspend fun removeProfile(email: String) {
        userDao.removeUser(email)
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapFirebaseException(e))
        }
    }
}
