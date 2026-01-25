package ru.assistant.aicwl.chat.tokens

/**
 * Platform-agnostic интерфейс для хранения статистики токенов.
 * Использует expect/actual паттерн для платформенно-специфичной реализации.
 *
 * Android: SharedPreferences
 * iOS: UserDefaults
 * Desktop: локальный файл
 */
expect class TokenStorage() {

    /**
     * Загружает сохраненную статистику токенов.
     *
     * @return JSON строка с TokenStatistics или null если данных нет
     */
    suspend fun loadTokenStatistics(): String?

    /**
     * Сохраняет статистику токенов.
     *
     * @param jsonData JSON строка с TokenStatistics
     */
    suspend fun saveTokenStatistics(jsonData: String)

    /**
     * Очищает сохраненную статистику.
     */
    suspend fun clearTokenStatistics()
}
