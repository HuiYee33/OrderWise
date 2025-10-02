package com.example.orderwise2.ui

import androidx.compose.foundation.layout.* //provide layout function like row and column
import androidx.compose.foundation.lazy.LazyColumn //display vertical scrolling
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items //show item in list
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* //provide modern UI element like button, card
import androidx.compose.runtime.* //keep track of changing data in the screen, update automatic UI if data changes using remember, mutableStateOf, LaunchedEffect
import androidx.compose.ui.Alignment //position items
import androidx.compose.ui.Modifier //things like padding, size, color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp //density-independent pixels (use for layout like height)
import androidx.compose.ui.unit.sp //scale-independent pixels (use for text)
import androidx.navigation.NavController
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip //make sure corner stay round
import androidx.compose.foundation.layout.padding //space inside element
import androidx.compose.foundation.layout.fillMaxSize //make ad big as possible
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log //print messages for testing and fixing bug
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DishStats(
    val name: String,
    val quantity: Int,
    val percentage: Float,
    val color: Color
)

enum class OverviewRange { TODAY, WEEK, MONTH } //fix the value

@Composable
fun AdminDashboardScreen(navController: NavController) {
    //remember = data will not change if update UI
    var purchaseHistory by remember { mutableStateOf(listOf<PurchaseRecord>()) } // list that will hold all the order records, if the list change, update screen automatic
    var isLoading by remember { mutableStateOf(true) } //if the data is complete load, it will turn to false
    var selectedTab by remember { mutableStateOf(0) } //0 means the first tab is active when the screen starts.
    var selectedOverviewRange by remember { mutableStateOf(OverviewRange.TODAY) } //it starts as today
    var selectedPopularityCategory by remember { mutableStateOf<String?>(null) } // stores which food category is selected for the popularity section, can be nothing selected yet
    val db = FirebaseFirestore.getInstance() // connect to firestore
    val chatGPTService = remember { ChatGPTService() } // creates ChatGPT helper for AI suggestion
    val allMenuItems = rememberMenuItems()
    val categoriesFromMenu = remember(allMenuItems) { allMenuItems.map { it.category }.filter { it.isNotBlank() }.distinct() } //only do this again if allMenuItems changes,take category of item, remove empty category name, distinct=remove duplicate
    val categoriesFromHistory = remember(purchaseHistory) {
        purchaseHistory.flatMap { it.items } //Take all the items from all orders and make one big list
            .map { it.category } // Take the category for each item
            .filter { it.isNotBlank() } //Remove empty names
            .distinct() //remove duplicates
    }
    // combines menu categories and history categories into one list
    val categories = remember(categoriesFromMenu, categoriesFromHistory) { //Only updates if either categoriesFromMenu or categoriesFromHistory changes
        (categoriesFromMenu + categoriesFromHistory).distinct()
    }

    // Fetch purchase history from Firestore
    LaunchedEffect(Unit) {
        db.collection("purchaseHistory") //get purchase history collection from firebase
            .get() //ask db to read all the document in the collection
            .addOnSuccessListener { result -> //run this block if data loads successful
                val records = mutableListOf<PurchaseRecord>() // Create an empty list to store the purchase records
                for (doc in result) { //Loop through each document (record) in the result
                    try {
                        val record = doc.toObject(PurchaseRecord::class.java).copy(id = doc.id) //Convert the document to a PurchaseRecord object and keep its ID
                        records.add(record) //Add the converted record to the list
                    } catch (e: Exception) { //when conversation go wrong
                        Log.e("AdminDashboard", "Failed to parse record: ${doc.id}", e) //print error message to log with document id and error
                    }
                }
                purchaseHistory = records //save full list of pc to screen state
                isLoading = false //set loading to false(loading done)
            }
            .addOnFailureListener { e -> //if data load fail
                Log.e("AdminDashboard", "Failed to load purchase history", e)
                isLoading = false
            }

    }
    var totalCustomers by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        db.collection("users") // replace "users" with your actual customer collection name
            .get()
            .addOnSuccessListener { result ->
                totalCustomers = result.size() // number of customer documents
            }
            .addOnFailureListener { e ->
                Log.e("AdminDashboard", "Failed to load customers", e)
            }
    }


    // Compute dish popularity across all time (Overview uses range-specific filtering below)
    val dishCountMapAll = remember(purchaseHistory) { //  store how many of each dish was ordered,only re-calculate this if purchaseHistory changes
        val map = mutableMapOf<String, Int>() // An empty map that connects key (dish name) and value (quantity)
        purchaseHistory.forEach { record -> //Go through every purchase record
            record.items.forEach { item -> // go through every item in that order
                map[item.name] = map.getOrDefault(item.name, 0) + item.quantity
            }
        }
        map //return map as result
    }
    val nameToCategoryFromMenu = remember(allMenuItems) { allMenuItems.associate { it.name to it.category } } //turn list of menu item to map
    val nameToCategoryFromHistory = remember(purchaseHistory) { //using purchase history
        purchaseHistory.flatMap { it.items } //flatMap = take all orders and combine all their dishes into one big list
            .filter { it.category.isNotBlank() } //removes any dish that has an empty category
            .associate { it.name to it.category } //makes the map
    }
    val nameToCategory = remember(nameToCategoryFromMenu, nameToCategoryFromHistory) {
        // Prefer menu category; fall back to first seen in history
        nameToCategoryFromHistory + nameToCategoryFromMenu
    }
    val filteredDishCountMap = remember(selectedPopularityCategory, purchaseHistory, nameToCategory) {
        if (selectedPopularityCategory == null) dishCountMapAll else { //doesn’t filter — just shows the full popularity list from dishCountMapAll
            val map = mutableMapOf<String, Int>() //make an empty map for filtered results (dish name, quantity)
            purchaseHistory.forEach { record ->
                record.items.forEach { item ->
                    //use the category stored in the item (if it’s not empty),else tries to look up the category by dish name in nameToCategory,still nothing, it uses an empty string
                    val cat = if (item.category.isNotBlank()) item.category else nameToCategory[item.name] ?: ""
                    if (cat == selectedPopularityCategory) { // Only count items that match the selected category
                        map[item.name] = map.getOrDefault(item.name, 0) + item.quantity
                    }
                }
            }
            map
        }
    }
    val totalDishQuantity = filteredDishCountMap.values.sum().coerceAtLeast(1)
    val colorPalette = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF9C27B0), // Purple
        Color(0xFF00BCD4), // Cyan
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF3F51B5), // Indigo
        Color(0xFF795548)  // Brown
    )
    val dishStatsAll = dishCountMapAll.entries
        .sortedByDescending { it.value }
        .mapIndexed { index, (name, qty) ->
            val percentage = qty * 100f / totalDishQuantity
            val color = colorPalette[index % colorPalette.size]
            DishStats(name = name, quantity = qty, percentage = percentage, color = color)
        }
    val dishStats = filteredDishCountMap.entries
        .sortedByDescending { it.value }
        .mapIndexed { index, (name, qty) ->
            val filteredTotal = filteredDishCountMap.values.sum().coerceAtLeast(1)
            val percentage = qty * 100f / filteredTotal
            val color = colorPalette[index % colorPalette.size]
            DishStats(name = name, quantity = qty, percentage = percentage, color = color)
        }

    // Overall snapshot for AI assistant (unfiltered)
    val overallOrders = purchaseHistory.size
    val overallItems = purchaseHistory.sumOf { it.items.sumOf { item -> item.quantity } }
    val overallRevenue = purchaseHistory.sumOf { it.items.sumOf { item -> item.unitPrice * item.quantity } }

    // Today's revenue for AI assistant
    val todaysRevenue = remember(purchaseHistory) {
        val todayRecords = filterHistoryByRange(purchaseHistory, OverviewRange.TODAY)
        todayRecords.sumOf { it.items.sumOf { item -> item.unitPrice * item.quantity } }
    }

    val weeklyRevenue = remember(purchaseHistory) {
        val weekRecords = filterHistoryByRange(purchaseHistory, OverviewRange.WEEK)
        weekRecords.sumOf { it.items.sumOf { item -> item.unitPrice * item.quantity } }
    }

    val monthlyRevenue = remember(purchaseHistory) {
        val monthRecords = filterHistoryByRange(purchaseHistory, OverviewRange.MONTH)
        monthRecords.sumOf { it.items.sumOf { item -> item.unitPrice * item.quantity } }
    }

    val dashboardData = DashboardData(overallOrders, overallItems, overallRevenue, dishStats,totalCustomers, todaysRevenue,weeklyRevenue, monthlyRevenue)

    Scaffold(
        bottomBar = { AdminBottomNavigation(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize() //taking up the full screen
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
                    "Admin Dashboard",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    TabButton(
                        text = "Analytics",
                        icon = Icons.Default.Analytics,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TabButton(
                        text = "AI Assistant",
                        icon = Icons.Default.Chat, //default chat bubble icon provided by Material Icons in Jetpack Compose.
                        isSelected = selectedTab == 1, // Check if this tab is selected (true if selectedTab is 1)
                        onClick = { selectedTab = 1 } // When user clicks this tab, set selectedTab to 1 (make it active)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) { // Switch between tabs select by user
                0 -> { // user choose tab 0 (Analytic tab)
                    // Analytics Tab
                    if (isLoading) { //check whether data is still loading
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { // Show a loading spinner in the center of the screen
                            CircularProgressIndicator()
                        }
                    } else { // if data is loaded
                        AnalyticsContent(
                            purchaseHistory = purchaseHistory,
                            selectedRange = selectedOverviewRange,
                            onRangeChange = { selectedOverviewRange = it },
                            dishStats = dishStats,
                            categories = categories,
                            selectedCategory = selectedPopularityCategory,
                            onCategoryChange = { selectedPopularityCategory = it }
                        )
                    }
                }
                1 -> { //user select tab 1
                    ChatGPTWithSidebar(chatGPTService, dashboardData) // Display the ChatGPT AI Assistant component
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else Color.Black
            )
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun AnalyticsContent(
    purchaseHistory: List<PurchaseRecord>,
    selectedRange: OverviewRange,
    onRangeChange: (OverviewRange) -> Unit,
    dishStats: List<DishStats>,
    categories: List<String>,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        // Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OverviewRangeSelector(selected = selectedRange, onChange = onRangeChange)
                Spacer(modifier = Modifier.height(8.dp))
                val (totalOrders, totalItems, revenue) = remember(purchaseHistory, selectedRange) {
                    val filtered = filterHistoryByRange(purchaseHistory, selectedRange)
                    val orders = filtered.size
                    val items = filtered.sumOf { it.items.sumOf { item -> item.quantity } }
                    val rev = filtered.sumOf { it.items.sumOf { item -> item.unitPrice * item.quantity } }
                    Triple(orders, items, rev)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem("Total Orders", totalOrders.toString(), Color(0xFF4CAF50))
                    StatItem("Total Items", totalItems.toString(), Color(0xFF2196F3))
                    StatItem("Revenue", "RM %.2f".format(revenue), Color(0xFFFF9800))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dish Popularity Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Dish Popularity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryFilterSelector(
                    categories = categories,
                    selected = selectedCategory,
                    onChange = onCategoryChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (dishStats.isNotEmpty()) {
                    PieChart(
                        data = dishStats,
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                dishStats.forEach { dish ->
                    DishPopularityItem(dish)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

    }
}

@Composable
fun StatItem(title: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun DishPopularityItem(dish: DishStats) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(dish.color)
        ) {
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            dish.name,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Text(
            "${dish.quantity} orders",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "${dish.percentage.toInt()}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = dish.color
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(8.dp)
            	.clip(RoundedCornerShape(4.dp))
                .background(Color.LightGray)
        ) {
            Box(
                modifier = Modifier
                    .width((60 * dish.percentage / 100).dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dish.color)
            )
        }
    }
}

@Composable
fun PieChart(
    data: List<DishStats>,
    modifier: Modifier = Modifier,
    startAngle: Float = -90f
) {
    Canvas(modifier = modifier) {
        val totalQuantity = data.sumOf { it.quantity }.coerceAtLeast(1)
        var currentStartAngle = startAngle
        val diameter = size.minDimension
        val topLeft = Offset(
            (size.width - diameter) / 2f,
            (size.height - diameter) / 2f
        )
        val chartSize = Size(diameter, diameter)

        data.forEach { entry ->
            val sweepAngle = (entry.quantity.toFloat() / totalQuantity.toFloat()) * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    color = entry.color,
                    startAngle = currentStartAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = topLeft,
                    size = chartSize
                )
                currentStartAngle += sweepAngle
            }
        }
    }
}

@Composable
private fun OverviewRangeSelector(
    selected: OverviewRange,
    onChange: (OverviewRange) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(text = "Today", isSelected = selected == OverviewRange.TODAY) {
            onChange(OverviewRange.TODAY)
        }
        FilterChip(text = "Weekly", isSelected = selected == OverviewRange.WEEK) {
            onChange(OverviewRange.WEEK)
        }
        FilterChip(text = "Monthly", isSelected = selected == OverviewRange.MONTH) {
            onChange(OverviewRange.MONTH)
        }
    }
}

@Composable
private fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF4CAF50) else Color(0xFFE0E0E0)
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, color = if (isSelected) Color.White else Color.Black, fontSize = 12.sp)
    }
}

private fun filterHistoryByRange(
    purchaseHistory: List<PurchaseRecord>,
    range: OverviewRange
): List<PurchaseRecord> {
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    val now = Calendar.getInstance()

    fun parse(dateStr: String): Date? = try {
        format.parse(dateStr)
    } catch (_: ParseException) {
        null
    }

    return when (range) {
        OverviewRange.TODAY -> {
            val start = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.time
            val end = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.time
            purchaseHistory.filter { rec ->
                parse(rec.date)?.let { it.after(start) && it.before(end) } ?: false
            }
        }
        OverviewRange.WEEK -> {
            val cal = now.clone() as Calendar
            cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val start = cal.time
            val end = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.time
            purchaseHistory.filter { rec ->
                parse(rec.date)?.let { it.after(start) && it.before(end) } ?: false
            }
        }
        OverviewRange.MONTH -> {
            val cal = now.clone() as Calendar
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            val start = cal.time
            val end = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.time
            purchaseHistory.filter { rec ->
                parse(rec.date)?.let { it.after(start) && it.before(end) } ?: false
            }
        }
    }
}


@Composable
private fun CategoryFilterSelector(
    categories: List<String>,
    selected: String?,
    onChange: (String?) -> Unit
) {
    Column {
        Text("Category", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(text = "Overall", isSelected = selected == null) { onChange(null) }
            }
            if (categories.isEmpty()) {
                item { Text("No categories found", color = Color.Gray, fontSize = 12.sp) }
            } else {
                items(categories) { cat ->
                    FilterChip(text = cat, isSelected = selected == cat) { onChange(cat) }
                }
            }
        }
    }
} 