package ru.assistant.aicwl.chat.tokens

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.utils.createLogger
import ru.assistant.aicwl.chat.data.MessageTokenInfo

/**
 * Менеджер для отслеживания использования токенов и стоимости.
 * Хранит статистику за все время и предоставляет доступ к ней через StateFlow.
 *
 * Для платформы-agnostic реализации используется expect/actual паттерн
 * для сохранения данных (SharedPreferences на Android, UserDefaults на iOS).
 */
class TokenTracker(
    private val storage: TokenStorage
) {
    private val logger = createLogger("TokenTracker")
    private val json = Json { ignoreUnknownKeys = true }

    private val _statistics = MutableStateFlow<TokenStatistics>(TokenStatistics())
    val statistics: StateFlow<TokenStatistics> = _statistics.asStateFlow()

    // Flag to ensure statistics are loaded only once
    private var initialized = false

    // Последняя записанная информация о токенах для отображения в сообщениях
    private var lastTokenUsage: TokenUsage? = null

    init {
        logger.i("TokenTracker initialized")
    }

    /**
     * Ensures statistics are loaded before first access.
     * This method is idempotent and safe to call multiple times.
     */
    private suspend fun ensureInitialized() {
        if (!initialized) {
            loadStatistics()
            initialized = true
        }
    }

    /**
     * Загружает сохраненную статистику из хранилища.
     */
    private suspend fun loadStatistics() {
        try {
            val savedData = storage.loadTokenStatistics()
            if (savedData != null) {
                val stats = json.decodeFromString<TokenStatistics>(savedData)
                _statistics.value = stats
                logger.i("Loaded token statistics: ${stats.totalRequests} requests, ${stats.totalTokens} tokens, $${stats.totalCost}")
            } else {
                logger.i("No saved token statistics found, starting fresh")
            }
        } catch (e: Exception) {
            logger.e("Failed to load token statistics", e)
            // Продолжаем с пустой статистикой
        }
    }

    /**
     * Записывает использование токенов после завершения запроса.
     *
     * @param usage Данные об использовании токенов
     */
    suspend fun recordUsage(usage: TokenUsage) {
        try {
            ensureInitialized()

            // Сохраняем как последнее использование для отображения в сообщениях
            lastTokenUsage = usage

            val newStats = _statistics.value.addUsage(usage)
            _statistics.value = newStats

            // Сохраняем в персистентное хранилище
            val jsonData = json.encodeToString(newStats)
            storage.saveTokenStatistics(jsonData)

            logger.i("Recorded token usage: model=${usage.modelId}, " +
                    "prompt=${usage.promptTokens}, completion=${usage.completionTokens}, " +
                    "total=${usage.totalTokens}, cost=$${usage.estimatedCost}")

            logger.i("Total statistics: ${newStats.totalRequests} requests, " +
                    "${newStats.totalTokens} tokens, $${newStats.totalCost}")
        } catch (e: Exception) {
            logger.e("Failed to record token usage", e)
        }
    }

    /**
     * Создает запись об использовании токенов из API ответа.
     *
     * @param apiUsage Ответ API с данными о токенах
     * @param model Использованная модель
     * @param timestamp Время запроса
     */
    suspend fun recordFromApi(
        apiUsage: ru.assistant.aicwl.chat.data.Usage,
        model: ru.assistant.aicwl.chat.provider.model.UnifiedAIModel,
        timestamp: Long
    ) {
        val usage = TokenUsage.fromApiUsage(apiUsage, model, timestamp)
        if (usage != null) {
            recordUsage(usage)
        } else {
            logger.w("Failed to create TokenUsage from API response: null values")
        }
    }

    /**
     * Сбрасывает всю статистику использования токенов.
     */
    suspend fun resetStatistics() {
        try {
            val emptyStats = TokenStatistics()
            _statistics.value = emptyStats

            val jsonData = json.encodeToString(emptyStats)
            storage.saveTokenStatistics(jsonData)

            logger.i("Token statistics reset")
        } catch (e: Exception) {
            logger.e("Failed to reset token statistics", e)
        }
    }

    /**
     * Возвращает текущую статистику.
     */
    fun getCurrentStatistics(): TokenStatistics = _statistics.value

    /**
     * Возвращает статистику для конкретной модели.
     */
    fun getStatisticsForModel(modelId: String): TokenStatistics.ModelTokenStats? {
        return _statistics.value.getStatsForModel(modelId)
    }

    /**
     * Проверяет, есть ли сохраненная статистика.
     */
    fun hasStatistics(): Boolean {
        return _statistics.value.totalRequests > 0
    }

    /**
     * Возвращает общее количество запросов.
     */
    fun getTotalRequests(): Int = _statistics.value.totalRequests

    /**
     * Возвращает общее количество токенов.
     */
    fun getTotalTokens(): Int = _statistics.value.totalTokens

    /**
     * Возвращает общую стоимость.
     */
    fun getTotalCost(): Double = _statistics.value.totalCost

    /**
     * Возвращает общее количество токенов промптов.
     */
    fun getTotalPromptTokens(): Int = _statistics.value.totalPromptTokens

    /**
     * Возвращает общее количество токенов completion.
     */
    fun getTotalCompletionTokens(): Int = _statistics.value.totalCompletionTokens

    /**
     * Возвращает информацию о последнем использовании токенов.
     * Используется для отображения токенов в сообщениях чата.
     */
    fun getLastTokenUsage(): TokenUsage? = lastTokenUsage

    /**
     * Создаёт MessageTokenInfo из последнего использования токенов.
     * Возвращает null если нет информации о последнем использовании.
     */
    fun getLastMessageTokenInfo(): MessageTokenInfo? {
        return lastTokenUsage?.let { usage ->
            MessageTokenInfo(
                promptTokens = usage.promptTokens,
                completionTokens = usage.completionTokens,
                totalTokens = usage.totalTokens,
                cost = usage.estimatedCost
            )
        }
    }
}

/**
 * Singleton экземпляр TokenTracker.
 * Инициализируется при первом обращении.
 */
private var tokenTrackerInstance: TokenTracker? = null

/**
 * Глобальная функция для получения экземпляра TokenTracker.
 * Для KMP используем простой подход без synchronized, так как
 * инициализация обычно происходит в главном потоке при старте приложения.
 */
fun getTokenTracker(storage: TokenStorage): TokenTracker {
    return tokenTrackerInstance ?: TokenTracker(storage).also { tokenTrackerInstance = it }
}

/**
 * Инициализирует TokenTracker с указанным хранилищем.
 * Должна вызываться при запуске приложения.
 */
fun initializeTokenTracker(storage: TokenStorage) {
    getTokenTracker(storage)
}
