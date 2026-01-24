package ru.assistant.aicwl.chat.provider.oreal

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
import ru.assistant.aicwl.chat.data.oreal.OrealRequestMapper
import ru.assistant.aicwl.chat.data.oreal.OrealResponseMapper
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.AIProvider
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Oreal provider implementation via HuggingFace.
 * Handles communication with Oreal's API through HuggingFace router.
 *
 * API Documentation: https://huggingface.co/docs/api-inference
 */
class OrealProvider(
    private val apiKey: String,
    private val endpoint: String
) : AIProvider {

    private val logger = createLogger("OrealProvider")

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

    override fun getProviderType(): ProviderType = ProviderType.OREAL

    override fun getProviderName(): String = "Oreal"

    override fun getAvailableModels(): List<UnifiedAIModel> {
        return AIModelConfig.getModelsByProvider(ProviderType.OREAL)
    }

    override fun getDefaultModel(): UnifiedAIModel {
        return AIModelConfig.getDefaultModelForProvider(ProviderType.OREAL)
    }

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }

    override suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse> {
        logger.i("Sending request to Oreal. Model: ${request.modelId}")

        return try {
            // Convert unified request to Oreal format
            val orealRequest = OrealRequestMapper.toOrealRequest(request)

            // Log request (sanitized)
            logger.d("Oreal Request: ${orealRequest.model}, messages: ${orealRequest.messages.size}")
            logger.d("Oreal Parameters: maxTokens=${orealRequest.maxTokens}, stream=${orealRequest.stream}")

            // Make HTTP request
            val response: HttpResponse = client.post(endpoint) {
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("Authorization", "Bearer $apiKey")
                header("User-Agent", "AICWL/1.0")
                setBody(orealRequest)
            }

            val rawBody: String = response.body<String>()

            // Check for HTTP errors
            if (!response.status.isSuccess()) {
                logger.e("Oreal HTTP Error: ${response.status.value}")
                return handleOrealError(response.status.value, rawBody)
            }

            // Parse Oreal response
            val orealResponse = OrealResponseMapper.parseResponse(rawBody)

            // Convert to unified format
            val unifiedResponse = OrealResponseMapper.toUnifiedResponse(
                orealResponse = orealResponse,
                modelUsed = request.modelId,
                rawResponse = rawBody
            )

            logger.i("Oreal response received. Content length: ${unifiedResponse.content.length}")
            Result.success(unifiedResponse)

        } catch (e: Exception) {
            logger.e("Oreal request failed", e)
            Result.failure(e)
        }
    }

    override fun getEndpointInfo(): String {
        return "Oreal API (via HuggingFace): ${endpoint.replace("https://", "")}"
    }

    private fun handleOrealError(statusCode: Int, rawBody: String): Result<UnifiedChatResponse> {
        return try {
            val errorResponse = json.decodeFromString(ChatApiErrorResponse.serializer(), rawBody)
            val errorMsg = errorResponse.error?.message ?: "HTTP $statusCode"
            Result.failure(Exception("Oreal Error: $errorMsg"))
        } catch (e: Exception) {
            Result.failure(Exception("Oreal HTTP $statusCode: ${rawBody.take(200)}"))
        }
    }

    /**
     * Close the HTTP client.
     */
    override fun close() {
        logger.d("Closing Oreal provider HTTP client")
        client.close()
    }
}
