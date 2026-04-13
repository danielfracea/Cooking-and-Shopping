# Cook & Shop – Android App

Native Android version of the **Cook & Shop** web application, built with **Kotlin** and **Jetpack Compose**.

## ⚠️ Configuration Required

> **`app/google-services.json`** checked into this repo is a **non-functional placeholder**. You must replace it with a real file from the Firebase Console before the app will build or run correctly. See [Getting Started](#getting-started) below.

> **`WEB_CLIENT_ID`** in `AuthViewModel.kt` must also be replaced with your Firebase OAuth Web Client ID before Google Sign-In will work.

---


| Feature | Description |
|---------|-------------|
| 🔐 Authentication | Google Sign-In or Guest mode (via Firebase Auth) |
| 🛒 Shopping Lists | Create/delete lists, add/remove/check items, quick-add, share |
| 📖 Recipes | Browse, filter by tag, view steps & tools, add custom recipes, cooking wizard |
| 🥦 Ingredients | Full CRUD with nutritional info (calories, protein, carbs, fat) |
| 📅 Meal Planner | Weekly meal planner — assign recipes to breakfast/lunch/dinner slots |
| ⚙️ Settings | Switch between Metric and Imperial unit systems |
| ☁️ Cloud Sync | Firebase Firestore real-time sync (with offline SharedPreferences fallback) |

## Tech Stack

- **Kotlin** – programming language
- **Jetpack Compose** – declarative UI
- **Material 3** – design system
- **Navigation Compose** – in-app navigation
- **Firebase Auth** – authentication
- **Firebase Firestore** – cloud data sync
- **Credential Manager API** – Google Sign-In
- **ViewModel + StateFlow** – state management

## Project Structure

```
app/src/main/java/com/cooking/app/
├── CookingApp.kt                    # Application class
├── MainActivity.kt                  # Entry activity
├── data/
│   ├── model/
│   │   ├── Recipe.kt                # Recipe, RecipeStep, RecipeIngredient models
│   │   ├── Ingredient.kt            # Ingredient model + defaults
│   │   ├── ShoppingList.kt          # ShoppingList, ShoppingItem models
│   │   └── MealPlan.kt              # DayMeals, MealEntry models
│   └── repository/
│       └── AppRepository.kt         # Single repository: Firebase + SharedPreferences
└── ui/
    ├── theme/                       # Material3 colors, typography, theme
    ├── navigation/                  # Bottom nav + NavHost setup
    ├── auth/                        # Login screen + AuthViewModel
    ├── recipes/                     # Recipes list, detail, add screens + ViewModel
    ├── shoppinglists/               # Shopping lists screens + ViewModel
    ├── ingredients/                 # Ingredients screen + ViewModel
    ├── mealplanner/                 # Meal planner screen + ViewModel
    └── settings/                    # Settings screen + ViewModel
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- A Firebase project

### Setup

1. **Clone / open the project**
   Open the `android-app/` folder in Android Studio.

2. **Set up Firebase**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a project (or use your existing one)
   - Add an Android app with package name `com.cooking.app`
   - Enable **Authentication** → Sign-in providers → **Google**
   - Enable **Firestore Database**
   - Download `google-services.json` and place it at `app/google-services.json`
   - Copy `app/google-services.json.example` as a reference

3. **Configure Google Sign-In**
   - In `AuthViewModel.kt`, replace `YOUR_WEB_CLIENT_ID_HERE` with the Web Client ID from your Firebase project (found in Authentication → Sign-in method → Google → Web SDK configuration).

4. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or press ▶ Run in Android Studio.

### Guest Mode

The app supports a Guest mode that works fully offline using local SharedPreferences storage — no Firebase setup required for basic usage.

## Data Sync

- **Authenticated users**: data is synced to Firestore under `users/{uid}/data/{collection}`
- **Guest users**: data is stored locally in SharedPreferences only

## Minimum Requirements

- Android 8.0 (API level 26) or higher
