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
}