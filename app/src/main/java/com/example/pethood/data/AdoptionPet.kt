package com.example.pethood.data

import java.util.Date

data class AdoptionPet(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val type: String = "",
    val location: String = "",
    val description: String = "",
    val contactNumber: String = "",
    val petId: String = "",
    val userId: String = "",
    val ownerName: String = "",
    val ownerEmail: String = "",
    val date: Date = Date(),
    val imageUri: String = "",
    val isAdopted: Boolean = false
)