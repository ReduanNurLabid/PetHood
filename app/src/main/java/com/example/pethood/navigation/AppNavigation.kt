package com.example.pethood.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pethood.PetHoodApplication
import com.example.pethood.data.Pet
import com.example.pethood.data.PetCategory
import com.example.pethood.data.PetGender
import com.example.pethood.screens.AdoptionFormScreen
import com.example.pethood.screens.AdoptionPetDetailScreen
import com.example.pethood.screens.FindLostScreen
import com.example.pethood.screens.FoundPetReportScreen
import com.example.pethood.screens.HomeScreen
import com.example.pethood.screens.LandingScreen
import com.example.pethood.screens.LoginScreen
import com.example.pethood.screens.MissingPetReportScreen
import com.example.pethood.screens.PetDetailScreen
import com.example.pethood.screens.ProfileScreen
import com.example.pethood.screens.ReportedPetDetailScreen
import com.example.pethood.screens.ReportsScreen
import com.example.pethood.screens.SignupScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object FindLost : Screen("findlost")
    object Reports : Screen("reports")
    object Profile : Screen("profile")
    object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: String): String {
            return "pet_detail/$petId"
        }
    }
    object MissingPetReport : Screen("missing_pet_report")
    object FoundPetReport : Screen("found_pet_report")
    object ReportedPetDetail : Screen("reported_pet_detail/{id}") {
        fun createRoute(id: String): String {
            return "reported_pet_detail/$id"
        }
    }
    object AdoptionForm : Screen("adoption_form")
    object AdoptionPetDetail : Screen("adoption_pet_detail/{petId}") {
        fun createRoute(petId: String): String {
            return "adoption_pet_detail/$petId"
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String? = null
) {
    val context = LocalContext.current
    val userRepository = remember { PetHoodApplication.getInstance().userRepository }

    val initialScreen = startDestination ?: if (userRepository.isLoggedIn()) {
        Screen.Home.route
    } else {
        Screen.Landing.route
    }

    NavHost(
        navController = navController,
        startDestination = initialScreen
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                onGetStartedClick = {
                    navController.navigate(Screen.Signup.route)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                },
                onSignupClick = {
                    navController.navigate(Screen.Signup.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate(Screen.Signup.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Signup.route) {
            SignupScreen(
                onSignupSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Home.route) {
            val refreshTrigger = remember { mutableStateOf(0) }

            // When returning to Home from other screens, trigger a data refresh
            DisposableEffect(Unit) {
                // Increment refresh trigger when returning to Home screen
                refreshTrigger.value++
                onDispose { }
            }

            HomeScreen(
                refreshTrigger = refreshTrigger.value,
                navigateToRoute = { destination ->
                    navController.navigate(destination.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onPetClick = { pet ->
                    navController.navigate(Screen.PetDetail.createRoute(pet.id.toString()))
                },
                onPutUpForAdoptionClick = {
                    navController.navigate(Screen.AdoptionForm.route)
                },
                onAdoptionPetClick = { petId ->
                    navController.navigate(Screen.AdoptionPetDetail.createRoute(petId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.FindLost.route) {
            FindLostScreen(
                navigateToRoute = { destination ->
                    navController.navigate(destination.route)
                },
                onPetClick = { reportedPet ->
                    navController.navigate(
                        Screen.ReportedPetDetail.createRoute(
                            id = reportedPet.id
                        )
                    )
                }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route)
                },
                onReportMissingPetClick = {
                    navController.navigate(Screen.MissingPetReport.route)
                },
                onReportFoundPetClick = {
                    navController.navigate(Screen.FoundPetReport.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                navigateToRoute = { destination ->
                    navController.navigate(destination.route)
                },
                onLogout = {
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PetDetail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""

            PetDetailScreen(
                petId = petId,
                onBackClick = {
                    navController.popBackStack()
                },
                onRequestClick = {
                    // Handle request logic here
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.MissingPetReport.route) {
            MissingPetReportScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route)
                },
                onSubmitReport = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FoundPetReport.route) {
            FoundPetReportScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route)
                },
                onSubmitReport = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ReportedPetDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""

            ReportedPetDetailScreen(
                petId = id,
                onBackClick = {
                    navController.popBackStack()
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                },
                onPutUpForAdoptionClick = {
                    navController.navigate(Screen.AdoptionForm.route)
                }
            )
        }

        composable(Screen.AdoptionForm.route) {
            AdoptionFormScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.AdoptionPetDetail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getString("petId") ?: ""
            AdoptionPetDetailScreen(
                petId = petId,
                onBackClick = {
                    navController.popBackStack()
                },
                navigateToRoute = { destination ->
                    navController.navigate(destination.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

private fun getSamplePets(): List<Pet> {
    return listOf(
        Pet(
            id = "1",
            name = "Snoop",
            breed = "Pitbull",
            category = PetCategory.DOG,
            gender = PetGender.FEMALE,
            age = 2,
            description = "Friendly and playful pitbull looking for a forever home. Snoop enjoys long walks and playing fetch. She's great with kids and other pets.",
            imageUrl = "dog_snoop"
        ),
        Pet(
            id = "2",
            name = "Zimmer",
            breed = "Golden",
            category = PetCategory.DOG,
            gender = PetGender.MALE,
            age = 3,
            description = "Loving golden retriever that enjoys playing fetch. Zimmer is well-trained and loves to be around people. He's gentle and patient, making him perfect for families.",
            imageUrl = "dog_zimmer"
        ),
        Pet(
            id = "3",
            name = "Nemo",
            breed = "Criollo",
            category = PetCategory.DOG,
            gender = PetGender.MALE,
            age = 1,
            description = "Nemo is an intelligent and very active puppy who loves playing with a ball and going on long walks. He's also very affectionate and gentle.\nHe's eagerly awaiting a family who will welcome him and be willing to receive lots of love.",
            imageUrl = "dog_nemo"
        ),
        Pet(
            id = "4",
            name = "Bunty",
            breed = "Mixed",
            category = PetCategory.CAT,
            gender = PetGender.FEMALE,
            age = 2,
            description = "Gentle cat who loves to curl up in your lap. Bunty is a sweet, calm cat who enjoys peaceful environments. She's litter trained and gets along well with respectful children.",
            imageUrl = "cat_bunty"
        ),
        Pet(
            id = "5",
            name = "Nero",
            breed = "Persian",
            category = PetCategory.CAT,
            gender = PetGender.MALE,
            age = 4,
            description = "Majestic Persian cat looking for a quiet home. Nero is graceful and dignified, enjoying a calm household. His beautiful coat requires regular grooming, but he enjoys the attention.",
            imageUrl = "cat_nero"
        ),
        Pet(
            id = "6",
            name = "Noball",
            breed = "Deshi",
            category = PetCategory.CAT,
            gender = PetGender.FEMALE,
            age = 1,
            description = "Curious and adventurous kitten. Noball is playful and energetic, always exploring her surroundings. She's quick to learn and will bring lots of joy to any household.",
            imageUrl = "cat_noball"
        )
    )
}