package com.example.pethood.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Repository for handling user authentication and storage
 */
class UserRepository(context: Context) {
    
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Registers a new user
     * @return true if registration was successful, false if email already exists
     */
    fun registerUser(user: User): Boolean {
        val users = getUsers()
        
        // Check if user with this email already exists
        if (users.any { it.email.equals(user.email, ignoreCase = true) }) {
            return false
        }
        
        // Add new user and save
        val updatedUsers = users + user
        saveUsers(updatedUsers)
        
        return true
    }
    
    /**
     * Logs in a user
     * @return the User if credentials are valid, null otherwise
     */
    fun loginUser(email: String, password: String): User? {
        val users = getUsers()
        return users.find { 
            it.email.equals(email, ignoreCase = true) && it.password == password 
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
        val userJson = sharedPreferences.getString(CURRENT_USER_KEY, null)
        return if (userJson != null) {
            gson.fromJson(userJson, User::class.java)
        } else null
    }
    
    /**
     * Update the current user's profile image URL
     * @return true if update was successful
     */
    fun updateProfileImageUrl(imageUrl: String): Boolean {
        val currentUser = getCurrentUser() ?: return false
        
        // Create updated user with new image URL
        val updatedUser = currentUser.copy(profileImageUrl = imageUrl)
        
        // Update the user in the users list
        val users = getUsers()
        val updatedUsers = users.map { 
            if (it.id == currentUser.id) updatedUser else it 
        }
        
        // Save changes
        saveUsers(updatedUsers)
        saveCurrentUser(updatedUser)
        
        return true
    }
    
    /**
     * Update the current user's password
     * @return true if update was successful
     */
    fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        val currentUser = getCurrentUser() ?: return false
        
        // Verify current password
        if (currentUser.password != currentPassword) {
            return false
        }
        
        // Create updated user with new password
        val updatedUser = currentUser.copy(password = newPassword)
        
        // Update the user in the users list
        val users = getUsers()
        val updatedUsers = users.map { 
            if (it.id == currentUser.id) updatedUser else it 
        }
        
        // Save changes
        saveUsers(updatedUsers)
        saveCurrentUser(updatedUser)
        
        return true
    }
    
    /**
     * Clear the current user session (logout)
     */
    fun logout() {
        sharedPreferences.edit {
            remove(CURRENT_USER_KEY)
        }
    }
    
    /**
     * Check if a user is logged in
     */
    fun isLoggedIn(): Boolean {
        val userId = sharedPreferences.getString(CURRENT_USER_KEY, null)
        return userId != null
    }
    
    /**
     * Get the current user's ID
     */
    fun getCurrentUserId(): String {
        val userJson = sharedPreferences.getString(CURRENT_USER_KEY, null)
        return if (userJson != null) {
            try {
                val user = gson.fromJson(userJson, User::class.java)
                user?.id ?: "default_user"
            } catch (e: Exception) {
                "default_user"
            }
        } else {
            "default_user"
        }
    }
    
    // Private helper methods
    
    private fun getUsers(): List<User> {
        val usersJson = sharedPreferences.getString(USERS_KEY, null)
        return if (usersJson != null) {
            val type = object : TypeToken<List<User>>() {}.type
            gson.fromJson(usersJson, type)
        } else {
            emptyList()
        }
    }
    
    private fun saveUsers(users: List<User>) {
        sharedPreferences.edit {
            putString(USERS_KEY, gson.toJson(users))
        }
    }
    
    companion object {
        private const val USER_PREFS = "user_prefs"
        private const val USERS_KEY = "users"
        private const val CURRENT_USER_KEY = "current_user"
    }
}