package ru.assistant.aicwl.chat.provider.qwen

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
import ru.assistant.aicwl.chat.data.ChatApiErrorResponse
import ru.assistant.aicwl.chat.data.qwen.QwenRequestMapper
import ru.assistant.aicwl.chat.data.qwen.QwenResponseMapper
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.AIProvider
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Qwen provider implementation via HuggingFace.
 * Handles communication with Qwen's API through HuggingFace router.
 *
 * API Documentation: https://huggingface.co/docs/api-inference
 */
class QwenProvider(
    private val apiKey: String,
    private val endpoint: String
) : AIProvider {

    private val logger = createLogger("QwenProvider")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val client = HttpClient {
        install(HttpTimeout) {
            connectTimeoutMillis = 120_000
            requestTimeoutMillis = 300_000
            socketTimeoutMillis = 300_000
        }

        install(ContentNegotiation) {
            json(json)
        }
    }

    override fun getProviderType(): ProviderType = ProviderType.QWEN

    override fun getProviderName(): String = "Qwen"

    override fun getAvailableModels(): List<UnifiedAIModel> {
        return AIModelConfig.getModelsByProvider(ProviderType.QWEN)
    }

    override fun getDefaultModel(): UnifiedAIModel {
        return AIModelConfig.getDefaultModelForProvider(ProviderType.QWEN)
    }

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }

    override suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse> {
        logger.i("Sending request to Qwen. Model: ${request.modelId}")

        return try {
            // Convert unified request to Qwen format
            val qwenRequest = QwenRequestMapper.toQwenRequest(request)

            // Log request (sanitized)
            logger.d("Qwen Request: ${qwenRequest.model}, messages: ${qwenRequest.messages.size}")
            logger.d("Qwen Parameters: temperature=${qwenRequest.temperature}, " +
                     "maxTokens=${qwenRequest.maxTokens}, topP=${qwenRequest.topP}")

            // Make HTTP request
            val response: HttpResponse = client.post(endpoint) {
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("Authorization", "Bearer $apiKey")
                header("User-Agent", "AICWL/1.0")
                setBody(qwenRequest)
            }

            val rawBody: String = response.body<String>()

            // Check for HTTP errors
            if (!response.status.isSuccess()) {
                logger.e("Qwen HTTP Error: ${response.status.value}")
                return handleQwenError(response.status.value, rawBody)
            }

            // Parse Qwen response
            val qwenResponse = QwenResponseMapper.parseResponse(rawBody)

            // Convert to unified format
            val unifiedResponse = QwenResponseMapper.toUnifiedResponse(
                qwenResponse = qwenResponse,
                modelUsed = request.modelId,
                rawResponse = rawBody
            )

            logger.i("Qwen response received. Content length: ${unifiedResponse.content.length}")
            Result.success(unifiedResponse)

        } catch (e: Exception) {
            logger.e("Qwen request failed", e)
            Result.failure(e)
        }
    }

    override fun getEndpointInfo(): String {
        return "Qwen API (via HuggingFace): ${endpoint.replace("https://", "")}"
    }

    private fun handleQwenError(statusCode: Int, rawBody: String): Result<UnifiedChatResponse> {
        return try {
            val errorResponse = json.decodeFromString(ChatApiErrorResponse.serializer(), rawBody)
            val errorMsg = errorResponse.error?.message ?: "HTTP $statusCode"
            Result.failure(Exception("Qwen Error: $errorMsg"))
        } catch (e: Exception) {
            Result.failure(Exception("Qwen HTTP $statusCode: ${rawBody.take(200)}"))
        }
    }

    /**
     * Close the HTTP client.
     */
    override fun close() {
        logger.d("Closing Qwen provider HTTP client")
        client.close()
    }
}
