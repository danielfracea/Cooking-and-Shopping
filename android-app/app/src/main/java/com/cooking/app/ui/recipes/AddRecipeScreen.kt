package com.cooking.app.ui.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cooking.app.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    viewModel: RecipesViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("2") }
    var prepTime by remember { mutableStateOf("") }
    var selectedTags by remember { mutableStateOf<List<String>>(emptyList()) }
    var toolInput by remember { mutableStateOf("") }
    var tools by remember { mutableStateOf<List<String>>(emptyList()) }
    var steps by remember { mutableStateOf<List<RecipeStep>>(emptyList()) }
    var stepTitleInput by remember { mutableStateOf("") }
    var stepDescInput by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Recipe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic info
            item {
                Text("Basic Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("Recipe Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError,
                    supportingText = if (nameError) ({ Text("Name is required") }) else null
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = servings,
                        onValueChange = { servings = it },
                        label = { Text("Servings") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = prepTime,
                        onValueChange = { prepTime = it },
                        label = { Text("Prep Time (min)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // Tags
            item {
                Text("Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(RECIPE_TAGS) { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag),
                            onClick = {
                                selectedTags = if (selectedTags.contains(tag))
                                    selectedTags - tag
                                else
                                    selectedTags + tag
                            },
                            label = { Text(tag) }
                        )
                    }
                }
            }

            // Tools
            item {
                Text("Tools", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = toolInput,
                        onValueChange = { toolInput = it },
                        label = { Text("Add tool") },
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick = {
                            if (toolInput.isNotBlank()) {
                                tools = tools + toolInput.trim()
                                toolInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add tool")
                    }
                }
            }
            if (tools.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(tools) { tool ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(tool) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { tools = tools - tool },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Steps
            item {
                Text("Steps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(steps.indices.toList()) { index ->
                val step = steps[index]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${index + 1}.", fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(step.title, fontWeight = FontWeight.SemiBold)
                            Text(step.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { steps = steps.toMutableList().also { it.removeAt(index) } }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove step")
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("New Step", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = stepTitleInput,
                            onValueChange = { stepTitleInput = it },
                            label = { Text("Step title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = stepDescInput,
                            onValueChange = { stepDescInput = it },
                            label = { Text("Step description") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                        Button(
                            onClick = {
                                if (stepTitleInput.isNotBlank()) {
                                    steps = steps + RecipeStep(
                                        id = "step-${steps.size + 1}",
                                        title = stepTitleInput.trim(),
                                        description = stepDescInput.trim()
                                    )
                                    stepTitleInput = ""
                                    stepDescInput = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Add Step")
                        }
                    }
                }
            }

            // Save button
            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (name.isBlank()) { nameError = true; return@Button }
                        viewModel.addRecipe(
                            name = name.trim(),
                            description = description.trim(),
                            servings = servings.toIntOrNull() ?: 1,
                            prepTime = prepTime.toIntOrNull() ?: 0,
                            tags = selectedTags,
                            tools = tools,
                            ingredients = emptyList(),
                            steps = steps
                        )
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Recipe")
                }
            }
        }
    }
}
