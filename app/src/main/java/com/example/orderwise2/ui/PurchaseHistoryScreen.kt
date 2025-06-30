package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PurchaseHistoryScreen(navController: NavController) {
    val history = listOf(
        PurchaseRecord(
            date = "2024-05-30",
            items = listOf(
                CartItem("Shrimp Fried Rice", 1, 12.90, "Add Egg"),
                CartItem("Chicken Rice", 1, 11.20, "No Cucumber")
            ),
            feedback = "Delicious!"
        ),
        PurchaseRecord(
            date = "2024-05-25",
            items = listOf(
                CartItem("Mac & Cheese", 2, 10.50, "Extra Cheese"),
            ),
            feedback = ""
        )
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Purchase History", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(12.dp))
        if (history.isEmpty()) {
            Text("No purchase history.", color = Color.Gray)
        } else {
            history.forEach { record ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Date: ${record.date}", fontWeight = FontWeight.SemiBold)
                        record.items.forEach { item ->
                            Text("- ${item.name} x${item.quantity} (${item.remarks})", fontSize = 14.sp)
                        }
                        if (record.feedback.isNotBlank()) {
                            Text("Feedback: ${record.feedback}", fontSize = 14.sp, color = Color(0xFF388E3C))
                        } else {
                            OutlinedButton(
                                onClick = { /* TODO: Feedback logic */ },
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text("Leave Feedback")
                            }
                        }
                    }
                }
            }
        }
    }
} 