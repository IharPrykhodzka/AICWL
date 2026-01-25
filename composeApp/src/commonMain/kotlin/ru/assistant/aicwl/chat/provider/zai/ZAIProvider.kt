package ru.assistant.aicwl.chat.provider.zai

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
import ru.assistant.aicwl.chat.data.ChatRequestParameters
import ru.assistant.aicwl.chat.data.ChatApiErrorResponse
import ru.assistant.aicwl.chat.data.zai.ZAIRequestMapper
import ru.assistant.aicwl.chat.data.zai.ZAIResponseMapper
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.AIProvider
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Z.ai (智谱AI) provider implementation.
 * Handles communication with Z.ai's GLM model API.
 *
 * API Documentation: https://api.z.ai
 */
class ZAIProvider(
    private val apiKey: String,
    private val endpoint: String
) : AIProvider {

    private val logger = createLogger("ZAIProvider")

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

    override fun getProviderType(): ProviderType = ProviderType.ZAI

    override fun getProviderName(): String = "Z.ai"

    override fun getAvailableModels(): List<UnifiedAIModel> {
        return AIModelConfig.getModelsByProvider(ProviderType.ZAI)
    }

    override fun getDefaultModel(): UnifiedAIModel {
        return AIModelConfig.getDefaultModelForProvider(ProviderType.ZAI)
    }

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_API_KEY_HERE"
    }

    override suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse> {
        logger.i("Sending request to Z.ai. Model: ${request.modelId}")

        return try {
            // Convert unified request to Z.ai format
            val zaiRequest = ZAIRequestMapper.toZAIRequest(request)

            // Log request (sanitized)
            logger.d("Z.ai Request: ${zaiRequest.model}, messages: ${zaiRequest.messages.size}")
            logger.d("Z.ai Parameters: temperature=${zaiRequest.temperature}, " +
                     "doSample=${zaiRequest.doSample}, maxTokens=${zaiRequest.maxTokens}, " +
                     "topP=${zaiRequest.topP}, thinking=${zaiRequest.thinking?.type}")

            // Make HTTP request
            val response: HttpResponse = client.post(endpoint) {
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("Authorization", apiKey) // No "Bearer" prefix for Z.ai
                header("User-Agent", "AICWL/1.0")
                setBody(zaiRequest)
            }

            val rawBody: String = response.body<String>()

            // Check for HTTP errors
            if (!response.status.isSuccess()) {
                logger.e("Z.ai HTTP Error: ${response.status.value}")
                return handleZAIError(response.status.value, rawBody)
            }

            // Parse Z.ai response
            val zaiResponse = ZAIResponseMapper.parseResponse(rawBody)

            // Log usage data for debugging
            if (zaiResponse.usage != null) {
                logger.i("Z.ai usage data: prompt=${zaiResponse.usage.promptTokens}, " +
                        "completion=${zaiResponse.usage.completionTokens}, " +
                        "total=${zaiResponse.usage.totalTokens}")
            } else {
                logger.w("Z.ai response does NOT contain usage data!")
            }

            // Convert to unified format
            val unifiedResponse = ZAIResponseMapper.toUnifiedResponse(
                zaiResponse = zaiResponse,
                modelUsed = request.modelId,
                rawResponse = rawBody
            )

            logger.i("Z.ai response received. Content length: ${unifiedResponse.content.length}")
            Result.success(unifiedResponse)

        } catch (e: Exception) {
            logger.e("Z.ai request failed", e)
            Result.failure(e)
        }
    }

    override fun getEndpointInfo(): String {
        return "Z.ai API: ${endpoint.replace("https://", "")}"
    }

    private fun handleZAIError(statusCode: Int, rawBody: String): Result<UnifiedChatResponse> {
        return try {
            val errorResponse = json.decodeFromString(ChatApiErrorResponse.serializer(), rawBody)
            val errorMsg = errorResponse.error?.message ?: "HTTP $statusCode"
            Result.failure(Exception("Z.ai Error: $errorMsg"))
        } catch (e: Exception) {
            Result.failure(Exception("Z.ai HTTP $statusCode: ${rawBody.take(200)}"))
        }
    }

    /**
     * Close the HTTP client.
     */
    override fun close() {
        logger.d("Closing Z.ai provider HTTP client")
        client.close()
    }
}
