package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize

data class PreOrder(
    val id: String,
    val customerName: String,
    val dishName: String,
    val quantity: Int,
    val remarks: String,
    val time: String
)

data class DishStats(
    val name: String,
    val quantity: Int,
    val percentage: Float,
    val color: Color
)

@Composable
fun AdminDashboardScreen(navController: NavController) {
    val preOrders = remember {
        listOf(
            PreOrder("1", "John Doe", "Shrimp Fried Rice", 2, "Extra spicy", "10:30 AM"),
            PreOrder("2", "Jane Smith", "Chicken Curry", 1, "No onions", "11:15 AM"),
            PreOrder("3", "Mike Johnson", "Beef Noodles", 3, "Less salt", "12:00 PM"),
            PreOrder("4", "Sarah Wilson", "Vegetable Stir Fry", 1, "", "12:30 PM")
        )
    }

    val dishStats = remember {
        listOf(
            DishStats("Shrimp Fried Rice", 25, 35f, Color(0xFF4CAF50)),
            DishStats("Chicken Curry", 18, 25f, Color(0xFF2196F3)),
            DishStats("Beef Noodles", 15, 20f, Color(0xFFFF9800)),
            DishStats("Vegetable Stir Fry", 12, 20f, Color(0xFF9C27B0))
        )
    }

    Scaffold(
        bottomBar = { AdminBottomNavigation(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Admin Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today's Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Today's Overview",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("Total Orders", preOrders.size.toString(), Color(0xFF4CAF50))
                        StatItem("Total Items", preOrders.sumOf { it.quantity }.toString(), Color(0xFF2196F3))
                        StatItem("Revenue", "RM ${preOrders.size * 15}", Color(0xFFFF9800))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dish Popularity Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Dish Popularity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    dishStats.forEach { dish ->
                        DishPopularityItem(dish)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Today's Pre-Orders
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Today's Pre-Orders",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(preOrders) { order ->
                            PreOrderItem(order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun DishPopularityItem(dish: DishStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            dish.name,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Text(
            "${dish.quantity} orders",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "${dish.percentage.toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = dish.color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .width((60 * dish.percentage / 100).dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dish.color)
            )
        }
    }
}

@Composable
fun PreOrderItem(order: PreOrder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    order.customerName,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    order.time,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${order.dishName} x${order.quantity}",
                fontSize = 14.sp
            )
            if (order.remarks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Remarks: ${order.remarks}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
} 