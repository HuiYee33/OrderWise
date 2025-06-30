package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ReceiptScreen(navController: NavController) {
    val items = listOf(
        CartItem("Shrimp Fried Rice", 2, 12.90, "Add Egg, Remove Onion"),
        CartItem("Mac & Cheese", 1, 10.50, "Extra Cheese")
    )
    val subtotal = items.sumOf { it.unitPrice * it.quantity }
    val tax = subtotal * 0.06
    val total = subtotal + tax
    val loyaltyPoints = (total / 2).toInt()
    val businessAddress = "123 Cafe Lane, Kuala Lumpur, Malaysia"
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Receipt", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        items.forEach { item ->
            Row(Modifier.fillMaxWidth()) {
                Text("${item.name} x${item.quantity}", Modifier.weight(1f))
                Text("RM %.2f".format(item.unitPrice * item.quantity))
            }
            Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
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