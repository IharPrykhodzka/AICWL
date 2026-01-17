package ru.assistant.aicwl.chat.prompt.data

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic expect class for storing prompt preferences.
 * Uses expect/actual pattern for platform-specific implementations.
 */
expect class PromptPreferences() {
    /**
     * Observe custom main prompt changes.
     */
    fun getCustomPromptFlow(): Flow<String?>

    /**
     * Get current custom prompt or null if not set.
     */
    suspend fun getCustomPrompt(): String?

    /**
     * Save custom main prompt.
     */
    suspend fun saveCustomPrompt(prompt: String)

    /**
     * Clear custom prompt (will use default).
     */
    suspend fun clearCustomPrompt()

    /**
     * Observe additional rules changes.
     */
    fun getAdditionalRulesFlow(): Flow<String>

    /**
     * Get serialized additional rules.
     */
    suspend fun getAdditionalRules(): String

    /**
     * Save serialized additional rules.
     */
    suspend fun saveAdditionalRules(rulesJson: String)

    /**
     * Clear all additional rules.
     */
    suspend fun clearAdditionalRules()
}
