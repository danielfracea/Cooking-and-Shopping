package com.cooking.app.ui.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cooking.app.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipesScreen(
    viewModel: RecipesViewModel,
    onNavigateToAdd: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsState()
    val selectedRecipe by viewModel.selectedRecipe.collectAsState()
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var showCookingWizard by remember { mutableStateOf(false) }
    var wizardStep by remember { mutableIntStateOf(0) }

    if (selectedRecipe != null) {
        RecipeDetailScreen(
            recipe = selectedRecipe!!,
            onBack = { viewModel.selectRecipe(null) },
            onDelete = { viewModel.deleteRecipe(it); viewModel.selectRecipe(null) },
            onStartCooking = { showCookingWizard = true; wizardStep = 0 }
        )
        if (showCookingWizard && (selectedRecipe?.steps ?: emptyList()).isNotEmpty()) {
            CookingWizardDialog(
                recipe = selectedRecipe!!,
                currentStep = wizardStep,
                onNext = { if (wizardStep < (selectedRecipe!!.steps.size - 1)) wizardStep++ },
                onPrev = { if (wizardStep > 0) wizardStep-- },
                onClose = { showCookingWizard = false }
            )
        }
        return
    }

    val filteredRecipes = remember(recipes, selectedTag) {
        if (selectedTag == null) recipes
        else recipes.filter { it.tags.contains(selectedTag) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recipes", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Add recipe")
                    }
                }
            )
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📖", fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp))
                    Spacer(Modifier.height(16.dp))
                    Text("No recipes yet", style = MaterialTheme.typography.titleMedium)
                    Text("Add your first recipe!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onNavigateToAdd) { Text("Add Recipe") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedTag == null,
                                onClick = { selectedTag = null },
                                label = { Text("All") }
                            )
                        }
                        items(RECIPE_TAGS) { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = if (selectedTag == tag) null else tag },
                                label = { Text(tag) }
                            )
                        }
                    }
                }
                items(filteredRecipes) { recipe ->
                    RecipeCard(recipe = recipe, onClick = { viewModel.selectRecipe(recipe) })
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipe.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (recipe.description.isNotBlank()) {
                Text(
                    recipe.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (recipe.prepTime > 0) {
                    AssistChip(onClick = {}, label = { Text("⏱ ${recipe.prepTime} min") })
                }
                AssistChip(onClick = {}, label = { Text("👥 ${recipe.servings}") })
            }
            if (recipe.tags.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(recipe.tags) { tag ->
                        SuggestionChip(onClick = {}, label = { Text(tag, style = MaterialTheme.typography.labelSmall) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipe: Recipe,
    onBack: () -> Unit,
    onDelete: (String) -> Unit,
    onStartCooking: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(recipe.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
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
            // Tags
            if (recipe.tags.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(recipe.tags) { tag ->
                            SuggestionChip(onClick = {}, label = { Text(tag) })
                        }
                    }
                }
            }
            // Info cards
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (recipe.prepTime > 0) {
                        Card(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Prep Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${recipe.prepTime} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Servings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${recipe.servings}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Description
            if (recipe.description.isNotBlank()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Description", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(recipe.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            // Ingredients
            if (recipe.ingredients.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ingredients", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            recipe.ingredients.forEach { ing ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                                    Text("${ing.name} — ${ing.quantity} ${ing.unit}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            // Tools
            if (recipe.tools.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))
                            recipe.tools.forEach { tool ->
                                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                                    Text(tool, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
            // Steps
            if (recipe.steps.isNotEmpty()) {
                item {
                    Text("Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                items(recipe.steps.indices.toList()) { index ->
                    val step = recipe.steps[index]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(step.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(step.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onStartCooking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Cooking")
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Recipe") },
            text = { Text("Are you sure you want to delete \"${recipe.name}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(recipe.id); showDeleteConfirm = false }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CookingWizardDialog(
    recipe: Recipe,
    currentStep: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit
) {
    val step = recipe.steps[currentStep]
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text("Step ${currentStep + 1}/${recipe.steps.size}: ${step.title}", fontWeight = FontWeight.Bold)
        },
        text = { Text(step.description) },
        confirmButton = {
            if (currentStep < recipe.steps.size - 1) {
                Button(onClick = onNext) { Text("Next") }
            } else {
                Button(onClick = onClose) { Text("Finish 🎉") }
            }
        },
        dismissButton = {
            Row {
                if (currentStep > 0) {
                    TextButton(onClick = onPrev) { Text("Previous") }
                }
                TextButton(onClick = onClose) { Text("Close") }
            }
        }
    )
}
