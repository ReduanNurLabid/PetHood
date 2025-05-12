package com.example.pethood.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class UserRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val auth: FirebaseAuth = Firebase.auth
    
    private val firestore: FirebaseFirestore = Firebase.firestore
    
    suspend fun registerUser(user: User): Boolean = suspendCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update profile
                    val firebaseUser = task.result?.user
                    val profileUpdates = userProfileChangeRequest {
                        displayName = user.name
                    }
                    
                    firebaseUser?.uid?.let { uid ->
                        Log.d("UserRepository", "Creating user document in Firestore for uid: $uid")

                        val userData = hashMapOf(
                            "id" to uid,
                            "email" to user.email,
                            "name" to user.name,
                            "phoneNumber" to user.phoneNumber,
                            "profileImageUrl" to user.profileImageUrl,
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        
                        firestore.collection("users")
                            .document(uid)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d(
                                    "UserRepository",
                                    "User document created successfully in Firestore"
                                )

                                firebaseUser.updateProfile(profileUpdates)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "UserRepository",
                                            "Firebase Auth profile updated successfully"
                                        )

                                        val localUser = User(
                                            id = uid,
                                            email = user.email,
                                            password = user.password,
                                            name = user.name,
                                            phoneNumber = user.phoneNumber,
                                            profileImageUrl = user.profileImageUrl
                                        )
                                        saveCurrentUser(localUser)
                                        continuation.resume(true)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("UserRepository", "Failed to update profile", e)
                                        continuation.resume(true)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserRepository", "Failed to save user to Firestore", e)
                                
                                firestore.collection("users")
                                    .document(uid)
                                    .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                    .addOnSuccessListener {
                                        Log.d(
                                            "UserRepository",
                                            "User document created on second attempt"
                                        )
                                        continuation.resume(true)
                                    }
                                    .addOnFailureListener { e2 ->
                                        Log.e(
                                            "UserRepository",
                                            "Failed to save user on second attempt",
                                            e2
                                        )
                                        continuation.resume(false)
                                    }
                            }
                    } ?: continuation.resume(false)
                } else {
                    Log.e("UserRepository", "Registration failed", task.exception)
                    continuation.resume(false)
                }
            }
    }
    

    suspend fun loginUser(email: String, password: String): User? = suspendCoroutine { continuation ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        firestore.collection("users").document(firebaseUser.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    val user = User(
                                        id = firebaseUser.uid,
                                        email = firebaseUser.email ?: email,
                                        password = password, // Note: Don't store actual passwords
                                        name = document.getString("name") ?: "",
                                        phoneNumber = document.getString("phoneNumber") ?: "",
                                        profileImageUrl = document.getString("profileImageUrl") ?: ""
                                    )
                                    saveCurrentUser(user)
                                    continuation.resume(user)
                                } else {
                                    val user = User(
                                        id = firebaseUser.uid,
                                        email = firebaseUser.email ?: email,
                                        password = password,
                                        name = firebaseUser.displayName ?: "",
                                        phoneNumber = "",
                                        profileImageUrl = firebaseUser.photoUrl?.toString() ?: ""
                                    )
                                    saveCurrentUser(user)
                                    continuation.resume(user)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserRepository", "Failed to get user data", e)
                                val user = User(
                                    id = firebaseUser.uid,
                                    email = firebaseUser.email ?: email,
                                    password = password,
                                    name = firebaseUser.displayName ?: "",
                                    phoneNumber = "",
                                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: ""
                                )
                                saveCurrentUser(user)
                                continuation.resume(user)
                            }
                    } else {
                        continuation.resume(null)
                    }
                } else {
                    Log.e("UserRepository", "Login failed", task.exception)
                    continuation.resume(null)
                }
            }
    }

    fun saveCurrentUser(user: User) {
        sharedPreferences.edit {
            putString(CURRENT_USER_KEY, gson.toJson(user))
        }
    }
    

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        
        if (firebaseUser != null) {
            val userJson = sharedPreferences.getString(CURRENT_USER_KEY, null)
            return if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else {
                val user = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    password = "", // We don't store actual passwords
                    name = firebaseUser.displayName ?: "",
                    phoneNumber = "",
                    profileImageUrl = firebaseUser.photoUrl?.toString() ?: ""
                )
                saveCurrentUser(user)
                user
            }
        }
        
        return null
    }
    

    suspend fun updateProfileImageUrl(imageUrl: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("UserRepository", "Cannot update profile image URL: User not logged in")
            continuation.resume(false)
            return@suspendCoroutine
        }

        Log.d("UserRepository", "Updating profile image URL: $imageUrl")

        val userData = mapOf(
            "profileImageUrl" to imageUrl
        )

        firestore.collection("users").document(firebaseUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Profile image URL updated in Firestore successfully")
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(profileImageUrl = imageUrl)
                    saveCurrentUser(updatedUser)

                    try {
                        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                            photoUri = android.net.Uri.parse(imageUrl)
                        }
                        firebaseUser.updateProfile(profileUpdates)
                            .addOnSuccessListener {
                                Log.d("UserRepository", "Profile image also updated in Auth")
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "UserRepository",
                                    "Failed to update Auth profile image, but Firestore update succeeded",
                                    e
                                )
                            }
                    } catch (e: Exception) {
                        Log.w(
                            "UserRepository",
                            "Error parsing image URL for Auth profile, but Firestore update succeeded",
                            e
                        )
                    }

                    continuation.resume(true)
                } else {
                    Log.e("UserRepository", "Current user is null after Firestore update")
                    continuation.resume(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to update profile image URL", e)
                continuation.resume(false)
            }
    }
    

    suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }
        
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
            firebaseUser.email ?: "", currentPassword
        )
        firebaseUser.reauthenticate(credential)
            .addOnSuccessListener {
                firebaseUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        val currentUser = getCurrentUser()
                        if (currentUser != null) {
                            val updatedUser = currentUser.copy(password = newPassword)
                            saveCurrentUser(updatedUser)
                            continuation.resume(true)
                        } else {
                            continuation.resume(false)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserRepository", "Failed to update password", e)
                        continuation.resume(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to reauthenticate user", e)
                continuation.resume(false)
            }
    }
    

    suspend fun updatePhoneNumber(phoneNumber: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("UserRepository", "Cannot update phone number: User not logged in")
            continuation.resume(false)
            return@suspendCoroutine
        }

        Log.d("UserRepository", "Updating phone number to: $phoneNumber")

        val userData = mapOf(
            "phoneNumber" to phoneNumber
        )

        firestore.collection("users").document(firebaseUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Phone number updated in Firestore successfully")
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(phoneNumber = phoneNumber)
                    saveCurrentUser(updatedUser)
                    continuation.resume(true)
                } else {
                    Log.e("UserRepository", "Current user is null after phone number update")
                    continuation.resume(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to update phone number", e)
                continuation.resume(false)
            }
    }


    suspend fun updateName(name: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("UserRepository", "Cannot update name: User not logged in")
            continuation.resume(false)
            return@suspendCoroutine
        }

        Log.d("UserRepository", "Updating name to: $name")

        val userData = mapOf(
            "name" to name
        )

        firestore.collection("users").document(firebaseUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Name updated in Firestore successfully")

                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    displayName = name
                }

                firebaseUser.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        Log.d("UserRepository", "Name also updated in Auth profile")
                        // Update local user data
                        val currentUser = getCurrentUser()
                        if (currentUser != null) {
                            val updatedUser = currentUser.copy(name = name)
                            saveCurrentUser(updatedUser)
                            continuation.resume(true)
                        } else {
                            Log.e("UserRepository", "Current user is null after name update")
                            continuation.resume(false)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("UserRepository", "Failed to update name in Auth profile", e)
                        continuation.resume(true)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to update name", e)
                continuation.resume(false)
            }
    }


    suspend fun refreshCurrentUser(): User? = suspendCoroutine { continuation ->
        val currentUserId = getCurrentUserId()
        if (currentUserId == "default_user" || auth.currentUser == null) {
            continuation.resume(null)
            return@suspendCoroutine
        }
        
        firestore.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val currentUser = getCurrentUser()
                    
                    val updatedUser = User(
                        id = currentUserId,
                        email = document.getString("email") ?: currentUser?.email ?: "",
                        password = currentUser?.password ?: "",
                        name = document.getString("name") ?: "",
                        phoneNumber = document.getString("phoneNumber") ?: "",
                        profileImageUrl = document.getString("profileImageUrl") ?: ""
                    )
                    
                    saveCurrentUser(updatedUser)
                    continuation.resume(updatedUser)
                } else {
                    Log.e("UserRepository", "User document does not exist in Firestore")
                    continuation.resume(getCurrentUser())
                }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to refresh user data", e)
                continuation.resume(getCurrentUser())
            }
    }
    

    fun logout() {
        auth.signOut()
        sharedPreferences.edit {
            remove(CURRENT_USER_KEY)
        }
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "default_user"
    }
    
    companion object {
        private const val USER_PREFS = "user_prefs"
        private const val CURRENT_USER_KEY = "current_user"
    }
}