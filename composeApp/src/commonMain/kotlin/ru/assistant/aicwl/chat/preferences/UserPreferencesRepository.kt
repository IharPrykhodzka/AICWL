package ru.assistant.aicwl.chat.preferences

import kotlinx.coroutines.flow.Flow
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel

/**
 * Repository interface for managing user preferences.
 * Handles persistence of user's selected AI provider and model.
 */
interface UserPreferencesRepository {

    /**
     * Flow of selected provider for reactive updates.
     */
    fun getSelectedProviderFlow(): Flow<ProviderType>

    /**
     * Get currently selected provider synchronously.
     */
    suspend fun getSelectedProvider(): ProviderType

    /**
     * Set the selected provider.
     */
    suspend fun setSelectedProvider(provider: ProviderType)

    /**
     * Get selected model for a specific provider.
     */
    suspend fun getSelectedModel(provider: ProviderType): String?

    /**
     * Set the selected model for a provider.
     */
    suspend fun setSelectedModel(provider: ProviderType, modelId: String)

    /**
     * Get the default model to use (provider + model ID).
     */
    suspend fun getDefaultModel(): UnifiedAIModel
}
