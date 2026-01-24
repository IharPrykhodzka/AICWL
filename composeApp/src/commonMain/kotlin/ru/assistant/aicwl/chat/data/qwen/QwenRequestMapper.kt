package ru.assistant.aicwl.chat.data.qwen

import ru.assistant.aicwl.chat.data.ChatCompletionRequest
import ru.assistant.aicwl.chat.data.ChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest

/**
 * Mapper for converting unified requests to Qwen-specific format.
 * This isolates Qwen-specific logic from the rest of the application.
 */
object QwenRequestMapper {

    /**
     * Convert unified chat request to Qwen format (OpenAI-compatible).
     */
    fun toQwenRequest(unifiedRequest: UnifiedChatRequest): ChatCompletionRequest {
        val messages = unifiedRequest.messages.map { toQwenMessage(it) }
        val params = unifiedRequest.parameters

        return ChatCompletionRequest(
            model = unifiedRequest.modelId,
            messages = messages,
            temperature = params?.temperature,
            maxTokens = params?.maxTokens,
            topP = params?.topP,
            stream = false, // Qwen via HuggingFace requires stream=false
            doSample = null, // Qwen doesn't use this parameter
            thinking = null, // Qwen doesn't use thinking parameter in HuggingFace format
            n = null // Qwen doesn't use n parameter
        )
    }

    /**
     * Convert unified message to Qwen message format.
     */
    private fun toQwenMessage(message: UnifiedChatMessage): ChatMessage {
        return ChatMessage(
            role = when (message.role) {
                ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM -> "system"
                ru.assistant.aicwl.chat.data.unified.MessageRole.USER -> "user"
                ru.assistant.aicwl.chat.data.unified.MessageRole.ASSISTANT -> "assistant"
            },
            content = message.content,
            reasoning_content = null // Qwen doesn't support reasoning_content
        )
    }
}
