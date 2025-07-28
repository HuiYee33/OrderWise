package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// ReceiptScreen: Displays a detailed receipt for a completed purchase
@Composable
fun ReceiptScreen(navController: NavController, purchase: PurchaseRecord?) {
    // If no purchase, show message and return
    if (purchase == null) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No recent purchase found.")
            Button(onClick = { navController.navigate(Screen.MenuHome.route) }) {
                Text("Return to Home")
            }
        }
        return
    }
    // Calculate totals and loyalty points
    val items = purchase.items
    val subtotal = items.sumOf { it.unitPrice * it.quantity }
    val tax = subtotal * 0.06
    val total = subtotal + tax
    val loyaltyPoints = (total).toInt()
    val businessAddress = "123 Cafe Lane, Kuala Lumpur, Malaysia"
    // Main receipt layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Receipt", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        // Show pickup info if present
        if (purchase.pickupDate != null && purchase.pickupTimeSlot != null) {
            Text("Pick up on ${purchase.pickupDate} at ${purchase.pickupTimeSlot}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
        }
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Row(Modifier.fillMaxWidth()) {
                Text("${item.name} x${item.quantity}", Modifier.weight(1f))
                Text("RM %.2f".format(item.unitPrice * item.quantity))
            }
            if (item.remarks.isNotBlank()) {
                Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
            }
        }
        Divider()
        Row(Modifier.fillMaxWidth()) {
            Text("Subtotal:", Modifier.weight(1f))
            Text("RM %.2f".format(subtotal))
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Tax (6%):", Modifier.weight(1f))
            Text("RM %.2f".format(tax))
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Total:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("RM %.2f".format(total), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("Loyalty Points Earned: $loyaltyPoints", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("Business Address:", fontWeight = FontWeight.SemiBold)
        Text(businessAddress, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { navController.navigate(Screen.MenuHome.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Return to Home")
        }
    }
} 