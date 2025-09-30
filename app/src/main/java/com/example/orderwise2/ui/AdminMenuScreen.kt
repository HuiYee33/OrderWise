package com.example.orderwise2.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.example.orderwise2.ui.IngredientOption
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.AsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.FileInputStream
import android.util.Log
import java.util.UUID
import android.content.Intent
import com.example.orderwise2.ui.CloudinaryManager
import androidx.compose.material3.ExperimentalMaterial3Api

data class MenuItem(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val stockStatus: StockStatus = StockStatus.AVAILABLE,
    val imageUri: String = "",
    val ingredients: String = "",
    val additionalOptions: List<IngredientOption> = emptyList()
)

enum class StockStatus {
    AVAILABLE, NOT_AVAILABLE
}

@Composable
fun rememberMenuItems(): List<MenuItem> {
    val menuItems = remember { mutableStateListOf<MenuItem>() }
    val db = FirebaseFirestore.getInstance()

    DisposableEffect(Unit) {
        val registration: ListenerRegistration = db.collection("menu")
            .addSnapshotListener { snapshot, e ->
                if (e == null && snapshot != null) {
                    menuItems.clear()
                    for (doc in snapshot.documents) {
                        val item = doc.toObject(MenuItem::class.java)
                        if (item != null) menuItems.add(item)
                    }
                }
            }
        onDispose { registration.remove() }
    }

    return menuItems
}

// AdminMenuScreen: Main screen for admin to manage menu items
@Composable
fun AdminMenuScreen(navController: NavController) {
    val menuItems = rememberMenuItems()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        bottomBar = { AdminBottomNavigation(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Menu Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Add Item")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Menu Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuItems) { item ->
                    MenuItemCard(
                        item = item,
                        onEdit = { selectedItem = item },
                        onDelete = {
                            db.collection("menu").document(item.id).delete()
                        }
                    )
                }
            }
        }
    }

    // Add/Edit Dialog
    if (showAddDialog || selectedItem != null) {
        MenuItemDialog(
            item = selectedItem,
            onDismiss = {
                showAddDialog = false
                selectedItem = null
            },
            onSave = { newItem ->
                db.collection("menu").document(newItem.id).set(newItem)
                showAddDialog = false
                selectedItem = null
            }
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Section 1: Food Image and Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Left side: Food Image
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imageUri.isNotEmpty()) {
                        AsyncImage(
                            model = item.imageUri,
                            contentDescription = "Food Image",
                            modifier = Modifier.size(100.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = "Food Image",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )
                    }
                    // Not Available label at the bottom
                    if (item.stockStatus == StockStatus.NOT_AVAILABLE) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0xFFFF9800), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                "Not Available",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Right side: Food Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        item.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.stockStatus == StockStatus.NOT_AVAILABLE) Color.Red else Color.Unspecified
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "RM ${item.price}",
                        fontSize = 16.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Category: ${item.category}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ingredients: ${item.ingredients}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Section 2: Action Buttons (Edit, Delete only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Edit Button
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Delete Button
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemDialog(
    item: MenuItem?,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var price by remember { mutableStateOf(item?.price?.toString() ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Main course") }
    var ingredients by remember { mutableStateOf(item?.ingredients ?: "") }
    var stockStatus by remember { mutableStateOf(item?.stockStatus ?: StockStatus.AVAILABLE) }
    var imageUri by remember { mutableStateOf(item?.imageUri ?: "") }
    var localImageUri by remember { mutableStateOf<Uri?>(null) }
    var internalImageFile by remember { mutableStateOf<File?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val baseIngredients = ingredients.split(",").map { it.trim() }.filter { it.isNotBlank() }
    var additionalOptionsState by remember {
        mutableStateOf(
            baseIngredients.associateWith { baseIng ->
                val existing = item?.additionalOptions?.find { it.name == baseIng }
                Pair(existing != null, existing?.price?.toString() ?: "")
            }
        )
    }

    // Category dropdown state and options
    val categoryOptions = listOf("Main course", "Soup", "Beverage", "Dessert")
    var isCategoryMenuExpanded by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("ImagePicker", "Received URI: $uri")
        if (uri != null) {
            if (uri.scheme == "content") {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    Log.d("ImagePicker", "Persistable URI permission taken for $uri")
                } catch (e: Exception) {
                    Log.w("ImagePicker", "Failed to take persistable URI permission for $uri", e)
                }
            }
            localImageUri = uri
            // Copy to internal storage
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.cacheDir, "picked_image_${UUID.randomUUID()}.jpg")
                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                    internalImageFile = file
                    Log.d("ImagePicker", "Copied image to internal storage: ${file.absolutePath}")
                } else {
                    internalImageFile = null
                    Log.w("ImagePicker", "Failed to open input stream for URI: $uri")
                }
            } catch (e: Exception) {
                internalImageFile = null
                Log.e("ImagePicker", "Failed to copy image to internal storage", e)
            }
        } else {
            Log.w("ImagePicker", "Image selection cancelled or no URI received.")
            Toast.makeText(context, "Image selection cancelled.", Toast.LENGTH_SHORT).show()
            internalImageFile = null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Menu Item" else "Edit Menu Item") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Image Upload Section
                Text("Food Image:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Image")
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show selected image preview (local or remote)
                if (localImageUri != null) {
                    AsyncImage(model = localImageUri, contentDescription = "Selected Image", modifier = Modifier.height(100.dp))
                } else if (imageUri.isNotEmpty()) {
                    AsyncImage(model = imageUri, contentDescription = "Menu Image", modifier = Modifier.height(100.dp))
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Category dropdown (exposed)
                Text("Category", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = isCategoryMenuExpanded,
                    onExpandedChange = { isCategoryMenuExpanded = !isCategoryMenuExpanded }
                ) {
                    TextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryMenuExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = isCategoryMenuExpanded,
                        onDismissRequest = { isCategoryMenuExpanded = false }
                    ) {
                        categoryOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    category = option
                                    isCategoryMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ingredients,
                    onValueChange = {
                        ingredients = it
                        // Update additional options state when ingredients change
                        val newBase = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotBlank() }
                        additionalOptionsState = newBase.associateWith { baseIng ->
                            val existing = item?.additionalOptions?.find { it.name == baseIng }
                            Pair(existing != null, existing?.price?.toString() ?: "")
                        }
                    },
                    label = { Text("Ingredients") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Allow as Additional Option:", fontWeight = FontWeight.Bold)
                Column {
                    baseIngredients.forEach { ing ->
                        val (checked, priceStr) = additionalOptionsState[ing] ?: Pair(false, "")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    additionalOptionsState = additionalOptionsState.toMutableMap().apply {
                                        this[ing] = Pair(isChecked, priceStr)
                                    }
                                }
                            )
                            Text(ing, modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = priceStr,
                                onValueChange = { newPrice ->
                                    additionalOptionsState = additionalOptionsState.toMutableMap().apply {
                                        this[ing] = Pair(checked, newPrice)
                                    }
                                },
                                label = { Text("Price") },
                                modifier = Modifier.width(100.dp),
                                singleLine = true,
                                enabled = checked
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Stock Status:", fontWeight = FontWeight.Bold)
                Row {
                    StockStatus.values().forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            RadioButton(
                                selected = stockStatus == status,
                                onClick = { stockStatus = status }
                            )
                            Text(status.name.replace("_", " "))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        var finalImageUrl = imageUri
                        if (internalImageFile != null) {
                            Log.d("ImageUpload", "Internal file exists: ${internalImageFile!!.exists()}, path: ${internalImageFile!!.absolutePath}")
                            Log.d("ImageUpload", "Internal file size: ${internalImageFile!!.length()}")
                            isUploading = true
                            try {
                                val inputStream: InputStream = FileInputStream(internalImageFile!!)
                                CloudinaryManager.uploadImage(
                                    inputStream = inputStream,
                                    onSuccess = { url ->
                                        finalImageUrl = url
                                        Log.d("ImageUpload", "Download URL: $finalImageUrl")
                                        isUploading = false
                                        // Continue with saving
                                        val additionalOptions = additionalOptionsState.filter { it.value.first }
                                            .map { IngredientOption(it.key, it.value.second.toDoubleOrNull() ?: 0.0) }
                                        val newItem = MenuItem(
                                            id = item?.id ?: UUID.randomUUID().toString(),
                                            name = name,
                                            price = price.toDoubleOrNull() ?: 0.0,
                                            description = description,
                                            category = category,
                                            stockStatus = stockStatus,
                                            imageUri = finalImageUrl,
                                            ingredients = ingredients,
                                            additionalOptions = additionalOptions
                                        )
                                        onSave(newItem)
                                    },
                                    onError = { error ->
                                        Log.e("ImageUpload", "Upload failed: $error")
                                        Toast.makeText(context, "Image upload failed: $error", Toast.LENGTH_LONG).show()
                                        isUploading = false
                                    }
                                )
                                inputStream.close()
                                internalImageFile!!.delete()
                                internalImageFile = null
                            } catch (e: Exception) {
                                isUploading = false
                                Log.e("ImageUpload", "Upload failed: ${e.message}", e)
                                Toast.makeText(context, "Image upload failed. See logs for details.", Toast.LENGTH_LONG).show()
                                return@launch
                            }
                        } else {
                            val additionalOptions = additionalOptionsState.filter { it.value.first }
                                .map { IngredientOption(it.key, it.value.second.toDoubleOrNull() ?: 0.0) }
                            val newItem = MenuItem(
                                id = item?.id ?: UUID.randomUUID().toString(),
                                name = name,
                                price = price.toDoubleOrNull() ?: 0.0,
                                description = description,
                                category = category,
                                stockStatus = stockStatus,
                                imageUri = finalImageUrl,
                                ingredients = ingredients,
                                additionalOptions = additionalOptions
                            )
                            onSave(newItem)
                        }
                    }
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 