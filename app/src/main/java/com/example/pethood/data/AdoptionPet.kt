package com.example.pethood.data

import java.util.Date

/**
 * Data class representing a pet that is put up for adoption
 */
data class AdoptionPet(
    val id: String,
    val name: String,
    val category: String, // "Cat", "Dog", or "Bird"
    val type: String, // Breed or specific type
    val location: String,
    val description: String,
    val contactNumber: String,
    val imageUri: String,
    val userId: String, // ID of the user who put the pet up for adoption
    val date: Date
) 