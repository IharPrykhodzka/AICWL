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
        displayName = "Z.ai",
        description = "Chinese AI provider with GLM models",
        requiresApiKey = true
    ),
    QWEN(
        displayName = "Qwen",
        description = "Alibaba's large language model via HuggingFace",
        requiresApiKey = true
    ),
    OREAL(
        displayName = "Oreal",
        description = "InternLM OREAL model via HuggingFace",
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
