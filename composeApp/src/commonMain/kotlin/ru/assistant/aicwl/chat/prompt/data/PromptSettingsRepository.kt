package ru.assistant.aicwl.chat.prompt.data

import kotlinx.coroutines.flow.Flow
import ru.assistant.aicwl.chat.prompt.model.PromptSettings

/**
 * Repository interface for managing prompt settings persistence.
 * Follows Clean Architecture - Domain layer defines contract.
 */
interface PromptSettingsRepository {
    /**
     * Flow of prompt settings for reactive UI updates.
     */
    fun getSettingsFlow(): Flow<PromptSettings>

    /**
     * Get current settings synchronously.
     */
    suspend fun getSettings(): PromptSettings

    /**
     * Save complete settings.
     */
    suspend fun saveSettings(settings: PromptSettings)

    /**
     * Update custom main prompt.
     */
    suspend fun saveCustomPrompt(prompt: String)

    /**
     * Add a new rule to additional rules.
     */
    suspend fun addRule(ruleText: String)

    /**
     * Remove a rule by ID.
     */
    suspend fun removeRule(ruleId: String)

    /**
     * Clear all additional rules.
     */
    suspend fun clearRules()

    /**
     * Reset custom prompt (will use default).
     */
    suspend fun resetCustomPrompt()
}
