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

data class Review(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val dishName: String,
    val adminReply: String? = null
)

data class ReviewFeedback(
    val id: String = "",
    val date: String = "",
    val items: List<CartItem> = emptyList(),
    val feedback: String = "",
    val adminReply: String? = null
)

@Composable
fun AdminReviewScreen(navController: NavController) {
    var feedbackList by remember { mutableStateOf(listOf<ReviewFeedback>()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFeedback by remember { mutableStateOf<ReviewFeedback?>(null) }
    var showReplyDialog by remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()

    // Fetch feedback from Firestore
    LaunchedEffect(Unit) {
        db.collection("purchaseHistory")
            .get()
            .addOnSuccessListener { result ->
                val records = mutableListOf<ReviewFeedback>()
                for (doc in result) {
                    try {
                        val record = doc.toObject(ReviewFeedback::class.java).copy(id = doc.id)
                        if (record.feedback.isNotBlank()) {
                            records.add(record)
                        }
                    } catch (e: Exception) {
                        Log.e("AdminReview", "Failed to parse record: ${doc.id}", e)
                    }
                }
                feedbackList = records
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("AdminReview", "Failed to load feedback", e)
                isLoading = false
            }
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
                    "Customer Feedback",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${feedbackList.size} feedbacks",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (feedbackList.isEmpty()) {
                    Text("No customer feedback yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(feedbackList) { feedback ->
                            FeedbackCard(
                                feedback = feedback,
                                onReply = {
                                    selectedFeedback = feedback
                                    showReplyDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Reply Dialog
    if (showReplyDialog && selectedFeedback != null) {
        ReplyDialog(
            feedback = selectedFeedback!!,
            onDismiss = {
                showReplyDialog = false
                selectedFeedback = null
            },
            onReply = { reply ->
                // Save reply to Firestore
                db.collection("purchaseHistory").document(selectedFeedback!!.id)
                    .update("adminReply", reply)
                feedbackList = feedbackList.map {
                    if (it.id == selectedFeedback!!.id) it.copy(adminReply = reply) else it
                }
                showReplyDialog = false
                selectedFeedback = null
            }
        )
    }
}

@Composable
fun FeedbackCard(
    feedback: ReviewFeedback,
    onReply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    feedback.date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            // Order Details
            feedback.items.forEach { item ->
                Text("${item.name} x${item.quantity} - RM %.2f".format(item.unitPrice * item.quantity), fontSize = 14.sp)
                if (item.remarks.isNotBlank()) {
                    Text("Remarks: ${item.remarks}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Customer Feedback
            Text("Customer Feedback:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(feedback.feedback, fontSize = 14.sp)
            // Admin Reply
            if (!feedback.adminReply.isNullOrBlank()) {
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
                            feedback.adminReply ?: "",
                            fontSize = 14.sp,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Reply Button
            if (feedback.adminReply.isNullOrBlank()) {
                Button(
                    onClick = onReply,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reply")
                }
            }
        }
    }
}

@Composable
fun ReplyDialog(
    feedback: ReviewFeedback,
    onDismiss: () -> Unit,
    onReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reply to Feedback") },
        text = {
            Column {
                Text("Customer Feedback:")
                Text(feedback.feedback, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Your Reply") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { if (replyText.isNotBlank()) onReply(replyText) }) {
                Text("Send Reply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 