package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

	// Load vouchers once
	LaunchedEffect(Unit) {
		repo.loadActiveVouchers { vouchers = it }
	}

	Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
		Column(modifier = Modifier.fillMaxSize()) {

			// Header
			Text("Redeem Voucher", style = MaterialTheme.typography.titleLarge)
			Spacer(Modifier.height(12.dp))

			// LazyColumn for vouchers (scrollable)
			LazyColumn(
				modifier = Modifier.weight(1f), // takes all remaining space
				verticalArrangement = Arrangement.spacedBy(12.dp)
			) {
				items(vouchers) { v ->
					Card(Modifier.fillMaxWidth()) {
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
							Button(
								onClick = {
									repo.redeemVoucher(v) { ok ->
										status = if (ok) "Redeemed ${v.title}" else "Not enough points"
									}
								},
								modifier = Modifier.fillMaxWidth()
							) {
								Text("Redeem")
							}
						}
					}
				}

				// Bottom spacer so last item is not blocked by status
				item {
					Spacer(modifier = Modifier.height(16.dp))
				}
			}

			// Status text at bottom
			if (status.isNotBlank()) {
				Spacer(Modifier.height(8.dp))
				Text(status)
			}
		}
	}
}
