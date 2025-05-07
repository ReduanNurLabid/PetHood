package com.example.pethood.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pethood.PetHoodApplication
import com.example.pethood.R
import com.example.pethood.data.ReportedPet
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.components.SearchBar
import com.example.pethood.ui.theme.PetHoodTheme
import com.example.pethood.ui.theme.PrimaryRed
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindLostScreen(
    navigateToRoute: (Screen) -> Unit = {},
    onPetClick: (ReportedPet) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTabIndex by remember { mutableStateOf(0) } // 0 for Missing, 1 for Found
    
    // Add a refresh trigger
    var refreshTrigger by remember { mutableStateOf(0) }

    // Get data from repository
    val context = LocalContext.current
    val reportedPetRepository = remember { PetHoodApplication.getInstance().reportedPetRepository }

    // Store pets in state
    var missingPets by remember { mutableStateOf<List<ReportedPet>>(emptyList()) }
    var foundPets by remember { mutableStateOf<List<ReportedPet>>(emptyList()) }

    // Load data on launch and when refreshed
    LaunchedEffect(refreshTrigger) {
        try {
            Log.d("FindLostScreen", "Fetching missing and found pets...")

            // Get missing pets
            val missingPetsList = reportedPetRepository.getAllMissingPets()
            Log.d("FindLostScreen", "Loaded ${missingPetsList.size} missing pets")

            // Get found pets
            val foundPetsList = reportedPetRepository.getAllFoundPets()
            Log.d("FindLostScreen", "Loaded ${foundPetsList.size} found pets")

            // Update state with separate lists
            missingPets = missingPetsList
            foundPets = foundPetsList

            Log.d(
                "FindLostScreen",
                "Total: ${missingPets.size} missing pets, ${foundPets.size} found pets"
            )
        } catch (e: Exception) {
            Log.e("FindLostScreen", "Error loading pets: ${e.message}", e)
        }
    }

    // Refresh the screen when it becomes visible
    LaunchedEffect(Unit) {
        // Trigger a refresh of the data
        refreshTrigger++
    }

    // Filter based on search query
    val filteredMissingPets = missingPets.filter {
        searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    val filteredFoundPets = foundPets.filter {
        searchQuery.isEmpty() ||
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.type.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
    }

    // Displayed pets based on tab selection
    val displayedPets = if (selectedTabIndex == 0) filteredMissingPets else filteredFoundPets

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
        ) {
            // App title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Missing & Found Pets",
                    color = PrimaryRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Add log info for debugging in dev
                Text(
                    text = "(${missingPets.size}/${foundPets.size})",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }

            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            // Tab selector for Missing/Found
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = PrimaryRed,
                indicator = {},
                divider = {}
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .background(
                                    color = if (selectedTabIndex == 0) PrimaryRed else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                "Missing Pets",
                                color = if (selectedTabIndex == 0) Color.White else PrimaryRed
                            )
                        }
                    }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                                .background(
                                    color = if (selectedTabIndex == 1) Color(0xFF4CAF50) else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 16.dp)
                        ) {
                            Text(
                                "Found Pets",
                                color = if (selectedTabIndex == 1) Color.White else Color(0xFF4CAF50)
                            )
                        }
                    }
                )
            }

            // Pet grid
            if (displayedPets.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No pets to display",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedPets) { pet ->
                        ReportedPetCard(
                            pet = pet,
                            onPetClick = { onPetClick(pet) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportedPetCard(
    pet: ReportedPet,
    onPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val formattedDate = dateFormat.format(pet.date)

    val cardColor =
        if (pet.isMissing) PrimaryRed.copy(alpha = 0.1f) else Color(0xFF4CAF50).copy(alpha = 0.1f)
    val statusColor = if (pet.isMissing) PrimaryRed else Color(0xFF4CAF50)
    val statusText = if (pet.isMissing) "Missing Pet" else "Found Pet"

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onPetClick
    ) {
        Column {
            // Pet image
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
            ) {
                // Use uploaded image if available, otherwise use template
                if (pet.imageUri != null && pet.imageUri.isNotEmpty()) {
                    val uri = try {
                        android.net.Uri.parse(pet.imageUri)
                    } catch (e: Exception) {
                        Log.e("FindLostScreen", "Failed to parse URI: ${e.message}", e)
                        null
                    }

                    if (uri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = uri,
                                onError = {
                                    Log.e(
                                        "FindLostScreen",
                                        "Error loading image: ${it.result.throwable.message}"
                                    )
                                }
                            ),
                            contentDescription = pet.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // No uploaded image, use drawable resource
                        val resourceId = if (pet.imageUrl != null && pet.imageUrl.isNotEmpty()) {
                            context.resources.getIdentifier(
                                pet.imageUrl, "drawable", context.packageName
                            )
                        } else 0

                        val imageRes = if (resourceId != 0) resourceId else R.drawable.pet_logo

                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = pet.name ?: "Pet",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    // No uploaded image, use drawable resource
                    val resourceId = if (pet.imageUrl != null && pet.imageUrl.isNotEmpty()) {
                        context.resources.getIdentifier(
                            pet.imageUrl, "drawable", context.packageName
                        )
                    } else 0
                    
                    val imageRes = if (resourceId != 0) resourceId else R.drawable.pet_logo
                    
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = pet.name ?: "Pet",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Status badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(
                            color = statusColor,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = statusText,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Pet info
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = pet.name ?: "Unknown Pet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = pet.type ?: "Unknown Type",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_calendar),
                        contentDescription = "Date",
                        modifier = Modifier.size(12.dp),
                        tint = statusColor
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = formattedDate,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FindLostScreenPreview() {
    PetHoodTheme {
        FindLostScreen()
    }
}