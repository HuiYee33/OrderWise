package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// ReviewFoodScreen: Allows users to leave reviews and ratings for food items
@Composable
fun ReviewFoodScreen(
    navController: NavController,
    cartItem: CartItem,
    cartViewModel: CartViewModel
) {
    var quantity by remember { mutableStateOf(cartItem.quantity) }
    val cost = cartItem.unitPrice * quantity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(cartItem.name, fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleLarge.fontSize)
            if (cartItem.remarks.isNotBlank()) {
                Text("Remarks: ${cartItem.remarks}")
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Quantity:", fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                }
                Text(quantity.toString(), modifier = Modifier.width(32.dp))
                IconButton(onClick = { quantity++ }) {
                    Icon(Icons.Default.Add, contentDescription = "Increase")
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Cost: RM %.2f".format(cost), fontWeight = FontWeight.Bold)
        }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigate(Screen.MenuHome.route) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add More")
            }
            Spacer(Modifier.width(16.dp))
            Button(
                onClick = {
                    // Remove the old item and add the updated one
                    cartViewModel.removeFromCart(
                        cartViewModel.cartItems.indexOfFirst {
                            it.name == cartItem.name && it.remarks == cartItem.remarks
                        }
                    )
                    cartViewModel.addToCart(cartItem.copy(quantity = quantity))
                    navController.navigate(Screen.Cart.route)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Make Payment")
            }
        }
    }
} 