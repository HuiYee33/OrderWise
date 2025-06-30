package com.example.orderwise2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FoodDetailScreen(navController: NavController, cartViewModel: CartViewModel, foodId: String) {
    // Create food item based on the selected food ID
    val food = when (foodId) {
        "shrimp_fried_rice" -> FoodItem(
            id = "shrimp_fried_rice",
            name = "Shrimp Fried Rice",
            imageRes = android.R.drawable.ic_menu_gallery,
            ingredients = listOf("Rice", "Shrimp", "Egg", "Onion", "Garlic"),
            price = 12.90
        )
        "mac_cheese" -> FoodItem(
            id = "mac_cheese",
            name = "Mac & Cheese",
            imageRes = android.R.drawable.ic_menu_gallery,
            ingredients = listOf("Macaroni", "Cheese", "Milk", "Butter"),
            price = 10.50
        )
        "chicken_rice" -> FoodItem(
            id = "chicken_rice",
            name = "Chicken Rice",
            imageRes = android.R.drawable.ic_menu_gallery,
            ingredients = listOf("Rice", "Chicken", "Cucumber", "Soy Sauce"),
            price = 11.20
        )
        else -> FoodItem(
            id = foodId,
            name = "Unknown Food",
            imageRes = android.R.drawable.ic_menu_gallery,
            ingredients = listOf("Standard ingredients"),
            price = 10.00
        )
    }
    
    // Create ingredient options based on the food's base ingredients
    val baseIngredients = food.ingredients.toSet()
    val additionalIngredients = when (food.id) {
        "shrimp_fried_rice" -> listOf(
            IngredientOption("Extra Shrimp", 3.00),
            IngredientOption("Extra Egg", 1.50),
            IngredientOption("Chili", 0.50),
            IngredientOption("Spring Onion", 0.30)
        )
        "mac_cheese" -> listOf(
            IngredientOption("Extra Cheese", 2.00),
            IngredientOption("Bacon Bits", 2.50),
            IngredientOption("Breadcrumbs", 1.00),
            IngredientOption("Truffle Oil", 5.00)
        )
        "chicken_rice" -> listOf(
            IngredientOption("Extra Chicken", 3.50),
            IngredientOption("Chili Sauce", 0.50),
            IngredientOption("Ginger", 0.30),
            IngredientOption("Sesame Oil", 1.00)
        )
        else -> listOf(
            IngredientOption("Extra Portion", 2.00),
            IngredientOption("Special Sauce", 1.00)
        )
    }
    
    // Combine base ingredients (always included) with additional options
    val allIngredientOptions = additionalIngredients
    
    var selectedAdditionalIngredients by remember { mutableStateOf(setOf<String>()) }
    var quantity by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = food.imageRes),
            contentDescription = food.name,
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Text(food.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text("Base Ingredients: ${baseIngredients.joinToString(", ")}", fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
        Spacer(Modifier.height(18.dp))
        Text("Additional Options", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            allIngredientOptions.forEach { ingredient ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedAdditionalIngredients.contains(ingredient.name),
                        onCheckedChange = { checked ->
                            selectedAdditionalIngredients = if (checked) {
                                selectedAdditionalIngredients + ingredient.name
                            } else {
                                selectedAdditionalIngredients - ingredient.name
                            }
                        }
                    )
                    Text(ingredient.name, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text("+RM %.2f".format(ingredient.price), fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quantity:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(quantity.toString(), fontSize = 18.sp, modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = { quantity++ }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { 
                // Calculate total price including selected additional ingredients
                val additionalPrice = allIngredientOptions
                    .filter { selectedAdditionalIngredients.contains(it.name) }
                    .sumOf { it.price }
                val totalPrice = food.price + additionalPrice
                
                // Create cart item with selected additional ingredients as remarks
                val remarks = if (selectedAdditionalIngredients.isNotEmpty()) {
                    "Added: ${selectedAdditionalIngredients.joinToString(", ")}"
                } else {
                    "Standard"
                }
                
                val cartItem = CartItem(
                    name = food.name,
                    quantity = quantity,
                    unitPrice = totalPrice,
                    remarks = remarks
                )
                
                cartViewModel.addToCart(cartItem)
                navController.navigate(Screen.Cart.route)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Add to Cart")
        }
    }
} 