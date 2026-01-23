package ru.assistant.aicwl.chat.data.unified

import kotlinx.serialization.Serializable
import ru.assistant.aicwl.chat.data.ChatRequestParameters
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Unified chat completion request that works across all AI providers.
 * This abstraction layer decouples the application from provider-specific APIs.
 *
 * @property providerType Target AI provider
 * @property modelId Model identifier (provider-specific)
 * @property messages Conversation history including system prompt
 * @property parameters Generation parameters (temperature, maxTokens, etc.)
 * @property stream Whether to stream responses (not all providers support this)
 */
@Serializable
data class UnifiedChatRequest(
    val providerType: ProviderType,
    val modelId: String,
    val messages: List<UnifiedChatMessage>,
    val parameters: ChatRequestParameters? = null,
    val stream: Boolean = false
)
