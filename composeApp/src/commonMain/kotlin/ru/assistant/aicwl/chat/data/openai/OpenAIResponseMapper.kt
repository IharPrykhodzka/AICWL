package ru.assistant.aicwl.chat.data.openai

import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.data.unified.TokenUsage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Mapper for converting OpenAI responses to unified format.
 */
object OpenAIResponseMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse raw OpenAI response string.
     */
    fun parseResponse(rawBody: String): OpenAIChatResponse {
        return json.decodeFromString(OpenAIChatResponse.serializer(), rawBody)
    }

    /**
     * Convert OpenAI response to unified format.
     */
    fun toUnifiedResponse(
        openaiResponse: OpenAIChatResponse,
        modelUsed: String,
        rawResponse: String
    ): UnifiedChatResponse {
        val choice = openaiResponse.choices?.firstOrNull()
        val message = choice?.message

        val content = message?.content ?: "No response from model"

        return UnifiedChatResponse(
            content = content,
            role = message?.role ?: "assistant",
            thinkingContent = null, // OpenAI doesn't expose reasoning in standard API
            usage = openaiResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            },
            modelUsed = modelUsed,
            finishReason = choice?.finishReason,
            providerType = ProviderType.OPENAI,
            rawResponse = rawResponse
        )
    }
}
