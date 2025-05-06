package com.example.pethood.data

import java.util.Date

data class ReportedPet(
    val id: String = "",
    val name: String,
    val type: String,
    val lastSeen: String,
    val description: String,
    val imageUrl: String = "", // For drawable resources
    val imageUri: String = "", // For user-uploaded images (URI string)
    val userId: String = "",
    val petId: String = "",
    val isMissing: Boolean, // true if missing, false if found
    val date: Date = Date() // reported date
)