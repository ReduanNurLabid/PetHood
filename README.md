# PetHood

PetHood is a modern mobile application designed to connect pet owners and pet enthusiasts, facilitating pet adoption with a user-friendly interface and intuitive navigation. The app provides a platform for users to browse pets available for adoption, put their own pets up for adoption, and connect with pet owners.

## Table of Contents
- [Features](#features)
- [Architecture](#architecture)
- [Frontend Implementation](#frontend-implementation)
- [Backend Implementation](#backend-implementation)
- [UI Components](#ui-components)
- [Screens](#screens)
- [Data Management](#data-management)
- [Future Enhancements](#future-enhancements)

## Features

### Core Features
1. **Reels-Style Pet Browsing** - A TikTok/Instagram Reels-inspired vertical scrolling interface for browsing pets by category (Dogs, Cats, Birds).
2. **Independent Category Scrolling** - Each animal category maintains its own scroll state, allowing users to browse one category without losing their position in others.
3. **Pet Search** - Users can search for pets by name or type across all categories with real-time filtering.
4. **Pet Adoption System** - Users can view pets available for adoption and contact the owner directly via phone integration.
5. **Put Up for Adoption** - Users can list their pets for adoption by providing details and uploading images.

### Additional Features
1. **Contact Integration** - Integrated phone dialer to contact pet owners directly from the app.
2. **Image Upload** - Support for uploading and displaying pet images when listing for adoption.
3. **Category Badges** - Visual indicators showing the pet category (Dog, Cat, Bird) on each pet card.
4. **Form Validation** - Input validation for the adoption form, ensuring all required fields are filled.
5. **Consistent Design Language** - Color-coded UI elements and consistent styling across the app.
6. **Bottom Navigation** - Easy navigation between Home, FindLost, Reports, and Profile sections.

## Architecture

PetHood follows the MVVM (Model-View-ViewModel) architecture pattern with a repository-based data layer, leveraging Jetpack Compose for the UI layer.

### Key Components:
- **UI Layer** - Built entirely with Jetpack Compose
- **Data Layer** - Repository pattern for data management
- **Navigation** - Compose Navigation for screen transitions
- **State Management** - Compose state management with `remember` and `mutableStateOf`
- **Application Class** - `PetHoodApplication` serves as the central point for repository initialization

## Frontend Implementation

### UI Framework
The app is built using **Jetpack Compose**, Google's modern declarative UI toolkit for Android. Compose was chosen for its modern approach to UI development, which allows for:
- Concise, declarative UI code
- Reactive UI updates based on state changes
- Easy component reuse
- Built-in animations and transitions

### Key UI Features:

#### 1. Reels-Style Interface
- Implemented using Compose's `VerticalPager` component for a smooth, full-screen scrolling experience
- Each category (Dogs, Cats, Birds) has its own independent pager with distinct state management
- State persistence when switching between categories using `rememberPagerState`
- Optimized rendering of pet cards for smooth performance

```kotlin
@Composable
fun DisplayCategoryPets(
    pets: List<AdoptionPet>,
    category: PetCategory,
    pagerState: PagerState,
    onContactClick: (AdoptionPet) -> Unit
) {
    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val pet = pets[page]
        ReelsPetCard(pet = pet, category = category, onContactClick = { onContactClick(pet) })
    }
}
```

#### 2. ReelsPetCard Design
- Custom `ReelsPetCard` component with a consistent design for all pet listings
- 65/35 split layout with larger image area and information section below
- Category badges in the top-right corner color-coded by animal type
- Contact button with rounded corners for initiating communication
- Information hierarchy with larger text for name and smaller text for details

#### 3. Category Selector
- Custom tab interface for switching between pet categories
- Visual indicators for the currently selected category
- Color-coded tabs matching the theme colors for each pet type:
  - Dogs: #FF415B (red)
  - Cats: #6A1B9A (purple)
  - Birds: #9C27B0 (violet)

#### 4. Search Functionality
- Custom `SearchBar` component with real-time filtering
- Searches across pet names and types
- Clean, minimalist design with a search icon and placeholder text

### Navigation
Navigation is implemented using the Jetpack Navigation Component for Compose:
- `AppNavigation` class defining the navigation graph
- Bottom navigation bar for main app sections
- Type-safe navigation with screen route constants
- Support for passing arguments between screens

## Backend Implementation

### Data Management
The app uses a repository pattern for data management with three main repositories:

#### 1. AdoptionPetRepository
- Manages pets available for adoption
- Stores adoption pet data in-memory using `mutableStateListOf`
- Provides methods for adding, retrieving, and filtering adoption pets
- Initializes with sample data for demonstration purposes

#### 2. UserRepository
- Handles user authentication and profile management
- Stores user preferences and session data

#### 3. ReportedPetRepository
- Manages reported missing and found pets
- Supports filtering and searching reported pets

### Data Models
The app uses several data models to represent different entities:

#### 1. AdoptionPet
```kotlin
data class AdoptionPet(
    val id: String,
    val name: String,
    val category: String, // "Cat", "Dog", or "Bird"
    val type: String, // Breed or specific type
    val location: String,
    val description: String,
    val contactNumber: String,
    val imageUri: String,
    val userId: String, // ID of the user who put the pet up for adoption
    val date: Date
)
```

#### 2. Pet (Room Entity)
```kotlin
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breed: String,
    val category: PetCategory,
    val gender: PetGender,
    val age: Int,
    val description: String,
    val imageUrl: String,
    val isFavorite: Boolean = false,
    // Additional fields for adoption pets
    val isAdoptionPet: Boolean = false,
    val adoptionPetId: String = "",
    val adoptionImageUri: String = "",
    val location: String = "",
    val contactNumber: String = ""
)
```

#### 3. Enums
```kotlin
enum class PetCategory { DOG, CAT, OTHER }
enum class PetGender { MALE, FEMALE }
```

### State Management
The app uses Compose's state management capabilities:
- `mutableStateOf` for UI state (search queries, selected categories)
- `remember` for component-level state persistence
- `mutableStateListOf` for observable collections in repositories
- Derived state for filtered collections using `remember(dependencies) { computation }`

```kotlin
val dogPets = remember(allAdoptionPets, searchQuery) {
    allAdoptionPets.filter { 
        it.category.equals("Dog", ignoreCase = true) &&
        (searchQuery.isEmpty() || 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.type.contains(searchQuery, ignoreCase = true))
    }
}
```

### System Integration
The app integrates with system features:
- Phone dialer for contacting pet owners
- Image picker for uploading pet photos
- Toast notifications for user feedback

## UI Components

### Custom Components
1. **ReelsPetCard** - The main card component for displaying pet information with a large image, pet details, and contact button.
2. **CategorySelector** - Tab selector for switching between pet categories with visual indicators for the selected tab.
3. **SearchBar** - Custom search input with real-time filtering functionality.
4. **BottomNavigationBar** - Navigation component with icons and labels for the main app sections.

### Design System
- **Color Scheme**:
  - Primary Red: #FF415B (Dogs)
  - Purple: #6A1B9A (Cats)
  - Violet: #9C27B0 (Birds/Adoption)
  - White and light grays for backgrounds
  - Dark grays for text

- **Typography**:
  - Large titles: 24sp, Bold
  - Section headers: 20sp, Bold
  - Pet names: 24sp, Bold
  - Pet details: 16-18sp, Regular
  - Button text: 18sp, Bold

- **Shapes and Styling**:
  - Rounded corners (20dp) for cards
  - Pill-shaped buttons (24dp corners)
  - Subtle card elevation (2dp)
  - Category badges with 12dp corners

## Screens

### 1. HomeScreen
The main screen of the application featuring:
- App title with "PetHood" branding
- "Put Up for Adoption" button
- Search bar for filtering pets
- Category selector tabs (Dogs, Cats, Birds)
- Vertical pager for browsing pets in a reels-style format
- Independent scroll state for each category
- Bottom navigation bar

### 2. AdoptionFormScreen
Form for users to list their pets for adoption:
- Pet image upload functionality
- Pet name, category, and type/breed inputs
- Location and contact information fields
- Pet description textarea
- Form validation to ensure all required fields are filled
- Submission handling with user feedback

### 3. FindLostScreen
Screen for browsing and reporting lost and found pets:
- Tab selection for Missing/Found pets
- Search functionality for filtering results
- List of reported pets with details
- Bottom navigation bar

### 4. ReportsScreen
Screen for reporting missing or found pets:
- Options to report a missing pet or found pet
- Information about the reporting process
- Bottom navigation bar

### 5. ProfileScreen
User profile management screen:
- User information display
- Account settings
- Pet listings management
- Bottom navigation bar

## Data Management

### Application Class
The `PetHoodApplication` class serves as the central point for repository initialization:
```kotlin
class PetHoodApplication : Application() {
    // Lazy initialization of the repositories
    val userRepository: UserRepository by lazy {
        UserRepository(applicationContext)
    }

    val reportedPetRepository: ReportedPetRepository by lazy {
        ReportedPetRepository(applicationContext)
    }
    
    val adoptionPetRepository: AdoptionPetRepository by lazy {
        AdoptionPetRepository()
    }

    companion object {
        private lateinit var instance: PetHoodApplication

        fun getInstance(): PetHoodApplication {
            return instance
        }
    }
}
```

### Room Database
The app uses Room for persistent storage of pet data:
```kotlin
@Database(entities = [Pet::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    
    companion object {
        @Volatile
        private var INSTANCE: PetDatabase? = null
        
        fun getDatabase(context: Context): PetDatabase {
            // Database initialization
        }
    }
}
```

## Future Enhancements

1. **Backend API Integration** - Replace local data storage with cloud-based API for real-time data
2. **User Authentication** - Implement user accounts with email/password or social login
3. **Chat Functionality** - In-app messaging between users instead of phone-only contact
4. **Location Services** - Map integration for finding pets nearby and location-based filtering
5. **Favorites System** - Allow users to save and track favorite pets
6. **Push Notifications** - Alert users about new pets matching their preferences
7. **Social Sharing** - Share pet listings to social media platforms
8. **Advanced Filtering** - Filter pets by more criteria (age, size, etc.)
9. **Complete FindLost Implementation** - Fully implement the missing/found pet reporting system
10. **Analytics and Insights** - Track user engagement and app performance

---

Developed with ❤️ using Jetpack Compose 