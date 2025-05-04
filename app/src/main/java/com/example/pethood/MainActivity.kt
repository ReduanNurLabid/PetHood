package com.example.pethood

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.pethood.navigation.AppNavigation
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.theme.PetHoodTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MainActivity : ComponentActivity() {
    // Create a mutable state to hold the selected image URI
    companion object {
        val selectedImageUri = mutableStateOf<Uri?>(null)

        // Function to save an image URI to app storage and return the saved file URI
        fun saveImageToInternalStorage(context: Context, sourceUri: Uri): Uri? {
            try {
                val contentResolver: ContentResolver = context.contentResolver

                // Create a unique filename with UUID
                val imageFileName = "pethood_image_${UUID.randomUUID()}.jpg"
                val imagesDir = File(context.filesDir, "pet_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                val imageFile = File(imagesDir, imageFileName)

                // Copy the content from the source URI to our file
                contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        val buffer = ByteArray(4 * 1024) // 4kb buffer
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }

                // Return the URI for the saved file
                return Uri.fromFile(imageFile)
            } catch (e: IOException) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Failed to save image: ${e.message}")
                return null
            }
        }
    }
    
    // Track if there was a crash or error
    private var hadError = false
    
    // Track current navigation screen to restore after recreation
    private var currentScreen: String? = null
    
    // Register the activity result launcher to handle image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        try {
            // If a URI was returned from the picker
            if (uri != null) {
                // Save the image to internal storage
                val savedUri = saveImageToInternalStorage(this, uri)

                // Update the selected image URI with our saved file URI
                selectedImageUri.value = savedUri
                android.util.Log.d("MainActivity", "Saved image URI: $savedUri")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting image URI: ${e.message}", e)
            Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Function to launch the image picker
    fun pickImage() {
        try {
            imagePickerLauncher.launch("image/*")
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error launching image picker: ${e.message}", e)
            Toast.makeText(this, "Error selecting image. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Restore state if available
        savedInstanceState?.let {
            currentScreen = it.getString("current_screen")
            hadError = it.getBoolean("had_error", false)
        }
        
        try {
            enableEdgeToEdge()
            setContent {
                PetHoodTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        val navController = rememberNavController()
                        
                        // Use saved state to determine start destination
                        val startDestination = if (hadError) {
                            // If there was an error, go to home screen to recover
                            Screen.Home.route
                        } else {
                            // Otherwise use the saved screen if available
                            currentScreen
                        }
                        
                        AppNavigation(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Handle any errors during setup
            android.util.Log.e("MainActivity", "Error setting up the app: ${e.message}", e)
            hadError = true
            
            // Try to recover with a simpler UI
            setContent {
                PetHoodTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(startDestination = Screen.Home.route)
                    }
                }
            }
            
            Toast.makeText(this, "Had trouble loading. Restarting app.", Toast.LENGTH_LONG).show()
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current screen for restoration
        currentScreen?.let {
            outState.putString("current_screen", it)
        }
        outState.putBoolean("had_error", hadError)
    }
}