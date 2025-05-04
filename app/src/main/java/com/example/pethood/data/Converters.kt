package com.example.pethood.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPetCategory(value: PetCategory): String {
        return value.name
    }
    
    @TypeConverter
    fun toPetCategory(value: String): PetCategory {
        return enumValueOf(value)
    }
    
    @TypeConverter
    fun fromPetGender(value: PetGender): String {
        return value.name
    }
    
    @TypeConverter
    fun toPetGender(value: String): PetGender {
        return enumValueOf(value)
    }
}