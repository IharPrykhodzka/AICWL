package ru.assistant.aicwl.chat.data.zai

import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.data.ChatCompletionResponse
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.data.unified.TokenUsage
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Mapper for converting Z.ai responses to unified format.
 * This isolates Z.ai-specific response handling.
 */
object ZAIResponseMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse raw Z.ai response string.
     */
    fun parseResponse(rawBody: String): ChatCompletionResponse {
        return json.decodeFromString(ChatCompletionResponse.serializer(), rawBody)
    }

    /**
     * Convert Z.ai response to unified format.
     */
    fun toUnifiedResponse(
        zaiResponse: ChatCompletionResponse,
        modelUsed: String,
        rawResponse: String
    ): UnifiedChatResponse {
        val choice = zaiResponse.choices?.firstOrNull()
        val message = choice?.message

        val content = message?.content ?: ""
        val reasoningContent = message?.reasoning_content

        // Use reasoning_content if content is empty
        val actualContent = when {
            !content.isNullOrBlank() -> content
            !reasoningContent.isNullOrBlank() -> reasoningContent
            else -> "No response from model"
        }

        return UnifiedChatResponse(
            content = actualContent,
            role = message?.role ?: "assistant",
            thinkingContent = reasoningContent,
            usage = zaiResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            },
            modelUsed = modelUsed,
            finishReason = choice?.finishReason,
            providerType = ProviderType.ZAI,
            rawResponse = rawResponse
        )
    }
}
