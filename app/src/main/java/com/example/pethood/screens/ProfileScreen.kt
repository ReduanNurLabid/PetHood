package com.example.pethood.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.data.User
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PetHoodTheme
import com.example.pethood.ui.theme.PrimaryRed
import kotlinx.coroutines.flow.Flow
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateToRoute: (Screen) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository = PetHoodApplication.getInstance().userRepository
    var currentUser by remember { mutableStateOf<User?>(null) }
    
    // Fetch user details
    val userFlow: Flow<User?> = remember {
        userRepository.getUser()
    }

    androidx.compose.runtime.LaunchedEffect(userFlow) {
        userFlow.collect { user ->
            currentUser = user
        }
    }
    
    // State variables for the dialogs
    var showImageUrlDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf(currentUser?.profileImageUrl ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Profile.route,
                onNavigate = { navigateToRoute(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // User avatar with edit button
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser?.profileImageUrl?.isNotEmpty() == true) {
                    // Load profile image from URL
                    Image(
                        painter = rememberAsyncImagePainter(model = currentUser.profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default icon if no image URL
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp),
                        tint = PrimaryRed
                    )
                }
                
                // Edit button overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PrimaryRed),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { showImageUrlDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Image",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // User info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "User Information",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentUser != null) {
                        Text(
                            text = "Name",
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = "${currentUser.name}", color = Color.Gray)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Email",
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = "${currentUser.email}", color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Phone: ${currentUser.phoneNumber.ifEmpty { "Not provided" }}",
                            fontSize = 16.sp,
                            color = Color.DarkGray
                        )
                    } else {
                        Text(
                            text = "User information not available",
                            fontSize = 16.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Account actions section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Account Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Update Profile Image Button
                    Button(
                        onClick = { showImageUrlDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Update Profile Image",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Change Password Button
                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Change Password",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout button
            Button(
                onClick = {
                    userRepository.logout()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Logout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Profile Image URL Dialog
    if (showImageUrlDialog) {
        Popup(onDismissRequest = { showImageUrlDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Update Profile Image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showImageUrlDialog = false }
                        ) {
                            Text("Cancel")
                        }
                        
                        TextButton(
                            onClick = {
                                if (imageUrl.isNotEmpty()) {
                                    val success = userRepository.updateProfileImageUrl(imageUrl)
                                    if (success) {
                                        Toast.makeText(context, "Profile image updated", Toast.LENGTH_SHORT).show()
                                        showImageUrlDialog = false
                                    } else {
                                        Toast.makeText(context, "Failed to update profile image", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
    
    // Change Password Dialog
    if (showPasswordDialog) {
        Popup(onDismissRequest = { showPasswordDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Change Password",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Current Password
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // New Password
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Confirm New Password
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true
                    )
                    
                    // Error message if any
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { 
                                showPasswordDialog = false
                                // Reset fields and error message
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                                errorMessage = null
                            }
                        ) {
                            Text("Cancel")
                        }
                        
                        TextButton(
                            onClick = {
                                when {
                                    currentPassword.isEmpty() -> {
                                        errorMessage = "Please enter your current password"
                                    }
                                    newPassword.isEmpty() -> {
                                        errorMessage = "Please enter a new password"
                                    }
                                    newPassword.length < 6 -> {
                                        errorMessage = "Password must be at least 6 characters"
                                    }
                                    newPassword != confirmPassword -> {
                                        errorMessage = "Passwords do not match"
                                    }
                                    else -> {
                                        val success = userRepository.updatePassword(currentPassword, newPassword)
                                        if (success) {
                                            Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                            showPasswordDialog = false
                                            // Reset fields and error message
                                            currentPassword = ""
                                            newPassword = ""
                                            confirmPassword = ""
                                            errorMessage = null
                                        } else {
                                            errorMessage = "Current password is incorrect"
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview(){
    PetHoodTheme {
        ProfileScreen()
    }
}