package com.example.pethood.data

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository for handling reported pets (missing and found)
 */
class ReportedPetRepository() {

    private val firestore = Firebase.firestore
    private val missingPetsCollection = firestore.collection("missingPets")
    private val foundPetsCollection = firestore.collection("foundPets")

    init {
        // Don't add sample data automatically
        Log.d("ReportedPetRepository", "Repository initialized without adding sample data")
    }

    suspend fun addMissingPet(pet: ReportedPet) {
        // Always set isMissing=true for missing pets
        val petWithMissing = pet.copy(isMissing = true)
        val documentReference = missingPetsCollection.document()
        val petWithId = petWithMissing.copy(id = documentReference.id)
        documentReference.set(petWithId).await()
    }

    suspend fun addFoundPet(pet: ReportedPet) {
        // Always set isMissing=false for found pets
        val petWithFound = pet.copy(isMissing = false)
        val documentReference = foundPetsCollection.document()
        val petWithId = petWithFound.copy(id = documentReference.id)
        documentReference.set(petWithId).await()
    }

    // For backward compatibility
    suspend fun addReportedPet(pet: ReportedPet) {
        if (pet.isMissing) {
            addMissingPet(pet)
        } else {
            addFoundPet(pet)
        }
    }

    /**
     * Get all reported pets (combines both collections)
     */
    suspend fun getAllReportedPets(): List<ReportedPet> {
        // Get missing pets
        val missingSnapshot = missingPetsCollection.get().await()
        val missingPets = missingSnapshot.toObjects(ReportedPet::class.java)

        // Get found pets
        val foundSnapshot = foundPetsCollection.get().await()
        val foundPets = foundSnapshot.toObjects(ReportedPet::class.java)

        // Combine the lists
        return missingPets + foundPets
    }

    /**
     * Get all missing pets
     */
    suspend fun getAllMissingPets(): List<ReportedPet> {
        try {
            Log.d("ReportedPetRepository", "Fetching missing pets from Firestore...")
            val snapshot = missingPetsCollection.get().await()
            val pets = snapshot.toObjects(ReportedPet::class.java)
            Log.d("ReportedPetRepository", "Loaded ${pets.size} missing pets")
            return pets.map { it.copy(isMissing = true) } // Ensure isMissing flag is set
        } catch (e: Exception) {
            Log.e("ReportedPetRepository", "Error fetching missing pets: ${e.message}", e)
            return emptyList()
        }
    }

    /**
     * Get all found pets
     */
    suspend fun getAllFoundPets(): List<ReportedPet> {
        try {
            Log.d("ReportedPetRepository", "Fetching found pets from Firestore...")
            val snapshot = foundPetsCollection.get().await()
            val pets = snapshot.toObjects(ReportedPet::class.java)
            Log.d("ReportedPetRepository", "Loaded ${pets.size} found pets")
            return pets.map { it.copy(isMissing = false) } // Ensure isMissing flag is not set
        } catch (e: Exception) {
            Log.e("ReportedPetRepository", "Error fetching found pets: ${e.message}", e)
            return emptyList()
        }
    }

    suspend fun updateReportedPet(pet: ReportedPet) {
        pet.id?.let {
            // Check if it's a missing or found pet
            if (pet.isMissing) {
                missingPetsCollection.document(it).set(pet).await()
            } else {
                foundPetsCollection.document(it).set(pet).await()
            }
        }
    }

    suspend fun deleteMissingPet(petId: String) {
        missingPetsCollection.document(petId).delete().await()
    }

    suspend fun deleteFoundPet(petId: String) {
        foundPetsCollection.document(petId).delete().await()
    }

    // For backward compatibility
    suspend fun deleteReportedPet(petId: String, isMissing: Boolean = true) {
        if (isMissing) {
            deleteMissingPet(petId)
        } else {
            deleteFoundPet(petId)
        }
    }

    suspend fun getPetById(id: String, isMissing: Boolean): ReportedPet? {
        return try {
            val collection = if (isMissing) missingPetsCollection else foundPetsCollection
            val documentSnapshot = collection.document(id).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(ReportedPet::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Try to find a pet across both collections
    suspend fun getPetById(id: String): ReportedPet? {
        val missingPet = getPetById(id, true)
        if (missingPet != null) {
            return missingPet
        }
        return getPetById(id, false)
    }

    // Alias method to maintain compatibility with existing code
    suspend fun getReportedPet(id: String): ReportedPet? {
        return getPetById(id)
    }

    /**
     * Delete a pet by ID
     */
    suspend fun deletePet(id: String, isMissing: Boolean) {
        val pets = if (isMissing) {
            getAllMissingPets()
        } else {
            getAllFoundPets()
        }
        pets.find { it.id == id }?.let {
            deleteReportedPet(it.id!!)
        }
    }

    /**
     * Search for pets by query string
     */
    suspend fun searchPets(query: String, isMissing: Boolean): List<ReportedPet> {
        val pets = if (isMissing) getAllMissingPets() else getAllFoundPets()

        return if (query.isBlank()) {
            pets
        } else {
            pets.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.type.contains(query, ignoreCase = true) ||
                        it.lastSeen.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Add sample data for testing/demo purposes
     */
    suspend fun addSampleData() {
        val missingPets = listOf(
            ReportedPet(
                id = "1",
                name = "Max",
                type = "Golden Retriever",
                lastSeen = "Central Park",
                description = "Medium-sized golden retriever with a blue collar. Responds to 'Max'.",
                imageUrl = "dog_zimmer",
                imageUri = "",
                contactNumber = "555-123-4567",
                userId = "user1",
                petId = "",
                isMissing = true,
                date = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000)
            ),
            ReportedPet(
                id = "2",
                name = "Luna",
                type = "Siamese Cat",
                lastSeen = "Main Street",
                description = "Small siamese cat with blue eyes. Very shy.",
                imageUrl = "cat_nero",
                imageUri = "",
                contactNumber = "555-987-6543",
                userId = "user2",
                petId = "",
                isMissing = true,
                date = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
            )
        )

        val foundPets = listOf(
            ReportedPet(
                id = "3",
                name = "Unknown Dog",
                type = "Mixed Breed",
                lastSeen = "Park Avenue",
                description = "Medium-sized mixed breed dog. No collar but seems well-behaved.",
                imageUrl = "dog_nemo",
                imageUri = "",
                contactNumber = "555-246-8135",
                userId = "user3",
                petId = "",
                isMissing = false,
                date = Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000)
            ),
            ReportedPet(
                id = "4",
                name = "Unknown Cat",
                type = "Tabby",
                lastSeen = "Elm Street",
                description = "Young tabby cat with white paws. Very friendly.",
                imageUrl = "cat_bunty",
                imageUri = "",
                contactNumber = "555-369-1470",
                userId = "user4",
                petId = "",
                isMissing = false,
                date = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000)
            )
        )

        missingPets.forEach { addMissingPet(it) }
        foundPets.forEach { addFoundPet(it) }
    }

    // Returns a Flow of reported pets for real-time updates
    fun getReportedPets(): kotlinx.coroutines.flow.Flow<List<ReportedPet>> {
        return kotlinx.coroutines.flow.flow {
            emit(getAllReportedPets())
        }
    }
}