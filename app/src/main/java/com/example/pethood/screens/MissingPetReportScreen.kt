package com.example.pethood.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.MainActivity
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.ReportedPet
import com.example.pethood.data.ReportedPetRepository
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PetHoodTheme
import com.example.pethood.ui.theme.PrimaryRed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingPetReportScreen(
    onBackClick: () -> Unit = {},
    navigateToRoute: (Screen) -> Unit = {},
    onSubmitReport: () -> Unit = {}
) {
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("") }
    var lastSeen by remember { mutableStateOf("") }
    var petDescription by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val reportedPetRepository: ReportedPetRepository = PetHoodApplication.getInstance().reportedPetRepository
    val userRepository = PetHoodApplication.getInstance().userRepository
    // Get the selected image URI from the MainActivity
    val selectedImageUri = MainActivity.selectedImageUri.value

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Reports.route,
                onNavigate = { navigateToRoute(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryRed, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "PetHood",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Report a Missing Pet header
            Text(
                text = "Report a Missing Pet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enter your details subheader
            Text(
                text = "Enter your details",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pet name field
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                placeholder = { Text("Pet name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryRed
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Type of Pet field
            OutlinedTextField(
                value = petType,
                onValueChange = { petType = it },
                placeholder = { Text("Type of Pet") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryRed
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Last Seen field
            OutlinedTextField(
                value = lastSeen,
                onValueChange = { lastSeen = it },
                placeholder = { Text("Last Seen") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryRed
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pet description field
            OutlinedTextField(
                value = petDescription,
                onValueChange = { petDescription = it },
                placeholder = { Text("Write about your pet") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryRed
                ),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Contact number field
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                placeholder = { Text("Your contact number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = PrimaryRed
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image upload section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f)
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        // Launch the image picker when the box is clicked
                        try {
                            (context as? MainActivity)?.pickImage()
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Failed to open image picker: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Display the selected image
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                        contentDescription = "Selected pet image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Display the upload icon
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_image_upload),
                            contentDescription = "Upload Image",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Click to add pet image",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button
            Button(
                onClick = {
                    // Validate input
                    when {
                        petName.isBlank() -> {
                            errorMessage = "Please enter your pet's name"
                        }

                        petType.isBlank() -> {
                            errorMessage = "Please enter the type of pet"
                        }

                        lastSeen.isBlank() -> {
                            errorMessage = "Please enter where the pet was last seen"
                        }

                        petDescription.isBlank() -> {
                            errorMessage = "Please enter a description of your pet"
                        }

                        contactNumber.isBlank() -> {
                            errorMessage = "Please provide your contact number"
                        }

                        selectedImageUri == null -> {
                            errorMessage = "Please select an image of your pet"
                        }

                        else -> {
                            isLoading = true
                            errorMessage = null

                            // Get current user ID
                            val currentUserId = userRepository.getCurrentUserId()


                            // Create the reported pet
                            val reportedPet = ReportedPet(
                                name = petName,
                                type = petType,
                                lastSeen = lastSeen ,
                                description = petDescription,
                                contactNumber = contactNumber,
                                userId = currentUserId, // Add the reporter's ID
                                isMissing = true, // This is a missing pet report
                                imageUrl = "dog_snoop", // Using placeholder image for now
                                imageUri = selectedImageUri?.toString() ?: "" // Store the URI string
                            )
                            
                            // Save the report
                            CoroutineScope(Dispatchers.IO).launch {
                                reportedPetRepository.addReportedPet(reportedPet)
                            }

                            Toast.makeText(
                                context,
                                "Missing pet reported successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSubmitReport()

                            // Reset the selected image
                            MainActivity.selectedImageUri.value = null
                            
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFA30000)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Report Missing Pet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MissingPetReportScreenPreview() {
    PetHoodTheme {
        MissingPetReportScreen()
    }
}