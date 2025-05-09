package com.example.pethood.screens

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.AdoptionPetRepository
import com.example.pethood.data.Pet
import com.example.pethood.data.PetCategory
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.components.CategorySelector
import com.example.pethood.ui.components.PetCard
import com.example.pethood.ui.components.SearchBar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navigateToRoute: (Screen) -> Unit = {},
    adoptionPetRepository: AdoptionPetRepository = PetHoodApplication.getInstance().adoptionPetRepository,
    onPetClick: (Pet) -> Unit = {},
    onPutUpForAdoptionClick: () -> Unit = {},
    onAdoptionPetClick: (String) -> Unit = {},
    refreshTrigger: Int = 0 // External refresh trigger
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PetCategory.DOG) }
    val scope = rememberCoroutineScope()

    // Store pets in state
    var allPets by remember { mutableStateOf<List<Pet>>(emptyList()) }

    // Add a refresh trigger to reload data when needed
    var internalRefreshTrigger by remember { mutableStateOf(0) }

    // Combine external and internal refresh triggers
    val combinedRefreshTrigger = refreshTrigger + internalRefreshTrigger

    // Function to load adoption pets
    fun loadAdoptionPets() {
        scope.launch {
            try {
                adoptionPetRepository.getAllAdoptionPets()
                    .collect { result ->
                        result.fold(
                            onSuccess = { adoptionPets ->
                                Log.d("HomeScreen", "Loaded ${adoptionPets.size} adoption pets")

                                // Convert AdoptionPet to Pet
                                allPets = adoptionPets.map { adoptionPet ->
                                    Pet(
                                        id = adoptionPet.id,
                                        name = adoptionPet.name,
                                        breed = adoptionPet.type,
                                        category = when (adoptionPet.category.lowercase()) {
                                            "dog" -> PetCategory.DOG
                                            "cat" -> PetCategory.CAT
                                            else -> PetCategory.OTHER
                                        },
                                        gender = com.example.pethood.data.PetGender.MALE, // Default
                                        age = 0, // Not available
                                        description = adoptionPet.description,
                                        imageUrl = adoptionPet.imageUri, // Use the URI string directly
                                        contactNumber = adoptionPet.contactNumber,
                                        location = adoptionPet.location
                                    ).also {
                                        // Debug log to verify the image URL
                                        Log.d(
                                            "HomeScreen",
                                            "Pet ${it.name} has image URL: ${it.imageUrl}"
                                        )
                                    }
                                }
                            },
                            onFailure = { exception ->
                                // Handle failure
                                Log.e("HomeScreen", "Failed to load pets: ${exception.message}")
                                Toast.makeText(
                                    context,
                                    "Failed to load pets: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Exception when loading pets: ${e.message}", e)
                Toast.makeText(
                    context,
                    "Failed to load pets: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Load data when screen is first created and force refresh when screen appears
    DisposableEffect(Unit) {
        // Force an immediate data refresh when the screen is created
        loadAdoptionPets()

        // Schedule another refresh after a short delay to ensure data is loaded
        scope.launch {
            delay(300)
            internalRefreshTrigger++
        }

        onDispose { }
    }

    // Refresh data when either trigger changes
    LaunchedEffect(combinedRefreshTrigger) {
        Log.d("HomeScreen", "Refreshing data with trigger: $combinedRefreshTrigger")
        loadAdoptionPets()
    }

    // Check data and log results
    LaunchedEffect(allPets) {
        Log.d("HomeScreen", "Loaded ${allPets.size} pets")
        if (allPets.isEmpty()) {
            Log.d("HomeScreen", "No pets loaded, checking Firestore directly")
            // Try direct check
            scope.launch {
                try {
                    val snapshot = Firebase.firestore.collection("adoptionPets").get().await()
                    Log.d(
                        "HomeScreen",
                        "Direct Firestore check: ${snapshot.size()} documents found"
                    )
                    snapshot.documents.forEach { doc ->
                        Log.d("HomeScreen", "Document: ${doc.id} - ${doc.data}")
                    }
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Error checking Firestore directly: ${e.message}", e)
                }
            }
        }
    }

    // Filter pets by category and search query
    val filteredPets = remember(allPets, searchQuery, selectedCategory) {
        allPets.filter { pet ->
            val matchesCategory = when (selectedCategory) {
                PetCategory.DOG -> pet.category == PetCategory.DOG
                PetCategory.CAT -> pet.category == PetCategory.CAT
                else -> pet.category == PetCategory.OTHER
            }
            val matchesSearchQuery = searchQuery.isEmpty() ||
                    pet.name.contains(searchQuery, ignoreCase = true) ||
                    pet.breed.contains(searchQuery, ignoreCase = true)
            
            matchesCategory && matchesSearchQuery
        }
    }

    val pagerState = rememberPagerState(initialPage = 0) {
        filteredPets.size
    }

    val gradient = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.1f), Color(0xFFE0E0E0).copy(alpha = 0.3f)), tileMode = TileMode.Clamp)
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Home.route,
                onNavigate = { navigateToRoute(it) }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(gradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // App title and Adoption button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PetHood",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Button to put a pet up for adoption
                    Button(
                        onClick = onPutUpForAdoptionClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C27B0)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Put Up for Adoption",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Search bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                // Categories
                CategorySelector(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )

                DisplayPets(
                    pets = filteredPets,
                    category = selectedCategory,
                    pagerState = pagerState,
                    onPetClick = { pet ->
                        onAdoptionPetClick(pet.id)
                    },
                    onContactClick = { pet ->
                        if (pet.contactNumber.isNotEmpty()) {
                            val intent =
                                android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${pet.contactNumber}")
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
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayPets(
    pets: List<Pet>,
    category: PetCategory,
    pagerState: PagerState,
    onPetClick: (Pet) -> Unit,
    onContactClick: (Pet) -> Unit
) {
    if (pets.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.pet_logo),
                    contentDescription = "No pets",
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No pets available in this category",
                    fontSize = 18.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Try another category or check back later",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
        ) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val pet = pets[page]
                
                // Adding logger to check image URLs
                Log.d("DisplayPets", "Pet: ${pet.name}, Image URL: ${pet.imageUrl}")
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 16.dp)
                ) {
                    PetCard(
                        pet = pet,
                        onPetClick = { onPetClick(pet) },
                        onFavoriteClick = { /* Do nothing for now */ },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Add contact button over the card
                    Button(
                        onClick = { onContactClick(pet) },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_phone),
                            contentDescription = "Contact",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contact Owner",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Add page indicators
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in pets.indices) {
                    val isSelected = i == pagerState.currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                if (isSelected) Color(0xFF9C27B0) else Color.Gray.copy(alpha = 0.5f),
                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}