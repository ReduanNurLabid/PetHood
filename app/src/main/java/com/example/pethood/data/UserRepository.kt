package com.example.pethood.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling user data using Firestore.
 */
class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    /**
     * Retrieves a user by their ID.
     * @param userId The ID of the user to retrieve.
     * @return The User object if found, null otherwise.
     */
    suspend fun getUser(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                document.toObject<User>()
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network issues)
            e.printStackTrace()
            null
        }
    }

    /**
     * Adds a new user to Firestore.
     * @param user The User object to add.
     * @return true if successful, false otherwise.
     */
    suspend fun addUser(user: User): Boolean {
        return try {
            // Let Firestore auto-generate the document ID
            val documentReference = usersCollection.document()
            // Set the generated ID to the user object
            val userWithId = user.copy(id = documentReference.id)
            // Set the data using the reference.
            documentReference.set(userWithId).await()
            true
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            false
        }
    }

    /**
     * Updates an existing user in Firestore.
     * @param user The updated User object.
     * @return true if successful, false otherwise.
     */
    suspend fun updateUser(user: User): Boolean {
        return try {
            usersCollection.document(user.id).set(user).await()
            true
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            false
        }
    }

    /**
     * Deletes a user from Firestore.
     * @param userId The ID of the user to delete.
     * @return true if successful, false otherwise.
     */
    suspend fun deleteUser(userId: String): Boolean {
        return try {
            usersCollection.document(userId).delete().await()
            true
        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
            false
        }
    }    
    /**
    * Retrieves a user by their email
    * @param userEmail The email of the user to retrieve.
    * @return The User object if found, null otherwise.
    */
    suspend fun getUserByEmail(userEmail: String): User? {
        return try {
            val querySnapshot = usersCollection.whereEqualTo("email", userEmail).get().await()
            if (!querySnapshot.isEmpty) {
                querySnapshot.documents[0].toObject<User>()
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle exceptions (e.g., network issues)
            e.printStackTrace()
            null
        }
    }
}