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
import com.google.firebase.firestore.FirebaseFirestore
import com.example.orderwise2.ui.MenuItem
import com.example.orderwise2.ui.StockStatus
import com.example.orderwise2.ui.rememberMenuItems
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage

@Composable
fun MenuHomeScreen(navController: NavController) {
    val menuItems = rememberMenuItems().filter { it.stockStatus == StockStatus.AVAILABLE }
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
            items(menuItems) { food ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            navController.navigate(Screen.FoodDetail.route.replace("{foodId}", food.id))
                        },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        if (food.imageUri.isNotEmpty()) {
                            AsyncImage(
                                model = food.imageUri,
                                contentDescription = food.name,
                                modifier = Modifier
                                    .height(100.dp)
                                    .fillMaxWidth()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                                contentDescription = food.name,
                                modifier = Modifier
                                    .height(100.dp)
                                    .fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(food.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "RM ${food.price}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF4CAF50)
                        )
                    }
                }
            }
        }
    }
} 