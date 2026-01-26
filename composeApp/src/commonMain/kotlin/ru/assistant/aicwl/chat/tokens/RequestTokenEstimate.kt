package ru.assistant.aicwl.chat.tokens

import kotlinx.serialization.Serializable
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.data.InterviewHistoryEntry

/**
 * Детальная оценка токенов для полного запроса к AI.
 * Содержит разбивку по компонентам: системный промпт, история сообщений, текущий ввод.
 *
 * @property systemPromptTokens Токены в системном промпте
 * @property historyTokens Токены в истории переписки
 * @property inputTokens Токены в текущем вводе пользователя
 * @property totalPromptTokens Общее количество токенов промпта (system + history + input)
 * @property estimatedCompletionTokens Оценочное количество токенов в ответе
 * @property totalTokens Общее количество токенов (prompt + estimated completion)
 * @property estimatedCost Оценочная стоимость запроса в USD
 * @property systemPromptLength Длина системного промпта в символах
 * @property historyMessageCount Количество сообщений в истории
 * @property inputLength Длина ввода пользователя в символах
 */
@Serializable
data class RequestTokenEstimate(
    val systemPromptTokens: Int = 0,
    val historyTokens: Int = 0,
    val inputTokens: Int = 0,
    val totalPromptTokens: Int = 0,
    val estimatedCompletionTokens: Int = 0,
    val totalTokens: Int = 0,
    val estimatedCost: Double = 0.0,
    val systemPromptLength: Int = 0,
    val historyMessageCount: Int = 0,
    val inputLength: Int = 0
) {
    companion object {
        /**
         * Создаёт детальную оценку токенов для запроса с историей.
         *
         * @param inputText Текущий ввод пользователя
         * @param conversationHistory История переписки
         * @param model Используемая модель AI
         * @param customSystemPrompt Опциональный кастомный системный промпт
         * @param currentQuestionNumber Номер текущего вопроса (для режима интервью)
         * @param fixedTotalQuestions Зафиксированное общее количество вопросов
         */
        fun forRequestWithHistory(
            inputText: String,
            conversationHistory: List<InterviewHistoryEntry>,
            model: UnifiedAIModel,
            customSystemPrompt: String? = null,
            currentQuestionNumber: Int? = null,
            fixedTotalQuestions: Int? = null
        ): RequestTokenEstimate {
            // 1. Считаем токены системного промпта
            val systemPrompt = if (currentQuestionNumber != null && conversationHistory.isNotEmpty()) {
                // Для режима бизнес-аналитика добавляем информацию о прогрессе
                buildString {
                    append(customSystemPrompt ?: SystemPromptConfig.getSystemPrompt())
                    appendLine()
                    appendLine()
                    appendLine("ТЕКУЩИЙ ПРОГРЕСС ИНТЕРВЬЮ:")
                    appendLine("- Это вопрос №$currentQuestionNumber из интервью")
                    if (fixedTotalQuestions != null) {
                        appendLine("- ЗАФИКСИРОВАННОЕ количество вопросов: $fixedTotalQuestions")
                        appendLine("- totalQuestions ДОЛЖЕН оставаться $fixedTotalQuestions - НЕ МЕНЯЙ ЕГО!")
                    }
                    appendLine("- Учти эту информацию при формировании ответа")
                }
            } else {
                customSystemPrompt ?: SystemPromptConfig.getSystemPrompt()
            }

            val systemPromptTokens = TokenCounter.estimateTokens(systemPrompt)

            // 2. Считаем токены истории сообщений
            val historyText = conversationHistory.joinToString("\n") { entry ->
                "${entry.role.name}: ${entry.content}"
            }
            val historyTokens = if (historyText.isNotBlank()) {
                TokenCounter.estimateTokens(historyText)
            } else {
                0
            }

            // 3. Считаем токены текущего ввода
            val inputTokens = TokenCounter.estimateTokens(inputText)

            // 4. Общее количество токенов промпта
            val totalPromptTokens = systemPromptTokens + historyTokens + inputTokens

            // 5. Оцениваем количество токенов в ответе
            val temperature = 0.7f // Можно добавить в параметры в будущем
            val estimatedCompletionTokens = TokenCounter.estimateCompletionTokens(
                totalPromptTokens,
                temperature
            )

            // 6. Общее количество токенов
            val totalTokens = totalPromptTokens + estimatedCompletionTokens

            // 7. Рассчитываем стоимость
            val estimatedCost = model.estimateCost(
                inputTokens = totalPromptTokens,
                outputTokens = estimatedCompletionTokens
            ) ?: 0.0

            return RequestTokenEstimate(
                systemPromptTokens = systemPromptTokens,
                historyTokens = historyTokens,
                inputTokens = inputTokens,
                totalPromptTokens = totalPromptTokens,
                estimatedCompletionTokens = estimatedCompletionTokens,
                totalTokens = totalTokens,
                estimatedCost = estimatedCost,
                systemPromptLength = systemPrompt.length,
                historyMessageCount = conversationHistory.size,
                inputLength = inputText.length
            )
        }

        /**
         * Создаёт оценку для простого запроса без истории.
         */
        fun forSimpleRequest(
            inputText: String,
            model: UnifiedAIModel,
            customSystemPrompt: String? = null
        ): RequestTokenEstimate {
            return forRequestWithHistory(
                inputText = inputText,
                conversationHistory = emptyList(),
                model = model,
                customSystemPrompt = customSystemPrompt
            )
        }
    }

    /**
     * Возвращает процент токенов системного промпта от общего количества токенов запроса.
     * Процент вычисляется от totalTokens (prompt + completion), а не только от prompt.
     */
    val systemPromptPercent: Float
        get() = if (totalTokens > 0) {
            (systemPromptTokens.toFloat() / totalTokens.toFloat() * 100)
        } else {
            0f
        }

    /**
     * Возвращает процент токенов истории от общего количества токенов запроса.
     * Процент вычисляется от totalTokens (prompt + completion), а не только от prompt.
     */
    val historyPercent: Float
        get() = if (totalTokens > 0) {
            (historyTokens.toFloat() / totalTokens.toFloat() * 100)
        } else {
            0f
        }

    /**
     * Возвращает процент токенов ввода от общего количества токенов запроса.
     * Процент вычисляется от totalTokens (prompt + completion), а не только от prompt.
     */
    val inputPercent: Float
        get() = if (totalTokens > 0) {
            (inputTokens.toFloat() / totalTokens.toFloat() * 100)
        } else {
            0f
        }

    /**
     * Возвращает процент оценочных токенов completion от общего количества токенов запроса.
     */
    val completionPercent: Float
        get() = if (totalTokens > 0) {
            (estimatedCompletionTokens.toFloat() / totalTokens.toFloat() * 100)
        } else {
            0f
        }
}
