package com.example.pethood.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    
    @Query("SELECT * FROM pets WHERE category = :category ORDER BY name ASC")
    fun getPetsByCategory(category: PetCategory): Flow<List<Pet>>
    
    @Query("SELECT * FROM pets WHERE id = :petId")
    suspend fun getPetById(petId: Long): Pet?
    
    @Query("SELECT * FROM pets WHERE name LIKE '%' || :searchQuery || '%' OR breed LIKE '%' || :searchQuery || '%'")
    fun searchPets(searchQuery: String): Flow<List<Pet>>
    
    @Insert
    suspend fun insertPet(pet: Pet): Long
    
    @Insert
    suspend fun insertPets(pets: List<Pet>)
    
    @Update
    suspend fun updatePet(pet: Pet)
    
    @Query("UPDATE pets SET isFavorite = :isFavorite WHERE id = :petId")
    suspend fun updateFavoriteStatus(petId: Long, isFavorite: Boolean)
    
    @Delete
    suspend fun deletePet(pet: Pet)
}