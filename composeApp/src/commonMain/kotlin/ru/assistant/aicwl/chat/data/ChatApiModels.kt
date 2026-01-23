package ru.assistant.aicwl.chat.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Тело запроса для API завершения чата.
 *
 * Этот класс представляет полный запрос к API чата Z.AI, включая идентификатор модели,
 * список сообщений и необязательные параметры генерации.
 *
 * Параметры генерации передаются как отдельные поля на верхнем уровне JSON-объекта,
 * в соответствии с требованиями Z.AI API.
 *
 * @property model Идентификатор модели (например, "glm-4.7", "glm-4.6", "glm-4.5-air")
 * @property messages Список сообщений в разговоре (системный промпт, история, текущий запрос)
 * @property temperature Контролирует случайность генерации (0.0 - 2.0)
 * @property maxTokens Максимальное количество токенов в ответе
 * @property topP Nucleus sampling параметр (0.0 - 1.0)
 * @property stream Потоковый режим ответа
 * @property doSample Использовать ли семплирование
 * @property thinking Конфигурация режима мышления (chain-of-thought)
 * @property n Количество попыток семантического семплирования
 *
 * @see ChatRequestParameters Документация по доступным параметрам генерации
 */
@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    @SerialName("temperature")
    val temperature: Float? = null,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    @SerialName("top_p")
    val topP: Float? = null,
    val stream: Boolean = false,
    @SerialName("do_sample")
    val doSample: Boolean? = null,
    val thinking: ThinkingConfig? = null,
    val n: Int? = null
) {
    companion object {
        /**
         * Создаёт запрос из ChatRequestParameters.
         * Этот метод обеспечивает совместимость со старым кодом, использующим ChatRequestParameters.
         */
        fun fromParameters(
            model: String,
            messages: List<ChatMessage>,
            parameters: ChatRequestParameters?
        ): ChatCompletionRequest {
            return ChatCompletionRequest(
                model = model,
                messages = messages,
                temperature = parameters?.temperature,
                maxTokens = parameters?.maxTokens,
                topP = parameters?.topP,
                stream = parameters?.stream ?: false,
                doSample = parameters?.doSample,
                thinking = parameters?.thinking,
                n = parameters?.n
            )
        }
    }
}

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

/**
 * Message role enum.
 *
 * NOTE: Это дубликат MessageRole из ru.assistant.aicwl.chat.data.unified.
 * В будущем следует мигрировать на unified-версию и удалить этот дубликат.
 * Сохраняется для обратной совместимости с существующим кодом.
 */
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

    /**
     * Преобразует запись в UnifiedChatMessage для новой архитектуры.
     */
    fun toUnifiedChatMessage(): ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage {
        return ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage(
            role = when (role) {
                MessageRole.USER -> ru.assistant.aicwl.chat.data.unified.MessageRole.USER
                MessageRole.ASSISTANT -> ru.assistant.aicwl.chat.data.unified.MessageRole.ASSISTANT
                MessageRole.SYSTEM -> ru.assistant.aicwl.chat.data.unified.MessageRole.SYSTEM
            },
            content = content
        )
    }
}
