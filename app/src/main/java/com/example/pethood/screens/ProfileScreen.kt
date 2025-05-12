package com.example.pethood.screens

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PetHoodTheme
import com.example.pethood.ui.theme.PrimaryRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navigateToRoute: (Screen) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userRepository = PetHoodApplication.getInstance().userRepository
    var currentUser by remember { mutableStateOf(userRepository.getCurrentUser()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        isLoading = true
        try {
            val refreshedUser = userRepository.refreshCurrentUser()
            currentUser = refreshedUser ?: userRepository.getCurrentUser()
            loadError = null
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error refreshing user data", e)
            loadError = "Failed to load profile data: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    var showImageUrlDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imageUrlFocusRequester = remember { FocusRequester() }
    val phoneNumberFocusRequester = remember { FocusRequester() }
    val nameFocusRequester = remember { FocusRequester() }
    val currentPasswordFocusRequester = remember { FocusRequester() }

    androidx.compose.runtime.LaunchedEffect(showImageUrlDialog, showPhoneDialog, showNameDialog) {
        if (showImageUrlDialog) {
            imageUrl = currentUser?.profileImageUrl ?: ""
            kotlinx.coroutines.delay(100)
            try {
                imageUrlFocusRequester.requestFocus()
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Failed to request focus", e)
            }
        }
        if (showPhoneDialog) {
            phoneNumber = currentUser?.phoneNumber ?: ""
            kotlinx.coroutines.delay(100)
            try {
                phoneNumberFocusRequester.requestFocus()
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Failed to request focus", e)
            }
        }
        if (showNameDialog) {
            userName = currentUser?.name ?: ""
            kotlinx.coroutines.delay(100)
            try {
                nameFocusRequester.requestFocus()
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Failed to request focus", e)
            }
        }
    }

    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

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

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(color = PrimaryRed)
                    }
                }
                loadError != null -> {
                    Text(
                        text = loadError!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (currentUser?.profileImageUrl?.isNotEmpty() == true) {
                    Image(
                        painter = rememberAsyncImagePainter(model = currentUser?.profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(16.dp),
                        tint = PrimaryRed
                    )
                }
                
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentUser?.name ?: "",
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showNameDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Email",
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = currentUser?.email ?: "", color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Phone",
                            fontSize = 18.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentUser?.phoneNumber?.isBlank() == true) "Not provided" else currentUser?.phoneNumber
                                    ?: "Not provided",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showPhoneDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Phone",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
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

                    Button(
                        onClick = { showNameDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Update Name",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showPhoneDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF03A9F4)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Update Phone Number",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
    
    if (showImageUrlDialog) {
        AlertDialog(
            onDismissRequest = { showImageUrlDialog = false },
            title = {
                Text(
                    text = "Update Profile Image",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    placeholder = { Text("https://example.com/image.jpg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(imageUrlFocusRequester),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (imageUrl.isNotEmpty()) {
                            coroutineScope.launch {
                                val success = userRepository.updateProfileImageUrl(imageUrl)
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Profile image updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showImageUrlDialog = false
                                    currentUser = userRepository.getCurrentUser()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to update profile image",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Please enter a valid URL", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = {
                Text(
                    text = "Change Password",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(currentPasswordFocusRequester),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                    
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {
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
                                coroutineScope.launch {
                                    val success = userRepository.updatePassword(
                                        currentPassword,
                                        newPassword
                                    )
                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "Password updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        showPasswordDialog = false
                                        currentPassword = ""
                                        newPassword = ""
                                        confirmPassword = ""
                                        errorMessage = null
                                        currentUser = userRepository.getCurrentUser()
                                    } else {
                                        errorMessage = "Current password is incorrect"
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    errorMessage = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = {
                Text(
                    text = "Update Phone Number",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(phoneNumberFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (phoneNumber.isNotEmpty()) {
                            coroutineScope.launch {
                                val success = userRepository.updatePhoneNumber(phoneNumber)
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Phone number updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showPhoneDialog = false
                                    currentUser = userRepository.getCurrentUser()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to update phone number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a valid phone number",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = {
                Text(
                    text = "Update Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (userName.isNotEmpty()) {
                            coroutineScope.launch {
                                val success = userRepository.updateName(userName)
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "Name updated",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showNameDialog = false
                                    currentUser = userRepository.getCurrentUser()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to update name",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Please enter a valid name",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview(){
    PetHoodTheme {
        ProfileScreen()
    }
}