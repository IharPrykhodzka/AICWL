package ru.assistant.aicwl.chat.data.oreal

import ru.assistant.aicwl.chat.data.ChatCompletionRequest
import ru.assistant.aicwl.chat.data.ChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest

/**
 * Mapper for converting unified requests to Oreal-specific format.
 * This isolates Oreal-specific logic from the rest of the application.
 */
object OrealRequestMapper {

    /**
     * Convert unified chat request to Oreal format (OpenAI-compatible).
     */
    fun toOrealRequest(unifiedRequest: UnifiedChatRequest): ChatCompletionRequest {
        val messages = unifiedRequest.messages.map { toOrealMessage(it) }
        val params = unifiedRequest.parameters

        return ChatCompletionRequest(
            model = unifiedRequest.modelId,
            messages = messages,
            temperature = null, // Oreal does not support temperature parameter
            maxTokens = params?.maxTokens,
            topP = null, // Oreal does not support top_p parameter
            stream = false, // Oreal requires stream=false
            doSample = null, // Oreal doesn't use this parameter
            thinking = null, // Oreal doesn't use thinking parameter
            n = null // Oreal doesn't use n parameter
        )
    }

    /**
     * Convert unified message to Oreal message format.
     */
    private fun toOrealMessage(message: UnifiedChatMessage): ChatMessage {
        return ChatMessage(
            role = when (message.role) {
                ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM -> "system"
                ru.assistant.aicwl.chat.data.unified.MessageRole.USER -> "user"
                ru.assistant.aicwl.chat.data.unified.MessageRole.ASSISTANT -> "assistant"
            },
            content = message.content,
            reasoning_content = null // Oreal doesn't support reasoning_content
        )
    }
}
