package com.example.pethood.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Repository for managing pets available for adoption
 */
class AdoptionPetRepository(context: Context) {
    // In-memory list of adoption pets
    private val adoptionPets: SnapshotStateList<AdoptionPet> = mutableStateListOf()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(ADOPTION_PETS_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    // Firestore database reference
    private val firestore: FirebaseFirestore = Firebase.firestore
    private val petsCollection = firestore.collection("adoptionPets")

    // Sample data initialization for demo purposes
    init {
        // Load existing pets or add sample pets if none exist
        loadPets()
    }
    
    /**
     * Add a new pet for adoption
     */
    suspend fun addAdoptionPet(pet: AdoptionPet): Flow<Result<Unit>> = flow {
        try {
            // Create a new document reference to get a unique ID
            val documentRef = petsCollection.document()
            val newDocId = documentRef.id
            Log.d("AdoptionPetRepository", "Generated new document ID: $newDocId")

            // Create a pet with the new document ID
            val petWithNewId = pet.copy(id = newDocId)

            try {
                // Add pet to Firestore using the generated document ID
                documentRef.set(petWithNewId).await()
                Log.d("AdoptionPetRepository", "Successfully added pet to Firestore: ${pet.name}")

                // Add to local cache
                adoptionPets.add(petWithNewId)
                emit(Result.success(Unit))
            } catch (e: Exception) {
                Log.e("AdoptionPetRepository", "Error adding pet to Firestore: ${e.message}", e)
                emit(Result.failure(e))
            }
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error adding pet: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get all pets available for adoption
     */
    suspend fun getAllAdoptionPets(): Flow<Result<List<AdoptionPet>>> = flow {
        try {
            Log.d("AdoptionPetRepository", "Fetching all adoption pets from Firestore...")
            val snapshot = petsCollection.get().await()

            Log.d(
                "AdoptionPetRepository",
                "Found ${snapshot.size()} documents in adoptionPets collection"
            )

            val petsList = mutableListOf<AdoptionPet>()
            for (document in snapshot.documents) {
                try {
                    val pet = document.toObject(AdoptionPet::class.java)
                    if (pet != null) {
                        Log.d("AdoptionPetRepository", "Loaded pet: ${pet.name} (ID: ${pet.id})")
                        petsList.add(pet)
                    } else {
                        Log.w(
                            "AdoptionPetRepository",
                            "Failed to convert document ${document.id} to AdoptionPet"
                        )
                        // Try to manually create pet from document data
                        val data = document.data
                        if (data != null) {
                            try {
                                val manualPet = AdoptionPet(
                                    id = document.id,
                                    name = data["name"] as? String ?: "Unknown",
                                    category = data["category"] as? String ?: "Unknown",
                                    type = data["type"] as? String ?: "Unknown",
                                    location = data["location"] as? String ?: "",
                                    description = data["description"] as? String ?: "",
                                    contactNumber = data["contactNumber"] as? String ?: "",
                                    imageUri = data["imageUri"] as? String ?: "",
                                    userId = data["userId"] as? String ?: "",
                                    date = (data["date"] as? com.google.firebase.Timestamp)?.toDate()
                                        ?: Date()
                                )
                                Log.d(
                                    "AdoptionPetRepository",
                                    "Manually created pet: ${manualPet.name}"
                                )
                                petsList.add(manualPet)
                            } catch (e: Exception) {
                                Log.e(
                                    "AdoptionPetRepository",
                                    "Error creating pet manually: ${e.message}",
                                    e
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(
                        "AdoptionPetRepository",
                        "Error converting document ${document.id}: ${e.message}",
                        e
                    )
                }
            }

            // Update local cache
            adoptionPets.clear()
            adoptionPets.addAll(petsList)

            Log.d("AdoptionPetRepository", "Successfully loaded ${petsList.size} pets")
            emit(Result.success(petsList))
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error getting adoption pets: ${e.message}", e)

            // Try direct access as a fallback
            try {
                Log.d("AdoptionPetRepository", "Attempting direct collection access...")
                val directSnapshot = Firebase.firestore.collection("adoptionPets").get().await()
                Log.d(
                    "AdoptionPetRepository",
                    "Direct access found ${directSnapshot.size()} documents"
                )
            } catch (e2: Exception) {
                Log.e("AdoptionPetRepository", "Direct access also failed: ${e2.message}", e2)
            }

            // Fall back to local cache if available
            if (adoptionPets.isNotEmpty()) {
                Log.d("AdoptionPetRepository", "Falling back to ${adoptionPets.size} cached pets")
                emit(Result.success(adoptionPets.toList()))
            } else {
                Log.e("AdoptionPetRepository", "No cached pets available, returning failure")
                emit(Result.failure(e))
            }
        }
    }
    
    /**
     * Get pets available for adoption by category
     */
    suspend fun getAdoptionPetsByCategory(category: String): Flow<Result<List<AdoptionPet>>> = flow {
        try {
            val snapshot = petsCollection.whereEqualTo("category", category).get().await()
            val petsList = mutableListOf<AdoptionPet>()
            for (document in snapshot.documents) {
                document.toObject(AdoptionPet::class.java)?.let { pet ->
                    petsList.add(pet)
                }
            }
            emit(Result.success(petsList))
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error getting pets by category: ${e.message}", e)
            // Fall back to local filtering
            val filteredPets = adoptionPets.filter {
                it.category.equals(category, ignoreCase = true)
            }
            emit(Result.success(filteredPets))
        }
    }
    
    /**
     * Get adoption pets added by a specific user
     */
    suspend fun getAdoptionPetsByUser(userId: String): Flow<Result<List<AdoptionPet>>> = flow {
        try {
            val snapshot = petsCollection.whereEqualTo("userId", userId).get().await()
            val petsList = mutableListOf<AdoptionPet>()
            for (document in snapshot.documents) {
                document.toObject(AdoptionPet::class.java)?.let { pet ->
                    petsList.add(pet)
                }
            }
            emit(Result.success(petsList))
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error getting pets by user: ${e.message}", e)
            // Fall back to local filtering
            val filteredPets = adoptionPets.filter { it.userId == userId }
            emit(Result.success(filteredPets))
        }
    }
    
    /**
     * Remove an adoption pet by ID
     */
    suspend fun removeAdoptionPet(petId: String): Flow<Result<Unit>> = flow {
        try {
            petsCollection.document(petId).delete().await()
            // Remove from local cache
            val petToRemove = adoptionPets.find { it.id == petId }
            petToRemove?.let {
                adoptionPets.remove(it)
            }
            emit(Result.success(Unit))
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error removing pet: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Get a specific adoption pet by its ID
     */
    suspend fun getAdoptionPetById(id: String): Flow<Result<AdoptionPet>> = flow {
        // First check local cache
        val localPet = adoptionPets.find { it.id == id }
        if (localPet != null) {
            emit(Result.success(localPet))
            return@flow
        }

        try {
            // If not found locally, try to fetch from Firestore
            val document = petsCollection.document(id).get().await()
            if (document.exists()) {
                val pet = document.toObject(AdoptionPet::class.java)
                // Add to local cache if found
                pet?.let {
                    if (!adoptionPets.contains(it)) {
                        adoptionPets.add(it)
                    }
                    emit(Result.success(it))
                } ?: emit(Result.failure(Exception("Pet data could not be parsed")))
            } else {
                emit(Result.failure(Exception("Pet not found")))
            }
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error getting pet by ID: ${e.message}", e)
            emit(Result.failure(e))
        }
    }
    
    /**
     * Add sample pets for demonstration
     */
    private fun addSamplePets() {
        val samplePets = listOf(
            AdoptionPet(
                id = "1",
                name = "Buddy",
                category = "Dog",
                type = "Golden Retriever",
                location = "Central Park",
                description = "Friendly 2-year-old Golden Retriever. Good with kids and other pets.",
                contactNumber = "555-123-4567",
                imageUri = "",
                userId = "user1",
                date = Date()
            ),
            AdoptionPet(
                id = "2",
                name = "Whiskers",
                category = "Cat",
                type = "Tabby",
                location = "Downtown",
                description = "Playful 1-year-old tabby cat. Litter trained and very affectionate.",
                contactNumber = "555-987-6543",
                imageUri = "",
                userId = "user2",
                date = Date()
            ),
            AdoptionPet(
                id = "3",
                name = "Tweety",
                category = "Bird",
                type = "Canary",
                location = "Uptown",
                description = "Beautiful yellow canary with melodious singing. Includes cage and supplies.",
                contactNumber = "555-567-8901",
                imageUri = "",
                userId = "user3",
                date = Date()
            ),
            AdoptionPet(
                id = "4",
                name = "Max",
                category = "Dog",
                type = "German Shepherd",
                location = "Riverside Park",
                description = "Loyal and intelligent 3-year-old German Shepherd. Well-trained and great as a family protector.",
                contactNumber = "555-234-5678",
                imageUri = "",
                userId = "user4",
                date = Date()
            ),
            AdoptionPet(
                id = "5",
                name = "Luna",
                category = "Cat",
                type = "Siamese",
                location = "Midtown",
                description = "Elegant and vocal 2-year-old Siamese cat. Very clean and loves to cuddle.",
                contactNumber = "555-345-6789",
                imageUri = "",
                userId = "user5",
                date = Date()
            ),
            AdoptionPet(
                id = "6",
                name = "Charlie",
                category = "Dog",
                type = "Beagle",
                location = "Washington Square",
                description = "Energetic 1-year-old Beagle puppy. Loves to play and great with children.",
                contactNumber = "555-456-7890",
                imageUri = "",
                userId = "user1",
                date = Date()
            ),
            AdoptionPet(
                id = "7",
                name = "Oliver",
                category = "Cat",
                type = "Maine Coon",
                location = "East Village",
                description = "Majestic 4-year-old Maine Coon. Very fluffy and gentle with a friendly personality.",
                contactNumber = "555-567-8901",
                imageUri = "",
                userId = "user2",
                date = Date()
            ),
            AdoptionPet(
                id = "8",
                name = "Rio",
                category = "Bird",
                type = "Parakeet",
                location = "Brooklyn Heights",
                description = "Colorful and cheerful parakeet. Already knows a few words and very interactive.",
                contactNumber = "555-678-9012",
                imageUri = "",
                userId = "user3",
                date = Date()
            ),
            AdoptionPet(
                id = "9",
                name = "Rocky",
                category = "Dog",
                type = "Boxer",
                location = "Battery Park",
                description = "Strong and friendly 3-year-old Boxer. Great with exercise and outdoor activities.",
                contactNumber = "555-789-0123",
                imageUri = "",
                userId = "user4",
                date = Date()
            ),
            AdoptionPet(
                id = "10",
                name = "Bella",
                category = "Cat",
                type = "Persian",
                location = "Tribeca",
                description = "Beautiful long-haired Persian cat. Very calm and enjoys peaceful environments.",
                contactNumber = "555-890-1234",
                imageUri = "",
                userId = "user5",
                date = Date()
            ),
            AdoptionPet(
                id = "11",
                name = "Coco",
                category = "Bird",
                type = "Cockatiel",
                location = "Greenwich Village",
                description = "Friendly and musical cockatiel. Whistles tunes and enjoys being handled.",
                contactNumber = "555-901-2345",
                imageUri = "",
                userId = "user1",
                date = Date()
            ),
            AdoptionPet(
                id = "12",
                name = "Cooper",
                category = "Dog",
                type = "Labrador Retriever",
                location = "Hudson River Park",
                description = "Playful 2-year-old chocolate Lab. Loves water and retrieving toys.",
                contactNumber = "555-012-3456",
                imageUri = "",
                userId = "user2",
                date = Date()
            ),
            AdoptionPet(
                id = "13",
                name = "Milo",
                category = "Cat",
                type = "Scottish Fold",
                location = "SoHo",
                description = "Adorable 1-year-old Scottish Fold with unique folded ears. Very sweet and loving.",
                contactNumber = "555-123-4567",
                imageUri = "",
                userId = "user3",
                date = Date()
            ),
            AdoptionPet(
                id = "14",
                name = "Polly",
                category = "Bird",
                type = "African Grey Parrot",
                location = "Upper East Side",
                description = "Intelligent African Grey parrot. Great vocabulary and extremely smart.",
                contactNumber = "555-234-5678",
                imageUri = "",
                userId = "user4",
                date = Date()
            ),
            AdoptionPet(
                id = "15",
                name = "Bailey",
                category = "Dog",
                type = "Shih Tzu",
                location = "Chelsea",
                description = "Sweet 3-year-old Shih Tzu. Low-shedding and perfect for apartment living.",
                contactNumber = "555-345-6789",
                imageUri = "",
                userId = "user5",
                date = Date()
            )
        )
        
        adoptionPets.addAll(samplePets)
        savePets()
    }

    /**
     * Load pets from SharedPreferences
     */
    private fun loadPets() {
        val petsJson = sharedPreferences.getString(ADOPTION_PETS_KEY, null)
        if (petsJson != null) {
            val type = object : TypeToken<List<AdoptionPet>>() {}.type
            val loadedPets: List<AdoptionPet> = gson.fromJson(petsJson, type)
            adoptionPets.clear()
            adoptionPets.addAll(loadedPets)
            Log.d(
                "AdoptionPetRepository",
                "Loaded ${adoptionPets.size} pets from SharedPreferences"
            )
        } else {
            // If no pets are loaded, add sample data
            Log.d("AdoptionPetRepository", "No pets found in SharedPreferences, adding sample data")
            addSamplePets()
        }
    }

    /**
     * Save pets to SharedPreferences
     */
    private fun savePets() {
        val petsJson = gson.toJson(adoptionPets.toList())
        sharedPreferences.edit {
            putString(ADOPTION_PETS_KEY, petsJson)
        }
        Log.d("AdoptionPetRepository", "Saved ${adoptionPets.size} pets to SharedPreferences")
    }

    companion object {
        private const val ADOPTION_PETS_PREFS = "adoption_pets_prefs"
        private const val ADOPTION_PETS_KEY = "adoption_pets"
    }
}