package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

// CartScreen: Displays the user's current cart, allows editing and checkout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, cartViewModel: CartViewModel) {
    val cartItems = cartViewModel.cartItems
    val subtotal = cartViewModel.getTotal()

    Scaffold(
        bottomBar = { UserBottomNavigation(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Text("Your Cart", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))

            if (cartItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your cart is empty.", color = Color.Gray, fontSize = 16.sp)
                    Spacer(Modifier.height(16.dp))
                    Text("Add some delicious food to get started!", color = Color.Gray, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f), // take up remaining space
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(cartItems) { index, item ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        item.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { cartViewModel.removeFromCart(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                                    }
                                }
                                Text("Remarks: ${item.remarks}", fontSize = 14.sp, color = Color.Gray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Qty: ${item.quantity}", fontSize = 14.sp)
                                    Spacer(Modifier.width(16.dp))
                                    Text("Unit: RM %.2f".format(item.unitPrice), fontSize = 14.sp)
                                    Spacer(Modifier.weight(1f))
                                    Text(
                                        "Total: RM %.2f".format(item.unitPrice * item.quantity),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Quantity: ", fontSize = 14.sp)
                                    IconButton(onClick = {
                                        cartViewModel.updateQuantity(index, item.quantity - 1)
                                    }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    Text(
                                        item.quantity.toString(),
                                        fontSize = 16.sp,
                                        modifier = Modifier.width(32.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    IconButton(onClick = {
                                        cartViewModel.updateQuantity(index, item.quantity + 1)
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider()
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Subtotal:", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.weight(1f))
                    Text("RM %.2f".format(subtotal), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { navController.navigate(Screen.MenuHome.route) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Add More")
                    }
                    Button(
                        onClick = { navController.navigate(Screen.OrderLater.route) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Checkout Now")
                    }
                }
            }
        }
    }
}
