package com.example.pethood.data

import java.util.Date

data class ReportedPet(
    val id: Long = 0,
    val name: String,
    val type: String,
    val lastSeen: String,
    val description: String,
    val imageUrl: String = "", // For drawable resources
    val imageUri: String = "", // For user-uploaded images (URI string)
    val contactNumber: String = "", // Contact phone number of the reporter
    val reporterId: String = "", // ID of the user who reported the pet
    val isMissing: Boolean, // true if missing, false if found
    val date: Date = Date() // reported date
)