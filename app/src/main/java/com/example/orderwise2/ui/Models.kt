package com.example.orderwise2.ui

data class FoodItem(
    val id: String,
    val name: String,
    val imageRes: Int,
    val ingredients: List<String>,
    val price: Double
)

data class IngredientOption(
    val name: String = "",
    val price: Double = 0.0
)

data class CartItem(
    val name: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val remarks: String = ""
)

data class UserProfile(
    val email: String,
    val name: String,
    val password: String,
    val phone: String,
    val loyaltyPoints: Int
)

data class PurchaseRecord(
    val id: String = "",
    val date: String = "",
    val items: List<CartItem> = emptyList(),
    var feedback: String = "",
    val userEmail: String = ""
) 