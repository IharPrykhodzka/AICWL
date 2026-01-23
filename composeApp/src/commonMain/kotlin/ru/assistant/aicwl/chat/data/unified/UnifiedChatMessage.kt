package ru.assistant.aicwl.chat.data.unified

import kotlinx.serialization.Serializable

/**
 * Unified chat message format that can be converted to provider-specific formats.
 * This abstraction allows switching between AI providers without changing business logic.
 *
 * @property role Message role (system, user, assistant)
 * @property content Message text content
 * @property metadata Optional metadata (thinking content, images, etc.)
 */
@Serializable
data class UnifiedChatMessage(
    val role: MessageRole,
    val content: String,
    val metadata: MessageMetadata? = null
)

/**
 * Message roles following standard chat completion API conventions.
 */
@Serializable
enum class MessageRole {
    SYSTEM,
    USER,
    ASSISTANT
}

/**
 * Optional metadata for messages with additional information.
 */
@Serializable
data class MessageMetadata(
    val thinkingContent: String? = null,
    val imageUrl: String? = null,
    val toolCalls: List<ToolCall>? = null
)

@Serializable
data class ToolCall(
    val id: String,
    val type: String,
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    val name: String,
    val arguments: String
)
