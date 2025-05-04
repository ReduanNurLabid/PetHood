package com.example.pethood.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * Repository for handling reported pets (missing and found)
 */
class ReportedPetRepository(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(REPORTED_PETS_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    // StateFlow for observing changes in reported pets
    private val _missingPets = MutableStateFlow<List<ReportedPet>>(emptyList())
    val missingPets: StateFlow<List<ReportedPet>> = _missingPets.asStateFlow()

    private val _foundPets = MutableStateFlow<List<ReportedPet>>(emptyList())
    val foundPets: StateFlow<List<ReportedPet>> = _foundPets.asStateFlow()

    init {
        // Load pets from storage when repository is created
        loadPets()
    }

    /**
     * Add a new reported pet
     */
    fun reportPet(pet: ReportedPet): Long {
        val pets = if (pet.isMissing) {
            getAllMissingPets().toMutableList()
        } else {
            getAllFoundPets().toMutableList()
        }

        // Generate new ID
        val newId = (pets.maxOfOrNull { it.id } ?: 0) + 1
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

    /**
     * Get all missing pets
     */
    fun getAllMissingPets(): List<ReportedPet> {
        return _missingPets.value
    }

    /**
     * Get all found pets
     */
    fun getAllFoundPets(): List<ReportedPet> {
        return _foundPets.value
    }

    /**
     * Get a pet by ID
     */
    fun getPetById(id: Long, isMissing: Boolean): ReportedPet? {
        val pets = if (isMissing) getAllMissingPets() else getAllFoundPets()
        return pets.find { it.id == id }
    }

    /**
     * Delete a pet by ID
     */
    fun deletePet(id: Long, isMissing: Boolean) {
        val pets = if (isMissing) {
            getAllMissingPets().toMutableList()
        } else {
            getAllFoundPets().toMutableList()
        }
        
        // Find and remove the pet
        val petIndex = pets.indexOfFirst { it.id == id }
        if (petIndex >= 0) {
            pets.removeAt(petIndex)
            
            // Save the updated list
            if (isMissing) {
                saveMissingPets(pets)
                _missingPets.value = pets
            } else {
                saveFoundPets(pets)
                _foundPets.value = pets
            }
        }
    }

    /**
     * Search for pets by query string
     */
    fun searchPets(query: String, isMissing: Boolean): List<ReportedPet> {
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
    fun addSampleData() {
        // Check if we already have data
        if (_missingPets.value.isNotEmpty() || _foundPets.value.isNotEmpty()) {
            return
        }

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

        // Save the sample data
        saveMissingPets(missingPets)
        saveFoundPets(foundPets)

        // Update state flows
        _missingPets.value = missingPets
        _foundPets.value = foundPets
    }

    // Private helper methods

    private fun loadPets() {
        val missingPets = getMissingPetsFromPrefs()
        val foundPets = getFoundPetsFromPrefs()

        _missingPets.value = missingPets
        _foundPets.value = foundPets

        // If no pets are loaded, add sample data
        if (missingPets.isEmpty() && foundPets.isEmpty()) {
            addSampleData()
        }
    }

    private fun getMissingPetsFromPrefs(): List<ReportedPet> {
        val petsJson = sharedPreferences.getString(MISSING_PETS_KEY, null)
        return if (petsJson != null) {
            val type = object : TypeToken<List<ReportedPet>>() {}.type
            gson.fromJson(petsJson, type)
        } else {
            emptyList()
        }
    }

    private fun getFoundPetsFromPrefs(): List<ReportedPet> {
        val petsJson = sharedPreferences.getString(FOUND_PETS_KEY, null)
        return if (petsJson != null) {
            val type = object : TypeToken<List<ReportedPet>>() {}.type
            gson.fromJson(petsJson, type)
        } else {
            emptyList()
        }
    }

    private fun saveMissingPets(pets: List<ReportedPet>) {
        sharedPreferences.edit {
            putString(MISSING_PETS_KEY, gson.toJson(pets))
        }
    }

    private fun saveFoundPets(pets: List<ReportedPet>) {
        sharedPreferences.edit {
            putString(FOUND_PETS_KEY, gson.toJson(pets))
        }
    }

    companion object {
        private const val REPORTED_PETS_PREFS = "reported_pets_prefs"
        private const val MISSING_PETS_KEY = "missing_pets"
        private const val FOUND_PETS_KEY = "found_pets"
    }
}