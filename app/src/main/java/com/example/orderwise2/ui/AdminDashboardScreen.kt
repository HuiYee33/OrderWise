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
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

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
    var purchaseHistory by remember { mutableStateOf(listOf<PurchaseRecord>()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()

    // Fetch purchase history from Firestore
    LaunchedEffect(Unit) {
        db.collection("purchaseHistory")
            .get()
            .addOnSuccessListener { result ->
                val records = mutableListOf<PurchaseRecord>()
                for (doc in result) {
                    try {
                        val record = doc.toObject(PurchaseRecord::class.java).copy(id = doc.id)
                        records.add(record)
                    } catch (e: Exception) {
                        Log.e("AdminDashboard", "Failed to parse record: ${doc.id}", e)
                    }
                }
                purchaseHistory = records
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("AdminDashboard", "Failed to load purchase history", e)
                isLoading = false
            }
    }

    // Compute summary
    val totalOrders = purchaseHistory.size
    val totalItems = purchaseHistory.sumOf { it.items.sumOf { item -> item.quantity } }
    val revenue = purchaseHistory.sumOf { it.items.sumOf { item -> item.unitPrice * item.quantity } }
    val dishCountMap = mutableMapOf<String, Int>()
    purchaseHistory.forEach { record ->
        record.items.forEach { item ->
            dishCountMap[item.name] = dishCountMap.getOrDefault(item.name, 0) + item.quantity
        }
    }
    val dishStats = dishCountMap.entries.sortedByDescending { it.value }.map { (name, qty) ->
        DishStats(name, qty, 0f, Color(0xFF4CAF50)) // You can add color/percentage logic if needed
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

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Overview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Overview",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Total Orders", totalOrders.toString(), Color(0xFF4CAF50))
                            StatItem("Total Items", totalItems.toString(), Color(0xFF2196F3))
                            StatItem("Revenue", "RM %.2f".format(revenue), Color(0xFFFF9800))
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