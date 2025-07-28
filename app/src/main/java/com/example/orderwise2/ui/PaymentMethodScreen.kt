package com.example.orderwise2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// PaymentMethodScreen: Lets users select and manage payment methods
@Composable
fun PaymentMethodScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val paymentMethods = listOf("Touch N Go", "Online Banking", "Credit / Debit Card")
    var selectedPaymentMethod by remember { mutableStateOf(cartViewModel.selectedPaymentMethod.ifEmpty { paymentMethods[0] }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)) // Light beige background
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
                text = "Select Payment Method",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                color = Color.Black
            )
        }

        // Payment Methods
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            paymentMethods.forEach { method ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == method,
                        onClick = { 
                            selectedPaymentMethod = method
                        },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFFFFD700), // Gold color
                            unselectedColor = Color(0xFFFFD700) // Gold color
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = method,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }

        // Confirm Button
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                cartViewModel.setSelectedPaymentMethod(selectedPaymentMethod)
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700) // Gold color
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Confirm Payment Method",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
} 