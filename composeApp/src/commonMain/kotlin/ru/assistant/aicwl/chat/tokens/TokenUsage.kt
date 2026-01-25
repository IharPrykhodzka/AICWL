package ru.assistant.aicwl.chat.tokens

import kotlinx.serialization.Serializable
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.PlatformTime

/**
 * Запись об использовании токенов для одного запроса.
 * Хранит информацию о затраченных токенах и стоимости.
 *
 * @property modelId Модель, которая использовалась
 * @property promptTokens Токены во входных данных (prompt)
 * @property completionTokens Токены в ответе (completion)
 * @property totalTokens Общее количество токенов
 * @property estimatedCost Рассчитанная стоимость в USD
 * @property timestamp Время запроса
 */
@Serializable
data class TokenUsage(
    val modelId: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val estimatedCost: Double,
    val timestamp: Long
) {
    companion object {
        /**
         * Создает запись из API ответа.
         */
        fun fromApiUsage(
            apiUsage: ru.assistant.aicwl.chat.data.Usage,
            model: UnifiedAIModel,
            timestamp: Long
        ): TokenUsage? {
            val promptTokens = apiUsage.promptTokens ?: return null
            val completionTokens = apiUsage.completionTokens ?: return null
            val totalTokens = apiUsage.totalTokens ?: return null

            val estimatedCost = model.estimateCost(promptTokens, completionTokens) ?: 0.0

            return TokenUsage(
                modelId = model.modelId,
                promptTokens = promptTokens,
                completionTokens = completionTokens,
                totalTokens = totalTokens,
                estimatedCost = estimatedCost,
                timestamp = timestamp
            )
        }
    }
}

/**
 * Агрегированная статистика использования токенов.
 * Содержит накопленную информацию за все время.
 *
 * @property totalRequests Общее количество запросов
 * @property totalPromptTokens Всего токенов во входных данных
 * @property totalCompletionTokens Всего токенов в ответах
 * @property totalTokens Общее количество токенов
 * @property totalCost Общая стоимость в USD
 * @property modelBreakdown Статистика по каждой модели
 * @property lastUpdate Время последнего обновления
 */
@Serializable
data class TokenStatistics(
    val totalRequests: Int = 0,
    val totalPromptTokens: Int = 0,
    val totalCompletionTokens: Int = 0,
    val totalTokens: Int = 0,
    val totalCost: Double = 0.0,
    val modelBreakdown: Map<String, ModelTokenStats> = emptyMap(),
    val lastUpdate: Long = 0L
) {
    /**
     * Статистика для конкретной модели.
     */
    @Serializable
    data class ModelTokenStats(
        val modelId: String,
        val requestCount: Int = 0,
        val promptTokens: Int = 0,
        val completionTokens: Int = 0,
        val totalTokens: Int = 0,
        val cost: Double = 0.0
    )

    /**
     * Добавляет новую запись использования токенов.
     */
    fun addUsage(usage: TokenUsage): TokenStatistics {
        val newModelStats = modelBreakdown[usage.modelId]?.let { existing ->
            existing.copy(
                requestCount = existing.requestCount + 1,
                promptTokens = existing.promptTokens + usage.promptTokens,
                completionTokens = existing.completionTokens + usage.completionTokens,
                totalTokens = existing.totalTokens + usage.totalTokens,
                cost = existing.cost + usage.estimatedCost
            )
        } ?: ModelTokenStats(
            modelId = usage.modelId,
            requestCount = 1,
            promptTokens = usage.promptTokens,
            completionTokens = usage.completionTokens,
            totalTokens = usage.totalTokens,
            cost = usage.estimatedCost
        )

        return copy(
            totalRequests = totalRequests + 1,
            totalPromptTokens = totalPromptTokens + usage.promptTokens,
            totalCompletionTokens = totalCompletionTokens + usage.completionTokens,
            totalTokens = totalTokens + usage.totalTokens,
            totalCost = totalCost + usage.estimatedCost,
            modelBreakdown = modelBreakdown + (usage.modelId to newModelStats),
            lastUpdate = PlatformTime.currentTimeMillis()
        )
    }

    /**
     * Сбрасывает статистику.
     */
    fun reset(): TokenStatistics = TokenStatistics()

    /**
     * Возвращает статистику для конкретной модели.
     */
    fun getStatsForModel(modelId: String): ModelTokenStats? {
        return modelBreakdown[modelId]
    }
}

/**
 * Результат оценки токенов для текста перед отправкой.
 *
 * @property estimatedTokens Оценочное количество токенов
 * @property estimatedCost Оценочная стоимость в USD
 * @property characterCount Количество символов
 */
@Serializable
data class TokenEstimate(
    val estimatedTokens: Int,
    val estimatedCost: Double,
    val characterCount: Int
) {
    companion object {
        /**
         * Создает оценку на основе текста и модели.
         * Использует эмпирическое правило: ~4 символа на токен для английского,
         * ~2-3 символа на токен для русского/китайского.
         */
        fun fromText(text: String, model: UnifiedAIModel): TokenEstimate {
            val characterCount = text.length

            // Определяем примерное количество токенов
            // Для смешанного текста используем среднее значение
            val tokensPerChar = when {
                // Проверяем наличие CJK символов (китайский, японский, корейский)
                text.any { it.code in 0x4E00..0x9FFF } -> 0.4  // ~2.5 символа на токен
                // Проверяем наличие кириллицы
                text.any { it.code in 0x0400..0x04FF } -> 0.35  // ~3 символа на токен
                // По умолчанию для латиницы
                else -> 0.25  // ~4 символа на токен
            }

            val estimatedTokens = (characterCount * tokensPerChar).toInt().coerceAtLeast(1)

            // Оцениваем стоимость (только для входных токенов)
            val estimatedCost = if (model.inputCostPerMillion != null) {
                model.inputCostPerMillion * estimatedTokens / 1_000_000
            } else {
                0.0
            }

            return TokenEstimate(
                estimatedTokens = estimatedTokens,
                estimatedCost = estimatedCost,
                characterCount = characterCount
            )
        }

        /**
         * Создает оценку для пары prompt + completion.
         */
        fun fromRequest(
            promptText: String,
            estimatedCompletionTokens: Int,
            model: UnifiedAIModel
        ): TokenEstimate {
            val promptEstimate = fromText(promptText, model)

            val totalEstimatedTokens = promptEstimate.estimatedTokens + estimatedCompletionTokens

            val estimatedCost = model.estimateCost(
                inputTokens = promptEstimate.estimatedTokens,
                outputTokens = estimatedCompletionTokens
            ) ?: 0.0

            return TokenEstimate(
                estimatedTokens = totalEstimatedTokens,
                estimatedCost = estimatedCost,
                characterCount = promptText.length
            )
        }
    }
}
