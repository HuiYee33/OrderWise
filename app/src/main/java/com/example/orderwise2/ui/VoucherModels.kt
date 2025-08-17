package com.example.orderwise2.ui
import com.google.firebase.firestore.PropertyName

data class Voucher(
	val id: String = "",
	val title: String = "",
	val code: String = "",
	val pointsRequired: Int = 0,
	val discountType: String = "amount", // "amount" or "percent"
	val discountValue: Double = 0.0,
	@get:PropertyName("isActive") @set:PropertyName("isActive")
	var isActive: Boolean = true
)

data class UserVoucher(
	val id: String = "", // redemption id (doc id in user subcollection)
	val voucherId: String = "",
	val title: String = "",
	val code: String = "",
	val discountType: String = "amount",
	val discountValue: Double = 0.0,
	val status: String = "redeemed", // redeemed | used
	val redeemedAt: Long = System.currentTimeMillis()
)



