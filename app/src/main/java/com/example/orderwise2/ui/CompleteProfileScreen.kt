package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

// CompleteProfileScreen: Screen for users to complete their profile information
@Composable
fun CompleteProfileScreen(navController: NavController) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email ?: ""
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Complete Your Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("Email") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { newValue ->
                // Allow only digits and max length 12
                val filtered = newValue.filter { it.isDigit() }
                if (filtered.length <= 12) {
                    phone = filtered
                }
            },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Optional: show error if less than 10 digits
        if (phone.isNotEmpty() && phone.length < 10) {
            Text(
                text = "Phone number must be between 10–12 digits.",
                color = Color.Red,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.height(16.dp))
        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }
        Button(
            onClick = {
                if (username.isBlank() || phone.isBlank()) {
                    errorMessage = "Please fill in all fields."
                    return@Button
                }
                isLoading = true
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(email)
                    .set(mapOf("email" to email, "username" to username, "phone" to phone))
                    .addOnSuccessListener {
                        isLoading = false
                        navController.navigate(Screen.MenuHome.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                    .addOnFailureListener {
                        isLoading = false
                        errorMessage = "Failed to save profile. Try again."
                    }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
            else Text("Save")
        }
    }
} 