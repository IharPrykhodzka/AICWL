package ru.assistant.aicwl.chat.data.unified

import kotlinx.serialization.Serializable
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Unified chat completion response from any AI provider.
 * Normalizes provider-specific response formats into a consistent structure.
 *
 * @property content Generated text content
 * @property role Message role (typically "assistant")
 * @property thinkingContent Optional reasoning/chain-of-thought content
 * @property usage Token usage information
 * @property modelUsed The model that generated the response
 * @property finishReason Why the generation stopped (length, stop, etc.)
 * @property rawResponse Raw provider response for debugging
 */
@Serializable
data class UnifiedChatResponse(
    val content: String,
    val role: String = "assistant",
    val thinkingContent: String? = null,
    val usage: TokenUsage? = null,
    val modelUsed: String,
    val finishReason: String? = null,
    val providerType: ProviderType,
    val rawResponse: String? = null
)

/**
 * Token usage information across providers.
 * Different providers report different metrics, so all fields are optional.
 */
@Serializable
data class TokenUsage(
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
)

/**
 * Result wrapper for AI responses with error handling.
 */
sealed class AIResponseResult {
    data class Success(val response: UnifiedChatResponse) : AIResponseResult()
    data class Error(
        val message: String,
        val errorCode: String? = null,
        val isRetryable: Boolean = false
    ) : AIResponseResult()
}
