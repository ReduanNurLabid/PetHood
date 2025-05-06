package com.example.pethood.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await


/**
 * Repository for managing pets available for adoption
 */
class AdoptionPetRepository(context: Context) {
    // In-memory list of adoption pets
    private val adoptionPets: SnapshotStateList<AdoptionPet> = mutableStateListOf()

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(ADOPTION_PETS_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Sample data initialization for demo purposes
    init {
        // Load existing pets or add sample pets if none exist
        loadPets()
    }
    
    /**
     * Add a new pet for adoption
     */
    fun addAdoptionPet(pet: AdoptionPet) {
        try {
            // Validate the pet object before adding
            if (pet.id.isBlank()) {
                Log.e("AdoptionPetRepository", "Error: Pet ID is blank")
                return
            }
            
            // Check if this pet ID already exists (avoid duplicates)
            if (adoptionPets.any { it.id == pet.id }) {
                Log.w("AdoptionPetRepository", "Warning: Pet with ID ${pet.id} already exists, not adding duplicate")
                return
            }
            
            Log.d("AdoptionPetRepository", "Adding pet: ${pet.name}, ID: ${pet.id}, Category: ${pet.category}")
            adoptionPets.add(pet)
            // Save to SharedPreferences
            savePets()
            Log.d("AdoptionPetRepository", "Successfully added pet, total pets: ${adoptionPets.size}")
        } catch (e: Exception) {
            Log.e("AdoptionPetRepository", "Error adding pet: ${e.message}", e)
            throw e // Rethrow to allow proper error handling upstream
        }
    }
    
    /**
     * Get all pets available for adoption
     */
    fun getAllAdoptionPets(): List<AdoptionPet> {
        return adoptionPets.toList()
    }
    
    /**
     * Get pets available for adoption by category
     */
    fun getAdoptionPetsByCategory(category: String): List<AdoptionPet> {
        return adoptionPets.filter { it.category.equals(category, ignoreCase = true) }
    }
    
    /**
     * Get adoption pets added by a specific user
     */
    fun getAdoptionPetsByUser(userId: String): List<AdoptionPet> {
        return adoptionPets.filter { it.userId == userId }
    }
    
    /**
     * Remove an adoption pet by ID
     */
    fun removeAdoptionPet(petId: String) {
        val petToRemove = adoptionPets.find { it.id == petId }
        petToRemove?.let {
            adoptionPets.remove(it)
            // Save changes to SharedPreferences
            savePets()
        }
    }
    
    /**
     * Get a specific adoption pet by its ID
     */
    fun getAdoptionPetById(id: String): AdoptionPet? {
        return adoptionPets.find { it.id == id }
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