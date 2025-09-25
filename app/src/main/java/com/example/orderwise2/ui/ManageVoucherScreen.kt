package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ManageVoucherScreen(navController: NavController) {
    val repo = remember { VoucherRepository() }
    var vouchers by remember { mutableStateOf<List<Voucher>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    // listen in real-time
    DisposableEffect(Unit) {
        val registration = repo.listenAllVouchers { list ->
            vouchers = list.sortedBy { it.title }
            isLoading = false
        }
        onDispose { registration.remove() }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("${Screen.AdminVoucher.route}?voucherId=") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Voucher")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Manage Vouchers", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else if (vouchers.isEmpty()) {
                Text("No vouchers yet. Tap + to add.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vouchers, key = { it.id }) { v ->
                        VoucherRow(
                            voucher = v,
                            onEdit = { navController.navigate("${Screen.AdminVoucher.route}?voucherId=${v.id}") },
                            onDelete = { confirmDeleteId = v.id }
                        )
                    }
                }
            }
        }
    }

    if (confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            confirmButton = {
                TextButton(onClick = {
                    val id = confirmDeleteId ?: return@TextButton
                    repo.deleteVoucher(id) { _ -> }
                    confirmDeleteId = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDeleteId = null }) { Text("Cancel") } },
            title = { Text("Delete voucher?") },
            text = { Text("This action cannot be undone.") }
        )
    }
}

@Composable
private fun VoucherRow(
    voucher: Voucher,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(voucher.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Code: ${voucher.code}")
                Text("Points: ${voucher.pointsRequired}")
                val discount = if (voucher.discountType == "percent") "${voucher.discountValue}%" else "RM ${voucher.discountValue}"
                Text("Discount: $discount")
                Text(if (voucher.isActive) "Active" else "Inactive", color = if (voucher.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
        }
    }
}
