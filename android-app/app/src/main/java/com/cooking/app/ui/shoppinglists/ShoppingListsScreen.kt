package com.cooking.app.ui.shoppinglists

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsScreen(
    viewModel: ShoppingListsViewModel,
    onOpenList: (String) -> Unit
) {
    val lists by viewModel.lists.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var newListName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shopping Lists", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "New list")
                    }
                }
            )
        }
    ) { padding ->
        if (lists.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🛒", fontSize = androidx.compose.ui.unit.TextUnit(48f, androidx.compose.ui.unit.TextUnitType.Sp))
                    Spacer(Modifier.height(16.dp))
                    Text("No shopping lists", style = MaterialTheme.typography.titleMedium)
                    Text("Create your first list!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showCreateDialog = true }) { Text("New List") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lists) { list ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    Card(
                        onClick = { onOpenList(list.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(list.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                val checkedCount = list.items.count { it.checked }
                                Text(
                                    "${list.items.size} items · $checkedCount done",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showDeleteConfirm = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete List") },
                            text = { Text("Delete \"${list.name}\"?") },
                            confirmButton = {
                                TextButton(onClick = { viewModel.deleteList(list.id); showDeleteConfirm = false }) {
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

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newListName = "" },
            title = { Text("New Shopping List") },
            text = {
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("List name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            val list = viewModel.createList(newListName.trim())
                            newListName = ""
                            showCreateDialog = false
                            onOpenList(list.id)
                        }
                    },
                    enabled = newListName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newListName = "" }) { Text("Cancel") }
            }
        )
    }
}
