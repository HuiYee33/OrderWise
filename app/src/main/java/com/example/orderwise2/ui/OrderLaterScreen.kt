package com.example.orderwise2.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background //background color or image
import androidx.compose.foundation.border //add a border
import androidx.compose.foundation.layout.* //layout tools like Row, Column, Box, Spacer
import androidx.compose.foundation.rememberScrollState //keep track of scrolling position when something can be scrolled
import androidx.compose.foundation.shape.CircleShape //predefined circular shape for things like profile pictures or round buttons
import androidx.compose.foundation.shape.RoundedCornerShape //rounded corners for cards, buttons
import androidx.compose.foundation.verticalScroll //scroll vertically inside a layout like Column
import androidx.compose.material.icons.Icons //collection of Material Design icons
import androidx.compose.material.icons.filled.ArrowBack //back arrow icon
import androidx.compose.material3.* //Material 3 UI components like buttons, text fields, cards
import androidx.compose.runtime.* //store and update values in UI using things like remember and mutableStateOf
import androidx.compose.ui.Alignment //align things inside layouts (e.g., center, start, end)
import androidx.compose.ui.Modifier //toolbox for styling and positioning
import androidx.compose.ui.draw.clip //Cuts a UI element into a specific shape (circle, rounded)
import androidx.compose.ui.graphics.Color //use color
import androidx.compose.ui.res.painterResource //Loads images from the res/drawable folder
import androidx.compose.ui.text.font.FontWeight //font thickness (bold, medium, light)
import androidx.compose.ui.text.style.TextAlign //text alignment (left, center, right)
import androidx.compose.ui.unit.dp //density-independent pixels (for padding, size)
import androidx.compose.ui.unit.sp //scale-independent pixels (for font sizes)
import androidx.navigation.NavController //Controls screen navigation (moving between pages/screens)
import java.text.SimpleDateFormat //Formats dates into human-readable strings
import java.util.Date //current date and time
import java.util.Locale //language/region setting (e.g., English US, Chinese MY)
import java.util.UUID //Generates a unique ID for something
import com.google.firebase.auth.FirebaseAuth //handle user authentication (sign in, sign up)
import com.google.firebase.firestore.FieldValue //special Firestore values like server timestamps, array operations
import android.widget.Toast //Shows a small popup message on screen
import com.google.firebase.firestore.FirebaseFirestore //read and write data to Firestore database
import androidx.compose.ui.platform.LocalContext //current context (needed for things like Toast)
import com.google.firebase.firestore.SetOptions //Specifies how Firestore should merge or overwrite data when saving

// OrderLaterScreen: Allows users to schedule orders for later pickup
@Composable
fun OrderLaterScreen(
    navController: NavController,
    cartItems: List<CartItem>,
    cartViewModel: CartViewModel //stores and manages cart data, and updates the UI when data changes
) {
    val context = LocalContext.current //will use it to show feedback to the user like voucher applied
    var voucherCode by remember { mutableStateOf("") } //mutableStateOf = store value & notify compose
    val subtotal = cartItems.sumOf { it.unitPrice * it.quantity } //calculate total price before discount
    var appliedVoucher by remember { mutableStateOf<UserVoucher?>(null) } //store applied voucher, null if haven't applied
    var showApplyDialog by remember { mutableStateOf(false) } //show dialog if true
    val discount = appliedVoucher?.let { uv ->//if no voucher apply(appliedVoucher=false),skip block
        when (uv.discountType) { //check type of discount
            "amount" -> uv.discountValue //this is a fix disc, just return that number
            else -> subtotal * (uv.discountValue / 100.0) //percentage discount, muultiply it
        }
    } ?: 0.0 //no voucher applied, discount 0.0
    val tax = (subtotal - discount).coerceAtLeast(0.0) * 0.06 // 6% tax, coerceAtLeast=ensure amount after discount can't be negative
    val total = (subtotal - discount).coerceAtLeast(0.0) + tax

    Column( //vertical layout
        modifier = Modifier //set of instructions that change how the Column looks or behaves
            .fillMaxSize() //Makes the Column take up the entire screen (width and height)
            .background(Color(0xFFF8B259)) // Light beige background
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row( //places child elements side by side, from left to right
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), //avoid touch the edges
            verticalAlignment = Alignment.CenterVertically //align all item inside row vertically in center
        ) {
            //create a button with icon only
            IconButton(onClick = { navController.popBackStack() }) { //go back previous screen when click
                Icon(
                    imageVector = Icons.Default.ArrowBack, //Uses the built-in Material icon for back arrows
                    contentDescription = "Back", //Uses the built-in Material icon for back arrows
                    tint = Color.Black //Paints the icon black
                )
            }
            Spacer(modifier = Modifier.width(8.dp)) //Adds horizontal empty space between the back arrow and the next element
            Text(
                text = "Check Out",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
            )
        }

        // Order Summary Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp) //Adds space on the left and right sides so content isn’t touching the edges
        ) {
            Text(
                text = "Order Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp) //add space below the text
            )
            
            // Column headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End //push all items to right side of the row
            ) {
                Text(
                    text = "Unit price",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 16.dp), //add space on right

                )
                Text(
                    text = "Quantity",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Order items
            cartItems.forEach { item ->
                OrderItemRow(item = item)
                if (item != cartItems.last()) { //if it's not last item
                    Divider( //draws a horizontal line between items
                        modifier = Modifier.padding(vertical = 8.dp),//gives space above and below the divider
                        color = Color.Gray.copy(alpha = 0.3f) //30% transparent of the line
                    )
                }
            }

            // Subtotal, Discount, Tax, and Total
            Spacer(modifier = Modifier.height(16.dp)) //Spacer is like an invisible box that takes up space
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, //Places first item at the left, last item at the right, and spreads others evenly
                    verticalAlignment = Alignment.CenterVertically //text, icons, or other components inside are vertically centered with each other
                ) {
                    Text(
                        text = "Subtotal",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "RM ${String.format("%.2f", subtotal)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Discount
                if (discount > 0) { //show this part if discount more than 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween, // text in left and right
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Discount (${appliedVoucher?.code})",
                            fontSize = 16.sp,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "- RM ${String.format("%.2f", discount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Tax
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tax (6%)",
                        fontSize = 16.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "RM ${String.format("%.2f", tax)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        fontSize = 18.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RM ${String.format("%.2f", total)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Order For Later Button
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { 
                navController.navigate(Screen.DateTimeSelection.route) //go to the Date & Time Selection screen
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp), //space between button and screen
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFF4E4BC) // background color of the button, Light gold/beige
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Order For Later",
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        // Display the selected pickup date and time if chosen
        if (cartViewModel.selectedDate != null && cartViewModel.selectedTimeSlot != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected: ",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        //display date and time
                        text = "${cartViewModel.selectedDate?.displayText} at ${cartViewModel.selectedTimeSlot?.displayText}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }

        // Payment Details Section
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payment Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { 
                        navController.navigate(Screen.PaymentMethod.route)
                    }
                ) {
                    Text(
                        text = "See all",
                        color = Color.Blue,
                        fontSize = 14.sp
                    )
                }
            }
            
            // Display selected payment method
            if (cartViewModel.selectedPaymentMethod.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected: ",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = cartViewModel.selectedPaymentMethod,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
            }
        }

        // Offers Section
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Offers",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp) //add space below the text
            )
            
            Button(
                onClick = { showApplyDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8F8F8) // Light off-white
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    //show voucher code if applied
                    text = if (appliedVoucher == null) "Select Voucher" else "Voucher: ${appliedVoucher?.code} (Change)",
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            ApplyVoucherDialog(
                open = showApplyDialog, //dialog should be visible if true, false = hide it
                onDismiss = { showApplyDialog = false }, //disappear if user close dialog
                onApplied = { uv -> appliedVoucher = uv } //store selected voucher to appliedVoucher
            )
        }

        // Place Order Button: creates a PurchaseRecord including pickup info if present
        Spacer(modifier = Modifier.height(16.dp))
        DividerWithDiamonds()
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                // Get user email
                val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""
                // Get selected pickup date and time slot (if any)
                val pickupDate = cartViewModel.selectedDate?.displayText
                val pickupTimeSlot = cartViewModel.selectedTimeSlot?.displayText
                // Create purchase record with pickup info
                val record = PurchaseRecord(
                    id = UUID.randomUUID().toString(),
                    date = getCurrentDateString(),
                    items = cartItems.toList(), //saves all items currently in the shopping cart
                    feedback = "",
                    userEmail = userEmail,
                    pickupDate = pickupDate,
                    pickupTimeSlot = pickupTimeSlot
                )
                // Add record to history and clear cart
                cartViewModel.addPurchaseRecord(record)
                cartViewModel.clearCart()
                // Loyalty points logic (earnings)
                val pointsEarned = total.toInt() //convert total purchase amount to int
                if (userEmail.isNotEmpty()) { //skip adding point if no login
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userEmail) //look for user's email as id
                        .set( //add or update data in that document
                            mapOf(
                                "email" to userEmail, //save email in database
                                "loyaltyPoints" to FieldValue.increment(pointsEarned.toLong()) //increase point by pointsEarned using FieldValue.increment()
                            ),
                            SetOptions.merge()) //update the existing document without deleting other fields
                        .addOnSuccessListener { //show toast notification if successfully update
                            Toast.makeText(
                                context,
                                "You earned $pointsEarned loyalty points!",
                                Toast.LENGTH_LONG //how long should stay on screen (long=3.5 second, short = 2 s)
                            ).show()
                        }
                    // Mark voucher used if applied
                    appliedVoucher?.let { uv ->
                        VoucherRepository(db).markVoucherUsed(uv.id) { } //update voucher id in database
                    }
                }
                // Navigate to payment success screen
                navController.navigate(Screen.PaymentSuccess.route)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD3D3D3) // Light gray/purple
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Place Order",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OrderItemRow(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Food image (placeholder circle)
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.name.take(1).uppercase(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Item details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (item.remarks.isNotBlank()) {
                Text(
                    text = "Remark:",
                    fontSize = 12.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
                item.remarks.split(",").forEach { remark ->
                    Text(
                        text = "• $remark",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }
        }
        
        // Price and quantity
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "RM${String.format("%.2f", item.unitPrice)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.quantity}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DividerWithDiamonds() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .height(1.dp)
                .weight(1f)
                .background(Color.Gray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.5f))
        )
    }
}

fun getCurrentDateString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date())
} 