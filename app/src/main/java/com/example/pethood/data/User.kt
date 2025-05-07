package com.example.pethood.data

data class User(
    val id: String = "",
    val email: String,
    val password: String,
    val name: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = ""
)