package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

data class OperatingHours(
    val day: String,
    val hours: String,
    val isOpen: Boolean
)

data class SalesReport(
    val period: String,
    val totalSales: Double,
    val totalOrders: Int,
    val averageOrderValue: Double
)

@Composable
fun AdminCafeProfileScreen(navController: NavController) {
    var operatingHours by remember {
        mutableStateOf(
            listOf(
                OperatingHours("Monday", "8:00 AM - 10:00 PM", true),
                OperatingHours("Tuesday", "8:00 AM - 10:00 PM", true),
                OperatingHours("Wednesday", "8:00 AM - 10:00 PM", true),
                OperatingHours("Thursday", "8:00 AM - 10:00 PM", true),
                OperatingHours("Friday", "8:00 AM - 11:00 PM", true),
                OperatingHours("Saturday", "9:00 AM - 11:00 PM", true),
                OperatingHours("Sunday", "9:00 AM - 9:00 PM", true)
            )
        )
    }

    val salesReports = remember {
        listOf(
            SalesReport("Today", 1250.50, 45, 27.79),
            SalesReport("This Week", 8750.25, 320, 27.34),
            SalesReport("This Month", 32500.75, 1180, 27.54),
            SalesReport("Last Month", 29800.50, 1050, 28.38)
        )
    }

    var showEditDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val webClientId = "1068919815592-dmloavrch01uahvhrl9iaaddv57hov5c.apps.googleusercontent.com"
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    Scaffold(
        bottomBar = { AdminBottomNavigation(navController) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Cafe Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Edit Profile")
                    }
                }
            }

            item {
                // Cafe Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Cafe Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        InfoRow("Cafe Name", "OrderWise Cafe")
                        InfoRow("Address", "123 Main Street, Kuala Lumpur, Malaysia")
                        InfoRow("Phone", "+60 12-345 6789")
                        InfoRow("Email", "info@orderwisecafe.com")
                        InfoRow("Website", "www.orderwisecafe.com")
                    }
                }
            }

            item {
                // Operating Hours Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Operating Hours",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        operatingHours.forEach { hours ->
                            OperatingHoursRow(hours)
                            if (hours != operatingHours.last()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item {
                // Sales Reports Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Sales Reports",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        salesReports.forEach { report ->
                            SalesReportRow(report)
                            if (report != salesReports.last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            item {
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        EditProfileDialog(
            onDismiss = { showEditDialog = false },
            operatingHours = operatingHours,
            onSave = { updatedHours, cafeName, address, phone, email ->
                // Save to Firestore
                val profileData = hashMapOf(
                    "cafeName" to cafeName,
                    "address" to address,
                    "phone" to phone,
                    "email" to email,
                    "operatingHours" to updatedHours.map { mapOf(
                        "day" to it.day,
                        "hours" to it.hours,
                        "isOpen" to it.isOpen
                    ) }
                )
                db.collection("cafeProfile").document("main").set(profileData)
                    .addOnSuccessListener {
                        operatingHours = updatedHours
                        Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun OperatingHoursRow(hours: OperatingHours) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            hours.day,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                hours.hours,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (hours.isOpen) Color(0xFF4CAF50) else Color.Red)
            )
        }
    }
}

@Composable
fun SalesReportRow(report: SalesReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                report.period,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Total Sales",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        "RM ${String.format("%.2f", report.totalSales)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column {
                    Text(
                        "Orders",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        report.totalOrders.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        "Avg Order",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        "RM ${String.format("%.2f", report.averageOrderValue)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    onDismiss: () -> Unit,
    operatingHours: List<OperatingHours>,
    onSave: (List<OperatingHours>, String, String, String, String) -> Unit
) {
    var cafeName by remember { mutableStateOf("OrderWise Cafe") }
    var address by remember { mutableStateOf("123 Main Street, Kuala Lumpur, Malaysia") }
    var phone by remember { mutableStateOf("+60 12-345 6789") }
    var email by remember { mutableStateOf("info@orderwisecafe.com") }
    var hoursState by remember { mutableStateOf(operatingHours.map { it.copy() }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cafe Profile") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = cafeName,
                    onValueChange = { cafeName = it },
                    label = { Text("Cafe Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Operating Hours", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                hoursState.forEachIndexed { idx, hours ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(hours.day, modifier = Modifier.width(90.dp))
                        OutlinedTextField(
                            value = hours.hours,
                            onValueChange = { newHours ->
                                hoursState = hoursState.toMutableList().also { it[idx] = it[idx].copy(hours = newHours) }
                            },
                            label = { Text("Hours") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Checkbox(
                            checked = hours.isOpen,
                            onCheckedChange = { checked ->
                                hoursState = hoursState.toMutableList().also { it[idx] = it[idx].copy(isOpen = checked) }
                            }
                        )
                        Text(if (hours.isOpen) "Open" else "Closed", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(hoursState, cafeName, address, phone, email) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 