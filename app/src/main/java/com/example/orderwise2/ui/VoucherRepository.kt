package com.example.orderwise2.ui

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class VoucherRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

	fun createVoucher(voucher: Voucher, onComplete: (Boolean) -> Unit) {
		val id = if (voucher.id.isBlank()) db.collection("vouchers").document().id else voucher.id
		db.collection("vouchers").document(id)
			.set(voucher.copy(id = id))
			.addOnSuccessListener { onComplete(true) }
			.addOnFailureListener { onComplete(false) }
	}

	fun updateVoucher(voucher: Voucher, onComplete: (Boolean) -> Unit) {
		if (voucher.id.isBlank()) {
			onComplete(false)
			return
		}
		db.collection("vouchers").document(voucher.id)
			.set(voucher)
			.addOnSuccessListener { onComplete(true) }
			.addOnFailureListener { onComplete(false) }
	}

	fun deleteVoucher(id: String, callback: (Boolean) -> Unit) {
		db.collection("vouchers").document(id)
			.delete()
			.addOnSuccessListener { callback(true) }
			.addOnFailureListener { callback(false) }
	}

	fun loadActiveVouchers(onResult: (List<Voucher>) -> Unit) {
		db.collection("vouchers")
			.whereEqualTo("isActive", true)
			.get()
			.addOnSuccessListener { snap ->
				val list = snap.documents.mapNotNull { it.toObject(Voucher::class.java) }
				onResult(list)
			}
			.addOnFailureListener { onResult(emptyList()) }
	}

	fun listenAllVouchers(onResult: (List<Voucher>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
		return db.collection("vouchers")
			.addSnapshotListener { snap, _ ->
				if (snap == null) {
					onResult(emptyList())
					return@addSnapshotListener
				}
				val list = snap.documents.mapNotNull { it.toObject(Voucher::class.java) }
				onResult(list)
			}
	}

	fun getVoucher(id: String, onResult: (Voucher?) -> Unit) {
		db.collection("vouchers").document(id)
			.get()
			.addOnSuccessListener { doc -> onResult(doc.toObject(Voucher::class.java)) }
			.addOnFailureListener { onResult(null) }
	}

	fun redeemVoucher(voucher: Voucher, onComplete: (Boolean) -> Unit) {
		val email = FirebaseAuth.getInstance().currentUser?.email ?: return onComplete(false)
		val userDoc = db.collection("users").document(email)
		userDoc.get().addOnSuccessListener { doc ->
			val currentPoints = (doc.getLong("loyaltyPoints") ?: 0L).toInt()
			if (currentPoints < voucher.pointsRequired) {
				onComplete(false)
				return@addOnSuccessListener
			}
			// Deduct points and add redeemed voucher
			db.runBatch { batch ->
				batch.update(userDoc, "loyaltyPoints", FieldValue.increment(-voucher.pointsRequired.toLong()))
				val redeemedRef = userDoc.collection("redeemedVouchers").document()
				val userVoucher = UserVoucher(
					id = redeemedRef.id,
					voucherId = voucher.id,
					title = voucher.title,
					code = voucher.code,
					discountType = voucher.discountType,
					discountValue = voucher.discountValue,
					status = "redeemed"
				)
				batch.set(redeemedRef, userVoucher)
			}.addOnSuccessListener { onComplete(true) }
			 .addOnFailureListener { onComplete(false) }
		}.addOnFailureListener { onComplete(false) }
	}

	fun loadUserRedeemedVouchers(onResult: (List<UserVoucher>) -> Unit) {
		val email = FirebaseAuth.getInstance().currentUser?.email ?: return onResult(emptyList())
		db.collection("users").document(email)
			.collection("redeemedVouchers")
			.whereEqualTo("status", "redeemed")
			.get()
			.addOnSuccessListener { snap ->
				val list = snap.documents.mapNotNull { it.toObject(UserVoucher::class.java) }
				onResult(list)
			}
			.addOnFailureListener { onResult(emptyList()) }
	}

	fun markVoucherUsed(userVoucherId: String, onComplete: (Boolean) -> Unit) {
		val email = FirebaseAuth.getInstance().currentUser?.email ?: return onComplete(false)
		db.collection("users").document(email)
			.collection("redeemedVouchers").document(userVoucherId)
			.update(mapOf("status" to "used"))
			.addOnSuccessListener { onComplete(true) }
			.addOnFailureListener { onComplete(false) }
	}
}



