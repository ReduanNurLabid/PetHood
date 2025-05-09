package com.example.pethood.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.MainActivity
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.AdoptionPet
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PetHoodTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdoptionFormScreen(
    onBackClick: () -> Unit,
    navigateToRoute: (Screen) -> Unit
) {
    val context = LocalContext.current
    val adoptionPetRepository = PetHoodApplication.getInstance().adoptionPetRepository
    
    var submitSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            coroutineScope.launch {
                delay(500)
                navigateToRoute(Screen.Home)
            }
        }
    }

    val adoptionColor = Color(0xFF9C27B0)
    
    // Form state
    var name by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("") }
    var petCategory by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    var petImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf("") }
    var useImageUrl by remember { mutableStateOf(false) }
    
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Cat", "Dog", "Bird")
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Save the image to internal storage for persistence
            val savedUri = MainActivity.saveImageToInternalStorage(context, uri)
            petImageUri = savedUri
        }
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
                .padding(16.dp)
        ) {
            // Header with back button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(adoptionColor, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Text(
                    text = "Put Pet Up for Adoption",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Image source toggle
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Image Source:",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
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
                    
                    RadioButton(
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
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Image selection section based on toggle
            if (useImageUrl) {
                // Image URL input
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "Enter a valid image URL (e.g., https://example.com/image.jpg)",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
                
                if (imageUrl.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUrl),
                            contentDescription = "Pet Image from URL",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            } else {
                // Pet Image Upload
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable {
                            try {
                                launcher.launch("image/*")
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "AdoptionFormScreen",
                                    "Error launching image picker: ${e.message}",
                                    e
                                )
                                Toast.makeText(
                                    context,
                                    "Error selecting image. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (petImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(model = petImageUri),
                            contentDescription = "Pet Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.pet_logo),
                                contentDescription = "Upload Image",
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Text(
                                text = "Tap to upload pet image",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pet Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Pet Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pet Category Dropdown
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = petCategory,
                    onValueChange = { /* Read-only field */ },
                    label = { Text("Pet Category") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand dropdown"
                            )
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable { expanded = !expanded }
                ) {}
                
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(text = category) },
                            onClick = {
                                petCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pet Type/Breed
            OutlinedTextField(
                value = petType,
                onValueChange = { petType = it },
                label = { Text("Pet Breed/Type") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contact Number
            OutlinedTextField(
                value = contactNumber,
                onValueChange = { contactNumber = it },
                label = { Text("Contact Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Submit Button
            Button(
                onClick = {
                    if (validateForm(name, petCategory, petType, location, contactNumber)) {
                        try {
                            val userId = PetHoodApplication.getInstance().userRepository.getCurrentUserId()
                            val imageUriString = petImageUri?.toString() ?: ""
                            android.util.Log.d("AdoptionFormScreen", "Using image URI for submission: $imageUriString")

                            petImageUri?.let {
                                android.util.Log.d(
                                    "AdoptionFormScreen",
                                    "Image URI scheme: ${it.scheme}"
                                )
                                android.util.Log.d(
                                    "AdoptionFormScreen",
                                    "Image URI path: ${it.path}"
                                )
                                android.util.Log.d(
                                    "AdoptionFormScreen",
                                    "Image URI exists: ${
                                        context.contentResolver.openInputStream(it)?.close()
                                    }"
                                )
                            }

                            val tempId = "temp-" + UUID.randomUUID().toString()

                            // Determine which image source to use
                            val finalImageSource = if (useImageUrl && imageUrl.isNotBlank()) {
                                imageUrl
                            } else {
                                imageUriString
                            }

                            val newPet = AdoptionPet(
                                id = tempId,
                                name = name,
                                category = petCategory,
                                type = petType,
                                location = location,
                                description = description,
                                contactNumber = contactNumber,
                                imageUri = finalImageSource,
                                userId = userId,
                                date = Date()
                            )

                            coroutineScope.launch {
                                try {
                                    adoptionPetRepository.addAdoptionPet(newPet)
                                        .collect { result ->
                                            result.fold(
                                                onSuccess = {
                                                    android.util.Log.d(
                                                        "AdoptionFormScreen",
                                                        "Pet adoption submission successful"
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Pet successfully put up for adoption!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    submitSuccess = true
                                                },
                                                onFailure = { exception ->
                                                    android.util.Log.e(
                                                        "AdoptionFormScreen",
                                                        "Error submitting pet: ${exception.message}",
                                                        exception
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Error creating adoption: ${exception.message}",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            )
                                        }
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "Error creating adoption: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error creating adoption: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = adoptionColor
                )
            ) {
                Text(
                    text = "Put Up for Adoption",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Validation function
private fun validateForm(
    name: String,
    category: String,
    type: String,
    location: String,
    contactNumber: String
): Boolean {
    return name.isNotBlank() && 
           category.isNotBlank() && 
           type.isNotBlank() && 
           location.isNotBlank() && 
           contactNumber.isNotBlank()
}

@Preview(showBackground = true)
@Composable
fun AdoptionFormScreenPreview() {
    PetHoodTheme {
        AdoptionFormScreen(
            onBackClick = {},
            navigateToRoute = {}
        )
    }
}