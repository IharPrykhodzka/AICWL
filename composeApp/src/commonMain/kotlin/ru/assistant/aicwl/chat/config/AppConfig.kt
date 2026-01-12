package ru.assistant.aicwl.chat.config

/**
 * Application configuration loaded from config.properties or platform-specific sources.
 *
 * IMPORTANT: Create config.properties in project root with:
 * llm.z.api.key=YOUR_API_KEY_HERE
 */
expect object AppConfig {
    val zApiKey: String
    val zApiEndpoint: String
}
