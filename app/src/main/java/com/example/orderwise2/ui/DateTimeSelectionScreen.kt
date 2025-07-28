package com.example.orderwise2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

// Composable for selecting date and time for order pickup
@Composable
fun DateTimeSelectionScreen(
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance()
    tomorrow.add(Calendar.DAY_OF_YEAR, 1)

    val dateFormat = SimpleDateFormat("EEE dd", Locale.ENGLISH)
    val todayFormatted = dateFormat.format(today.time)
    val tomorrowFormatted = dateFormat.format(tomorrow.time)

    val dates = listOf(
        DateOption(todayFormatted, today.time, false),
        DateOption(tomorrowFormatted, tomorrow.time, false)
    )

    val timeSlots = generateTimeSlots()

    var selectedDateIndex by remember { mutableStateOf(0) } // 0 for today, 1 for tomorrow
    var selectedTimeSlot by remember { mutableStateOf(timeSlots[1]) } // Default to 9:30-10:00

    // Update dates with selection state
    val updatedDates = dates.mapIndexed { index, dateOption ->
        dateOption.copy(isSelected = index == selectedDateIndex)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC)) // Light beige background
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Select date and time",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                color = Color.Black
            )
        }

        // Date Selection Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Date",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Add Today and Tomorrow labels above the date options
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Today", fontSize = 12.sp, color = Color.Gray)
                    DateSelectionItem(
                        dateOption = updatedDates[0],
                        onClick = { selectedDateIndex = 0 }
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tomorrow", fontSize = 12.sp, color = Color.Gray)
                    DateSelectionItem(
                        dateOption = updatedDates[1],
                        onClick = { selectedDateIndex = 1 }
                    )
                }
            }
        }

        // Time Selection Section
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Time",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(timeSlots) { timeSlot ->
                    TimeSlotItem(
                        timeSlot = timeSlot,
                        isSelected = timeSlot == selectedTimeSlot,
                        onClick = { selectedTimeSlot = timeSlot }
                    )
                    if (timeSlot != timeSlots.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Gray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }

        // Confirm Button: saves selected date/time to CartViewModel
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                cartViewModel.setSelectedDateTime(updatedDates[selectedDateIndex], selectedTimeSlot)
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700) // Gold color
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Confirm Date & Time",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

// Composable for a selectable date option
@Composable
fun DateSelectionItem(
    dateOption: DateOption,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            // .fillMaxWidth() removed to allow side-by-side display
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dateOption.displayText.split(" ")[0], // Day name (Mon, Tue)
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (dateOption.isSelected) Color(0xFFFFD700) // Gold when selected
                    else Color.Transparent
                )
                .border(
                    width = if (dateOption.isSelected) 0.dp else 1.dp,
                    color = Color.Gray.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateOption.displayText.split(" ")[1], // Day number (28, 29)
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// Composable for a selectable time slot
@Composable
fun TimeSlotItem(
    timeSlot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = timeSlot.displayText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color(0xFFFFD700), // Gold color
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// Data class representing a selectable date option
// displayText: e.g., "Tue 29"
// date: actual Date object
// isSelected: whether this option is currently selected
data class DateOption(
    val displayText: String,
    val date: Date,
    val isSelected: Boolean
)

// Data class representing a selectable time slot
// displayText: e.g., "10am - 10.30am"
// startTime, endTime: time in 24h format (e.g., "10:00", "10:30")
data class TimeSlot(
    val displayText: String,
    val startTime: String,
    val endTime: String
)

// Generates a list of time slots for selection
fun generateTimeSlots(): List<TimeSlot> {
    val timeSlots = mutableListOf<TimeSlot>()
    val startHour = 9
    val endHour = 17 // 5 PM
    for (hour in startHour until endHour) {
        for (minute in listOf(0, 30)) {
            val startTime = String.format("%d:%02d", hour, minute)
            val slotEndHour = if (minute == 30) hour + 1 else hour
            val slotEndMinute = if (minute == 30) 0 else 30
            val endTime = String.format("%d:%02d", slotEndHour, slotEndMinute)
            val displayText = if (hour < 12) {
                "$startTime AM - $endTime AM"
            } else if (hour == 12) {
                "$startTime PM - $endTime PM"
            } else {
                val startHour12 = hour - 12
                val endHour12 = slotEndHour - 12
                "${startHour12}:${String.format("%02d", minute)} PM - ${endHour12}:${String.format("%02d", slotEndMinute)} PM"
            }
            timeSlots.add(TimeSlot(displayText, startTime, endTime))
        }
    }
    return timeSlots
} 