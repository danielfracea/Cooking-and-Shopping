package com.cooking.app.ui.mealplanner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cooking.app.data.model.*
import com.cooking.app.ui.recipes.RecipesViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerScreen(
    viewModel: MealPlannerViewModel,
    recipesViewModel: RecipesViewModel
) {
    val plan by viewModel.plan.collectAsState()
    val recipes by recipesViewModel.recipes.collectAsState()

    val today = remember { LocalDate.now() }
    // Show current week starting Monday
    val weekStart = remember { today.with(DayOfWeek.MONDAY) }
    val weekDays = remember { (0..6).map { weekStart.plusDays(it.toLong()) } }

    var showPickerFor by remember { mutableStateOf<Pair<String, String>?>(null) } // date to slot

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Planner", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weekDays) { date ->
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val dayMeals = plan[dateStr] ?: DayMeals()
                val isToday = date == today

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (isToday) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.format(DateTimeFormatter.ofPattern("MMM d"))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                        if (isToday) {
                            Text("Today", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(8.dp))

                        MEAL_SLOTS.forEach { slot ->
                            val entry = when (slot) {
                                "breakfast" -> dayMeals.breakfast
                                "lunch" -> dayMeals.lunch
                                "dinner" -> dayMeals.dinner
                                else -> null
                            }
                            MealSlotRow(
                                slot = slot,
                                entry = entry,
                                onAdd = { showPickerFor = dateStr to slot },
                                onClear = { viewModel.setMeal(dateStr, slot, null) }
                            )
                        }
                    }
                }
            }
        }
    }

    showPickerFor?.let { (date, slot) ->
        RecipePickerDialog(
            slot = slot,
            recipes = recipes,
            onDismiss = { showPickerFor = null },
            onPick = { recipe ->
                viewModel.setMeal(date, slot, MealEntry(recipe.id, recipe.name))
                showPickerFor = null
            }
        )
    }
}

@Composable
fun MealSlotRow(
    slot: String,
    entry: MealEntry?,
    onAdd: () -> Unit,
    onClear: () -> Unit
) {
    val emoji = when (slot) {
        "breakfast" -> "🌅"
        "lunch" -> "☀️"
        "dinner" -> "🌙"
        else -> "🍽"
    }
    val label = slot.replaceFirstChar { it.uppercase() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$emoji $label", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(90.dp))
        if (entry != null) {
            Text(
                text = entry.recipeName,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
            }
        } else {
            TextButton(onClick = onAdd, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pick recipe", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RecipePickerDialog(
    slot: String,
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onPick: (Recipe) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pick recipe for ${slot.replaceFirstChar { it.uppercase() }}") },
        text = {
            if (recipes.isEmpty()) {
                Text("No recipes available")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(recipes) { recipe ->
                        TextButton(
                            onClick = { onPick(recipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(recipe.name, fontWeight = FontWeight.SemiBold)
                                if (recipe.tags.isNotEmpty()) {
                                    Text(
                                        recipe.tags.joinToString(", "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
