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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    fun generateUniqueUserId(): String {
        val prefix = "25016"
        val suffix = (System.currentTimeMillis() % 10000000).toString().padStart(7, '0')
        return "$prefix$suffix"
    }

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun syncUserFromFirebase() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            val activeUser = userDao.getLoggedInUser().first()
            if (activeUser != null && !activeUser.isGuest) {
                Log.d(TAG, "No Firebase session found. Resetting active local user session.")
                userDao.logOutAll()
            }
            return
        }

        try {
            Log.d(TAG, "Attempting to sync profile for UID: ${firebaseUser.uid}")
            val document = firestore.collection("users").document(firebaseUser.uid).get().await()
            
            if (document.exists()) {
                val emailVal = document.getString("email") ?: firebaseUser.email ?: firebaseUser.phoneNumber ?: "user_${firebaseUser.uid}"
                val user = UserEntity(
                    email = emailVal,
                    uniqueUserId = document.getString("uniqueUserId") ?: "250160226720",
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
                val emailVal = firebaseUser.email ?: firebaseUser.phoneNumber ?: "user_${firebaseUser.uid}"
                val assignedId = if (emailVal.contains("250160226720")) "250160226720" else generateUniqueUserId()
                val user = UserEntity(
                    email = emailVal,
                    uniqueUserId = assignedId,
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
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                Log.w(TAG, "Permission denied during background sync. Continuing with local session.")
            } else {
                throw mapFirebaseException(e)
            }
        }
    }

    suspend fun loginWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Authentication failed: No UID returned")
            userDao.logOutAll()
            handleAuthResult(userId, email, "password")
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Email Login Error: ${e.message}", e)
            Result.failure(mapFirebaseException(e))
        }
    }

    suspend fun registerWithEmail(email: String, password: String): Result<Unit> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Account creation failed: No UID returned")
            userDao.logOutAll()
            handleAuthResult(userId, email, "password")
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Email Registration Error: ${e.message}", e)
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
            val currentUser = userDao.getLoggedInUser().first() ?: throw Exception("No active user session found")
            
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
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null) {
                    val userMap = mapOf(
                        "name" to name,
                        "age" to age,
                        "gender" to gender,
                        "phoneNumber" to phoneNumber,
                        "weight" to weight,
                        "height" to height,
                        "email" to user.email,
                        "uniqueUserId" to user.uniqueUserId,
                        "photoUrl" to user.photoUrl,
                        "provider" to user.provider,
                        "isProfileComplete" to true,
                        "lastLogin" to user.lastLogin
                    )
                    try {
                        Log.d(TAG, "Writing profile to Firestore path: users/${firebaseUser.uid}")
                        firestore.collection("users").document(firebaseUser.uid).set(userMap).await()
                        Log.d(TAG, "Firestore write successful")
                    } catch (e: Exception) {
                        Log.w(TAG, "Firestore write failed (${e.message}). Proceeding with local profile update.")
                    }
                }
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
            val userId = authResult.user?.uid ?: throw Exception("Google Authentication failed: No UID returned")
            
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
        forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null,
        onCodeSent: (String, PhoneAuthProvider.ForceResendingToken) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onVerificationFailed: (FirebaseException) -> Unit
    ) {
        val cleanNumber = phoneNumber.trim().replace(" ", "").replace("-", "")
        val formattedNumber = if (!cleanNumber.startsWith("+")) {
            "+91$cleanNumber"
        } else {
            cleanNumber
        }

        val builder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(c: PhoneAuthCredential) = onVerificationCompleted(c)
                override fun onVerificationFailed(e: FirebaseException) = onVerificationFailed(e)
                override fun onCodeSent(id: String, t: PhoneAuthProvider.ForceResendingToken) = onCodeSent(id, t)
            })

        if (forceResendingToken != null) {
            builder.setForceResendingToken(forceResendingToken)
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    suspend fun signInWithPhone(credential: PhoneAuthCredential): Result<Unit> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val userId = authResult.user?.uid ?: throw Exception("Phone Authentication failed: No UID returned")
            
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
            var doc: DocumentSnapshot? = null
            try {
                doc = firestore.collection("users").document(userId).get().await()
            } catch (e: Exception) {
                Log.w(TAG, "Firestore profile fetch failed (${e.message}). Falling back to Firebase Auth details.")
            }
            val firebaseUser = firebaseAuth.currentUser
            
            val emailVal = identifier.ifBlank { firebaseUser?.email ?: firebaseUser?.phoneNumber ?: "user_$userId" }
            val user = if (doc != null && doc.exists()) {
                UserEntity(
                    email = doc.getString("email") ?: emailVal,
                    uniqueUserId = doc.getString("uniqueUserId") ?: "250160226720",
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
                val assignedId = if (emailVal.contains("250160226720")) "250160226720" else generateUniqueUserId()
                UserEntity(
                    email = emailVal,
                    uniqueUserId = assignedId,
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
            Log.d(TAG, "User session successfully saved locally: ${user.email}, isProfileComplete: ${user.isProfileComplete}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(PERM_TAG, "Auth Profile Save Error: ${e.message}", e)
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
        if (e is FirebaseAuthInvalidCredentialsException) {
            return Exception("Incorrect password or invalid credentials. Please try again.")
        }
        if (e is FirebaseAuthInvalidUserException) {
            return Exception("No account found with this email. Please sign up first.")
        }
        if (e is FirebaseAuthUserCollisionException) {
            return Exception("An account already exists with this email. Please log in instead.")
        }
        if (e is FirebaseAuthWeakPasswordException) {
            return Exception("Password is too weak. Please use at least 6 characters.")
        }
        if (e is FirebaseAuthException) {
            val message = when (e.errorCode) {
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "Incorrect password or credentials."
                "ERROR_USER_NOT_FOUND" -> "No account found with this email."
                "ERROR_USER_DISABLED" -> "This account has been disabled."
                "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later."
                "ERROR_OPERATION_NOT_ALLOWED" -> "Selected login method is not enabled in Firebase Console."
                "ERROR_WEAK_PASSWORD" -> "Password is too weak. Must be at least 6 characters."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "An account already exists with this email."
                "ERROR_INVALID_EMAIL" -> "Invalid email address format."
                "ERROR_INVALID_VERIFICATION_CODE" -> "Invalid 6-digit OTP code. Please check and try again."
                "ERROR_SESSION_EXPIRED" -> "Verification code expired. Please request a new code."
                else -> e.localizedMessage ?: e.message ?: "Authentication failed."
            }
            return Exception(message)
        }
        if (e is FirebaseFirestoreException) {
            return when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED -> 
                    Exception("Database permission denied. Please verify Firestore security rules.")
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
        return Exception(e.localizedMessage ?: e.message ?: "An unexpected error occurred.")
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
