package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
@Composable
fun ApplyVoucherDialog(
	open: Boolean,
	onDismiss: () -> Unit,
	onApplied: (UserVoucher) -> Unit
) {
	if (!open) return

	val repo = remember { VoucherRepository() }
	var vouchers by remember { mutableStateOf<List<UserVoucher>?>(null) }
	val scrollState = rememberScrollState()

	LaunchedEffect(Unit) {
		repo.loadUserRedeemedVouchers { vouchers = it }
	}

	AlertDialog(
		onDismissRequest = onDismiss,
		confirmButton = {
			TextButton(onClick = onDismiss) { Text("Close") }
		},
		title = { Text("Apply Voucher") },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 400.dp)
					.verticalScroll(scrollState)
			) {
				when {
					vouchers == null -> Text("Loading vouchers...")
					vouchers.isNullOrEmpty() -> Text("No redeemed vouchers available.")
					else -> {
						vouchers?.forEach { uv ->
							Card(
								modifier = Modifier
									.fillMaxWidth()
									.padding(vertical = 4.dp)
							) {
								Column(Modifier.padding(8.dp)) {
									Text(uv.title, style = MaterialTheme.typography.titleMedium)
									Text("Code: ${uv.code}")
									val discountLabel = if (uv.discountType == "amount") {
										"RM ${String.format("%.2f", uv.discountValue)} off"
									} else {
										"${uv.discountValue.toInt()}% off"
									}
									Text(discountLabel)
									Spacer(Modifier.height(6.dp))
									Button(
										onClick = {
											onApplied(uv) // pass selected voucher to parent
											onDismiss()   // then safely dismiss the dialog
										},
										modifier = Modifier.fillMaxWidth()
									) {
										Text("Apply")
									}
								}
							}
						}
					}
				}
			}
		}
	)
}


