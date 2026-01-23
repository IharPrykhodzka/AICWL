package ru.assistant.aicwl.chat.provider

/**
 * Enumeration of supported AI providers.
 * Each provider has its own API structure and authentication method.
 */
enum class ProviderType(
    val displayName: String,
    val description: String,
    val requiresApiKey: Boolean = true
) {
    ZAI(
        displayName = "Z.ai (智谱AI)",
        description = "Chinese AI provider with GLM models",
        requiresApiKey = true
    ),
    OPENAI(
        displayName = "OpenAI",
        description = "Leading AI research lab with GPT models",
        requiresApiKey = true
    ),
    ANTHROPIC(
        displayName = "Anthropic",
        description = "AI safety company with Claude models",
        requiresApiKey = true
    );

    companion object {
        /**
         * Default provider for new users.
         */
        val DEFAULT = ZAI

        /**
         * Get provider by display name (case-insensitive).
         */
        fun fromDisplayName(name: String): ProviderType {
            return entries.find {
                it.displayName.equals(name, ignoreCase = true)
            } ?: DEFAULT
        }
    }
}
