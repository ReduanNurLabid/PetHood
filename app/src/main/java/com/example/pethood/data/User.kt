package com.example.pethood.data

import java.util.UUID

data class User(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val password: String,
    val name: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = ""
)