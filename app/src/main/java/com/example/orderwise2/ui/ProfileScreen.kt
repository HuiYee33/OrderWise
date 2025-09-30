package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    LaunchedEffect(user) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(user.email!!).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userProfile = UserProfile(
                            email = user.email!!,
                            name = document.getString("username") ?: "",
                            password = "********",
                            phone = document.getString("phone") ?: "",
                            loyaltyPoints = document.getLong("loyaltyPoints")?.toInt() ?: 0
                        )
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        } else {
            isLoading = false
        }
    }

    val webClientId = "1068919815592-dmloavrch01uahvhrl9iaaddv57hov5c.apps.googleusercontent.com"
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    Scaffold(
        bottomBar = { UserBottomNavigation(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Text("Profile", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (userProfile != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Email: ${userProfile!!.email}")
                        Text("Name: ${userProfile!!.name}")
                        Text("Phone: ${userProfile!!.phone}")
                        Text(
                            "Loyalty Points: ${userProfile!!.loyaltyPoints}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                editName = userProfile!!.name
                                editPhone = userProfile!!.phone
                                showEditDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Edit Profile")
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate(Screen.RedeemVoucher.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Redeem Vouchers") }
                OutlinedButton(
                    onClick = { navController.navigate(Screen.PurchaseHistory.route) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("View Purchase History") }
                Button(
                    onClick = {
                        googleSignInClient.signOut().addOnCompleteListener {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Log Out") }
            } else {
                Text("Unable to load profile. Please try again.")
            }
        }
    }

    if (showEditDialog && userProfile != null) {
        val isPhoneValid = editPhone.length in 10..12

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { input ->
                            val filtered = input.filter { it.isDigit() }
                            if (filtered.length <= 12) {
                                editPhone = filtered
                            }
                        },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (editPhone.isNotEmpty() && !isPhoneValid) {
                        Text(
                            text = "Phone number must be 10–12 digits.",
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(userProfile!!.email)
                            .update(mapOf(
                                "username" to editName,
                                "phone" to editPhone
                            ))
                            .addOnSuccessListener {
                                userProfile = userProfile!!.copy(name = editName, phone = editPhone)
                                showEditDialog = false
                            }
                    },
                    enabled = editName.isNotBlank() && isPhoneValid
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }
}
