package com.example.orderwise2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class UIChatMessage(
    val content: String,
    val isUser: Boolean, //true if user send message, false if system sent
    val timestamp: Long = System.currentTimeMillis() // The time the message was created (in milliseconds), defaults to now
)

@Composable
fun ChatSidebar(
    chatHistory: List<List<UIChatMessage>>,
    onSelectChat: (Int) -> Unit,
    onNewChat: () -> Unit,
    selectedChatIndex: Int?
) {
    Column(modifier = Modifier.fillMaxHeight().width(280.dp).background(Color(0xFFF5F5F5))) {
        Text("Chats", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(16.dp))
        Button(onClick = onNewChat, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text("+ New chat")
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(chatHistory) { idx, chat ->
                val title = chat.firstOrNull()?.content?.take(30) ?: "(Empty chat)"
                val isSelected = idx == selectedChatIndex
                TextButton(
                    onClick = { onSelectChat(idx) },
                    modifier = Modifier.fillMaxWidth().background(if (isSelected) Color(0xFFE0E0E0) else Color.Transparent)
                ) {
                    Text(title, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black)
                }
            }
        }
    }
}

@Composable
fun ChatGPTWithSidebar(chatGPTService: ChatGPTService, dashboardData: DashboardData? = null) {
    var chatHistory by remember { mutableStateOf(listOf(listOf<UIChatMessage>())) }
    var selectedChatIndex by remember { mutableStateOf(0) }
    var drawerOpen by remember { mutableStateOf(false) }
    // Input and sending handled inside ChatGPTComponent

    ModalNavigationDrawer(
        drawerContent = {
            ChatSidebar(
                chatHistory = chatHistory,
                onSelectChat = { selectedChatIndex = it; drawerOpen = false },
                onNewChat = {
                    chatHistory = chatHistory + listOf(listOf())
                    selectedChatIndex = chatHistory.lastIndex
                    drawerOpen = false
                },
                selectedChatIndex = selectedChatIndex
            )
        },
        drawerState = rememberDrawerState(if (drawerOpen) DrawerValue.Open else DrawerValue.Closed),
        gesturesEnabled = true
    ) {
        Box(Modifier.fillMaxSize()) {
            IconButton(onClick = { drawerOpen = true }, modifier = Modifier.padding(16.dp).align(Alignment.TopStart)) {
                Icon(Icons.Default.Menu, contentDescription = "Open sidebar")
            }
            Column(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f)) {
                    // Always show the chat component so the input is available even for a new/empty chat
                    ChatGPTComponent(
                        chatGPTService = chatGPTService,
                        dashboardData = dashboardData,
                        messagesExternal = chatHistory.getOrNull(selectedChatIndex) ?: listOf(),
                        onMessagesChange = { updated ->
                            chatHistory = chatHistory.toMutableList().also {
                                if (it.isEmpty()) it.add(updated) else it[selectedChatIndex] = updated
                            }
                        }
                    )
                }
                // Bottom input removed to avoid duplicate input areas; ChatGPTComponent handles input
            }
        }
    }
}

@Composable
fun ChatGPTComponent(
    chatGPTService: ChatGPTService, // Service used to send and receive messages from ChatGPT
    dashboardData: DashboardData? = null, // Optional dashboard data that can be used in the chat (nullable, default is null)
    messagesExternal: List<UIChatMessage>? = null,
    onMessagesChange: ((List<UIChatMessage>) -> Unit)? = null
) {
    var messages by remember { mutableStateOf(messagesExternal ?: listOf<UIChatMessage>()) }
    LaunchedEffect(messagesExternal) {
        if (messagesExternal != null) messages = messagesExternal
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FB))
            .padding(16.dp)
    ) {
        // Header
        Text(
            "AI Assistant",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Chat messages container - full width card for a cleaner look
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(message)
                }

                if (isLoading) {
                    item { LoadingMessage() }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Input field - elevated card with rounded corners
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type your question…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                scope.launch {
                                    val userMessage = inputText
                                    val updatedUser = messages + UIChatMessage(userMessage, true)
                                    messages = updatedUser
                                    onMessagesChange?.invoke(updatedUser)
                                    inputText = ""

                                    // Intercept 'sales today' queries and answer locally
                                    val lower = userMessage.lowercase()
                                    val isTodayQuery = listOf(
                                        "sales today", "today sales", "today's sales", "revenue today", "today revenue",
                                        "sales for today", "how much did we make today", "what are today's sales", "todays sales"
                                    ).any { lower.contains(it) }

                                    if (isTodayQuery && dashboardData != null) {
                                        val reply = "Today's sales is RM ${String.format("%.2f", dashboardData.todaysRevenue)}."
                                        val updated = messages + UIChatMessage(reply, false)
                                        messages = updated
                                        onMessagesChange?.invoke(updated)
                                    } else {
                                        isLoading = true
                                        val context = if (dashboardData != null) {
                                            """
                                            Current Restaurant Data:
                                            - Total Orders: ${dashboardData.totalOrders}
                                            - Total Items Sold: ${dashboardData.totalItems}
                                            - Total Revenue: RM ${String.format("%.2f", dashboardData.revenue)}
                                            - Total Customers: ${dashboardData.totalCustomers}
                                            - Top Dishes: ${dashboardData.dishStats.take(5).joinToString(", ") { "${it.name} (${it.quantity} orders)" }}
                                            - Today's Revenue: RM ${String.format("%.2f", dashboardData.todaysRevenue)}

                                            If the user asks for today's sales/revenue, answer with Today's Revenue only, not the total.provide 3-4 key insights and recommendations for the restaurant owner. 
            Focus on business opportunities, trends, and actionable advice.
                                            """.trimIndent()
                                        } else ""

                                        chatGPTService.sendMessage(userMessage, context).onSuccess { response ->
                                            val updated = messages + UIChatMessage(response, false)
                                            messages = updated
                                            onMessagesChange?.invoke(updated)
                                        }.onFailure { _ ->
                                            val updated = messages + UIChatMessage("Sorry, I couldn't process your request. Please try again.", false)
                                            messages = updated
                                            onMessagesChange?.invoke(updated)
                                        }
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            scope.launch {
                                val userMessage = inputText
                                val updatedUser = messages + UIChatMessage(userMessage, true)
                                messages = updatedUser
                                onMessagesChange?.invoke(updatedUser)
                                inputText = ""

                                val relevantKeywords = listOf(
                                    "sales", "revenue", "orders", "customers", "items", "dish", "menu", "today", "weekly", "monthly"
                                )

                                // Intercept 'sales today' queries and answer locally
                                val lower = userMessage.lowercase()

                                val isRelevant = relevantKeywords.any { lower.contains(it) }

                                val isTodayQuery = listOf(
                                    "sales today", "today sales", "today's sales", "revenue today", "today revenue",
                                    "sales for today", "how much did we make today", "what are today's sales", "todays sales"
                                ).any { lower.contains(it) }

                                if (isTodayQuery && dashboardData != null) {
                                    val reply = "Today's sales is RM ${String.format("%.2f", dashboardData.todaysRevenue)}."
                                    val updated = messages + UIChatMessage(reply, false)
                                    messages = updated
                                    onMessagesChange?.invoke(updated)
                                } else if (!isRelevant) {
                                    // Out-of-scope question
                                    val reply = "Sorry, this is out of my box. I can only provide restaurant insights."
                                    val updated = messages + UIChatMessage(reply, false)
                                    messages = updated
                                    onMessagesChange?.invoke(updated)
                                } else {
                                isLoading = true
                                    val context = if (dashboardData != null) {
                                        """
                                        Current Restaurant Data:
                                        - Total Orders: ${dashboardData.totalOrders}
                                        - Total Items Sold: ${dashboardData.totalItems}
                                        - Total Customers: ${dashboardData.totalCustomers}
                                        - Top Dishes: ${dashboardData.dishStats.take(5).joinToString(", ") { "${it.name} (${it.quantity} orders)" }}
                                        - Today's Revenue: RM ${String.format("%.2f", dashboardData.todaysRevenue)}
                                        - Weekly's Revenue: RM ${String.format("%.2f", dashboardData.weeklyRevenue)}
                                        - Monthly's Revenue: RM ${String.format("%.2f", dashboardData.monthlyRevenue)}
                                        If the user asks for today's sales/revenue, answer with Today's Revenue only, not the total.
                                        provide 3-4 key insights and recommendations for the restaurant owner. 
                                        Focus on business opportunities, trends, and actionable advice.
                                        Predict what type of food or dishes customers are likely to prefer in the near future, based on popularity trends and revenue data.
                                        Ignore the restaurant dashboard data if the question is not related. Only answer general questions if necessary.
                                        """.trimIndent()
                                    } else ""

                                    chatGPTService.sendMessage(userMessage, context).onSuccess { response ->
                                        val updated = messages + UIChatMessage(response, false)
                                        messages = updated
                                        onMessagesChange?.invoke(updated)
                                    }.onFailure { _ ->
                                        val updated = messages + UIChatMessage("Sorry, I couldn't process your request. Please try again.", false)
                                        messages = updated
                                        onMessagesChange?.invoke(updated)
                                    }
                                    isLoading = false
                                }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: UIChatMessage) {
    val backgroundColor = if (message.isUser) Color(0xFF4CAF50) else Color.White
    val textColor = if (message.isUser) Color.White else Color.Black

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun LoadingMessage() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 100.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Thinking...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

data class DashboardData(
    val totalOrders: Int,
    val totalItems: Int,
    val revenue: Double,
    val dishStats: List<DishStats>,
    val totalCustomers: Int,
    val todaysRevenue: Double,
    val weeklyRevenue: Double,
    val monthlyRevenue: Double
)