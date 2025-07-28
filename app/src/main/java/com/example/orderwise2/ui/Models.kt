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

// Data class representing a completed purchase record
// Includes optional pickupDate and pickupTimeSlot for 'order later' orders
// pickupDate: The day the user wants to pick up the order (e.g., "Tue 29")
// pickupTimeSlot: The time window for pickup (e.g., "10am - 10.30am")
data class PurchaseRecord(
    val id: String = "", // Unique purchase ID
    val date: String = "", // Date of purchase (order placed)
    val items: List<CartItem> = emptyList(), // List of items purchased
    var feedback: String = "", // User feedback for this purchase
    val userEmail: String = "", // Email of the user who made the purchase
    val pickupDate: String? = null, // Optional: pickup day for 'order later' orders
    val pickupTimeSlot: String? = null // Optional: pickup time slot for 'order later' orders
) 