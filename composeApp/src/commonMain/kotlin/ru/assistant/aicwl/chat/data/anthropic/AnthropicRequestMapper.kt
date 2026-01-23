package ru.assistant.aicwl.chat.data.anthropic

import ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest

/**
 * Mapper for converting unified requests to Anthropic-specific format.
 */
object AnthropicRequestMapper {

    /**
     * Convert unified chat request to Anthropic format.
     *
     * Note: Anthropic's API structure is different:
     * - System message is separated from messages array
     * - Messages array only contains user/assistant alternations
     */
    fun toAnthropicRequest(unifiedRequest: UnifiedChatRequest): AnthropicMessageRequest {
        // Extract system prompt
        val systemPrompt = unifiedRequest.messages
            .firstOrNull { it.role == ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM }
            ?.content

        // Filter out system message and convert rest
        val messages = unifiedRequest.messages
            .filter { it.role != ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM }
            .map { toAnthropicMessage(it) }

        return AnthropicMessageRequest(
            model = unifiedRequest.modelId,
            messages = messages,
            maxTokens = unifiedRequest.parameters?.maxTokens ?: 4096,
            systemPrompt = systemPrompt,
            temperature = unifiedRequest.parameters?.temperature,
            topP = unifiedRequest.parameters?.topP,
            stream = unifiedRequest.stream
        )
    }

    /**
     * Convert unified message to Anthropic message format.
     */
    private fun toAnthropicMessage(message: UnifiedChatMessage): AnthropicMessage {
        return AnthropicMessage(
            role = when (message.role) {
                ru.assistant.aicwl.chat.data.unified.MessageRole.USER -> "user"
                ru.assistant.aicwl.chat.data.unified.MessageRole.ASSISTANT -> "assistant"
                ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM -> "user" // Should be filtered out
            },
            content = message.content
        )
    }
}
