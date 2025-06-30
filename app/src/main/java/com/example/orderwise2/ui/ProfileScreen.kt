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

@Composable
fun ProfileScreen(navController: NavController) {
    val user = remember {
        UserProfile(
            email = "jane.doe@email.com",
            name = "Jane Doe",
            password = "********",
            phone = "+60123456789",
            loyaltyPoints = 120,
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("Profile", fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Card(
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Email: ${user.email}")
                Text("Name: ${user.name}")
                Text("Password: ${user.password}")
                Text("Phone: ${user.phone}")
                Text("Loyalty Points: ${user.loyaltyPoints}", fontWeight = FontWeight.SemiBold)
            }
        }
        Button(
            onClick = { /* TODO: Redeem voucher logic */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Redeem Vouchers")
        }
        OutlinedButton(
            onClick = { navController.navigate(Screen.PurchaseHistory.route) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("View Purchase History")
        }
    }
} 