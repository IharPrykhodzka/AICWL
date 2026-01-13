package ru.assistant.aicwl.chat.config

/**
 * Конфигурация приложения, загружаемая из config.properties или платформенно-зависимых источников.
 *
 * ВАЖНО: Создайте config.properties в корне проекта с:
 * llm.z.api.key=YOUR_API_KEY_HERE
 */
expect object AppConfig {
    val zApiKey: String
    val zApiEndpoint: String
}
