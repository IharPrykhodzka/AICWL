package ru.assistant.aicwl.chat.data.anthropic

import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.data.unified.TokenUsage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Mapper for converting Anthropic responses to unified format.
 */
object AnthropicResponseMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse raw Anthropic response string.
     */
    fun parseResponse(rawBody: String): AnthropicMessageResponse {
        return json.decodeFromString(AnthropicMessageResponse.serializer(), rawBody)
    }

    /**
     * Convert Anthropic response to unified format.
     *
     * Note: Anthropic returns content as blocks, we need to extract text.
     */
    fun toUnifiedResponse(
        anthropicResponse: AnthropicMessageResponse,
        modelUsed: String,
        rawResponse: String
    ): UnifiedChatResponse {
        // Extract text from content blocks
        val content = anthropicResponse.content
            ?.firstOrNull { it.type == "text" }
            ?.text ?: "No response from model"

        return UnifiedChatResponse(
            content = content,
            role = anthropicResponse.role ?: "assistant",
            thinkingContent = null, // Anthropic doesn't expose thinking in current API
            usage = anthropicResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.inputTokens,
                    completionTokens = it.outputTokens,
                    totalTokens = it.totalTokens
                )
            },
            modelUsed = modelUsed,
            finishReason = anthropicResponse.stopReason,
            providerType = ProviderType.ANTHROPIC,
            rawResponse = rawResponse
        )
    }
}
