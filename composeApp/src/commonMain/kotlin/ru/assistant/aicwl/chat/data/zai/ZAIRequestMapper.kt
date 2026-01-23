package ru.assistant.aicwl.chat.data.zai

import ru.assistant.aicwl.chat.data.ChatCompletionRequest
import ru.assistant.aicwl.chat.data.ChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Mapper for converting unified requests to Z.ai-specific format.
 * This isolates Z.ai-specific logic from the rest of the application.
 */
object ZAIRequestMapper {

    /**
     * Convert unified chat request to Z.ai format.
     */
    fun toZAIRequest(unifiedRequest: UnifiedChatRequest): ChatCompletionRequest {
        val messages = unifiedRequest.messages.map { toZAIMessage(it) }
        val params = unifiedRequest.parameters

        return ChatCompletionRequest(
            model = unifiedRequest.modelId,
            messages = messages,
            temperature = params?.temperature,
            maxTokens = params?.maxTokens,
            topP = params?.topP,
            stream = unifiedRequest.stream,
            doSample = params?.doSample,
            thinking = params?.thinking,
            n = params?.n
        )
    }

    /**
     * Convert unified message to Z.ai message format.
     */
    private fun toZAIMessage(message: UnifiedChatMessage): ChatMessage {
        return ChatMessage(
            role = when (message.role) {
                ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM -> "system"
                ru.assistant.aicwl.chat.data.unified.MessageRole.USER -> "user"
                ru.assistant.aicwl.chat.data.unified.MessageRole.ASSISTANT -> "assistant"
            },
            content = message.content,
            reasoning_content = message.metadata?.thinkingContent
        )
    }
}
