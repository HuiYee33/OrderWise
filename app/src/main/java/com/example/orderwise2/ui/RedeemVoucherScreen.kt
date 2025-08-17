package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun RedeemVoucherScreen(navController: NavController) {
	val repo = remember { VoucherRepository() }
	var vouchers by remember { mutableStateOf<List<Voucher>>(emptyList()) }
	var status by remember { mutableStateOf("") }

	LaunchedEffect(Unit) {
		repo.loadActiveVouchers { vouchers = it } ////get vouchers from the database, and once they’re ready, update vouchers list.”
	}

	Column(Modifier.fillMaxSize().padding(16.dp)) {
		Text("Redeem Voucher", style = MaterialTheme.typography.titleLarge)
		Spacer(Modifier.height(12.dp))
		vouchers.forEach { v ->
			Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
				Column(Modifier.padding(12.dp)) {
                    Text(v.title, style = MaterialTheme.typography.titleMedium)
                    Text("Code: ${v.code}", color = Color.Gray)
                    Text("Requires ${v.pointsRequired} points", color = Color.Gray)
                    val discountText = if (v.discountType == "amount") {
                        "Discount: RM ${String.format("%.2f", v.discountValue)}"
                    } else {
                        "Discount: ${v.discountValue.toInt()}%"
                    }
                    Text(discountText)
					Spacer(Modifier.height(8.dp))
					Button(onClick = { repo.redeemVoucher(v) { ok -> status = if (ok) "Redeemed ${'$'}{v.title}" else "Not enough points" } }) {
						Text("Redeem")
					}
				}
			}
		}
		Spacer(Modifier.height(8.dp))
		if (status.isNotBlank()) Text(status)
	}
}


