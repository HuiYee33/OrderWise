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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.util.Log

// HomeScreen: Main landing page for users after login
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var dishStats by remember { mutableStateOf(listOf<Pair<String, Int>>()) }
    var isLoading by remember { mutableStateOf(true) }
    val db = FirebaseFirestore.getInstance()
    val userEmail = FirebaseAuth.getInstance().currentUser?.email
    // Removed phone dialog state and logic

    // Fetch purchase history and compute popular dishes
    LaunchedEffect(Unit) {
        db.collection("purchaseHistory")
            .get()
            .addOnSuccessListener { result ->
                val dishCountMap = mutableMapOf<String, Int>()
                for (doc in result) {
                    try {
                        val record = doc.toObject(PurchaseRecord::class.java)
                        record.items.forEach { item ->
                            dishCountMap[item.name] = dishCountMap.getOrDefault(item.name, 0) + item.quantity
                        }
                    } catch (e: Exception) {
                        Log.e("HomeScreen", "Failed to parse record: ${doc.id}", e)
                    }
                }
                dishStats = dishCountMap.entries.sortedByDescending { it.value }.map { it.key to it.value }
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("HomeScreen", "Failed to load purchase history", e)
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Popular Dishes", fontWeight = FontWeight.Bold, fontSize = 22.sp) })
        },
        bottomBar = { UserBottomNavigation(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                if (dishStats.isEmpty()) {
                    Text("No popular dishes yet.", color = Color.Gray, modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(dishStats) { (name, qty) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(4.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                                    Text("Ordered: $qty", fontSize = 16.sp, color = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate(Screen.MenuHome.route) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("View Full Menu")
            }
        }
    }
    // Removed phone number dialog
} 