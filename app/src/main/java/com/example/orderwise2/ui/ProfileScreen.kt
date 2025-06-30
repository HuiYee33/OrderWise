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
    val context = LocalContext.current
    val webClientId = "1068919815592-dmloavrch01uahvhrl9iaaddv57hov5c.apps.googleusercontent.com"
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
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
        ) {
            Text("Log Out")
        }
    }
} 