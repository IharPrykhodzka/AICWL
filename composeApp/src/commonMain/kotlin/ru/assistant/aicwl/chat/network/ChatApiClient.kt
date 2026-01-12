package ru.assistant.aicwl.chat.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.config.AppConfig
import ru.assistant.aicwl.chat.data.ChatCompletionRequest
import ru.assistant.aicwl.chat.data.ChatCompletionResponse
import ru.assistant.aicwl.chat.data.ChatMessage
import ru.assistant.aicwl.chat.data.ChatApiErrorResponse
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * HTTP Client for communicating with Z.AI Chat API.
 * Handles authentication and JSON serialization.
 */
class ChatApiClient {
    private val logger = createLogger("ChatApiClient")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val client = HttpClient {
        // HttpTimeout plugin with increased timeouts for slow AI models
        install(HttpTimeout) {
            // Timeout for establishing connection
            connectTimeoutMillis = 120_000  // 2 minutes

            // Total timeout for the entire request (waiting for response)
            requestTimeoutMillis = 300_000   // 5 minutes

            // Socket timeout - time between received data packets
            socketTimeoutMillis = 300_000    // 5 minutes
        }

        // Content negotiation for JSON
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Send a chat completion request with specified model.
     *
     * @param modelId The model identifier (e.g., "glm-4.7", "glm-4.6", "glm-4.5-air")
     * @param messages List of chat messages in the conversation
     * @return Result containing ChatCompletionResponse or error
     */
    suspend fun sendChatRequest(
        modelId: String,
        messages: List<ChatMessage>
    ): Result<ChatCompletionResponse> {
        logger.i("Sending request to model: $modelId, messages count: ${messages.size}")
        logger.d("Endpoint: ${AppConfig.zApiEndpoint}")

        val request = ChatCompletionRequest(
            model = modelId,
            messages = messages
        )

        // Debug: log request JSON
        val requestString = json.encodeToString(ChatCompletionRequest.serializer(), request)
        logger.d("Request JSON: $requestString")

        return try {
            val response: HttpResponse = client.post(AppConfig.zApiEndpoint) {
                // Key headers for Z.AI API
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("Accept-Language", "en-US,en")
                header("Authorization", AppConfig.zApiKey)  // WITHOUT "Bearer " prefix!
                header("User-Agent", "AICWL/1.0")

                logger.d("Request headers: Authorization=${maskApiKey(AppConfig.zApiKey)}")

                setBody(request)
            }

            // Get raw response body for logging
            val rawBody: String = response.bodyAsText()
            logger.d("Raw response (${response.status}): ${rawBody.take(500)}...")

            // Check HTTP status
            if (!response.status.isSuccess()) {
                logger.e("HTTP Error: ${response.status.value} - ${rawBody}")

                // Try to parse error response
                try {
                    val errorResponse = json.decodeFromString(ChatApiErrorResponse.serializer(), rawBody)
                    val errorMsg = errorResponse.error?.message ?: "HTTP ${response.status.value}"
                    return Result.failure(Exception("API Error: ${errorResponse.error?.code ?: response.status.value} - $errorMsg"))
                } catch (e: Exception) {
                    return Result.failure(Exception("HTTP ${response.status.value}: ${rawBody.take(200)}"))
                }
            }

            // Parse successful response
            val parsedResponse = json.decodeFromString(ChatCompletionResponse.serializer(), rawBody)

            // Check if choices exist
            if (parsedResponse.choices.isNullOrEmpty()) {
                logger.e("Response has no choices field. Raw: $rawBody")
                return Result.failure(Exception("Invalid API response: 'choices' field is missing or empty"))
            }

            logger.i("Response received. Choices count: ${parsedResponse.choices.size}")
            parsedResponse.choices.forEachIndexed { index, choice ->
                logger.d("Choice[$index]: role=${choice.message.role}, content length=${choice.message.content.length}")
            }

            Result.success(parsedResponse)
        } catch (e: Exception) {
            logger.e("Request failed for model: $modelId", e)
            logger.e("Exception type: ${e::class.simpleName}, Message: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Send a simple user message and get the response.
     *
     * @param modelId The model identifier
     * @param userMessage The user's message text
     * @return Result containing ChatCompletionResponse or error
     */
    suspend fun sendUserMessage(
        modelId: String,
        userMessage: String
    ): Result<ChatCompletionResponse> {
        logger.i("Sending user message. Model: $modelId, Message length: ${userMessage.length}")
        logger.d("Message preview: ${userMessage.take(100)}...")

        val messages = listOf(
            ChatMessage(role = "user", content = userMessage)
        )
        return sendChatRequest(modelId, messages)
    }

    /**
     * Close the client when done.
     */
    fun close() {
        logger.i("Closing HTTP client")
        client.close()
    }

    /**
     * Mask API key for logging (show only first 8 and last 4 characters).
     */
    private fun maskApiKey(key: String): String {
        return if (key.length > 12) {
            "${key.take(8)}...${key.takeLast(4)}"
        } else {
            "***"
        }
    }
}

/**
 * Singleton instance of the API client.
 */
val chatApiClient = ChatApiClient()
