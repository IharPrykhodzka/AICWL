package ru.assistant.aicwl.chat.provider.openai

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
import ru.assistant.aicwl.chat.data.openai.OpenAIRequestMapper
import ru.assistant.aicwl.chat.data.openai.OpenAIResponseMapper
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.AIProvider
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * OpenAI provider implementation.
 * Handles communication with OpenAI's GPT model API.
 *
 * API Documentation: https://platform.openai.com/docs/api-reference
 */
class OpenAIProvider(
    private val apiKey: String,
    private val endpoint: String = "https://api.openai.com/v1/chat/completions"
) : AIProvider {

    private val logger = createLogger("OpenAIProvider")

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

    override fun getProviderType(): ProviderType = ProviderType.OPENAI

    override fun getProviderName(): String = "OpenAI"

    override fun getAvailableModels(): List<UnifiedAIModel> {
        return AIModelConfig.getModelsByProvider(ProviderType.OPENAI)
    }

    override fun getDefaultModel(): UnifiedAIModel {
        return AIModelConfig.getDefaultModelForProvider(ProviderType.OPENAI)
    }

    override fun isConfigured(): Boolean {
        return apiKey.isNotBlank() && apiKey != "YOUR_OPENAI_API_KEY_HERE"
    }

    override suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse> {
        logger.i("Sending request to OpenAI. Model: ${request.modelId}")

        return try {
            // Convert unified request to OpenAI format
            val openaiRequest = OpenAIRequestMapper.toOpenAIRequest(request)

            logger.d("OpenAI Request: ${openaiRequest.model}, messages: ${openaiRequest.messages.size}")

            // Make HTTP request
            val response: HttpResponse = client.post(endpoint) {
                header("Content-Type", "application/json")
                header("Authorization", "Bearer $apiKey") // OpenAI uses "Bearer" prefix
                header("OpenAI-Organization", "") // Optional: add org ID if needed
                setBody(openaiRequest)
            }

            val rawBody: String = response.body<String>()

            // Check for HTTP errors
            if (!response.status.isSuccess()) {
                logger.e("OpenAI HTTP Error: ${response.status.value}")
                return handleOpenAIError(response.status.value, rawBody)
            }

            // Parse OpenAI response
            val openaiResponse = OpenAIResponseMapper.parseResponse(rawBody)

            // Convert to unified format
            val unifiedResponse = OpenAIResponseMapper.toUnifiedResponse(
                openaiResponse = openaiResponse,
                modelUsed = request.modelId,
                rawResponse = rawBody
            )

            logger.i("OpenAI response received. Content length: ${unifiedResponse.content.length}")
            Result.success(unifiedResponse)

        } catch (e: Exception) {
            logger.e("OpenAI request failed", e)
            Result.failure(e)
        }
    }

    override fun getEndpointInfo(): String {
        return "OpenAI API: ${endpoint.replace("https://", "")}"
    }

    private fun handleOpenAIError(statusCode: Int, rawBody: String): Result<UnifiedChatResponse> {
        return try {
            val errorJson = json.parseToJsonElement(rawBody)
            val errorMsg = errorJson.jsonObject?.get("error")?.jsonObject
                ?.get("message")?.jsonPrimitive?.content ?: "HTTP $statusCode"
            Result.failure(Exception("OpenAI Error: $errorMsg"))
        } catch (e: Exception) {
            Result.failure(Exception("OpenAI HTTP $statusCode: ${rawBody.take(200)}"))
        }
    }

    /**
     * Close the HTTP client.
     */
    override fun close() {
        logger.d("Closing OpenAI provider HTTP client")
        client.close()
    }
}
