package ru.assistant.aicwl.chat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Тело запроса для API завершения чата.
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>
)

/**
 * Отдельное сообщение в разговоре.
 */
@Serializable
data class ChatMessage(
    val role: String,  // "user", "assistant", "system"
    val content: String,
    val reasoning_content: String? = null  // Для моделей с chain-of-thought
)

/**
 * Ответ от API завершения чата.
 * Для успешных ответов с вариантами (choices).
 */
@Serializable
data class ChatCompletionResponse(
    val id: String? = null,
    val choices: List<Choice>? = null,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finishReason: String? = null
)

@Serializable
data class Usage(
    val promptTokens: Int? = null,
    val completionTokens: Int? = null,
    val totalTokens: Int? = null
)

/**
 * Формат ответа об ошибке API.
 */
@Serializable
data class ChatApiErrorResponse(
    val error: ApiError? = null
)

@Serializable
data class ApiError(
    val message: String,
    val type: String? = null,
    val code: String? = null
)

/**
 * Сообщение чата для UI-состояния.
 */
data class UiChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = 0L
)

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}

/**
 * Запись истории для режима бизнес-аналитика.
 * Хранит сообщение с его ролью для правильного контекста.
 */
data class InterviewHistoryEntry(
    val role: MessageRole,
    val content: String,
    val questionNumber: Int? = null,      // Номер вопроса если это вопрос от ассистента
    val totalQuestions: Int? = null       // Общее количество вопросов если известно
) {
    /**
     * Преобразует запись в ChatMessage для API.
     */
    fun toChatMessage(): ChatMessage {
        return ChatMessage(
            role = when (role) {
                MessageRole.USER -> "user"
                MessageRole.ASSISTANT -> "assistant"
                MessageRole.SYSTEM -> "system"
            },
            content = content
        )
    }
}
