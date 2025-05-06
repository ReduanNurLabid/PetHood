package com.example.pethood.data

import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import kotlinx.coroutines.tasks.await

/**
 * Repository for handling reported pets (missing and found)
 */
class ReportedPetRepository() {

    private val firestore = FirebaseFirestore.getInstance()
    private val reportedPetsCollection = firestore.collection("reportedPets")

    /*

    init {
        // Load pets from storage when repository is created
        loadPets()
    }

    /**
     * Add a new reported pet
     *//*
    suspend fun reportPet(pet: ReportedPet): Long {
        val pets = if (pet.isMissing) {
            getAllMissingPets().toMutableList()
        } else {
            getAllFoundPets().toMutableList()
        }

        // Generate new ID, changed to string
        val newId = (pets.maxOfOrNull { it.id?.toLong() ?: 0 } ?: 0) + 1
        val petWithId = pet.copy(id = newId)

        // Add to list
        pets.add(petWithId)

        // Save the updated list
        if (pet.isMissing) {
            saveMissingPets(pets)
            _missingPets.value = pets
        } else {
            saveFoundPets(pets)
            _foundPets.value = pets
        }

        return newId
    }

    */
    suspend fun addReportedPet(pet: ReportedPet) {
        val documentReference = reportedPetsCollection.document()
        val petWithId = pet.copy(id = documentReference.id)
        documentReference.set(petWithId).await()
    }

    /**
     * Get all reported pets
     */
    suspend fun getAllReportedPets(): List<ReportedPet> {
        val snapshot = reportedPetsCollection.get().await()
        return snapshot.toObjects(ReportedPet::class.java)
    }

    suspend fun getAllMissingPets(): List<ReportedPet> {
        val snapshot = reportedPetsCollection.whereEqualTo("isMissing", true).get().await()
        return snapshot.toObjects(ReportedPet::class.java)
    }

    suspend fun getAllFoundPets(): List<ReportedPet> {
        val snapshot = reportedPetsCollection.whereEqualTo("isMissing", false).get().await()
        return snapshot.toObjects(ReportedPet::class.java)
    }
    
    suspend fun updateReportedPet(pet: ReportedPet) {
        pet.id?.let {
            reportedPetsCollection.document(it).set(pet).await()
        }
    }
    
    suspend fun deleteReportedPet(petId: String) {
        reportedPetsCollection.document(petId).delete().await()
    }

    suspend fun getPetById(id: String): ReportedPet? {
        return try {
            val documentSnapshot = reportedPetsCollection.document(id).get().await()
            if (documentSnapshot.exists()) {
                documentSnapshot.toObject(ReportedPet::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Delete a pet by ID
     */
    fun deletePet(id: Long, isMissing: Boolean) {
        val pets = if (isMissing) {
            //getAllMissingPets().toMutableList()
            emptyList<ReportedPet>()
        } else {
           // getAllFoundPets().toMutableList()
            emptyList<ReportedPet>()
        }
    }

    /**
     * Search for pets by query string
     */
    fun searchPets(query: String, isMissing: Boolean): List<ReportedPet> {
        return emptyList<ReportedPet>()
//        val pets = if (isMissing) getAllMissingPets() else getAllFoundPets()
//
//        return if (query.isBlank()) {
//            pets
//        } else {
//            pets.filter {
//                it.name.contains(query, ignoreCase = true) ||
//                        it.type.contains(query, ignoreCase = true) ||
//                        it.lastSeen.contains(query, ignoreCase = true) ||
//                        it.description.contains(query, ignoreCase = true)
//            }
//        }
    }

    /**
     * Add sample data for testing/demo purposes
     */
    fun addSampleData() {
        // Check if we already have data
        /*
        if (_missingPets.value.isNotEmpty() || _foundPets.value.isNotEmpty()) {
            return
        }
        */
        // Sample missing pets
        val missingPets = listOf(
            ReportedPet(
                id = 1,
                name = "Max",
                type = "Golden Retriever",
                lastSeen = "Central Park",
                description = "Medium-sized golden retriever with a blue collar. Responds to 'Max'.",
                imageUrl = "dog_zimmer", // Using existing assets
                imageUri = "", // No uploaded image for samples
                contactNumber = "555-123-4567", // Sample contact number
                reporterId = "user1", // Sample reporter ID
                isMissing = true,
                date = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000) // 5 days ago
            ),
            ReportedPet(
                id = 2,
                name = "Luna",
                type = "Siamese Cat",
                lastSeen = "Main Street",
                description = "Small siamese cat with blue eyes. Very shy.",
                imageUrl = "cat_nero", // Using existing assets
                imageUri = "", // No uploaded image for samples
                contactNumber = "555-987-6543", // Sample contact number
                reporterId = "user2", // Sample reporter ID
                isMissing = true,
                date = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000) // 2 days ago
            )
        )

        // Sample found pets
        val foundPets = listOf(
            ReportedPet(
                id = 1,
                name = "Unknown Dog",
                type = "Mixed Breed",
                lastSeen = "Park Avenue",
                description = "Medium-sized mixed breed dog. No collar but seems well-behaved.",
                imageUrl = "dog_nemo", // Using existing assets
                imageUri = "", // No uploaded image for samples
                contactNumber = "555-246-8135", // Sample contact number
                reporterId = "user3", // Sample reporter ID
                isMissing = false,
                date = Date(System.currentTimeMillis() - 1 * 24 * 60 * 60 * 1000) // 1 day ago
            ),
            ReportedPet(
                id = 2,
                name = "Unknown Cat",
                type = "Tabby",
                lastSeen = "Elm Street",
                description = "Young tabby cat with white paws. Very friendly.",
                imageUrl = "cat_bunty", // Using existing assets
                imageUri = "", // No uploaded image for samples
                contactNumber = "555-369-1470", // Sample contact number
                reporterId = "user4", // Sample reporter ID
                isMissing = false,
                date = Date(System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000) // 3 days ago
            )
        )

        // Save the sample data, changed to add in db
        missingPets.forEach { addReportedPet(it)}
        foundPets.forEach { addReportedPet(it)}

    }

}