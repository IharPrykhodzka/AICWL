package ru.assistant.aicwl.chat.preferences

import kotlinx.serialization.Serializable
import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Data class for user preferences.
 */
@Serializable
data class UserPreferences(
    val selectedProvider: ProviderType = ProviderType.ZAI,
    val providerModels: Map<String, String> = emptyMap() // provider name -> model ID
) {
    /**
     * Get the selected model ID for a provider.
     */
    fun getModelId(provider: ProviderType): String? {
        return providerModels[provider.name]
    }

    /**
     * Create a copy with updated model for a provider.
     */
    fun withModel(provider: ProviderType, modelId: String): UserPreferences {
        return copy(providerModels = providerModels + (provider.name to modelId))
    }
}
