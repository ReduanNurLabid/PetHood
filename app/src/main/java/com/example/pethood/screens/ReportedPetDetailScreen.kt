package com.example.pethood.screens

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.Pet
import com.example.pethood.data.ReportedPet
import com.example.pethood.data.ReportedPetRepository
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PetHoodTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun ReportedPetDetailScreen(
    petId: String,
    onBackClick: () -> Unit,
    onContactClick: () -> Unit = {},
    navigateToRoute: (Screen) -> Unit,
    isCurrentUserReporter: Boolean = false,
    onPutUpForAdoptionClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val reportedPetRepository =
        PetHoodApplication.getInstance().reportedPetRepository
    var pet by remember { mutableStateOf<ReportedPet?>(null) }
    scope.launch {
        pet = reportedPetRepository.getReportedPet(petId)
    }

    if(pet==null){
        return
    }

    val statusColor = if (pet!!.isMissing) Color(0xFFA30000) else Color(0xFF4CAF50)
    val statusText = if (pet!!.isMissing) "Missing" else "Found"
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(pet!!.date)

    // Show contact dialog
    var showContactDialog by remember { mutableStateOf(false) }

    if (showContactDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showContactDialog = false },
            title = { Text("Contact Information") },
            text = {
                Column {
                    Text("Would you like to contact about this ${if (pet!!.isMissing) "missing" else "found"} pet?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Phone: ${pet!!.contactNumber.ifEmpty { "Not provided" }}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showContactDialog = false
                        // Open phone dialer with the actual contact number
                        if (pet!!.contactNumber.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${pet!!.contactNumber}")
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
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                    enabled = pet!!.contactNumber.isNotEmpty()
                ) {
                    Text("Call")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showContactDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show confirmation dialog for marking pet as found/adopted
    var showConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text(if (pet!!.isMissing) "Mark as Found" else "Mark as Adopted") },
            text = {
                Text(
                    if (pet!!.isMissing)
                        "Are you sure this pet has been found? This will remove the pet from the missing pets listings."
                    else
                        "Are you sure this pet has been handed to adoption? This will remove the pet from the found pets listings."
                )
            },
            confirmButton = {
                Button(
                    onClick = {                        scope.launch { reportedPetRepository.deletePet(pet!!.id, pet!!.isMissing) }
                        Toast.makeText(
                            context,
                            if (pet.isMissing) "Pet marked as found!" else "Pet marked as adopted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        showConfirmationDialog = false
                        // Navigate back to the FindLost screen
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showConfirmationDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.FindLost.route,
                onNavigate = { navigateToRoute(it) }
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
                if (pet!!.imageUri != null && pet!!.imageUri.isNotEmpty()) {
                    // Display uploaded image
                    val uri = try {
                        Uri.parse(pet!!.imageUri)
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "ReportedPetDetailScreen",
                            "Failed to parse URI: ${e.message}",
                            e,
                        )
                        null
                    }

                    if (uri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uri,
                                onError = {e->
                                    android.util.Log.e(
                                        "ReportedPetDetailScreen",
                                        "Error loading image: ${it.result.throwable.message}"
                                    )
                                }
                            ),
                            contentDescription = pet.name,
                            modifier = Modifier.
                                .fillMaxWidth()
                                .height(300.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback to template image if URI parsing failed
                        Image(
                            painter = painterResource(id = R.drawable.pet_logo),
                            contentDescription = pet!!.name ?: "Pet",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp) ,
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Display template image
                    val resourceId = if (pet.imageUrl != null && pet.imageUrl.isNotEmpty()) {
                        context.resources.getIdentifier(
                            pet!!.imageUrl, "drawable", context.packageName
                        )
                    } else 0

                    val imageRes = if (resourceId != 0) resourceId else R.drawable.pet_logo

                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = pet!!.name ?: "Pet",
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
                        .background(statusColor, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            color = statusColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pet information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Pet name and type
                Text(
                    text = pet!!.name ?: "Unknown Pet",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

               Text(
                    text = pet.type ?: "Unknown Type",
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ReportInfoCard(
                        title = "Date Reported",
                        value = formattedDate ,
                        modifier = Modifier.weight(1f)
                    )

                    // Location card
                    ReportInfoCard(
                        title = if (pet!!.isMissing) "Last Seen" else "Found At",
                        value = pet!!.lastSeen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About section
                Text(
                    text = "Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = pet!!.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Show different buttons based on whether current user is the reporter
                if (isCurrentUserReporter ) {
                    Column {
                        // Mark as found/adopted button
                        OutlinedButton(
                            onClick = { showConfirmationDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, statusColor),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = statusColor
                            )
                        ) {
                           Text(
                                text = if (pet.isMissing) "Mark as Found" else "Mark as Adopted",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        if (!pet.isMissing) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = onPutUpForAdoptionClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9C27B0) // Purple color for adoption
                                )
                            ) {
                              Text(
                                    text = "Put Up for Adoption",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Other users can directly call the reporter
                    Button(
                        onClick = {
                            if (pet!!.contactNumber.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${pet!!.contactNumber}")
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
                            .height(50.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = statusColor
                        ),
                        enabled = pet!!.contactNumber.isNotEmpty()
                    ) {
                        Text(
                            text = if (pet!!.isMissing) "I've Found This Pet" else "I'm The Owner",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportInfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F3F7)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissingPetDetailScreenAsReporterPreview() {
    val samplePet = ReportedPet(
        id = 1,
        name = "Max",
        type = "Golden Retriever",
        lastSeen = "Central Park",
        description = "Medium-sized golden retriever with a blue collar. Responds to 'Max'.",
        imageUrl = "dog_zimmer",
        imageUri = "",
        contactNumber = "555-123-4567",
        reporterId = "user1",
        isMissing = true,
        date = Date()
    )
    
   PetHoodTheme {
        ReportedPetDetailScreen(
            pet = samplePet,
            onBackClick = {},
            navigateToRoute = {},
            isCurrentUserReporter = true // User is the reporter
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MissingPetDetailScreenAsNonReporterPreview() {
    val samplePet = ReportedPet(
        id = 1,
        name = "Max",
        type = "Golden Retriever",
        lastSeen = "Central Park",
        description = "Medium-sized golden retriever with a blue collar. Responds to 'Max'.",
        imageUrl = "dog_zimmer",
        imageUri = "",
        contactNumber = "555-123-4567",
        reporterId = "user1",
        isMissing = true,
        date = Date()
    )
    
    PetHoodTheme {
        ReportedPetDetailScreen(
            pet = samplePet,
            onBackClick = {},
            navigateToRoute = {},
            isCurrentUserReporter = false // User is not the reporter
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FoundPetDetailScreenAsReporterPreview() {
    val samplePet = ReportedPet(
        id = 1,
        name = "Unknown Dog",
        type = "Mixed Breed",
        lastSeen = "Park Avenue",
        description = "Medium-sized mixed breed dog. No collar but seems well-behaved.",
        imageUrl = "dog_nemo",
        imageUri = "",
        contactNumber = "555-246-8135",
        reporterId = "user3",
        isMissing = false,
        date = Date()
    )
    
    PetHoodTheme {
        ReportedPetDetailScreen(
            pet = samplePet,
            onBackClick = {},
            navigateToRoute = {},
            isCurrentUserReporter = true, // User is the reporter
            onPutUpForAdoptionClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FoundPetDetailScreenAsNonReporterPreview() {
    val samplePet = ReportedPet(
        id = 1,
        name = "Unknown Dog",
        type = "Mixed Breed",
        lastSeen = "Park Avenue",
        description = "Medium-sized mixed breed dog. No collar but seems well-behaved.",
        imageUrl = "dog_nemo",
        imageUri = "",
        contactNumber = "555-246-8135",
        reporterId = "user3",
        isMissing = false,
        date = Date()
    )
    
    PetHoodTheme {
        ReportedPetDetailScreen(
            pet = samplePet,
            onBackClick = {},
            navigateToRoute = {},
            isCurrentUserReporter = false // User is not the reporter
        )
    }
} 