package com.example.pethood.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PetGender { MALE, FEMALE }

enum class PetCategory { DOG, CAT, OTHER }

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breed: String,
    val category: PetCategory,
    val gender: PetGender,
    val age: Int,
    val description: String,
    val imageUrl: String,
    val isFavorite: Boolean = false,
    // Additional fields for adoption pets
    val isAdoptionPet: Boolean = false,
    val adoptionPetId: String = "",
    val adoptionImageUri: String = "",
    val location: String = "",
    val contactNumber: String = ""
)