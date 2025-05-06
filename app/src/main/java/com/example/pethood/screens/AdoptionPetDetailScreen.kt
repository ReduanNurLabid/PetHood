package com.example.pethood.screens

import android.content.Intent
import android.net.Uri
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.AdoptionPet
import com.example.pethood.data.AdoptionPetRepository
import com.example.pethood.data.PetCategory
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionPetDetailScreen(
    petId: String,
    onBackClick: () -> Unit,
    navigateToRoute: (Screen) -> Unit = {}
) {
    // Get context and adoption pet repository
    val context = LocalContext.current
    val adoptionPetRepository: AdoptionPetRepository = PetHoodApplication.getInstance().adoptionPetRepository

    // State to hold the pet
    var pet: AdoptionPet? = null

    // Retrieve the pet asynchronously
    adoptionPetRepository.getAdoptionPet(petId){
        petResult ->
        if(petResult != null){
            pet = petResult
        } else{
            Toast.makeText(context,"Pet not found",Toast.LENGTH_SHORT).show()
        }
    }

    
    // If pet is null, show error and return
    if (pet == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Pet not found",
                fontSize = 20.sp,
                color = Color.Gray
            )
        }
        return
    }
    
    // Determine pet category
    val category = when (pet.category.lowercase()) {
        "dog" -> PetCategory.DOG.toString()
        "cat" -> PetCategory.CAT
        else -> PetCategory.OTHER
    }
    
    // Get category color
    val categoryColor = when (category) {
        PetCategory.DOG -> Color(0xFFFF415B)
        PetCategory.CAT -> Color(0xFF6A1B9A)
        else -> Color(0xFF9C27B0) // Birds
    }
    
    Scaffold(
            bottomBar = {
                BottomNavigationBar(currentRoute = Screen.Home.route, onNavigate = navigateToRoute)
            }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Pet image with back button
            Box(modifier = Modifier.fillMaxWidth()) {
                // Pet image
                if (pet.imageUri.isNotEmpty()) {
                    val uri = try {
                        Uri.parse(pet.imageUri)
                    } catch (e: Exception) {
                        android.util.Log.e("AdoptionPetDetailScreen", "Failed to parse URI: ${e.message}", e)
                        null
                    }

                    if (uri != null) {


                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uri,
                                onError = {
                                    android.util.Log.e(
                                        "AdoptionPetDetailScreen",
                                        "Error loading image: ${it.result.throwable.message}"
                                                                       )
                                }
                            ),
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback to a default image
                        Image(
                            painter = painterResource(id = R.drawable.pet_logo),
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Display a default image based on the pet category
                    val imageRes = when (category) {
                        PetCategory.DOG -> R.drawable.dog_nemo
                        PetCategory.CAT -> R.drawable.cat_nero
                        else -> R.drawable.pet_logo
                    }
                    
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = pet.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                // Category badge
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            color = categoryColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = pet.category,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Pet details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Pet name in large text
                Text(
                    text = pet.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pet breed/type
                Text(
                    text = pet.type,
                    fontSize = 20.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Location with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Location",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = pet.location,
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Description header
                Text(
                    text = "Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Full description
                Text(
                    text = pet.description.ifEmpty { "No description provided" },
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Contact button
                Button(
                    onClick = {
                        if (pet.contactNumber.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = android.net.Uri.parse("tel:${pet.contactNumber}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Could not open phone dialer",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "No contact number provided",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = categoryColor
                    )
                ) {
                    Text(
                        text = "Contact Owner",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
} 