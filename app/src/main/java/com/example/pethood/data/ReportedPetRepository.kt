package com.example.pethood.data

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReportedPetRepository() {

    private val firestore = Firebase.firestore
    private val missingPetsCollection = firestore.collection("missingPets")
    private val foundPetsCollection = firestore.collection("foundPets")

    init {
        Log.d("ReportedPetRepository", "Repository initialized without adding sample data")
    }

    suspend fun addMissingPet(pet: ReportedPet) {
        val petWithMissing = pet.copy(isMissing = true)
        val documentReference = missingPetsCollection.document()
        val petWithId = petWithMissing.copy(id = documentReference.id)
        documentReference.set(petWithId).await()
    }

    suspend fun addFoundPet(pet: ReportedPet) {
        val petWithFound = pet.copy(isMissing = false)
        val documentReference = foundPetsCollection.document()
        val petWithId = petWithFound.copy(id = documentReference.id)
        documentReference.set(petWithId).await()
    }

    suspend fun addReportedPet(pet: ReportedPet) {
        if (pet.isMissing) {
            addMissingPet(pet)
        } else {
            addFoundPet(pet)
        }
    }


    suspend fun getAllReportedPets(): List<ReportedPet> {
        val missingSnapshot = missingPetsCollection.get().await()
        val missingPets = missingSnapshot.toObjects(ReportedPet::class.java)

        val foundSnapshot = foundPetsCollection.get().await()
        val foundPets = foundSnapshot.toObjects(ReportedPet::class.java)

        return missingPets + foundPets
    }

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

    suspend fun getPetById(id: String): ReportedPet? {
        try {
            val missingPetDoc = missingPetsCollection.document(id).get().await()
            if (missingPetDoc.exists()) {
                val pet = missingPetDoc.toObject(ReportedPet::class.java)
                return pet?.copy(isMissing = true)
            }
        } catch (e: Exception) {
            Log.e("ReportedPetRepository", "Error looking up missing pet: ${e.message}", e)
        }
        
        try {
            val foundPetDoc = foundPetsCollection.document(id).get().await()
            if (foundPetDoc.exists()) {
                // Found in found pets collection, make sure isMissing is false
                val pet = foundPetDoc.toObject(ReportedPet::class.java)
                return pet?.copy(isMissing = false)
            }
        } catch (e: Exception) {
            Log.e("ReportedPetRepository", "Error looking up found pet: ${e.message}", e)
        }
        
        return null
    }

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

    fun getReportedPets(): kotlinx.coroutines.flow.Flow<List<ReportedPet>> {
        return kotlinx.coroutines.flow.flow {
            emit(getAllReportedPets())
        }
    }
}