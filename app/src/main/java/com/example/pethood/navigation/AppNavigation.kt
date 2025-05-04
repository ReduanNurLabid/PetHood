package com.example.pethood.navigation

import androidx.compose.runtime.Composable
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
import com.example.pethood.screens.FindLostScreen
import com.example.pethood.screens.FoundPetReportScreen
import com.example.pethood.screens.HomeScreen
import com.example.pethood.screens.LandingScreen
import com.example.pethood.screens.LoginScreen
import com.example.pethood.screens.MissingPetReportScreen
import com.example.pethood.screens.PetDetailScreen
import com.example.pethood.screens.ProfileScreen
import com.example.pethood.screens.ReportsScreen
import com.example.pethood.screens.SignupScreen
import com.example.pethood.screens.ReportedPetDetailScreen
import com.example.pethood.screens.AdoptionPetDetailScreen

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
    object FindLost : Screen("findlost")
    object Reports : Screen("reports")
    object Profile : Screen("profile")
    object PetDetail : Screen("pet_detail/{petId}") {
        fun createRoute(petId: Long): String {
            return "pet_detail/$petId"
        }
    }
    object MissingPetReport : Screen("missing_pet_report")
    object FoundPetReport : Screen("found_pet_report")
    object ReportedPetDetail : Screen("reported_pet_detail/{id}/{isMissing}") {
        fun createRoute(id: Long, isMissing: Boolean): String {
            return "reported_pet_detail/$id/$isMissing"
        }
    }
    object AdoptionForm : Screen("adoption_form")
    object AdoptionPetDetail : Screen("adoption_pet_detail/{petId}") {
        fun createRoute(petId: String): String {
            return "adoption_pet_detail/$petId"
        }
        
        // Create a new method to generate a Screen object with the petId
        fun withPetId(petId: String): Screen {
            // Instead of trying to extend the sealed class, just return AdoptionPetDetail itself
            // The navigation component will use the route which we can provide when navigating
            return AdoptionPetDetail
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

    // Determine start destination based on logged in status
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
                    // Navigate to home after successful login
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onSignupClick = {
                    navController.navigate(Screen.Signup.route) {
                        // Remove login from the back stack when moving to signup
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
                    // Navigate to home after successful signup
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Landing.route) { inclusive = true }
                    }
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        // Remove signup from the back stack when moving to login
                        popUpTo(Screen.Signup.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.navigateUp()
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navigateToRoute = { destination ->
                    // For basic navigation, just use the destination route
                    navController.navigate(destination.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onPetClick = { pet ->
                    navController.navigate(Screen.PetDetail.createRoute(pet.id))
                },
                onPutUpForAdoptionClick = {
                    navController.navigate(Screen.AdoptionForm.route)
                },
                onAdoptionPetClick = { petId ->
                    // Navigate to adoption pet detail with the specific ID
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
                    // Navigate to the ReportedPetDetailScreen with the pet ID and isMissing status
                    navController.navigate(
                        Screen.ReportedPetDetail.createRoute(
                            id = reportedPet.id,
                            isMissing = reportedPet.isMissing
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
                    // Navigate back to landing page when logged out
                    navController.navigate(Screen.Landing.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.PetDetail.route,
            arguments = listOf(
                navArgument("petId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getLong("petId") ?: 0L
            val pet = getSamplePets().find { it.id == petId }

            if (pet != null) {
                PetDetailScreen(
                    pet = pet,
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
                    // Handle report submission and navigate back to Reports screen
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
                    // Handle report submission and navigate back to Reports screen
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ReportedPetDetail.route,
            arguments = listOf(
                navArgument("id") { type = NavType.LongType },
                navArgument("isMissing") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            val isMissing = backStackEntry.arguments?.getBoolean("isMissing") ?: false
            
            // Get the reported pet from the repository
            val reportedPetRepository = PetHoodApplication.getInstance().reportedPetRepository
            val userRepository = PetHoodApplication.getInstance().userRepository
            val reportedPet = reportedPetRepository.getPetById(id, isMissing)
            
            if (reportedPet != null) {
                // Check if current user is the reporter
                val currentUserId = userRepository.getCurrentUserId()
                val isCurrentUserReporter = reportedPet.reporterId == currentUserId
                
                ReportedPetDetailScreen(
                    pet = reportedPet,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onContactClick = {
                        // Implement contact functionality
                        // For example, show a dialog with contact information
                    },
                    navigateToRoute = { destination ->
                        navController.navigate(destination.route) {
                            launchSingleTop = true
                        }
                    },
                    isCurrentUserReporter = isCurrentUserReporter,
                    onPutUpForAdoptionClick = {
                        navController.navigate(Screen.AdoptionForm.route)
                    }
                )
            }
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
                },
                onSubmit = { adoptionPet ->
                    try {
                        // Save to repository
                        val adoptionPetRepository = PetHoodApplication.getInstance().adoptionPetRepository
                        adoptionPetRepository.addAdoptionPet(adoptionPet)
                        
                        // Navigate to home screen explicitly instead of just popping back
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        // Error is already handled in the AdoptionFormScreen
                        // Just log the error here
                        android.util.Log.e("AppNavigation", "Error adding adoption pet: ${e.message}", e)
                    }
                }
            )
        }

        // Add the adoption pet detail route
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
            id = 1,
            name = "Snoop",
            breed = "Pitbull",
            category = PetCategory.DOG,
            gender = PetGender.FEMALE,
            age = 2,
            description = "Friendly and playful pitbull looking for a forever home. Snoop enjoys long walks and playing fetch. She's great with kids and other pets.",
            imageUrl = "dog_snoop"
        ),
        Pet(
            id = 2,
            name = "Zimmer",
            breed = "Golden",
            category = PetCategory.DOG,
            gender = PetGender.MALE,
            age = 3,
            description = "Loving golden retriever that enjoys playing fetch. Zimmer is well-trained and loves to be around people. He's gentle and patient, making him perfect for families.",
            imageUrl = "dog_zimmer"
        ),
        Pet(
            id = 3,
            name = "Nemo",
            breed = "Criollo",
            category = PetCategory.DOG,
            gender = PetGender.MALE,
            age = 1,
            description = "Nemo is an intelligent and very active puppy who loves playing with a ball and going on long walks. He's also very affectionate and gentle.\nHe's eagerly awaiting a family who will welcome him and be willing to receive lots of love.",
            imageUrl = "dog_nemo"
        ),
        Pet(
            id = 4,
            name = "Bunty",
            breed = "Mixed",
            category = PetCategory.CAT,
            gender = PetGender.FEMALE,
            age = 2,
            description = "Gentle cat who loves to curl up in your lap. Bunty is a sweet, calm cat who enjoys peaceful environments. She's litter trained and gets along well with respectful children.",
            imageUrl = "cat_bunty"
        ),
        Pet(
            id = 5,
            name = "Nero",
            breed = "Persian",
            category = PetCategory.CAT,
            gender = PetGender.MALE,
            age = 4,
            description = "Majestic Persian cat looking for a quiet home. Nero is graceful and dignified, enjoying a calm household. His beautiful coat requires regular grooming, but he enjoys the attention.",
            imageUrl = "cat_nero"
        ),
        Pet(
            id = 6,
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