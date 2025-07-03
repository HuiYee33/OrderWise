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

data class Review(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String,
    val dishName: String,
    val adminReply: String? = null
)

@Composable
fun AdminReviewScreen(navController: NavController) {
    var reviews by remember {
        mutableStateOf(
            listOf(
                Review("1", "John Doe", 5, "Excellent food! The shrimp fried rice was amazing.", "2024-01-15", "Shrimp Fried Rice"),
                Review("2", "Jane Smith", 4, "Good food but a bit too spicy for my taste.", "2024-01-14", "Chicken Curry"),
                Review("3", "Mike Johnson", 3, "Food was okay, but service was slow.", "2024-01-13", "Beef Noodles"),
                Review("4", "Sarah Wilson", 5, "Best curry I've ever had! Will definitely come back.", "2024-01-12", "Chicken Curry", "Thank you for your kind words! We're glad you enjoyed it."),
                Review("5", "David Brown", 2, "The food was cold when it arrived.", "2024-01-11", "Vegetable Stir Fry")
            )
        )
    }

    var selectedReview by remember { mutableStateOf<Review?>(null) }
    var showReplyDialog by remember { mutableStateOf(false) }

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
                    "Customer Reviews",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${reviews.size} reviews",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reviews List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        onReply = {
                            selectedReview = review
                            showReplyDialog = true
                        }
                    )
                }
            }
        }
    }

    // Reply Dialog
    if (showReplyDialog && selectedReview != null) {
        ReplyDialog(
            review = selectedReview!!,
            onDismiss = {
                showReplyDialog = false
                selectedReview = null
            },
            onReply = { reply ->
                reviews = reviews.map { 
                    if (it.id == selectedReview!!.id) it.copy(adminReply = reply) else it 
                }
                showReplyDialog = false
                selectedReview = null
            }
        )
    }
}

@Composable
fun ReviewCard(
    review: Review,
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
                Column {
                    Text(
                        review.customerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        review.dishName,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    RatingStars(review.rating)
                    Text(
                        review.date,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Comment
            Text(
                review.comment,
                fontSize = 14.sp
            )

            // Admin Reply
            if (review.adminReply != null) {
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
                            review.adminReply,
                            fontSize = 14.sp,
                            color = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reply Button
            if (review.adminReply == null) {
                Button(
                    onClick = onReply,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reply")
                }
            } else {
                Text(
                    "Replied",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun RatingStars(rating: Int) {
    Row {
        repeat(5) { index ->
            Text(
                if (index < rating) "★" else "☆",
                color = if (index < rating) Color(0xFFFFD700) else Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun ReplyDialog(
    review: Review,
    onDismiss: () -> Unit,
    onReply: (String) -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reply to Review") },
        text = {
            Column {
                Text(
                    "Customer: ${review.customerName}",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Dish: ${review.dishName}",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Review: ${review.comment}",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Your Reply") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (replyText.isNotBlank()) {
                        onReply(replyText)
                    }
                }
            ) {
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