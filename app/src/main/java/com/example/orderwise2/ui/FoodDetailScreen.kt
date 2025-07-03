package com.example.orderwise2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.orderwise2.ui.MenuItem
import com.example.orderwise2.ui.StockStatus
import com.example.orderwise2.ui.IngredientOption
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import coil.compose.AsyncImage

@Composable
fun FoodDetailScreen(navController: NavController, cartViewModel: CartViewModel, foodId: String) {
    var food by remember { mutableStateOf<MenuItem?>(null) }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(foodId) {
        db.collection("menu").document(foodId).get().addOnSuccessListener { doc ->
            food = doc.toObject(MenuItem::class.java)
        }
    }

    if (food == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // Use food!! safely below
    val baseIngredients = food!!.ingredients.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    val additionalIngredients = food!!.additionalOptions
    val allIngredientOptions = additionalIngredients
    var selectedBaseIngredients by remember { mutableStateOf(baseIngredients) }
    var selectedAdditionalIngredients by remember { mutableStateOf(setOf<String>()) }
    var quantity by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image (placeholder if empty)
        if (food!!.imageUri.isNotEmpty()) {
            AsyncImage(
                model = food!!.imageUri,
                contentDescription = food!!.name,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
            )
        } else {
            Image(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = food!!.name,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(food!!.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        Spacer(Modifier.height(8.dp))
        Text("Base Ingredients:", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
            baseIngredients.forEach { ingredient ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedBaseIngredients.contains(ingredient),
                        onCheckedChange = { checked ->
                            selectedBaseIngredients = if (checked) {
                                selectedBaseIngredients + ingredient
                            } else {
                                selectedBaseIngredients.filter { it != ingredient }.toSet()
                            }
                        }
                    )
                    Text(ingredient, fontSize = 16.sp)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Additional Options", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            allIngredientOptions.forEach { ingredient ->
                val isBaseSelected = selectedBaseIngredients.contains(ingredient.name)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedAdditionalIngredients.contains(ingredient.name),
                        onCheckedChange = { checked ->
                            if (isBaseSelected) {
                                selectedAdditionalIngredients = if (checked) {
                                    selectedAdditionalIngredients + ingredient.name
                                } else {
                                    selectedAdditionalIngredients - ingredient.name
                                }
                            }
                        },
                        enabled = isBaseSelected
                    )
                    Text(ingredient.name, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text("+RM %.2f".format(ingredient.price), fontSize = 14.sp, color = androidx.compose.ui.graphics.Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Quantity:", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease")
            }
            Text(quantity.toString(), fontSize = 18.sp, modifier = Modifier.width(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            IconButton(onClick = { quantity++ }) {
                Icon(Icons.Default.Add, contentDescription = "Increase")
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { 
                val additionalPrice = allIngredientOptions
                    .filter { selectedAdditionalIngredients.contains(it.name) }
                    .sumOf { it.price }
                val totalPrice = food!!.price + additionalPrice
                val removedIngredients = baseIngredients - selectedBaseIngredients
                val removeRemark = if (removedIngredients.isNotEmpty()) {
                    "Remove: ${removedIngredients.joinToString(", ")}"
                } else ""
                val addRemark = if (selectedAdditionalIngredients.isNotEmpty()) {
                    "Add: ${selectedAdditionalIngredients.joinToString(", ")}"
                } else ""
                val remarks = listOf(removeRemark, addRemark).filter { it.isNotBlank() }.joinToString("; ").ifBlank { "Standard" }
                val cartItem = CartItem(
                    name = food!!.name,
                    quantity = quantity,
                    unitPrice = totalPrice,
                    remarks = remarks
                )
                cartViewModel.addToCart(cartItem)
                navController.navigate(Screen.Cart.route)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text("Add to Cart")
        }
    }
} 