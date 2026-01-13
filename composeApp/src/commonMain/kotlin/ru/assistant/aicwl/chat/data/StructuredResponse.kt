package ru.assistant.aicwl.chat.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Структурированный ответ от AI в формате JSON.
 * Содержит все поля для универсального отображения ответа.
 */
@Serializable
data class StructuredAiResponse(
    val status: ResponseStatus,
    val summary: String,
    val reasoning: String,
    val actionItems: List<String> = emptyList(),
    val content: String,
    val highlights: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val meta: ResponseMeta
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            coerceInputValues = true  // Автоматически преобразует типы
            encodeDefaults = true
        }

        /**
         * Пытается распарсить строку как структурированный ответ.
         * Возвращает null, если строка не является валидным JSON.
         */
        fun tryParse(rawResponse: String): StructuredAiResponse? {
            if (rawResponse.isBlank()) return null

            return try {
                val cleaned = extractJson(rawResponse)
                json.decodeFromString(serializer(), cleaned)
            } catch (e: Exception) {
                // Логирование для отладки (можно включить при необходимости)
                // println("Failed to parse JSON: ${e.message}")
                // println("Response: ${rawResponse.take(500)}")
                null
            }
        }

        /**
         * Извлекает JSON из строки ответа AI.
         * Обрабатывает различные форматы ответов от AI.
         */
        private fun extractJson(response: String): String {
            var text = response.trim()

            // 1. Удаляем markdown-блоки если есть
            if (text.startsWith("```json")) {
                text = text
                    .removePrefix("```json")
                    .trim()
                    .removeSuffix("```")
                    .trim()
            } else if (text.startsWith("```")) {
                text = text
                    .removePrefix("```")
                    .trim()
                    .removeSuffix("```")
                    .trim()
            }

            // 2. Ищем JSON в тексте (находим первую { и последнюю })
            val firstBrace = text.indexOf('{')
            val lastBrace = text.lastIndexOf('}')

            if (firstBrace != -1 && lastBrace != -1 && firstBrace < lastBrace) {
                // JSON найден в тексте
                text = text.substring(firstBrace, lastBrace + 1)
            }

            // 3. Удаляем BOM и другие невидимые символы
            text = text.trim()

            return text
        }
    }
}

/**
 * Статус ответа AI.
 */
@Serializable
enum class ResponseStatus {
    /** Успешный ответ */
    @SerialName("success")
    SUCCESS,

    /** Ошибка */
    @SerialName("error")
    ERROR,

    /** Требуется уточнение запроса */
    @SerialName("needs_clarification")
    NEEDS_CLARIFICATION
}

/**
 * Метаданные ответа.
 */
@Serializable
data class ResponseMeta(
    val category: String = "",
    val confidence: Double = 0.0
)

/**
 * Тип сообщения в UI.
 * Определяет, как будет отображаться сообщение.
 */
enum class MessageType {
    /** Обычный текстовый ответ (не структурированный) */
    PLAIN_TEXT,

    /** Структурированный JSON-ответ */
    STRUCTURED,

    /** Ошибка */
    ERROR
}

/**
 * Расширенное сообщение чата для UI с поддержкой структурированных ответов.
 */
data class EnhancedChatMessage(
    val id: String,
    val role: MessageRole,
    val originalContent: String,
    val timestamp: Long = 0L,
    val messageType: MessageType = MessageType.PLAIN_TEXT,
    val structuredData: StructuredAiResponse? = null
) {
    /**
     * Возвращает текст для отображения в списке чата.
     */
    fun getDisplayText(): String {
        return when (messageType) {
            MessageType.STRUCTURED -> structuredData?.summary ?: originalContent
            else -> originalContent
        }
    }

    /**
     * Преобразует в UiChatMessage для обратной совместимости.
     */
    fun toUiChatMessage(): UiChatMessage {
        return UiChatMessage(
            id = id,
            role = role,
            content = getDisplayText(),
            timestamp = timestamp
        )
    }

    companion object {
        /**
         * Создаёт EnhancedChatMessage из сырого ответа AI.
         * Автоматически определяет тип сообщения.
         */
        fun fromAiResponse(
            id: String,
            content: String,
            timestamp: Long
        ): EnhancedChatMessage {
            val structured = StructuredAiResponse.tryParse(content)

            return EnhancedChatMessage(
                id = id,
                role = MessageRole.ASSISTANT,
                originalContent = content,
                timestamp = timestamp,
                messageType = if (structured != null) MessageType.STRUCTURED else MessageType.PLAIN_TEXT,
                structuredData = structured
            )
        }
    }
}
