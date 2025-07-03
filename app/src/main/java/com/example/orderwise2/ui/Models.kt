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
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val remarks: String
)

data class UserProfile(
    val email: String,
    val name: String,
    val password: String,
    val phone: String,
    val loyaltyPoints: Int
)

data class PurchaseRecord(
    val date: String,
    val items: List<CartItem>,
    val feedback: String
) 