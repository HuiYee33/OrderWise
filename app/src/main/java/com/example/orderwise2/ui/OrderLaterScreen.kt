package com.example.orderwise2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// OrderLaterScreen: Allows users to schedule orders for later pickup
@Composable
fun OrderLaterScreen(
    navController: NavController,
    cartItems: List<CartItem>,
    cartViewModel: CartViewModel
) {
    val context = LocalContext.current
    var voucherCode by remember { mutableStateOf("") }
    val subtotal = cartItems.sumOf { it.unitPrice * it.quantity }
    val tax = subtotal * 0.06 // 6% tax
    val total = subtotal + tax

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8B259)) // Light beige background
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Check Out",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
        }

        // Order Summary Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Order Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Unit price",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Text(
                    text = "Quantity",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Order items
            cartItems.forEach { item ->
                OrderItemRow(item = item)
                if (item != cartItems.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }

            // Subtotal, Tax, and Total
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Subtotal",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "RM ${String.format("%.2f", subtotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Tax
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tax (6%)",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "RM ${String.format("%.2f", tax)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RM ${String.format("%.2f", total)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Order For Later Button
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                navController.navigate(Screen.DateTimeSelection.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4E4BC) // Light gold/beige
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Order For Later",
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        // Display the selected pickup date and time if chosen
        if (cartViewModel.selectedDate != null && cartViewModel.selectedTimeSlot != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected: ",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${cartViewModel.selectedDate?.displayText} at ${cartViewModel.selectedTimeSlot?.displayText}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }

        // Payment Details Section
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { 
                        navController.navigate(Screen.PaymentMethod.route)
                    }
                ) {
                    Text(
                        text = "See all",
                        color = Color.Blue,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Display selected payment method
            if (cartViewModel.selectedPaymentMethod.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected: ",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = cartViewModel.selectedPaymentMethod,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }

        // Offers Section
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Offers",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Button(
                onClick = { /* Handle voucher selection */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8F8F8) // Light off-white
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Select Voucher",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Place Order Button: creates a PurchaseRecord including pickup info if present
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                // Get user email
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                // Get selected pickup date and time slot (if any)
                val pickupDate = cartViewModel.selectedDate?.displayText
                val pickupTimeSlot = cartViewModel.selectedTimeSlot?.displayText
                // Create purchase record with pickup info
                val record = PurchaseRecord(
                    id = UUID.randomUUID().toString(),
                    date = getCurrentDateString(),
                    items = cartItems.toList(),
                    feedback = "",
                    userEmail = userEmail,
                    pickupDate = pickupDate,
                    pickupTimeSlot = pickupTimeSlot
                )
                // Add record to history and clear cart
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
                // Navigate to payment success screen
                navController.navigate(Screen.PaymentSuccess.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD3D3D3) // Light gray/purple
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Place Order",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OrderItemRow(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Food image (placeholder circle)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.take(1).uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Item details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (item.remarks.isNotBlank()) {
                Text(
                    text = "Remark:",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
                item.remarks.split(",").forEach { remark ->
                    Text(
                        text = "• $remark",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
        }
        
        // Price and quantity
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "RM${String.format("%.2f", item.unitPrice)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.quantity}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DividerWithDiamonds() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.5f))
        )
    }
}

fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date())
} 