package com.example.pethood.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await

class PetRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val petsCollection = firestore.collection("pets")

    suspend fun getPets(): List<Pet> {
        return try {
            val snapshot = petsCollection.get().await()
            snapshot.documents.mapNotNull { document ->
                document.toObject<Pet>()?.copy(id = document.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPet(petId: String): Pet? {
        return try {
            val document = petsCollection.document(petId).get().await()
            document.toObject<Pet>()?.copy(id = document.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun addPet(pet: Pet): String? {
        return try {
            val documentReference = petsCollection.add(pet).await()
            documentReference.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePet(pet: Pet) {
        try {
            petsCollection.document(pet.id).set(pet).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun deletePet(petId: String) {
        try {
            petsCollection.document(petId).delete().await()
        } catch (e: Exception) {
            // Handle error
        }
    }
}