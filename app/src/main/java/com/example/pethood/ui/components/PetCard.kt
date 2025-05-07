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
            .width(160.dp)
            .padding(8.dp)
            .clickable { onPetClick(pet) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                // Pet Image
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                ) {
                    // Try loading image - could be from a drawable resource name or a URI string
                    val context = LocalContext.current

                    // Parse URI outside of composable functions
                    val uri =
                        if (pet.imageUrl.startsWith("content://") || pet.imageUrl.startsWith("file://")) {
                            try {
                                Uri.parse(pet.imageUrl)
                            } catch (e: Exception) {
                                Log.e("PetCard", "Failed to parse URI: ${e.message}", e)
                                null
                            }
                        } else null

                    // Resource ID check outside of composable functions
                    val resourceId = if (uri == null) {
                        try {
                            context.resources.getIdentifier(
                                pet.imageUrl, "drawable", context.packageName
                            )
                        } catch (e: Exception) {
                            Log.e("PetCard", "Failed to get resource ID: ${e.message}", e)
                            0
                        }
                    } else 0

                    // Choose the appropriate painter based on our checks
                    val painter = when {
                        uri != null -> {
                            // Load from URI using Coil
                            rememberAsyncImagePainter(
                                model = uri,
                                onError = {
                                    Log.e(
                                        "PetCard",
                                        "Error loading image from URI: ${it.result.throwable.message}"
                                    )
                                }
                            )
                        }
                        resourceId != 0 -> {
                            painterResource(id = resourceId)
                        }

                        else -> {
                            // Fallback
                            Log.d("PetCard", "Using fallback image for pet: ${pet.name}")
                            painterResource(id = R.drawable.pet_logo)
                        }
                    }

                    Image(
                        painter = painter,
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
                            .padding(8.dp)
                            .size(24.dp)
                            .align(Alignment.TopEnd)
                            .clickable { onFavoriteClick(pet) }
                    )
                }
                
                // Pet information
                Column(
                    modifier = Modifier
                        .padding(8.dp)
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
                            modifier = Modifier.size(16.dp)
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