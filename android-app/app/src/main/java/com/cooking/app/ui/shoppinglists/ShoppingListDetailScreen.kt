package com.cooking.app.ui.shoppinglists

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.cooking.app.data.model.*
import com.cooking.app.ui.ingredients.IngredientsViewModel
import com.cooking.app.ui.recipes.RecipesViewModel

enum class SortMode { DEFAULT, NAME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListDetailScreen(
    listId: String,
    shoppingViewModel: ShoppingListsViewModel,
    ingredientsViewModel: IngredientsViewModel,
    recipesViewModel: RecipesViewModel,
    onBack: () -> Unit
) {
    val lists by shoppingViewModel.lists.collectAsState()
    val list = remember(lists, listId) { lists.find { it.id == listId } }
    val ingredients by ingredientsViewModel.ingredients.collectAsState()
    val recipes by recipesViewModel.recipes.collectAsState()
    val context = LocalContext.current

    var quickAddText by remember { mutableStateOf("") }
    var sortMode by remember { mutableStateOf(SortMode.DEFAULT) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddFromRecipeDialog by remember { mutableStateOf(false) }

    if (list == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("List not found")
        }
        return
    }

    val checkedCount = list.items.count { it.checked }
    val totalCount = list.items.size
    val progressPercent = if (totalCount > 0) checkedCount.toFloat() / totalCount else 0f

    val sortedItems = remember(list.items, sortMode) {
        when (sortMode) {
            SortMode.DEFAULT -> list.items.sortedWith(compareBy { it.checked })
            SortMode.NAME -> list.items.sortedWith(compareBy({ it.checked }, { it.name }))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(list.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val shareText = buildString {
                            appendLine("🛒 ${list.name}")
                            list.items.forEach { item ->
                                val check = if (item.checked) "✓" else "•"
                                val qty = if (item.unit.isNotBlank()) "${item.quantity} ${item.unit}" else "${item.quantity}"
                                appendLine("$check ${item.name} ($qty)")
                            }
                        }
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Shopping List", shareText))
                        Toast.makeText(context, "List copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = {
                        sortMode = if (sortMode == SortMode.DEFAULT) SortMode.NAME else SortMode.DEFAULT
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddItemDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Progress
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(onClick = {}, label = { Text("$checkedCount/$totalCount items") })
                            Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        }
                        if (totalCount > 0) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progressPercent },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Quick add
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = quickAddText,
                            onValueChange = { quickAddText = it },
                            label = { Text("Quick add item...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            trailingIcon = if (quickAddText.isNotBlank()) ({
                                IconButton(onClick = { quickAddText = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }) else null
                        )
                        FilledIconButton(
                            onClick = {
                                if (quickAddText.isNotBlank()) {
                                    val name = quickAddText.trim()
                                    val matched = ingredients.find { it.name.equals(name, ignoreCase = true) }
                                    shoppingViewModel.addItemToList(
                                        listId,
                                        ShoppingItem(
                                            name = matched?.name ?: name,
                                            quantity = 1.0,
                                            unit = matched?.unit ?: "",
                                            ingredientId = matched?.id
                                        )
                                    )
                                    quickAddText = ""
                                }
                            },
                            enabled = quickAddText.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }

            // Add from recipe button
            item {
                OutlinedButton(
                    onClick = { showAddFromRecipeDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Ingredients from Recipe")
                }
            }

            if (list.items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = androidx.compose.ui.unit.TextUnit(40f, androidx.compose.ui.unit.TextUnitType.Sp))
                            Spacer(Modifier.height(8.dp))
                            Text("No items yet", style = MaterialTheme.typography.titleMedium)
                            Text("Add items using the button above", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(sortedItems) { item ->
                    ShoppingItemRow(
                        item = item,
                        onToggle = { shoppingViewModel.toggleItem(listId, item.id) },
                        onDelete = { shoppingViewModel.removeItemFromList(listId, item.id) }
                    )
                }
            }
        }
    }

    // Add Item Dialog
    if (showAddItemDialog) {
        AddItemDialog(
            ingredients = ingredients,
            onDismiss = { showAddItemDialog = false },
            onAdd = { item ->
                shoppingViewModel.addItemToList(listId, item)
                showAddItemDialog = false
            }
        )
    }

    // Add from Recipe Dialog
    if (showAddFromRecipeDialog) {
        AddFromRecipeDialog(
            recipes = recipes,
            onDismiss = { showAddFromRecipeDialog = false },
            onAdd = { recipe ->
                shoppingViewModel.addRecipeIngredientsToList(listId, recipe.ingredients)
                showAddFromRecipeDialog = false
            }
        )
    }
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (item.checked) TextDecoration.LineThrough else null,
                    color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
                if (item.unit.isNotBlank() || item.quantity != 1.0) {
                    val qty = if (item.quantity % 1 == 0.0) item.quantity.toInt().toString() else "%.2f".format(item.quantity)
                    Text(
                        text = if (item.unit.isNotBlank()) "$qty ${item.unit}" else qty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddItemDialog(
    ingredients: List<com.cooking.app.data.model.Ingredient>,
    onDismiss: () -> Unit,
    onAdd: (ShoppingItem) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Name with suggestions
                ExposedDropdownMenuBox(
                    expanded = expanded && name.isNotBlank(),
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; expanded = true },
                        label = { Text("Item name *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        singleLine = true
                    )
                    if (name.isNotBlank()) {
                        val filtered = ingredients.filter { it.name.contains(name, ignoreCase = true) }.take(5)
                        if (filtered.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                filtered.forEach { ing ->
                                    DropdownMenuItem(
                                        text = { Text(ing.name) },
                                        onClick = {
                                            name = ing.name
                                            unit = ing.unit
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Qty") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val matched = ingredients.find { it.name.equals(name, ignoreCase = true) }
                        onAdd(ShoppingItem(
                            name = matched?.name ?: name.trim(),
                            quantity = quantity.toDoubleOrNull() ?: 1.0,
                            unit = unit.trim(),
                            ingredientId = matched?.id
                        ))
                    }
                },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddFromRecipeDialog(
    recipes: List<com.cooking.app.data.model.Recipe>,
    onDismiss: () -> Unit,
    onAdd: (com.cooking.app.data.model.Recipe) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add from Recipe") },
        text = {
            if (recipes.isEmpty()) {
                Text("No recipes available")
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(recipes) { recipe ->
                        TextButton(
                            onClick = { onAdd(recipe) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(recipe.name, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${recipe.ingredients.size} ingredients",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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
