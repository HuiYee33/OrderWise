package com.example.orderwise2.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

class CartViewModel : ViewModel() {
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: SnapshotStateList<CartItem> = _cartItems

    private val _purchaseHistory = mutableStateListOf<PurchaseRecord>()
    val purchaseHistory: List<PurchaseRecord> = _purchaseHistory

    private val db = FirebaseFirestore.getInstance()

    fun addToCart(item: CartItem) {
        // Check if item already exists in cart
        val existingItemIndex = _cartItems.indexOfFirst { 
            it.name == item.name && it.remarks == item.remarks 
        }
        
        if (existingItemIndex != -1) {
            // Update quantity of existing item
            val existingItem = _cartItems[existingItemIndex]
            _cartItems[existingItemIndex] = existingItem.copy(
                quantity = existingItem.quantity + item.quantity
            )
        } else {
            // Add new item
            _cartItems.add(item)
        }
    }

    fun removeFromCart(index: Int) {
        if (index in _cartItems.indices) {
            _cartItems.removeAt(index)
        }
    }

    fun updateQuantity(index: Int, newQuantity: Int) {
        if (index in _cartItems.indices && newQuantity > 0) {
            val item = _cartItems[index]
            _cartItems[index] = item.copy(quantity = newQuantity)
        } else if (index in _cartItems.indices && newQuantity <= 0) {
            _cartItems.removeAt(index)
        }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun getTotal(): Double {
        return _cartItems.sumOf { it.unitPrice * it.quantity }
    }

    fun addPurchaseRecord(record: PurchaseRecord) {
        _purchaseHistory.add(record)
        db.collection("purchaseHistory")
            .document(record.id)
            .set(record)
    }

    fun updateFeedback(recordId: String, feedback: String) {
        db.collection("purchaseHistory").document(recordId)
            .update("feedback", feedback)
        // Also update in local list
        val idx = _purchaseHistory.indexOfFirst { it.id == recordId }
        if (idx != -1) {
            _purchaseHistory[idx] = _purchaseHistory[idx].copy(feedback = feedback)
        }
    }

    fun loadPurchaseHistory() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email
        db.collection("purchaseHistory")
            .get()
            .addOnSuccessListener { result ->
                _purchaseHistory.clear()
                for (doc in result) {
                    try {
                        val record = doc.toObject(PurchaseRecord::class.java)
                            .copy(id = doc.id)
                        if (record.userEmail == currentUserEmail) {
                            _purchaseHistory.add(record)
                        }
                    } catch (e: Exception) {
                        Log.e("PurchaseHistory", "Failed to parse record: ${doc.id}", e)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("PurchaseHistory", "Failed to load purchase history", e)
            }
    }
} 