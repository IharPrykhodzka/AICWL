package ru.assistant.aicwl.chat.data.qwen

import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.data.ChatCompletionResponse
import ru.assistant.aicwl.chat.data.unified.TokenUsage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Mapper for converting Qwen responses to unified format.
 * This isolates Qwen-specific response handling.
 */
object QwenResponseMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Parse raw Qwen response string.
     */
    fun parseResponse(rawBody: String): ChatCompletionResponse {
        return json.decodeFromString(ChatCompletionResponse.serializer(), rawBody)
    }

    /**
     * Convert Qwen response to unified format.
     */
    fun toUnifiedResponse(
        qwenResponse: ChatCompletionResponse,
        modelUsed: String,
        rawResponse: String
    ): UnifiedChatResponse {
        val choice = qwenResponse.choices?.firstOrNull()
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
            usage = qwenResponse.usage?.let {
                TokenUsage(
                    promptTokens = it.promptTokens,
                    completionTokens = it.completionTokens,
                    totalTokens = it.totalTokens
                )
            },
            modelUsed = modelUsed,
            finishReason = choice?.finishReason,
            providerType = ProviderType.QWEN,
            rawResponse = rawResponse
        )
    }
}
