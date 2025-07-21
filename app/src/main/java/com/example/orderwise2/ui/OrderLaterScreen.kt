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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.SetOptions

@Composable
fun OrderLaterScreen(
    navController: NavController,
    cartItems: List<CartItem>,
    cartViewModel: CartViewModel
) {
    val context = LocalContext.current
    val paymentMethods = listOf("Touch 'n Go", "Online Banking", "Credit/Debit Card")
    var selectedPayment by remember { mutableStateOf(paymentMethods[0]) }
    var selectedDate by remember { mutableStateOf("2024-06-01") }
    var selectedTime by remember { mutableStateOf("12:00 PM") }
    var voucherCode by remember { mutableStateOf("") }
    val subtotal = cartItems.sumOf { it.unitPrice * it.quantity }
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
        cartItems.forEach { item ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${item.name} x${item.quantity}", Modifier.weight(1f))
                Text("RM %.2f".format(item.unitPrice * item.quantity))
            }
            if (item.remarks.isNotBlank()) {
                Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
            }
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
            onClick = {
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                val record = PurchaseRecord(
                    id = UUID.randomUUID().toString(),
                    date = getCurrentDateString(),
                    items = cartItems.toList(),
                    feedback = "",
                    userEmail = userEmail
                )
                cartViewModel.addPurchaseRecord(record)
                cartViewModel.clearCart()
                // Loyalty points logic
                val pointsEarned = total.toInt()
                if (userEmail.isNotEmpty()) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userEmail)
                        .set(mapOf("email" to userEmail, "loyaltyPoints" to FieldValue.increment(pointsEarned.toLong())), SetOptions.merge())
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "You earned $pointsEarned loyalty points!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                navController.navigate(Screen.PaymentSuccess.route)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Pay Now")
        }
    }
}

fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date())
} 