package com.example.pethood.data


enum class PetGender { MALE, FEMALE }

enum class PetCategory { DOG, CAT, OTHER }


data class Pet(
    val id: String = "",
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