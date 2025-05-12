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
    companion object {
        val selectedImageUri = mutableStateOf<Uri?>(null)

        fun saveImageToInternalStorage(context: Context, sourceUri: Uri): Uri? {
            try {
                val contentResolver: ContentResolver = context.contentResolver

                val imageFileName = "pethood_image_${UUID.randomUUID()}.jpg"
                val imagesDir = File(context.filesDir, "pet_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                val imageFile = File(imagesDir, imageFileName)

                contentResolver.openInputStream(sourceUri)?.use { input ->
                    FileOutputStream(imageFile).use { output ->
                        val buffer = ByteArray(4 * 1024)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }

                return Uri.fromFile(imageFile)
            } catch (e: IOException) {
                e.printStackTrace()
                android.util.Log.e("MainActivity", "Failed to save image: ${e.message}")
                return null
            }
        }
    }
    
    private var hadError = false
    
    private var currentScreen: String? = null
    
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        try {
            if (uri != null) {
                val savedUri = saveImageToInternalStorage(this, uri)

                selectedImageUri.value = savedUri
                android.util.Log.d("MainActivity", "Saved image URI: $savedUri")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting image URI: ${e.message}", e)
            Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
        }
    }
    
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
                        
                        val startDestination = if (hadError) {
                            Screen.Home.route
                        } else {
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
            android.util.Log.e("MainActivity", "Error setting up the app: ${e.message}", e)
            hadError = true
            
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
        currentScreen?.let {
            outState.putString("current_screen", it)
        }
        outState.putBoolean("had_error", hadError)
    }
}