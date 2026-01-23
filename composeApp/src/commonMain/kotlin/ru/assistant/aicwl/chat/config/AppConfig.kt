package ru.assistant.aicwl.chat.config

/**
 * Конфигурация приложения, загружаемая из config.properties или платформенно-зависимых источников.
 *
 * ВАЖНО: Создайте config.properties в корне проекта с:
 * llm.z.api.key=YOUR_API_KEY_HERE
 * llm.openai.api.key=YOUR_OPENAI_API_KEY_HERE
 * llm.anthropic.api.key=YOUR_ANTHROPIC_API_KEY_HERE
 */
expect object AppConfig {
    // Z.ai Configuration
    val zApiKey: String
    val zApiEndpoint: String

    // OpenAI Configuration
    val openaiApiKey: String
    val openaiApiEndpoint: String

    // Anthropic Configuration
    val anthropicApiKey: String
    val anthropicApiEndpoint: String
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
            normalizedKey.equals("YOUR_OPENAI_API_KEY_HERE", ignoreCase = true) ||
            normalizedKey.equals("YOUR_ANTHROPIC_API_KEY_HERE", ignoreCase = true) ||
            normalizedKey.startsWith("your-", ignoreCase = true)) {
            return false
        }

        // OpenAI ключи обычно начинаются с "sk-"
        // Anthropic ключи обычно начинаются с "sk-ant-"
        // Z.ai ключи могут быть в другом формате

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
            "OPENAI" to isValidApiKey(AppConfig.openaiApiKey),
            "ANTHROPIC" to isValidApiKey(AppConfig.anthropicApiKey)
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
            append("  OpenAI: ${if (validation["OPENAI"] == true) "✓" else "✗"} ")
            append(if (AppConfig.openaiApiKey.isNotBlank()) maskApiKey(AppConfig.openaiApiKey) else "Not set")
            appendLine()
            append("  Anthropic: ${if (validation["ANTHROPIC"] == true) "✓" else "✗"} ")
            append(if (AppConfig.anthropicApiKey.isNotBlank()) maskApiKey(AppConfig.anthropicApiKey) else "Not set")
        }
    }
}
