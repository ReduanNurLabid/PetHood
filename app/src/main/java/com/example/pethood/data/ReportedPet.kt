package com.example.pethood.data

import java.util.Date

data class ReportedPet(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val lastSeen: String = "",
    val description: String = "",
    val imageUrl: String = "", 
    val imageUri: String = "", 
    val userId: String = "",
    val petId: String = "",
    val contactNumber: String = "", 
    val reporterName: String = "", 
    val reporterEmail: String = "", 
    val isMissing: Boolean = false, 
    val date: Date = Date() 
)