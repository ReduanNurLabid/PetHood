package com.example.pethood.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
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
import com.example.pethood.R
import com.example.pethood.navigation.Screen
import com.example.pethood.data.ReportedPet
import com.example.pethood.data.ReportedPetRepository
import com.example.pethood.ui.components.BottomNavigationBar
import com.example.pethood.ui.theme.PrimaryRed
import kotlinx.coroutines.flow.collect

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun ReportsScreen(
    reportedPetRepository: ReportedPetRepository,
    onBackClick: () -> Unit = {},
    navigateToRoute: (Screen) -> Unit = {},
    onReportMissingPetClick: () -> Unit = {},
    onReportFoundPetClick: () -> Unit = {}
) {
    val reportedPetsFlow = remember { reportedPetRepository.getReportedPets() }
    val reportedPets by reportedPetsFlow.collectAsState(initial = emptyList())


    val context = LocalContext.current
    var showAbandonedAnimalDialog by remember { mutableStateOf(false) }
    var showAnimalAbuseDialog by remember { mutableStateOf(false) }

    if (showAbandonedAnimalDialog) {
        AlertDialog(
            onDismissRequest = { showAbandonedAnimalDialog = false },
            title = { Text("Call Emergency Number") },
            text = { Text("Would you like to call 999 to report an abandoned animal?") },
            confirmButton = {
                Button(
                    onClick = {
                        showAbandonedAnimalDialog = false
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:999")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not make the call", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Call")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAbandonedAnimalDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAnimalAbuseDialog) {
        AlertDialog(
            onDismissRequest = { showAnimalAbuseDialog = false },
            title = { Text("Call Emergency Number") },
            text = { Text("Would you like to call 999 to report animal abuse?") },
            confirmButton = {
                Button(
                    onClick = {
                        showAnimalAbuseDialog = false
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:999")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not make the call", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Call")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showAnimalAbuseDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = Screen.Reports.route,
                onNavigate = { navigateToRoute(it) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(16.dp)
        ) {
            // Header with back button and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryRed, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "PetHood",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Help us save lives section
            Text(
                text = "Help us save lives!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "At PetHood, we strongly reject animal abuse and any type of harm, which is why we provide this space to help those who cannot defend themselves.",
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reportedPets) { pet ->
                    Card(modifier = Modifier
                        .fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pet_logo),
                                contentDescription = pet.description,
                                modifier = Modifier.size(50.dp)
                            )
                            Text(
                                text = pet.description,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onReportMissingPetClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F2D95)
                )
            ) {
                Text(
                    text = "Report a Missing Pet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Report found pet button
            Button(
                onClick = onReportFoundPetClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3F2D95)
                )
            ) {
                Text(
                    text = "Report a Found Pet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    title: String,
    imageRes: Int,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3F3F7)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)),
                contentScale = ContentScale.Crop
            )

            // Title and button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3F2D95)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onReportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3F2D95)
                    )
                ) {
                    Text(
                        text = "Report",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_phone),
                        contentDescription = "Call",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    MaterialTheme {
        val reportedPetRepository = ReportedPetRepository()
        ReportsScreen(
            onReportMissingPetClick = {},
            onReportFoundPetClick = {}
        )
    }
}