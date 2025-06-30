package com.example.orderwise2.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel

class CartViewModel : ViewModel() {
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: SnapshotStateList<CartItem> = _cartItems

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
} 