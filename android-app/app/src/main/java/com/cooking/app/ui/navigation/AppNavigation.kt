package com.cooking.app.ui.navigation

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cooking.app.data.repository.AppRepository
import com.cooking.app.ui.auth.*
import com.cooking.app.ui.ingredients.IngredientsScreen
import com.cooking.app.ui.ingredients.IngredientsViewModel
import com.cooking.app.ui.mealplanner.MealPlannerScreen
import com.cooking.app.ui.mealplanner.MealPlannerViewModel
import com.cooking.app.ui.recipes.*
import com.cooking.app.ui.settings.SettingsScreen
import com.cooking.app.ui.settings.SettingsViewModel
import com.cooking.app.ui.shoppinglists.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object ShoppingLists : Screen("shopping_lists", "Shopping", Icons.Default.ShoppingCart)
    data object Recipes : Screen("recipes", "Recipes", Icons.Default.MenuBook)
    data object Ingredients : Screen("ingredients", "Ingredients", Icons.Default.LocalGroceryStore)
    data object MealPlanner : Screen("meal_planner", "Planner", Icons.Default.CalendarMonth)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.ShoppingLists,
    Screen.Recipes,
    Screen.Ingredients,
    Screen.MealPlanner,
    Screen.Settings
)

@Composable
fun AppNavigation(repository: AppRepository, activity: Activity) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory(repository))
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            LoadingScreen()
        }
        is AuthState.Unauthenticated -> {
            LoginScreen(authViewModel = authViewModel, activity = activity)
        }
        is AuthState.Authenticated, is AuthState.Guest -> {
            val userId = authViewModel.currentUserId ?: return
            MainScreen(
                repository = repository,
                userId = userId,
                authViewModel = authViewModel,
                activity = activity
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun MainScreen(
    repository: AppRepository,
    userId: String,
    authViewModel: AuthViewModel,
    @Suppress("UNUSED_PARAMETER") activity: Activity
) {
    val navController = rememberNavController()

    // Create shared ViewModels
    val recipesViewModel: RecipesViewModel = viewModel(factory = RecipesViewModel.Factory(repository))
    val shoppingListsViewModel: ShoppingListsViewModel = viewModel(factory = ShoppingListsViewModel.Factory(repository))
    val ingredientsViewModel: IngredientsViewModel = viewModel(factory = IngredientsViewModel.Factory(repository))
    val mealPlannerViewModel: MealPlannerViewModel = viewModel(factory = MealPlannerViewModel.Factory(repository))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository))

    // Initialize all ViewModels with the current user ID
    LaunchedEffect(userId) {
        if (authViewModel.isGuest) {
            recipesViewModel.loadOffline()
            shoppingListsViewModel.loadOffline()
            ingredientsViewModel.loadOffline()
            mealPlannerViewModel.loadOffline()
        } else {
            recipesViewModel.init(userId)
            shoppingListsViewModel.init(userId)
            ingredientsViewModel.init(userId)
            mealPlannerViewModel.init(userId)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ShoppingLists.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ShoppingLists.route) {
                ShoppingListsScreen(
                    viewModel = shoppingListsViewModel,
                    onOpenList = { listId -> navController.navigate("shopping_list_detail/$listId") }
                )
            }
            composable("shopping_list_detail/{listId}") { backStackEntry ->
                val listId = backStackEntry.arguments?.getString("listId") ?: return@composable
                ShoppingListDetailScreen(
                    listId = listId,
                    shoppingViewModel = shoppingListsViewModel,
                    ingredientsViewModel = ingredientsViewModel,
                    recipesViewModel = recipesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Recipes.route) {
                RecipesScreen(
                    viewModel = recipesViewModel,
                    onNavigateToAdd = { navController.navigate("add_recipe") }
                )
            }
            composable("add_recipe") {
                AddRecipeScreen(
                    viewModel = recipesViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Ingredients.route) {
                IngredientsScreen(viewModel = ingredientsViewModel)
            }
            composable(Screen.MealPlanner.route) {
                MealPlannerScreen(
                    viewModel = mealPlannerViewModel,
                    recipesViewModel = recipesViewModel
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }
}


