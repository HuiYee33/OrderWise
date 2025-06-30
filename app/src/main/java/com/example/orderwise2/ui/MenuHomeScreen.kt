package com.example.orderwise2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun MenuHomeScreen(navController: NavController) {
    val foodList = listOf(
        FoodItem(
            id = "shrimp_fried_rice",
            name = "Shrimp Fried Rice",
            imageRes = android.R.drawable.ic_menu_gallery, // Placeholder image
            ingredients = listOf("Rice", "Shrimp", "Egg", "Onion", "Garlic"),
            price = 12.90
        ),
        FoodItem(
            id = "mac_cheese",
            name = "Mac & Cheese",
            imageRes = android.R.drawable.ic_menu_gallery, // Placeholder image
            ingredients = listOf("Macaroni", "Cheese", "Milk", "Butter"),
            price = 10.50
        ),
        FoodItem(
            id = "chicken_rice",
            name = "Chicken Rice",
            imageRes = android.R.drawable.ic_menu_gallery, // Placeholder image
            ingredients = listOf("Rice", "Chicken", "Cucumber", "Soy Sauce"),
            price = 11.20
        )
    )
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Menu", "Home", "Cart", "Profile")

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        icon = {
                            when (label) {
                                "Menu" -> Icon(Icons.Default.RestaurantMenu, contentDescription = null)
                                "Home" -> Icon(Icons.Default.Home, contentDescription = null)
                                "Cart" -> Icon(Icons.Default.ShoppingCart, contentDescription = null)
                                "Profile" -> Icon(Icons.Default.Person, contentDescription = null)
                            }
                        },
                        label = { Text(label) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            when (label) {
                                "Menu" -> navController.navigate(Screen.MenuHome.route)
                                "Home" -> navController.navigate(Screen.MenuHome.route)
                                "Cart" -> navController.navigate(Screen.Cart.route)
                                "Profile" -> navController.navigate(Screen.Profile.route)
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(innerPadding)
        ) {
            items(foodList) { food ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            // Navigate to FoodDetail with the food ID
                            navController.navigate(Screen.FoodDetail.route.replace("{foodId}", food.id))
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = food.imageRes),
                            contentDescription = food.name,
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
} 