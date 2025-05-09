package com.example.pethood.ui.components

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.R
import com.example.pethood.data.Pet
import com.example.pethood.data.PetGender

@Composable
fun PetCard(
    pet: Pet,
    onPetClick: (Pet) -> Unit,
    onFavoriteClick: (Pet) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .clickable { onPetClick(pet) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Pet Image
                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxWidth()
                ) {
                    // Try loading image - could be from a drawable resource name or a URI string
                    val context = LocalContext.current

                    // Handle URL-based images (http/https)
                    val imageModel = when {
                        pet.imageUrl.startsWith("http://") || pet.imageUrl.startsWith("https://") -> {
                            pet.imageUrl
                        }
                        pet.imageUrl.startsWith("content://") || pet.imageUrl.startsWith("file://") -> {
                            try {
                                Uri.parse(pet.imageUrl)
                            } catch (e: Exception) {
                                Log.e("PetCard", "Failed to parse URI: ${e.message}", e)
                                R.drawable.pet_logo
                            }
                        }
                        else -> {
                            // Try to find a drawable resource with this name
                            try {
                                val resourceId = context.resources.getIdentifier(
                                    pet.imageUrl, "drawable", context.packageName
                                )
                                if (resourceId != 0) {
                                    resourceId
                                } else {
                                    R.drawable.pet_logo
                                }
                            } catch (e: Exception) {
                                Log.e("PetCard", "Error resolving resource: ${e.message}", e)
                                R.drawable.pet_logo
                            }
                        }
                    }

                    Image(
                        painter = rememberAsyncImagePainter(
                            model = imageModel,
                            onError = {
                                Log.e("PetCard", "Error loading image: ${it.result.throwable.message}")
                            }
                        ),
                        contentDescription = "${pet.name}, ${pet.breed}",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Favorite button
                    Icon(
                        painter = painterResource(
                            id = if (pet.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
                        ),
                        contentDescription = if (pet.isFavorite) "Remove from favorites" else "Add to favorites",
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp)
                            .align(Alignment.TopEnd)
                            .clickable { onFavoriteClick(pet) },
                        tint = if (pet.isFavorite) Color.Red else Color.White
                    )
                }
                
                // Pet information
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Pet name
                        Text(
                            text = pet.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Gender icon
                        Icon(
                            painter = painterResource(
                                id = if (pet.gender == PetGender.MALE) R.drawable.ic_male else R.drawable.ic_female
                            ),
                            contentDescription = if (pet.gender == PetGender.MALE) "Male" else "Female",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Pet breed
                    Text(
                        text = pet.breed,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}