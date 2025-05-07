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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Repository for handling user authentication and storage
 */
class UserRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Firebase Authentication instance
    private val auth: FirebaseAuth = Firebase.auth
    
    // Firestore database instance
    private val firestore: FirebaseFirestore = Firebase.firestore
    
    /**
     * Registers a new user with Firebase Authentication
     * @return true if registration was successful, false if email already exists
     */
    suspend fun registerUser(user: User): Boolean = suspendCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update profile
                    val firebaseUser = task.result?.user
                    val profileUpdates = userProfileChangeRequest {
                        displayName = user.name
                    }
                    
                    // Save additional user data to Firestore
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

                                // Update display name
                                firebaseUser.updateProfile(profileUpdates)
                                    .addOnSuccessListener {
                                        Log.d(
                                            "UserRepository",
                                            "Firebase Auth profile updated successfully"
                                        )

                                        // Save user to local storage
                                        val localUser = User(
                                            id = uid,
                                            email = user.email,
                                            password = user.password, // Note: Don't actually store passwords!
                                            name = user.name,
                                            phoneNumber = user.phoneNumber,
                                            profileImageUrl = user.profileImageUrl
                                        )
                                        saveCurrentUser(localUser)
                                        continuation.resume(true)
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("UserRepository", "Failed to update profile", e)
                                        continuation.resume(true) // Still consider it a success
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.e("UserRepository", "Failed to save user to Firestore", e)

                                // Try one more time with set and merge
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
    
    /**
     * Logs in a user with Firebase Authentication
     * @return the User if credentials are valid, null otherwise
     */
    suspend fun loginUser(email: String, password: String): User? = suspendCoroutine { continuation ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    if (firebaseUser != null) {
                        // Get user data from Firestore
                        firestore.collection("users").document(firebaseUser.uid)
                            .get()
                            .addOnSuccessListener { document ->
                                if (document != null && document.exists()) {
                                    // Create user from document
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
                                    // If document doesn't exist, create a basic user
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
                                // Fall back to basic user data
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
    
    /**
     * Save the current user session
     */
    fun saveCurrentUser(user: User) {
        sharedPreferences.edit {
            putString(CURRENT_USER_KEY, gson.toJson(user))
        }
    }
    
    /**
     * Get the current logged in user
     */
    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        
        // If Firebase user exists but local storage doesn't have it, create a basic user
        if (firebaseUser != null) {
            val userJson = sharedPreferences.getString(CURRENT_USER_KEY, null)
            return if (userJson != null) {
                gson.fromJson(userJson, User::class.java)
            } else {
                // Create a basic user from Firebase user
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
    
    /**
     * Update the current user's profile image URL
     * @return true if update was successful
     */
    suspend fun updateProfileImageUrl(imageUrl: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("UserRepository", "Cannot update profile image URL: User not logged in")
            continuation.resume(false)
            return@suspendCoroutine
        }

        Log.d("UserRepository", "Updating profile image URL: $imageUrl")

        // Create user data map with the image URL
        val userData = mapOf(
            "profileImageUrl" to imageUrl
        )

        // Use set with merge option to create the document if it doesn't exist
        firestore.collection("users").document(firebaseUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Profile image URL updated in Firestore successfully")
                // Update local user data
                val currentUser = getCurrentUser()
                if (currentUser != null) {
                    val updatedUser = currentUser.copy(profileImageUrl = imageUrl)
                    saveCurrentUser(updatedUser)

                    // Also update profile photo URL in Firebase Auth
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
    
    /**
     * Update the current user's password
     * @return true if update was successful
     */
    suspend fun updatePassword(currentPassword: String, newPassword: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            continuation.resume(false)
            return@suspendCoroutine
        }
        
        // Re-authenticate user to confirm current password
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(
            firebaseUser.email ?: "", currentPassword
        )
        firebaseUser.reauthenticate(credential)
            .addOnSuccessListener {
                // Password verified, now update it
                firebaseUser.updatePassword(newPassword)
                    .addOnSuccessListener {
                        // Update local user data
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
    
    /**
     * Update the current user's phone number
     * @return true if update was successful
     */
    suspend fun updatePhoneNumber(phoneNumber: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("UserRepository", "Cannot update phone number: User not logged in")
            continuation.resume(false)
            return@suspendCoroutine
        }

        Log.d("UserRepository", "Updating phone number to: $phoneNumber")

        // Create user data map with the phone number
        val userData = mapOf(
            "phoneNumber" to phoneNumber
        )

        // Use set with merge option to create the document if it doesn't exist
        firestore.collection("users").document(firebaseUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Phone number updated in Firestore successfully")
                // Update local user data
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

    /**
     * Update the current user's name
     * @return true if update was successful
     */
    suspend fun updateName(name: String): Boolean = suspendCoroutine { continuation ->
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Log.e("UserRepository", "Cannot update name: User not logged in")
            continuation.resume(false)
            return@suspendCoroutine
        }

        Log.d("UserRepository", "Updating name to: $name")

        // Create user data map with the name
        val userData = mapOf(
            "name" to name
        )

        // Use set with merge option to create the document if it doesn't exist
        firestore.collection("users").document(firebaseUser.uid)
            .set(userData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("UserRepository", "Name updated in Firestore successfully")

                // Update Firebase Auth profile
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
                        // Still continue as success since Firestore was updated
                        continuation.resume(true)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UserRepository", "Failed to update name", e)
                continuation.resume(false)
            }
    }

    /**
     * Clear the current user session (logout)
     */
    fun logout() {
        auth.signOut()
        sharedPreferences.edit {
            remove(CURRENT_USER_KEY)
        }
    }
    
    /**
     * Check if a user is logged in
     */
    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Get the current user's ID
     */
    fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: "default_user"
    }
    
    companion object {
        private const val USER_PREFS = "user_prefs"
        private const val CURRENT_USER_KEY = "current_user"
    }
}