package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OrderLaterScreen(navController: NavController) {
    val paymentMethods = listOf("Touch 'n Go", "Online Banking", "Credit/Debit Card")
    var selectedPayment by remember { mutableStateOf(paymentMethods[0]) }
    var selectedDate by remember { mutableStateOf("2024-06-01") }
    var selectedTime by remember { mutableStateOf("12:00 PM") }
    var voucherCode by remember { mutableStateOf("") }
    val orderSummary = listOf(
        CartItem("Shrimp Fried Rice", 2, 12.90, "Add Egg, Remove Onion"),
        CartItem("Mac & Cheese", 1, 10.50, "Extra Cheese")
    )
    val subtotal = orderSummary.sumOf { it.unitPrice * it.quantity }
    val discount = if (voucherCode == "SAVE5") 5.0 else 0.0
    val total = subtotal - discount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Order Later & Payment", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Date:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = selectedDate,
                onValueChange = { selectedDate = it },
                modifier = Modifier.width(120.dp),
                singleLine = true
            )
            Spacer(Modifier.width(16.dp))
            Text("Time:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = selectedTime,
                onValueChange = { selectedTime = it },
                modifier = Modifier.width(100.dp),
                singleLine = true
            )
        }
        Divider()
        Text("Payment Method", fontWeight = FontWeight.SemiBold)
        paymentMethods.forEach { method ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedPayment == method,
                    onClick = { selectedPayment = method }
                )
                Text(method)
            }
        }
        Divider()
        Text("Order Summary", fontWeight = FontWeight.SemiBold)
        orderSummary.forEach { item ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${item.name} x${item.quantity}", Modifier.weight(1f))
                Text("RM %.2f".format(item.unitPrice * item.quantity))
            }
            Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            Text("Subtotal:", Modifier.weight(1f))
            Text("RM %.2f".format(subtotal))
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Voucher:", Modifier.weight(1f))
            OutlinedTextField(
                value = voucherCode,
                onValueChange = { voucherCode = it },
                placeholder = { Text("Enter code") },
                modifier = Modifier.width(120.dp),
                singleLine = true
            )
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Discount:", Modifier.weight(1f))
            Text("-RM %.2f".format(discount))
        }
        Row(Modifier.fillMaxWidth()) {
            Text("Total:", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text("RM %.2f".format(total), fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = { navController.navigate(Screen.PaymentSuccess.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pay Now")
        }
    }
} 