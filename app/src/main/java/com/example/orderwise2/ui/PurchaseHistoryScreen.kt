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

// PurchaseHistoryScreen: Shows user's past purchases and allows feedback
@Composable
fun PurchaseHistoryScreen(navController: NavController, cartViewModel: CartViewModel) {
    // Load purchase history when screen is shown
    LaunchedEffect(Unit) {
        cartViewModel.loadPurchaseHistory()
    }
    val history = cartViewModel.purchaseHistory
    val context = LocalContext.current
    // List of purchase records
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
            // Card for each purchase record
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Show order date
                    Text("Date: ${record.date}", fontWeight = FontWeight.Bold)
                    // Show pickup info if present
                    if (record.pickupDate != null && record.pickupTimeSlot != null) {
                        Text("Pick up on ${record.pickupDate} at ${record.pickupTimeSlot}", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    record.items.forEach { item ->
                        Text("${item.name} x${item.quantity} - RM %.2f".format(item.unitPrice * item.quantity))
                        if (item.remarks.isNotBlank()) {
                            Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                    }

                    // Admin Reply (if any)
                    if (!record.adminReply.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    "Admin Reply:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1976D2)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    record.adminReply ?: "",
                                    fontSize = 14.sp,
                                    color = Color(0xFF1976D2)
                                )
                            }
                        }
                    }

                    // Check if the feedback has already been submitted (not empty)
                    val isFeedbackSubmitted = record.feedback.isNotBlank()

                    // Input field for user to type feedback
                    OutlinedTextField(
                        value = feedback, // Current value of the text field
                        onValueChange = { feedback = it }, // Update the feedback variable when user types
                        label = { Text("Feedback") }, // Label shown above the text field
                        modifier = Modifier.fillMaxWidth(), // Make the field stretch full width
                        enabled =!isFeedbackSubmitted  //Disable input after submission
                    )
                    if (isFeedbackSubmitted) { // If feedback was already submitted
                        Text(
                            "Thank you for your feedback",
                            color = MaterialTheme.colorScheme.primary, // Use primary color from theme
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp) // Add spacing at the top
                        )
                    } else { // If feedback not yet submitted, show the Submit button
                        Button(
                            onClick = {
                                if (feedback.isNotBlank()) { // Only submit if feedback is not empty
                                    cartViewModel.updateFeedback(record.id, feedback) // Save feedback to Firestore
                                    Toast.makeText(context, "Thank you for your feedback", Toast.LENGTH_SHORT).show() // Show a small popup message
                                }
                            },
                            modifier = Modifier.align(Alignment.End), //at the right side of the container
                            enabled = feedback.isNotBlank() // Disable button if input is empty
                        ) {
                            Text("Submit Feedback")
                        }
                    }
                }
            }
        }
    }
} 