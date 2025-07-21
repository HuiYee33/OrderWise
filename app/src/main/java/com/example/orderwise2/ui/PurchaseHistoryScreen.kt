package com.example.orderwise2.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PurchaseHistoryScreen(navController: NavController, cartViewModel: CartViewModel) {
    LaunchedEffect(Unit) {
        cartViewModel.loadPurchaseHistory()
    }
    val history = cartViewModel.purchaseHistory
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        if (history.isEmpty()) {
            item {
                Text("No purchase history found.", color = Color.Gray, modifier = Modifier.padding(16.dp))
            }
        }
        items(history) { record ->
            var feedback by remember { mutableStateOf(record.feedback) }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Date: ${record.date}", fontWeight = FontWeight.Bold)
                    record.items.forEach { item ->
                        Text("${item.name} x${item.quantity} - RM %.2f".format(item.unitPrice * item.quantity))
                        if (item.remarks.isNotBlank()) {
                            Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                    }
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("Feedback") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = feedback.isBlank()
                    )
                    if (feedback.isNotBlank()) {
                        Text("Thank you for your feedback", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                    } else {
                        Button(
                            onClick = {
                                cartViewModel.updateFeedback(record.id, feedback)
                                Toast.makeText(context, "Thank you for your feedback", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Submit Feedback")
                        }
                    }
                }
            }
        }
    }
} 