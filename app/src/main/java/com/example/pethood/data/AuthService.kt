package com.example.pethood.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth: FirebaseAuth = Firebase.auth
    private val db = Firebase.firestore

    init {
        // Disable reCAPTCHA verification for testing (remove in production)
        val firebaseAuthSettings: FirebaseAuthSettings = auth.firebaseAuthSettings
        firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }

    suspend fun signup(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun signup(
        email: String,
        password: String,
        name: String,
        phoneNumber: String
    ): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = hashMapOf(
                    "uid" to authResult.user?.uid,
                    "name" to name,
                    "email" to email,
                    "phoneNumber" to phoneNumber
                )
                authResult.user?.uid?.let {
                    db.collection("users").document(it).set(user)
                }
            }
    }

    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        auth.signOut()
    }
}