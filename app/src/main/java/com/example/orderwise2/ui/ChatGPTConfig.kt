package com.example.orderwise2.ui

/**
 * Configuration class for ChatGPT integration
 * 
 * IMPORTANT: Replace YOUR_OPENAI_API_KEY with your actual OpenAI API key
 * You can get one from: https://platform.openai.com/api-keys
 */
object ChatGPTConfig {
    // Replace this with your actual OpenAI API key
    const val OPENAI_API_KEY = ""
    
    // API Configuration
    const val BASE_URL = "https://api.openai.com/"
    const val MODEL = "gpt-3.5-turbo"
    const val MAX_TOKENS = 1000
    const val TEMPERATURE = 0.7
    
    // Timeout settings (in seconds)
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
    
    // System prompt for restaurant management context
    val SYSTEM_PROMPT = """
        You are a helpful AI assistant for a restaurant management system. 
        Provide concise, actionable advice for restaurant owners and managers.
        Focus on practical solutions, industry best practices, and data-driven insights.
        Keep responses professional, informative, and under 200 words unless specifically asked for more detail.
    """.trimIndent()
    
    // Quick action topics
    val QUICK_ACTIONS = mapOf(
        "menu_optimization" to "menu optimization and pricing strategies",
        "customer_service" to "customer service and satisfaction improvement",
        "cost_management" to "cost management and profitability optimization",
        "staff_training" to "staff training and management best practices",
        "marketing" to "restaurant marketing and promotion strategies",
        "inventory" to "inventory management and waste reduction",
        "technology" to "restaurant technology trends and implementation",
        "compliance" to "food safety and regulatory compliance"
    )
} 