package com.example.orderwise2.ui

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

// ViewModel for managing cart state, purchase history, and order logic
class CartViewModel : ViewModel() {
    // List of items currently in the cart
    private val _cartItems = mutableStateListOf<CartItem>()
    val cartItems: SnapshotStateList<CartItem> = _cartItems

    // List of completed purchase records
    private val _purchaseHistory = mutableStateListOf<PurchaseRecord>()
    val purchaseHistory: List<PurchaseRecord> = _purchaseHistory

    // Currently selected payment method
    private val _selectedPaymentMethod = mutableStateOf("")
    val selectedPaymentMethod: String get() = _selectedPaymentMethod.value

    // Currently selected pickup date and time slot for 'order later'
    private val _selectedDate = mutableStateOf<DateOption?>(null)
    val selectedDate: DateOption? get() = _selectedDate.value

    private val _selectedTimeSlot = mutableStateOf<TimeSlot?>(null)
    val selectedTimeSlot: TimeSlot? get() = _selectedTimeSlot.value

    // Firestore database instance
    private val db = FirebaseFirestore.getInstance()

    // Add an item to the cart (or update quantity if already present)
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

    // Remove an item from the cart by index
    fun removeFromCart(index: Int) {
        if (index in _cartItems.indices) {
            _cartItems.removeAt(index)
        }
    }

    // Update the quantity of an item in the cart
    fun updateQuantity(index: Int, newQuantity: Int) {
        if (index in _cartItems.indices && newQuantity > 0) {
            val item = _cartItems[index]
            _cartItems[index] = item.copy(quantity = newQuantity)
        } else if (index in _cartItems.indices && newQuantity <= 0) {
            _cartItems.removeAt(index)
        }
    }

    // Clear all items from the cart
    fun clearCart() {
        _cartItems.clear()
    }

    // Calculate the total price of items in the cart
    fun getTotal(): Double {
        return _cartItems.sumOf { it.unitPrice * it.quantity }
    }

    // Add a completed purchase record to history and Firestore
    fun addPurchaseRecord(record: PurchaseRecord) {
        _purchaseHistory.add(record) // Add the new record to the local list (in memory)
        db.collection("purchaseHistory") // Save the record to Firestore database under the "purchaseHistory" collection
            .document(record.id)  // Use the record's ID as the document ID
            .set(record) // Store the entire record object in the database
    }

    // Update feedback for a purchase record
    fun updateFeedback(recordId: String, feedback: String) {
        // Update the "feedback" field in the Firestore document with the given record ID
        db.collection("purchaseHistory").document(recordId)
            .update("feedback", feedback)
        // Also update in local list
        val idx = _purchaseHistory.indexOfFirst { it.id == recordId }
        if (idx != -1) { // If the record is found in the list
            _purchaseHistory[idx] = _purchaseHistory[idx].copy(feedback = feedback) // Replace the old record with a copy that includes the new feedback
        }
    }

    // Load purchase history from Firestore for the current user
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

    // Set the selected payment method
    fun setSelectedPaymentMethod(paymentMethod: String) {
        _selectedPaymentMethod.value = paymentMethod
    }

    // Set the selected pickup date and time slot
    fun setSelectedDateTime(date: DateOption, timeSlot: TimeSlot) {
        _selectedDate.value = date
        _selectedTimeSlot.value = timeSlot
    }
} 