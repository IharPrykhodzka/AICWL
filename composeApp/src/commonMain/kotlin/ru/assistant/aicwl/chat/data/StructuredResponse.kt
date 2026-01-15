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
    val status: ResponseStatus? = null,  // Опциональный - определяется автоматически если null
    val summary: String = "",
    val reasoning: String = "",
    val actionItems: List<String> = emptyList(),
    val content: String = "",
    val highlights: List<String> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val questions: List<String> = emptyList(),
    val meta: ResponseMeta? = null,
    // Для режима бизнес-аналитика - отслеживание прогресса
    val questionNumber: Int? = null,
    val totalQuestions: Int? = null
) {
    /**
     * Вычисленный статус на основе содержимого ответа.
     */
    val computedStatus: ResponseStatus
        get() = status ?: when {
            questions.isNotEmpty() || questionNumber != null -> ResponseStatus.NEEDS_CLARIFICATION
            actionItems.isNotEmpty() -> ResponseStatus.SUCCESS
            content.isNotBlank() -> ResponseStatus.SUCCESS
            else -> ResponseStatus.SUCCESS
        }

    /**
     * Безопасное получение метаинформации.
     */
    val safeMeta: ResponseMeta
        get() = meta ?: ResponseMeta()

    /**
     * Безопасное получение summary.
     */
    val safeSummary: String
        get() = summary.ifBlank {
            when (computedStatus) {
                ResponseStatus.NEEDS_CLARIFICATION -> "Уточняющий вопрос"
                ResponseStatus.SUCCESS -> "Ответ"
                ResponseStatus.ERROR -> "Ошибка"
            }
        }
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
                val repaired = repairTruncatedJson(cleaned)
                json.decodeFromString(serializer(), repaired)
            } catch (e: Exception) {
                // Логирование для отладки парсинга JSON
                println("=== JSON Parsing Error ===")
                println("Error: ${e.message}")
                println("Response preview: ${rawResponse.take(500)}")
                println("Cleaned preview: ${extractJson(rawResponse).take(500)}")
                println("========================")
                null
            }
        }

        /**
         * Пытается восстановить обрезанный JSON.
         * Закрывает открытые скобки, массивы и строки.
         * Обрабатывает строки, обрезанные внутри значений.
         */
        private fun repairTruncatedJson(json: String): String {
            var repaired = json.trim()

            // Если JSON обрезан внутри строкового значения, удаляем всё после последней незакрытой кавычки
            val quotes = repaired.indices.filter { repaired[it] == '"' }

            // Если нечётное количество кавычек - строка обрезана
            if (quotes.size % 2 != 0) {
                // Находим последнюю закрывающую кавычку
                val lastQuoteIndex = quotes.lastOrNull { index ->
                    // Проверяем, не экранирована ли кавычка
                    val backslashCount = repaired.take(index).reversed().takeWhile { it == '\\' }.length
                    backslashCount % 2 == 0
                }

                if (lastQuoteIndex != null) {
                    // Обрезаем до последней валидной закрывающей кавычки
                    repaired = repaired.substring(0, lastQuoteIndex + 1)
                } else {
                    // Нет ни одной закрывающей кавычки - удаляем открывающую
                    val firstQuote = repaired.indexOf('"')
                    if (firstQuote != -1) {
                        repaired = repaired.substring(0, firstQuote)
                    }
                }
            }

            // Подсчитываем незакрытые скобки ПОСЛЕ удаления обрезанной строки
            val openBraces = repaired.count { it == '{' }
            val closeBraces = repaired.count { it == '}' }
            val openBrackets = repaired.count { it == '[' }
            val closeBrackets = repaired.count { it == ']' }

            // Закрываем массивы
            repeat(openBrackets - closeBrackets) {
                repaired += "]"
            }

            // Закрываем объекты
            repeat(openBraces - closeBraces) {
                repaired += "}"
            }

            // Если заканчивается на запятую, убираем её
            if (repaired.endsWith(",")) {
                repaired = repaired.dropLast(1)
            }

            return repaired
        }

        /**
         * Извлекает JSON из строки ответа AI.
         * Обрабатывает различные форматы ответов от AI.
         * Использует подсчёт скобок для поиска правильного конца JSON.
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

            // 2. Ищем JSON с помощью подсчёта скобок (для правильного поиска конца)
            val firstBrace = text.indexOf('{')
            if (firstBrace == -1) {
                return text  // Нет JSON объекта
            }

            // Ищем соответствующую закрывающую скобку
            var braceCount = 0
            var inString = false
            var escapeNext = false
            var endBrace = -1

            for (i in firstBrace until text.length) {
                val char = text[i]

                when {
                    escapeNext -> {
                        escapeNext = false
                    }
                    char == '\\' && inString -> {
                        escapeNext = true
                    }
                    char == '"' -> {
                        inString = !inString
                    }
                    !inString && char == '{' -> {
                        braceCount++
                    }
                    !inString && char == '}' -> {
                        braceCount--
                        if (braceCount == 0) {
                            endBrace = i
                            break
                        }
                    }
                }
            }

            if (endBrace != -1) {
                text = text.substring(firstBrace, endBrace + 1)
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

            // Логирование для отладки
            println("=== fromAiResponse ===")
            println("Content length: ${content.length}")
            println("Structured: ${structured != null}")
            if (structured != null) {
                println("Status: ${structured.computedStatus}")
                println("Questions count: ${structured.questions.size}")
                println("Summary: ${structured.safeSummary}")
                println("QuestionNumber: ${structured.questionNumber}")
                println("TotalQuestions: ${structured.totalQuestions}")
            } else {
                println("Failed to parse as structured response")
                println("Content preview: ${content.take(200)}")
            }
            println("=====================")

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
