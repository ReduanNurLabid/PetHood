package com.example.pethood.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.pethood.R
import com.example.pethood.data.Pet
import com.example.pethood.data.PetCategory
import com.example.pethood.data.PetGender
import com.example.pethood.navigation.Screen
import com.example.pethood.ui.components.BottomNavigationBar

@Composable
fun PetDetailScreen(
    pet: Pet,
    onBackClick: () -> Unit,
    onRequestClick: () -> Unit,
    navigateToRoute: (Screen) -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Home.route,
                onNavigate = { navigateToRoute(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Pet image with back button
            Box(modifier = Modifier.fillMaxWidth()) {
                // Pet image
                val resourceId = LocalContext.current.resources.getIdentifier(
                    pet.imageUrl, "drawable", LocalContext.current.packageName
                )
                val imageResource = if (resourceId != 0) {
                    resourceId
                } else {
                    R.drawable.pet_logo
                }

                Image(
                    painter = painterResource(id = imageResource),
                    contentDescription = "${pet.name}, ${pet.breed}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )

                // Back button
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Image indicator dots
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(10.dp)
                            .background(Color.LightGray, CircleShape)
                    )
                }
            }

            // Pet information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Pet name
                Text(
                    text = pet.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pet details in cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Age card
                    InfoCard(
                        title = "Age",
                        value = "${pet.age} ${if (pet.age == 1) "Year" else "Years"}",
                        modifier = Modifier.weight(1f)
                    )

                    // Weight card (estimated based on breed)
                    val estimatedWeight = when (pet.breed.lowercase()) {
                        "pitbull" -> "20 Kg"
                        "golden" -> "30 Kg"
                        "criollo" -> "15 Kg"
                        "mixed" -> "4 Kg"
                        "persian" -> "5 Kg"
                        "deshi" -> "3 Kg"
                        else -> "10 Kg"
                    }
                    InfoCard(
                        title = "Weight",
                        value = estimatedWeight,
                        modifier = Modifier.weight(1f)
                    )

                    // Gender card
                    InfoCard(
                        title = "Sex",
                        value = if (pet.gender == PetGender.MALE) "Male" else "Female",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About section
                Text(
                    text = "About",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = pet.description,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Request button
                Button(
                    onClick = onRequestClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Request",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .height(70.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF3F3F7)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PetDetailScreenPreview() {
    MaterialTheme {
        PetDetailScreen(
            pet = Pet(
                id = 3,
                name = "Nemo",
                breed = "Criollo",
                category = PetCategory.DOG,
                gender = PetGender.MALE,
                age = 1,
                description = "Nemo is an intelligent and very active puppy who loves playing with a ball and going on long walks. He's also very affectionate and gentle.\nHe's eagerly awaiting a family who will welcome him and be willing to receive lots of love.",
                imageUrl = "dog_nemo",
                isFavorite = false
            ),
            onBackClick = {},
            onRequestClick = {},
            navigateToRoute = {}
        )
    }
}