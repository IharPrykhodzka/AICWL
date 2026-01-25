package ru.assistant.aicwl.chat.tokens

import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel

/**
 * Утилита для подсчета и оценки токенов.
 * В Kotlin Multiplatform используется эмпирическая оценка, так как
 * полноценные токенизаторы требуют специфичных для платформы библиотек.
 *
 * Правила оценки:
 * - Для английского: ~4 символа на токен
 * - Для русского: ~3 символа на токен
 * - Для китайского/японского: ~2.5 символа на токен
 * - Для кода: ~3-4 символа на токен
 *
 * Эти оценки основаны на статистике для популярных моделей (GPT, GLM, Qwen).
 */
object TokenCounter {

    /**
     * Оценивает количество токенов в тексте.
     *
     * @param text Входной текст
     * @return Оценочное количество токенов
     */
    fun estimateTokens(text: String): Int {
        if (text.isBlank()) return 0

        val characterCount = text.length

        // Определяем доминирующий тип символов
        val hasCJK = text.any { it.code in 0x4E00..0x9FFF || it.code in 0x3040..0x309F }
        val hasCyrillic = text.any { it.code in 0x0400..0x04FF }
        val hasCode = text.contains("{") || text.contains("}") || text.contains(";") || text.contains("(")

        val tokensPerChar = when {
            // CJK символы (китайский, японский)
            hasCJK -> 0.4
            // Кириллица (русский)
            hasCyrillic && !hasCode -> 0.35
            // Программный код
            hasCode -> 0.3
            // Латиница по умолчанию
            else -> 0.25
        }

        return (characterCount * tokensPerChar).toInt().coerceAtLeast(1)
    }

    /**
     * Оценивает количество токенов для списка сообщений.
     * Учитывает все сообщения в контексте.
     *
     * @param messages Список сообщений
     * @return Оценочное количество токенов
     */
    fun estimateTokensForMessages(messages: List<String>): Int {
        return messages.sumOf { estimateTokens(it) }
    }

    /**
     * Оценивает стоимость запроса для указанной модели.
     *
     * @param promptText Текст промпта
     * @param estimatedCompletionTokens Оценочное количество токенов в ответе
     * @param model Модель AI
     * @return Оценочная стоимость в USD
     */
    fun estimateCost(
        promptText: String,
        estimatedCompletionTokens: Int,
        model: UnifiedAIModel
    ): Double {
        val promptTokens = estimateTokens(promptText)

        return model.estimateCost(
            inputTokens = promptTokens,
            outputTokens = estimatedCompletionTokens
        ) ?: 0.0
    }

    /**
     * Создает детальную оценку токенов для текста.
     *
     * @param text Входной текст
     * @param model Модель AI
     * @return Объект TokenEstimate с детальной информацией
     */
    fun estimateTextTokens(text: String, model: UnifiedAIModel): TokenEstimate {
        return TokenEstimate.fromText(text, model)
    }

    /**
     * Оценивает количество токенов в ответе на основе длины промпта.
     * Использует эмпирическое соотношение для типичных ответов.
     *
     * @param promptTokens Количество токенов в промпте
     * @param temperature Температура генерации (влияет на длину ответа)
     * @return Оценочное количество токенов в ответе
     */
    fun estimateCompletionTokens(
        promptTokens: Int,
        temperature: Float = 0.7f
    ): Int {
        // Эмпирическое правило: длина ответа обычно 50-200% от длины промпта
        // Temperature выше = более длинные и разнообразные ответы
        val multiplier = when {
            temperature < 0.3 -> 0.5f  // Низкая температура = короткие ответы
            temperature < 0.7 -> 1.0f  // Средняя температура = сопоставимые ответы
            else -> 1.5f               // Высокая температура = длинные ответы
        }

        return (promptTokens * multiplier).toInt().coerceAtLeast(50)
    }

    /**
     * Форматирует количество токенов для отображения.
     *
     * @param tokens Количество токенов
     * @return Отформатированная строка (например, "1.5K", "2.3M")
     */
    fun formatTokenCount(tokens: Int): String {
        return when {
            tokens >= 1_000_000 -> {
                val value = tokens / 1_000_000.0
                "${(value * 10).toInt() / 10.0}M"
            }
            tokens >= 1_000 -> {
                val value = tokens / 1_000.0
                "${(value * 10).toInt() / 10.0}K"
            }
            else -> tokens.toString()
        }
    }

    /**
     * Форматирует стоимость для отображения.
     *
     * @param cost Стоимость в USD
     * @return Отформатированная строка (например, "$0.00123")
     */
    fun formatCost(cost: Double): String {
        return when {
            cost < 0.0001 -> {
                // Для очень малых цен показываем 6 знаков после запятой
                val scaled = (cost * 1_000_000).toInt()
                "$0.${scaled.toString().padStart(6, '0')}"
            }
            cost < 0.01 -> {
                // Для малых цен показываем 5 знаков после запятой
                val scaled = ((cost * 100_000) + 0.5).toInt()
                "$0.${scaled.toString().padStart(5, '0')}"
            }
            cost < 1.0 -> {
                val scaled = ((cost * 100) + 0.5).toInt()
                "$${scaled / 100}.${scaled % 100}"
            }
            else -> {
                val scaled = ((cost * 100) + 0.5).toInt()
                "$${scaled / 100}.${scaled % 100}"
            }
        }
    }
}
