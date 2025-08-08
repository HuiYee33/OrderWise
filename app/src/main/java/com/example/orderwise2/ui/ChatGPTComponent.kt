package com.example.orderwise2.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
fun ChatGPTComponent(
    chatGPTService: ChatGPTService, // Service used to send and receive messages from ChatGPT
    dashboardData: DashboardData? = null // Optional dashboard data that can be used in the chat (nullable, default is null)
) {
    var messages by remember { mutableStateOf(listOf<UIChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            "AI Assistant",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message)
            }
            
            if (isLoading) {
                item {
                    LoadingMessage()
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick action buttons
        if (dashboardData != null) {
            QuickActionButtons(
                onGenerateInsights = {
                    scope.launch {
                        isLoading = true
                        chatGPTService.generateDashboardInsights( //ask GPT to give insight based on below criteria
                            dashboardData.totalOrders,
                            dashboardData.totalItems,
                            dashboardData.revenue,
                            dashboardData.dishStats
                        ).onSuccess { response -> // If ChatGPT gives a result, add it to the chat.
                            messages = messages + UIChatMessage(response, false)
                        }.onFailure { error ->
                            messages = messages + UIChatMessage("Sorry, I couldn't generate insights. Please try again.", false)
                        }
                        isLoading = false
                    }
                },
                onGetAdvice = { topic ->
                    scope.launch {
                        isLoading = true
                        chatGPTService.getRestaurantAdvice(topic                        ).onSuccess { response ->
                            messages = messages + UIChatMessage(response, false)
                        }.onFailure { error ->
                            messages = messages + UIChatMessage("Sorry, I couldn't get advice on that topic. Please try again.", false)
                        }
                        isLoading = false
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Test API Key button
        OutlinedButton(
            onClick = {
                scope.launch {
                    isLoading = true
                    chatGPTService.testApiKey().onSuccess { response ->
                        messages = messages + UIChatMessage("✅ $response", false)
                    }.onFailure { error ->
                        messages = messages + UIChatMessage("❌ API Test Failed: ${error.message}", false)
                    }
                    isLoading = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Test API Key", fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Input field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask me anything about restaurant management...") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (inputText.isNotBlank()) {
                            scope.launch {
                                val userMessage = inputText
                                messages = messages + UIChatMessage(userMessage, true)
                                inputText = ""
                                isLoading = true
                                
                                chatGPTService.sendMessage(userMessage).onSuccess { response ->
                                    messages = messages + UIChatMessage(response, false)
                                }.onFailure { error ->
                                    messages = messages + UIChatMessage("Sorry, I couldn't process your request. Please try again.", false)
                                }
                                isLoading = false
                            }
                        }
                    }
                )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        scope.launch {
                            val userMessage = inputText
                            messages = messages + UIChatMessage(userMessage, true)
                            inputText = ""
                            isLoading = true
                            
                            chatGPTService.sendMessage(userMessage).onSuccess { response ->
                                messages = messages + UIChatMessage(response, false)
                            }.onFailure { error ->
                                messages = messages + UIChatMessage("Sorry, I couldn't process your request. Please try again.", false)
                            }
                            isLoading = false
                        }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
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

@Composable
fun QuickActionButtons(
    onGenerateInsights: () -> Unit,
    onGetAdvice: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Quick Actions:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onGenerateInsights,
                modifier = Modifier.weight(1f)
            ) {
                Text("Generate Insights", fontSize = 12.sp)
            }
            
            OutlinedButton(
                onClick = { onGetAdvice(ChatGPTConfig.QUICK_ACTIONS["menu_optimization"] ?: "menu optimization") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Menu Advice", fontSize = 12.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onGetAdvice(ChatGPTConfig.QUICK_ACTIONS["customer_service"] ?: "customer service") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Customer Service", fontSize = 12.sp)
            }
            
            OutlinedButton(
                onClick = { onGetAdvice(ChatGPTConfig.QUICK_ACTIONS["cost_management"] ?: "cost management") },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cost Management", fontSize = 12.sp)
            }
        }
    }
}

data class DashboardData(
    val totalOrders: Int,
    val totalItems: Int,
    val revenue: Double,
    val dishStats: List<DishStats>
) 