package ru.assistant.aicwl.chat.data.anthropic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Anthropic-specific request and response models.
 * These mirror the Anthropic Claude API structure.
 */

@Serializable
data class AnthropicMessageRequest(
    val model: String,
    val messages: List<AnthropicMessage>,
    @SerialName("max_tokens")
    val maxTokens: Int,
    @SerialName("system")
    val systemPrompt: String? = null,
    @SerialName("temperature")
    val temperature: Float? = null,
    @SerialName("top_p")
    val topP: Float? = null,
    @SerialName("stream")
    val stream: Boolean = false
)

@Serializable
data class AnthropicMessage(
    val role: String,
    val content: String
)

@Serializable
data class AnthropicMessageResponse(
    val id: String? = null,
    val type: String? = null,
    val role: String? = null,
    val content: List<AnthropicContentBlock>? = null,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    val model: String? = null,
    val usage: AnthropicUsage? = null
)

@Serializable
data class AnthropicContentBlock(
    val type: String,
    val text: String? = null
)

@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens")
    val inputTokens: Int? = null,
    @SerialName("output_tokens")
    val outputTokens: Int? = null,
    @SerialName("total_tokens")
    val totalTokens: Int? = null
)
