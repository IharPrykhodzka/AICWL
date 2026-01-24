package ru.assistant.aicwl.chat.config

/**
 * Конфигурация приложения, загружаемая из config.properties или платформенно-зависимых источников.
 *
 * ВАЖНО: Создайте config.properties в корне проекта с:
 * llm.z.api.key=YOUR_API_KEY_HERE
 * llm.qwen.api.key=YOUR_API_KEY_HERE
 */
expect object AppConfig {
    // Z.ai Configuration
    val zApiKey: String
    val zApiEndpoint: String

    // Qwen Configuration
    val qwenApiKey: String
    val qwenApiEndpoint: String

    // Oreal Configuration
    val orealApiKey: String
    val orealApiEndpoint: String
}

/**
 * Валидация API ключей.
 */
object ApiKeyValidator {

    /**
     * Проверяет, что API ключ валиден.
     *
     * @param key API ключ для проверки
     * @return true если ключ валиден, false в противном случае
     */
    fun isValidApiKey(key: String): Boolean {
        if (key.isBlank()) return false

        // Проверка на placeholder-значения
        val normalizedKey = key.trim()
        if (normalizedKey.equals("YOUR_API_KEY_HERE", ignoreCase = true) ||
            normalizedKey.startsWith("your-", ignoreCase = true)) {
            return false
        }

        // Z.ai ключи могут быть в любом формате

        // Проверяем минимальную длину (все API ключи должны быть достаточно длинными)
        return key.length >= 20
    }

    /**
     * Проверяет, что все ключи сконфигурированы.
     *
     * @return Map с результатами валидации для каждого провайдера
     */
    fun validateAllKeys(): Map<String, Boolean> {
        return mapOf(
            "ZAI" to isValidApiKey(AppConfig.zApiKey),
            "QWEN" to isValidApiKey(AppConfig.qwenApiKey),
            "OREAL" to isValidApiKey(AppConfig.orealApiKey)
        )
    }

    /**
     * Маскирует API ключ для логирования.
     *
     * @param key API ключ
     * @return Маскированный ключ (первые 8 и последние 4 символа)
     */
    fun maskApiKey(key: String): String {
        return when {
            key.isBlank() -> "***"
            key.length <= 12 -> "***"
            else -> "${key.take(8)}...${key.takeLast(4)}"
        }
    }

    /**
     * Получает информацию о конфигурации API ключей.
     *
     * @return Строка с информацией о сконфигурированных ключах
     */
    fun getConfigInfo(): String {
        val validation = validateAllKeys()
        return buildString {
            appendLine("API Configuration:")
            append("  Z.ai: ${if (validation["ZAI"] == true) "✓" else "✗"} ")
            append(if (AppConfig.zApiKey.isNotBlank()) maskApiKey(AppConfig.zApiKey) else "Not set")
            appendLine()
            append("  Qwen: ${if (validation["QWEN"] == true) "✓" else "✗"} ")
            append(if (AppConfig.qwenApiKey.isNotBlank()) maskApiKey(AppConfig.qwenApiKey) else "Not set")
            appendLine()
            append("  Oreal: ${if (validation["OREAL"] == true) "✓" else "✗"} ")
            append(if (AppConfig.orealApiKey.isNotBlank()) maskApiKey(AppConfig.orealApiKey) else "Not set")
        }
    }
}
