package com.example.pethood.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.AdoptionPet
import com.example.pethood.data.Pet
import com.example.pethood.data.PetCategory
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.components.CategorySelector
import com.example.pethood.ui.components.SearchBar

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navigateToRoute: (Screen) -> Unit = {},
    onPetClick: (Pet) -> Unit = {},
    onPutUpForAdoptionClick: () -> Unit = {},
    onAdoptionPetClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(PetCategory.DOG) }
    
    // Get adoption pets from repository
    val adoptionPetRepository = remember { PetHoodApplication.getInstance().adoptionPetRepository }
    val allAdoptionPets = remember { adoptionPetRepository.getAllAdoptionPets() }
    
    // Create filtered lists for each category
    val dogPets = remember(allAdoptionPets, searchQuery) {
        allAdoptionPets.filter { 
            it.category.equals("Dog", ignoreCase = true) &&
            (searchQuery.isEmpty() || 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.type.contains(searchQuery, ignoreCase = true))
        }
    }
    
    val catPets = remember(allAdoptionPets, searchQuery) {
        allAdoptionPets.filter { 
            it.category.equals("Cat", ignoreCase = true) &&
            (searchQuery.isEmpty() || 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.type.contains(searchQuery, ignoreCase = true))
        }
    }
    
    val birdPets = remember(allAdoptionPets, searchQuery) {
        allAdoptionPets.filter { 
            it.category.equals("Bird", ignoreCase = true) &&
            (searchQuery.isEmpty() || 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.type.contains(searchQuery, ignoreCase = true))
        }
    }
    
    // Remember pager states for each category
    val dogPagerState = rememberPagerState(initialPage = 0, pageCount = { dogPets.size })
    val catPagerState = rememberPagerState(initialPage = 0, pageCount = { catPets.size })
    val birdPagerState = rememberPagerState(initialPage = 0, pageCount = { birdPets.size })

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
                        DisplayCategoryPets(
                            pets = dogPets,
                            category = PetCategory.DOG,
                            pagerState = dogPagerState,
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
                        DisplayCategoryPets(
                            pets = catPets,
                            category = PetCategory.CAT,
                            pagerState = catPagerState,
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
                    else -> { // Birds/OTHER
                        DisplayCategoryPets(
                            pets = birdPets,
                            category = PetCategory.OTHER,
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
fun DisplayCategoryPets(
    pets: List<AdoptionPet>,
    category: PetCategory,
    pagerState: androidx.compose.foundation.pager.PagerState,
    onPetClick: (AdoptionPet) -> Unit,
    onContactClick: (AdoptionPet) -> Unit
) {
    if (pets.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
            ReelsPetCard(
                pet = pet,
                category = category,
                onCardClick = { onPetClick(pet) },
                onContactClick = { onContactClick(pet) }
            )
        }
    }
}

@Composable
fun ReelsPetCard(
    pet: AdoptionPet,
    category: PetCategory,
    onCardClick: () -> Unit = {},
    onContactClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(480.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Pet image section - takes up 65% of the card
            Box(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxWidth()
            ) {
                if (pet.imageUri.isNotEmpty()) {
                    val uri = try {
                        Uri.parse(pet.imageUri)
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Failed to parse URI: ${e.message}", e)
                        null
                    }

                    if (uri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uri,
                                onError = {
                                    android.util.Log.e(
                                        "HomeScreen",
                                        "Error loading image: ${it.result.throwable.message}"
                                    )
                                }
                            ),
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback to a default image
                        Image(
                            painter = painterResource(id = R.drawable.pet_logo),
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // Display a default image based on the pet category
                    val imageRes = when (category) {
                        PetCategory.DOG -> R.drawable.dog_nemo
                        PetCategory.CAT -> R.drawable.cat_nero
                        else -> R.drawable.pet_logo
                    }
                    
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = pet.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Category badge
                Box(
                    modifier = Modifier
                        .padding(12.dp)
                        .background(
                            color = when (category) {
                                PetCategory.DOG -> Color(0xFFFF415B)
                                PetCategory.CAT -> Color(0xFF6A1B9A)
                                else -> Color(0xFF9C27B0) // Birds
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = when (category) {
                            PetCategory.DOG -> "Dog"
                            PetCategory.CAT -> "Cat"
                            else -> "Bird"
                        },
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Pet info section - 35% of card
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                // Pet info area
                Column {
                    // Pet name
                    Text(
                        text = pet.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    // Pet breed/type
                    Text(
                        text = pet.type,
                        fontSize = 18.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Location with icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_location),
                            contentDescription = "Location",
                            modifier = Modifier.size(16.dp),
                            tint = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = pet.location,
                            fontSize = 16.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description - show if there is content
                    if (pet.description.isNotEmpty()) {
                        Text(
                            text = pet.description,
                            fontSize = 16.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Contact Button - ensuring it's always at the bottom and matches screenshot
                androidx.compose.foundation.layout.Column {
                    // Red contact button with proper styling - use an additional clickable modifier to stop propagation
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color(0xFFFF2F55))
                            .clickable(
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = {
                                    // Stop event propagation to parent card
                                    onContactClick()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Contact",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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