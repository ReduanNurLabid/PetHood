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
    
    var useImageUrl by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }

    val context = LocalContext.current
    val reportedPetRepository: ReportedPetRepository = PetHoodApplication.getInstance().reportedPetRepository
    val userRepository = PetHoodApplication.getInstance().userRepository
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

            Text(
                text = "Report a Missing Pet",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your details",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Text(
                text = "Upload Pet Image",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = PrimaryRed
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    placeholder = { Text("Enter image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = PrimaryRed
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
                        Image(
                            painter = rememberAsyncImagePainter(model = selectedImageUri),
                            contentDescription = "Selected pet image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
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

            Button(
                onClick = {
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

                        !useImageUrl && selectedImageUri == null -> {
                            errorMessage = "Please select an image of your pet"
                        }
                        
                        useImageUrl && imageUrl.isBlank() -> {
                            errorMessage = "Please enter an image URL"
                        }

                        else -> {
                            isLoading = true
                            errorMessage = null

                            val currentUserId = userRepository.getCurrentUserId()
                            val currentUser = userRepository.getCurrentUser()
                            val reporterName = currentUser?.name ?: ""
                            val reporterEmail = currentUser?.email ?: ""

                            val finalImageSource = if (useImageUrl) imageUrl else selectedImageUri?.toString() ?: ""

                            val reportedPet = ReportedPet(
                                name = petName,
                                type = petType,
                                lastSeen = lastSeen,
                                description = petDescription,
                                contactNumber = contactNumber,
                                userId = currentUserId,
                                reporterName = reporterName,
                                reporterEmail = reporterEmail,
                                isMissing = true,
                                imageUrl = "dog_snoop",
                                imageUri = finalImageSource
                            )

                            CoroutineScope(Dispatchers.IO).launch {
                                reportedPetRepository.addReportedPet(reportedPet)
                            }

                            Toast.makeText(
                                context,
                                "Missing pet reported successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onSubmitReport()

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