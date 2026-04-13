package com.cooking.app.data.model

data class ShoppingItem(
    val id: String = "",
    val name: String = "",
    val quantity: Double = 1.0,
    val unit: String = "",
    val checked: Boolean = false,
    val ingredientId: String? = null
)

data class ShoppingList(
    val id: String = "",
    val name: String = "",
    val items: List<ShoppingItem> = emptyList(),
    val createdAt: String = ""
)
