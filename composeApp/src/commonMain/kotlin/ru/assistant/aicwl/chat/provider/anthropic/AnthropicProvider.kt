package ru.assistant.aicwl.chat.provider.anthropic

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.assistant.aicwl.chat.data.anthropic.AnthropicRequestMapper
import ru.assistant.aicwl.chat.data.anthropic.AnthropicResponseMapper
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.AIProvider
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Anthropic provider implementation.
 * Handles communication with Anthropic's Claude model API.
 *
 * API Documentation: https://docs.anthropic.com/claude/reference/messages_post
 */
class AnthropicProvider(
    private val apiKey: String,
    private val endpoint: String = "https://api.anthropic.com/v1/messages"
) : AIProvider {

    private val logger = createLogger("AnthropicProvider")

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

    override fun getProviderType(): ProviderType = ProviderType.ANTHROPIC

    override fun getProviderName(): String = "Anthropic"

    override fun getAvailableModels(): List<UnifiedAIModel> {
        return AIModelConfig.getModelsByProvider(ProviderType.ANTHROPIC)
    }

    override fun getDefaultModel(): UnifiedAIModel {
        return AIModelConfig.getDefaultModelForProvider(ProviderType.ANTHROPIC)
    }

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_ANTHROPIC_API_KEY_HERE"
    }

    override suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse> {
        logger.i("Sending request to Anthropic. Model: ${request.modelId}")

        return try {
            // Convert unified request to Anthropic format
            val anthropicRequest = AnthropicRequestMapper.toAnthropicRequest(request)

            logger.d("Anthropic Request: ${anthropicRequest.model}, messages: ${anthropicRequest.messages.size}")

            // Make HTTP request
            val response: HttpResponse = client.post(endpoint) {
                header("Content-Type", "application/json")
                header("x-api-key", apiKey) // Anthropic uses custom header
                header("anthropic-version", "2023-06-01")
                setBody(anthropicRequest)
            }

            val rawBody: String = response.body<String>()

            // Check for HTTP errors
            if (!response.status.isSuccess()) {
                logger.e("Anthropic HTTP Error: ${response.status.value}")
                return handleAnthropicError(response.status.value, rawBody)
            }

            // Parse Anthropic response
            val anthropicResponse = AnthropicResponseMapper.parseResponse(rawBody)

            // Convert to unified format
            val unifiedResponse = AnthropicResponseMapper.toUnifiedResponse(
                anthropicResponse = anthropicResponse,
                modelUsed = request.modelId,
                rawResponse = rawBody
            )

            logger.i("Anthropic response received. Content length: ${unifiedResponse.content.length}")
            Result.success(unifiedResponse)

        } catch (e: Exception) {
            logger.e("Anthropic request failed", e)
            Result.failure(e)
        }
    }

    override fun getEndpointInfo(): String {
        return "Anthropic API: ${endpoint.replace("https://", "")}"
    }

    private fun handleAnthropicError(statusCode: Int, rawBody: String): Result<UnifiedChatResponse> {
        return try {
            val errorJson = json.parseToJsonElement(rawBody)
            val errorMsg = errorJson.jsonObject?.get("error")?.jsonObject
                ?.get("message")?.jsonPrimitive?.content ?: "HTTP $statusCode"
            Result.failure(Exception("Anthropic Error: $errorMsg"))
        } catch (e: Exception) {
            Result.failure(Exception("Anthropic HTTP $statusCode: ${rawBody.take(200)}"))
        }
    }

    /**
     * Close the HTTP client.
     */
    override fun close() {
        logger.d("Closing Anthropic provider HTTP client")
        client.close()
    }
}
