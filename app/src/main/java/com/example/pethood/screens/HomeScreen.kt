package com.example.pethood.screens

import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.Pet
import com.example.pethood.data.PetCategory
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.components.CategorySelector
import com.example.pethood.ui.components.PetCard
import com.example.pethood.ui.components.SearchBar
import kotlinx.coroutines.flow.Flow

@SuppressLint("FlowOperatorInvokedInComposition")
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navigateToRoute: (Screen) -> Unit = {},
    petRepository: com.example.pethood.data.PetRepository = remember {
        PetHoodApplication.getInstance().petRepository
    },
    onPetClick: (Pet) -> Unit = {},
    onPutUpForAdoptionClick: () -> Unit = {},
    onAdoptionPetClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PetCategory.DOG) }

    //Get the pets from the repository
    val allPets: List<Pet> by petRepository.getPets().observeAsState(listOf())

    // Filter pets by category and search query
    val filteredPets = remember(allPets, searchQuery, selectedCategory) {
        allPets.filter { pet ->
            val matchesCategory = when (selectedCategory) {
                PetCategory.DOG -> pet.category.equals("Dog", ignoreCase = true)
                PetCategory.CAT -> pet.category.equals("Cat", ignoreCase = true)
                else -> pet.category.equals("Bird", ignoreCase = true) // Assuming other means Bird
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
    
    // Remember pager states for each category
    val dogPagerState = rememberPagerState(initialPage = 0, pageCount = { dogPets.size })
    val catPagerState = rememberPagerState(initialPage = 0, pageCount = { catPets.size })
    val birdPagerState = rememberPagerState(initialPage = 0, pageCount = { birdPets.size })

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

                // Display pets for the selected category
                when (selectedCategory) {
                    PetCategory.DOG -> {
                        DisplayPets(
                            pets = filteredPets,
                            category = PetCategory.DOG,
                            pagerState = pagerState,
                            onPetClick = { pet ->
                                onAdoptionPetClick(pet.id)
                            },
                            onContactClick = { pet ->
                                if (pet.contactNumber.isNotEmpty()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${pet.contactNumber}")
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
                    PetCategory.CAT -> {
                        DisplayPets(
                            pets = filteredPets,
                            category = PetCategory.CAT,
                            pagerState = pagerState,
                            onPetClick = { pet ->
                                onAdoptionPetClick(pet.id)
                            },
                            onContactClick = { pet ->
                                if (pet.contactNumber.isNotEmpty()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${pet.contactNumber}")
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
                    else -> {
                        DisplayPets(
                            pets = filteredPets,
                            category = PetCategory.BIRD,
                            pagerState = birdPagerState,
                            onPetClick = { pet ->
                                onAdoptionPetClick(pet.id)
                            },
                            onContactClick = { pet ->
                                if (pet.contactNumber.isNotEmpty()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${pet.contactNumber}")
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
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun DisplayPets(
    pets: List<Pet>,
    category: PetCategory,
    pagerState: androidx.compose.foundation.pager.PagerState,
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
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val pet = pets[page]
            PetCard(
                pet = pet,
                category = category,
                onCardClick = { onPetClick(pet) },
                onContactClick = { onContactClick(pet) }
            )
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
}