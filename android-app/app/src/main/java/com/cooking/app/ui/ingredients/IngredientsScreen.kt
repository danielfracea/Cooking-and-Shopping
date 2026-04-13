package com.cooking.app.ui.ingredients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cooking.app.data.model.Ingredient
import com.cooking.app.data.model.INGREDIENT_UNITS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientsScreen(viewModel: IngredientsViewModel) {
    val ingredients by viewModel.ingredients.collectAsState()
    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<Ingredient?>(null) }

    val categories = remember(ingredients) {
        ingredients.map { it.category }.distinct().sorted()
    }
    val filtered = remember(ingredients, search, selectedCategory) {
        ingredients.filter { ing ->
            ing.name.contains(search, ignoreCase = true) &&
                (selectedCategory == null || ing.category == selectedCategory)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingredients", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { editingIngredient = null; showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add ingredient")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filters
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (search.isNotBlank()) ({
                        IconButton(onClick = { search = "" }) { Icon(Icons.Default.Clear, contentDescription = "Clear") }
                    }) else null
                )
                var categoryExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedCategory ?: "All",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("All") }, onClick = { selectedCategory = null; categoryExpanded = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; categoryExpanded = false })
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No ingredients found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { ing ->
                        var showDeleteConfirm by remember { mutableStateOf(false) }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(ing.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    SuggestionChip(onClick = {}, label = { Text(ing.category, style = MaterialTheme.typography.labelSmall) })
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Text("💰 ${ing.price} RON/${ing.unit}", style = MaterialTheme.typography.bodySmall)
                                    Text("🔥 ${ing.calories.toInt()} kcal", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("P: ${ing.protein}g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("C: ${ing.carbs}g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("F: ${ing.fat}g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { editingIngredient = ing; showAddDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit")
                                    }
                                    TextButton(
                                        onClick = { showDeleteConfirm = true },
                                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Delete Ingredient") },
                                text = { Text("Delete \"${ing.name}\"?") },
                                confirmButton = {
                                    TextButton(onClick = { viewModel.deleteIngredient(ing.id); showDeleteConfirm = false }) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        IngredientDialog(
            initial = editingIngredient,
            onDismiss = { showAddDialog = false; editingIngredient = null },
            onSave = { updated ->
                if (editingIngredient != null) {
                    viewModel.updateIngredient(editingIngredient!!.id, updated)
                } else {
                    viewModel.addIngredient(updated)
                }
                showAddDialog = false
                editingIngredient = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDialog(
    initial: Ingredient?,
    onDismiss: () -> Unit,
    onSave: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var unit by remember { mutableStateOf(initial?.unit ?: "kg") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "0") }
    var calories by remember { mutableStateOf(initial?.calories?.toString() ?: "0") }
    var protein by remember { mutableStateOf(initial?.protein?.toString() ?: "0") }
    var carbs by remember { mutableStateOf(initial?.carbs?.toString() ?: "0") }
    var fat by remember { mutableStateOf(initial?.fat?.toString() ?: "0") }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial != null) "Edit Ingredient" else "Add Ingredient") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 480.dp)
            ) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f), singleLine = true)
                        ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }, modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = unit,
                                onValueChange = { unit = it },
                                label = { Text("Unit") },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                singleLine = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) }
                            )
                            ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                                INGREDIENT_UNITS.forEach { u ->
                                    DropdownMenuItem(text = { Text(u) }, onClick = { unit = u; unitExpanded = false })
                                }
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calories") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein g") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Carbs g") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fat g") }, modifier = Modifier.weight(1f), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(Ingredient(
                            name = name.trim(),
                            category = category.trim().ifBlank { "Other" },
                            unit = unit.trim().ifBlank { "kg" },
                            price = price.toDoubleOrNull() ?: 0.0,
                            calories = calories.toDoubleOrNull() ?: 0.0,
                            protein = protein.toDoubleOrNull() ?: 0.0,
                            carbs = carbs.toDoubleOrNull() ?: 0.0,
                            fat = fat.toDoubleOrNull() ?: 0.0
                        ))
                    }
                },
                enabled = name.isNotBlank()
            ) { Text(if (initial != null) "Update" else "Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
