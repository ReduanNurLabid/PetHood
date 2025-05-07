package com.example.pethood

import android.app.Application
import com.example.pethood.data.AdoptionPetRepository
import com.example.pethood.data.AuthService
import com.example.pethood.data.ReportedPetRepository
import com.example.pethood.data.UserRepository

class PetHoodApplication : Application() {

    val userRepository: UserRepository by lazy {
        UserRepository(applicationContext)
    }

    val reportedPetRepository: ReportedPetRepository by lazy {
        ReportedPetRepository()
    }
    
    val adoptionPetRepository: AdoptionPetRepository by lazy {
        AdoptionPetRepository(applicationContext)
    }

    val authService: AuthService by lazy {
        AuthService()
    }

    companion object {
        private lateinit var instance: PetHoodApplication

        fun getInstance(): PetHoodApplication {
            if (!::instance.isInitialized) {
                android.util.Log.e("PetHoodApplication", 
                    "Application instance not initialized. This could cause unexpected behavior.")
                
                return PetHoodApplication()
            }
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}