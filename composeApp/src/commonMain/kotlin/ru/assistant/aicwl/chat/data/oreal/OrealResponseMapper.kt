package ru.assistant.aicwl.chat.data.oreal

import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.data.ChatCompletionResponse
import ru.assistant.aicwl.chat.data.unified.TokenUsage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Mapper for converting Oreal responses to unified format.
 * This isolates Oreal-specific response handling.
 */
object OrealResponseMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse raw Oreal response string.
     */
    fun parseResponse(rawBody: String): ChatCompletionResponse {
        return json.decodeFromString(ChatCompletionResponse.serializer(), rawBody)
    }

    /**
     * Convert Oreal response to unified format.
     */
    fun toUnifiedResponse(
        orealResponse: ChatCompletionResponse,
        modelUsed: String,
        rawResponse: String
    ): UnifiedChatResponse {
        val choice = orealResponse.choices?.firstOrNull()
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
            usage = orealResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            },
            modelUsed = modelUsed,
            finishReason = choice?.finishReason,
            providerType = ProviderType.OREAL,
            rawResponse = rawResponse
        )
    }
}
