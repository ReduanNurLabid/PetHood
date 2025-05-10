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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.foundation.text.KeyboardOptions
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import com.example.pethood.MainActivity
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.ReportedPet
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PetHoodTheme
import com.example.pethood.ui.theme.PrimaryRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoundPetReportScreen(
    onBackClick: () -> Unit = {},
    navigateToRoute: (Screen) -> Unit = {},
    onSubmitReport: () -> Unit = {}
) {
    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("") }
    var lastSeen by remember { mutableStateOf("") }
    var petDetails by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Image URL variables
    var useImageUrl by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }

    val context = LocalContext.current
    val reportedPetRepository = PetHoodApplication.getInstance().reportedPetRepository
    val userRepository = PetHoodApplication.getInstance().userRepository
    // Get the selected image URI from the MainActivity
    val selectedImageUri = MainActivity.selectedImageUri.value
    val coroutineScope = rememberCoroutineScope()

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

            // Report a Found Pet header
            Text(
                text = "Report a Found Pet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50) // Green color
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Enter details subheader
            Text(
                text = "Enter details about the pet you found",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pet name field
            OutlinedTextField(
                value = petName,
                onValueChange = { petName = it },
                placeholder = { Text("Pet name (if known)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF4CAF50) // Green color
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
                    focusedBorderColor = Color(0xFF4CAF50) // Green color
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
                    focusedBorderColor = Color(0xFF4CAF50) // Green color
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Pet details field
            OutlinedTextField(
                value = petDetails,
                onValueChange = { petDetails = it },
                placeholder = { Text("Details about the pet") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = Color(0xFF4CAF50) // Green color
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
                    focusedBorderColor = Color(0xFF4CAF50) // Green color
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Image upload section
            Text(
                text = "Upload Pet Image",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF4CAF50) // Green color to match theme
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Image source toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                androidx.compose.material3.RadioButton(
                    selected = !useImageUrl,
                    onClick = { useImageUrl = false }
                )
                Text(
                    text = "Upload Image",
                    modifier = Modifier
                        .clickable { useImageUrl = false }
                        .padding(start = 4.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                androidx.compose.material3.RadioButton(
                    selected = useImageUrl,
                    onClick = { useImageUrl = true }
                )
                Text(
                    text = "Use Image URL",
                    modifier = Modifier
                        .clickable { useImageUrl = true }
                        .padding(start = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (useImageUrl) {
                // Image URL input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    placeholder = { Text("Enter image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = Color(0xFF4CAF50) // Green color to match theme
                    ),
                    singleLine = true
                )
                
                if (imageUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f)
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clip(RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "Pet image from URL",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit button - green color
            Button(
                onClick = {
                    // Validate input
                    when {
                        petName.isBlank() -> {
                            errorMessage = "Please enter a name for the found pet"
                        }

                        petType.isBlank() -> {
                            errorMessage = "Please enter the type of pet"
                        }

                        lastSeen.isBlank() -> {
                            errorMessage = "Please enter where the pet was found"
                        }

                        petDetails.isBlank() -> {
                            errorMessage = "Please enter details about the pet"
                        }
                        
                        contactNumber.isBlank() -> {
                            errorMessage = "Please provide your contact number"
                        }
                        
                        !useImageUrl && selectedImageUri == null -> {
                            errorMessage = "Please add a photo of the pet"
                        }
                        
                        useImageUrl && imageUrl.isBlank() -> {
                            errorMessage = "Please enter an image URL"
                        }

                        else -> {
                           coroutineScope.launch {
                               isLoading = true
                               errorMessage = null

                               // Get current user ID
                               val currentUserId = userRepository.getCurrentUserId()

                               if(currentUserId != null){
                                   // Get current user details
                                   val currentUser = userRepository.getCurrentUser()
                                   val reporterName = currentUser?.name ?: ""
                                   val reporterEmail = currentUser?.email ?: ""
                                   
                                   // Determine image source
                                   val finalImageSource = if (useImageUrl) imageUrl else selectedImageUri?.toString() ?: ""
                               
                                   // Create the reported pet
                                   val reportedPet = ReportedPet(
                                       name = petName,
                                       type = petType,
                                       lastSeen = lastSeen,
                                       description = petDetails,
                                       contactNumber = contactNumber,
                                       userId = currentUserId, // Add the reporter's ID
                                       reporterName = reporterName, // Add reporter's name
                                       reporterEmail = reporterEmail, // Add reporter's email
                                       isMissing = false, // This is a found pet report
                                       imageUrl = "cat_bunty", // Using placeholder image for now
                                       imageUri = finalImageSource // Store the URI string or URL
                                   )

                                   // Save the report
                                   reportedPetRepository.addReportedPet(reportedPet)

                                   Toast.makeText(
                                       context,
                                       "Found pet reported successfully!",
                                       Toast.LENGTH_SHORT
                                   ).show()
                                   onSubmitReport()

                                   // Reset the selected image
                                   MainActivity.selectedImageUri.value = null
                               }

                               isLoading = false
                           }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50) // Green color
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Report Found Pet",
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
fun FoundPetReportScreenPreview() {
    PetHoodTheme {
        FoundPetReportScreen()
    }
}