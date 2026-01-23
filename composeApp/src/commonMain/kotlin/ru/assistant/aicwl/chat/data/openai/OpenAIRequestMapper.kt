package ru.assistant.aicwl.chat.data.openai

import ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest

/**
 * Mapper for converting unified requests to OpenAI-specific format.
 */
object OpenAIRequestMapper {

    /**
     * Convert unified chat request to OpenAI format.
     */
    fun toOpenAIRequest(unifiedRequest: UnifiedChatRequest): OpenAIChatRequest {
        val messages = unifiedRequest.messages.map { toOpenAIMessage(it) }

        return OpenAIChatRequest(
            model = unifiedRequest.modelId,
            messages = messages,
            temperature = unifiedRequest.parameters?.temperature,
            maxTokens = unifiedRequest.parameters?.maxTokens,
            topP = unifiedRequest.parameters?.topP,
            stream = unifiedRequest.stream
        )
    }

    /**
     * Convert unified message to OpenAI message format.
     */
    private fun toOpenAIMessage(message: UnifiedChatMessage): OpenAIMessage {
        return OpenAIMessage(
            role = when (message.role) {
                ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM -> "system"
                ru.assistant.aicwl.chat.data.unified.MessageRole.USER -> "user"
                ru.assistant.aicwl.chat.data.unified.MessageRole.ASSISTANT -> "assistant"
            },
            content = message.content
        )
    }
}
