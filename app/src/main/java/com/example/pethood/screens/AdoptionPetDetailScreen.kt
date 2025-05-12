package com.example.pethood.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import android.net.Uri
import android.util.Log
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.pethood.data.PetCategory
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionPetDetailScreen(
    petId: String,
    onBackClick: () -> Unit,
    navigateToRoute: (Screen) -> Unit = {}
) {
    val context = LocalContext.current

    var pet by remember { mutableStateOf<com.example.pethood.data.AdoptionPet?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val adoptionPetRepository = PetHoodApplication.getInstance().adoptionPetRepository
    val userRepository = PetHoodApplication.getInstance().userRepository
    val currentUserId = userRepository.getCurrentUserId()
    val coroutineScope = rememberCoroutineScope()
    
    // Check if current user is the owner
    var isCurrentUserOwner by remember { mutableStateOf(false) }
    
    // Dialog states
    var showAdoptedConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(petId) {
        adoptionPetRepository.getAdoptionPetById(petId).collect { result ->
            result.onSuccess { adoptionPet ->
                pet = adoptionPet
                isCurrentUserOwner = adoptionPet.userId == currentUserId
            }.onFailure {
                // Optionally handle error
            }
            isLoading = false
        }
    }

    // If loading, show loading indicator
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
        return
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
    val category = when (pet?.category?.lowercase()) {
        "dog" -> PetCategory.DOG
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
            BottomNavigationBar(
                currentRoute = Screen.Home.route,
                onNavigate = navigateToRoute
            )
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
                if (!pet?.imageUri.isNullOrEmpty()) {
                    // Try to load from URI string
                    val uri = try {
                        Uri.parse(pet?.imageUri)
                    } catch (e: Exception) {
                        null
                    }

                    if (uri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uri,
                                onError = {
                                    Log.e(
                                        "AdoptionPetDetailScreen",
                                        "Error loading image: ${it.result.throwable.message}"
                                    )
                                },
                                onSuccess = {
                                    // Removed log statement
                                }
                            ),
                            contentDescription = pet?.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback to default image if URI parsing failed
                        FallbackImage(category, pet?.name)
                    }
                } else {
                    // Use default image based on category
                    FallbackImage(category, pet?.name)
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
                        text = pet?.category ?: "",
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
                    text = pet?.name ?: "",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Pet breed/type
                Text(
                    text = pet?.type ?: "",
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
                        text = pet?.location ?: "",
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
                    text = pet?.description?.ifEmpty { "No description provided" }
                        ?: "No description provided",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Owner information section
                androidx.compose.material3.Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Owner Information",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (pet?.ownerName?.isNotBlank() == true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Owner name",
                                    tint = categoryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pet?.ownerName ?: "",
                                    fontSize = 15.sp,
                                    color = Color.DarkGray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        if (pet?.ownerEmail?.isNotBlank() == true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Owner email",
                                    tint = categoryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pet?.ownerEmail ?: "",
                                    fontSize = 15.sp,
                                    color = Color.DarkGray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = "Contact number",
                                tint = categoryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pet?.contactNumber?.ifEmpty { "No contact number provided" }
                                    ?: "No contact number provided",
                                fontSize = 15.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Management buttons for the owner
                if (isCurrentUserOwner) {
                    // Show adoption status if the pet is already adopted
                    if (pet?.isAdopted == true) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "This pet has been marked as adopted",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    } else {
                        // Mark as adopted button
                        androidx.compose.material3.OutlinedButton(
                            onClick = { showAdoptedConfirmDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFF4CAF50)), // Green border
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text(
                                text = "Mark as Adopted",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Delete listing button
                    androidx.compose.material3.OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.Red),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Text(
                            text = "Delete Listing",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Contact button - only show for non-owners
                if (!isCurrentUserOwner) {
                    Button(
                        onClick = {
                            val contactNumber = pet?.contactNumber ?: ""
                            if (contactNumber.isNotEmpty()) {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:$contactNumber")
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
    
    if (showAdoptedConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showAdoptedConfirmDialog = false },
            title = { Text("Mark as Adopted") },
            text = { 
                Text("Are you sure this pet has been adopted? This will mark the pet as no longer available for adoption.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pet?.id?.let { id ->
                                adoptionPetRepository.markAsAdopted(id).collect { result ->
                                    result.onSuccess {
                                        Toast.makeText(
                                            context,
                                            "Pet marked as adopted!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Refresh pet data
                                        adoptionPetRepository.getAdoptionPetById(petId).collect { petResult ->
                                            petResult.onSuccess { updatedPet ->
                                                pet = updatedPet
                                            }
                                        }
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            "Failed to mark as adopted: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                            showAdoptedConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50) // Green color
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAdoptedConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Listing") },
            text = { 
                Text("Are you sure you want to delete this adoption listing? This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            pet?.id?.let { id ->
                                adoptionPetRepository.removeAdoptionPet(id).collect { result ->
                                    result.onSuccess {
                                        Toast.makeText(
                                            context,
                                            "Listing deleted successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        // Navigate back
                                        onBackClick()
                                    }.onFailure { error ->
                                        Toast.makeText(
                                            context,
                                            "Failed to delete listing: ${error.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showDeleteConfirmDialog = false
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Gray
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FallbackImage(category: PetCategory, contentDescription: String?) {
    val imageRes = when (category) {
        PetCategory.DOG -> R.drawable.dog_nemo
        PetCategory.CAT -> R.drawable.cat_nero
        else -> R.drawable.pet_logo
    }

    Image(
        painter = painterResource(id = imageRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentScale = ContentScale.Crop
    )
}