package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApplyVoucherDialog(
	open: Boolean, //if open = true (show dialog), open = false (close dialog)
	onDismiss: () -> Unit, //no input and return value, it will execute when user want to cancel dialog
	onApplied: (UserVoucher) -> Unit //take 1 input (user voucher) then appliedVoucher = selected voucher
) {
	if (!open) return //if open = false, function exist immediately
	val repo = remember { VoucherRepository() } //repo (know how to talk to database), Create VoucherRepository() only once and reuse it
	//list of vouchers, starting empty, and will update when data is fetched
	var vouchers by remember { mutableStateOf<List<UserVoucher>>(emptyList()) } //UI automatically updates when it changes because it is state

	//run this code when open the screen
	LaunchedEffect(Unit) { repo.loadUserRedeemedVouchers { vouchers = it } } //get vouchers from the database, and once they’re ready, update vouchers list.”

	AlertDialog( //pops up a window on top of the screen
		onDismissRequest = onDismiss, //If user wants to close the dialog, run onDismiss (function passed into ApplyVoucherDialog)
		confirmButton = { //if don't have it, button will show outside the dialog, not in the dialog’s footer
			TextButton(onClick = onDismiss) { Text("Close") } //Clicking button will close the dialog by calling onDismiss
		},
		title = { Text("Apply Voucher") },
		text = {
			Column(Modifier.fillMaxWidth()) {
				if (vouchers.isEmpty()) {
					Text("No redeemed vouchers available.")
				} else {
					vouchers.forEach { uv ->
						Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
							Column(Modifier.padding(8.dp)) { //arrange items vertically (title, code, discount, button)
								Text(uv.title, style = MaterialTheme.typography.titleMedium) //show voucher title with medium-sized text style
								Text("Code: ${uv.code}") //show voucher code
								val discountLabel = if (uv.discountType == "amount") {
									"RM ${String.format("%.2f", uv.discountValue)} off" //RM5.00
								} else {
									"${uv.discountValue.toInt()}% off" //5%
								}
								Text(discountLabel) //Show the discount label we created above
								Spacer(Modifier.height(6.dp))
								//When user clicks "Apply", that voucher gets passed back to the parent screen, and the dialog closes.
								Button(onClick = { onApplied(uv); onDismiss() }) { Text("Apply") }
							}
						}
					}
				}
			}
		}
	)
}


