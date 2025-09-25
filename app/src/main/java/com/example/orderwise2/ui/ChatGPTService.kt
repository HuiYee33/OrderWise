package com.example.orderwise2.ui

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import android.util.Log
import androidx.compose.ui.graphics.Color

// Data classes for ChatGPT API
data class ChatMessage( //create it before message send to ChatGPT
    val role: String, // user or system sent message
    val content: String
)

data class ChatRequest( //Send this message to ChatGPT
    val model: String = ChatGPTConfig.MODEL,
    val messages: List<ChatMessage>, // The list of messages in the conversation (user and assistant messages)
    val max_tokens: Int = ChatGPTConfig.MAX_TOKENS,  // Maximum number of tokens (length) allowed in the AI's response
    val temperature: Double = ChatGPTConfig.TEMPERATURE  // Controls creativity of the AI response (0.0 = focused, 1.0 = random)
)

data class ChatChoice( // store how many output generate by ChatGPT
    val index: Int,
    val message: ChatMessage,
    @SerializedName("finish_reason") //connect finishReason (JSON) to finish_reason (KotLin), if don't have this, GSON would look for a field name call finishReason
    val finishReason: String //finish_reason is to check whether the reply is complete or incomplete
)

data class ChatResponse( //ChatGPT respond answer
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoice>,
    val usage: Usage
)

data class Usage(
    @SerializedName("prompt_tokens") //how many token already use (ask question)
    val promptTokens: Int,
    @SerializedName("completion_tokens") //how many token used by ChatGPT in its reply
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)



// Retrofit( talk to web APIs easily, also handle JSON conversion) service interface
interface ChatGPTApiService { // Define a Retrofit interface to communicate with the ChatGPT API (like a rule that retrofit have to flw to send or receive message)
    @POST("chat/completions") //make a post request to this URL (Endpoint)
    suspend fun sendMessage( //suspend function that sends the request and waits for a reply from ChatGPT
        @Header("Authorization") authorization: String, //send API key securely in the request to make sure only allowed use can use.
        @Body request: ChatRequest //Send the user's message and settings to ChatGPT in JSON format
    ): Response<ChatResponse> //Receives ChatGPT’s reply in a clean Kotlin format
}

// ChatGPT Service class
class ChatGPTService {
    private val apiKey = ChatGPTConfig.OPENAI_API_KEY //store api key where it gets the key from a configuration file.

    private val loggingInterceptor = HttpLoggingInterceptor().apply { //for debugging (see details of HTTP request and respond
        level = HttpLoggingInterceptor.Level.BODY //level.body = logs full content of request/respond
    }

    //Add logging (Sets how long it will wait before timing out)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(ChatGPTConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(ChatGPTConfig.READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(ChatGPTConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(ChatGPTConfig.BASE_URL)
        .client(okHttpClient) //Use OkHttp to send requests, okHttpClient handle network rules like timeout, logging, internet connection
        .addConverterFactory(GsonConverterFactory.create()) //Convert JSON to Kotlin objects (convert api data using GSON)
        .build() //Finalize the setup (ready to be used to send API requests)
    
    private val apiService = retrofit.create(ChatGPTApiService::class.java) //Connect to the API Interface

    //Send message to chatgpt
    suspend fun sendMessage(message: String, context: String = ""): Result<String> {
        return try {
            val fullMessage = if (context.isNotEmpty()) {
                "Context: $context\n\nUser: $message"
            } else {
                message
            }

            //create chat request
            val chatRequest = ChatRequest(
                model = ChatGPTConfig.MODEL,
                messages = listOf(
                    ChatMessage("system", ChatGPTConfig.SYSTEM_PROMPT),
                    ChatMessage("user", fullMessage)
                ),
                max_tokens = ChatGPTConfig.MAX_TOKENS, //control how long and creative the answer is
                temperature = ChatGPTConfig.TEMPERATURE
            )

            //call api
            val response = apiService.sendMessage("Bearer $apiKey", chatRequest)
            
            if (response.isSuccessful) {
                val chatResponse = response.body()
                if (chatResponse?.choices?.isNotEmpty() == true) {
                    Result.success(chatResponse.choices[0].message.content) //returns the reply text from the first choice
                } else {
                    Log.e("ChatGPTService", "No response from ChatGPT: $chatResponse")
                    Result.failure(Exception("No response from ChatGPT"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ChatGPTService", "API call failed: ${response.code()} - $errorBody")
                Result.failure(Exception("API call failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatGPTService", "Exception in sendMessage: ", e)
            Result.failure(e)
        }
    }
    
    // Helper method to generate insights from dashboard data
    suspend fun generateDashboardInsights(
        totalOrders: Int,
        totalItems: Int,
        totalCustomers: Int,
        revenue: Double,
        dishStats: List<DishStats>

    ): Result<String> {
        val context = """
            Restaurant Dashboard Data:
            - Total Orders: $totalOrders
            - Total Items Sold: $totalItems
            - Total Customers: $totalCustomers
            - Total Revenue: RM ${String.format("%.2f", revenue)}
            
            - Top Dishes: ${dishStats.take(5).joinToString(", ") { "${it.name} (${it.quantity} orders)" }}
        """.trimIndent()
        
        val prompt = """
            Based on this restaurant dashboard data, provide 3-4 key insights and recommendations for the restaurant owner. 
            Focus on business opportunities, trends, and actionable advice.
        """.trimIndent()
        
        return sendMessage(prompt, context)
    }
    

    
    // Helper method for general restaurant management advice
    suspend fun getRestaurantAdvice(topic: String): Result<String> {
        val prompt = "Provide helpful advice about: $topic for a restaurant owner. Keep it concise and actionable."
        return sendMessage(prompt)
    }
    

    
    // Test function to verify API key
    suspend fun testApiKey(): Result<String> {
        Log.d("ChatGPTService", "Testing API key: ${apiKey.take(10)}...")
        return try {
            val testRequest = ChatRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage("user", "Hello, this is a test message.")
                ),
                max_tokens = 50,
                temperature = 0.7
            )
            
            val response = apiService.sendMessage("Bearer $apiKey", testRequest)
            
            if (response.isSuccessful) {
                val chatResponse = response.body()
                if (chatResponse?.choices?.isNotEmpty() == true) {
                    Log.d("ChatGPTService", "API key test successful!")
                    Result.success("API key is working: ${chatResponse.choices[0].message.content}")
                } else {
                    Log.e("ChatGPTService", "Test failed - no response body")
                    Result.failure(Exception("Test failed - no response body"))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                Log.e("ChatGPTService", "Test failed: ${response.code()} - $errorBody")
                Result.failure(Exception("Test failed: ${response.code()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("ChatGPTService", "Test exception: ", e)
            Result.failure(e)
        }
    }
} 