package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AdminVoucherScreen(navController: NavController) {
	val repo = remember { VoucherRepository() }
	var title by remember { mutableStateOf("") }
	var code by remember { mutableStateOf("") }
	var points by remember { mutableStateOf("") }
	var discountValue by remember { mutableStateOf("") }
	var discountType by remember { mutableStateOf("amount") }
	var isActive by remember { mutableStateOf(true) }
	var statusText by remember { mutableStateOf("") }
	var isButtonEnabled by remember { mutableStateOf(true) }

	Column(Modifier.fillMaxSize().padding(16.dp)) {
		Text("Create Voucher", style = MaterialTheme.typography.titleLarge)
		Spacer(Modifier.height(12.dp))
		OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
		Spacer(Modifier.height(8.dp))
		OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth())
		Spacer(Modifier.height(8.dp))
		OutlinedTextField(value = points, onValueChange = { points = it.filter { ch -> ch.isDigit() } }, label = { Text("Points Required") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
		Spacer(Modifier.height(8.dp))
		Row {
			FilterChip(selected = discountType == "amount", onClick = { discountType = "amount" }, label = { Text("RM off") })
			Spacer(Modifier.width(8.dp))
			FilterChip(selected = discountType == "percent", onClick = { discountType = "percent" }, label = { Text("% off") })
		}
		Spacer(Modifier.height(8.dp))
		OutlinedTextField(value = discountValue, onValueChange = { discountValue = it.filter { ch -> ch.isDigit() || ch == '.' } }, label = { Text("Discount Value") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
		Spacer(Modifier.height(8.dp))
		Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
			Checkbox(checked = isActive, onCheckedChange = { isActive = it })
			Text("Active")
		}
		Spacer(Modifier.height(12.dp))
		Button(
			onClick = {
				val voucher = Voucher(
					title = title.trim(),
					code = code.trim(),
					pointsRequired = points.toIntOrNull() ?: 0,
					discountType = discountType,
					discountValue = discountValue.toDoubleOrNull() ?: 0.0,
					isActive = isActive
				)
				repo.createVoucher(voucher) { ok ->
					if (ok) {
						statusText = "Voucher created"
						isButtonEnabled = false   // 🔒 disable button
					} else {
						statusText = "Failed to create"
					}
				}
			},
			enabled = isButtonEnabled   // ✅ control if button clickable
		) {
			Text("Save Voucher")
		}

		Spacer(Modifier.height(8.dp))
		if (statusText.isNotBlank()) Text(statusText)
	}
}


